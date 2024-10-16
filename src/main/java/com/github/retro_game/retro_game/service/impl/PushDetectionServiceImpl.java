package com.github.retro_game.retro_game.service.impl;

import com.github.retro_game.retro_game.dto.PushEntryDto;
import com.github.retro_game.retro_game.dto.TransportKindDto;
import com.github.retro_game.retro_game.dto.TransportReportAndPointsDto;
import com.github.retro_game.retro_game.repository.TransportReportRepository;
import com.github.retro_game.retro_game.service.PushDetectionService;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PushDetectionServiceImpl implements PushDetectionService {
  private final TransportReportRepository transportReportRepository;

  public PushDetectionServiceImpl(TransportReportRepository transportReportRepository) {
    this.transportReportRepository = transportReportRepository;
  }

  @Override
  public List<ArrayList<PushEntryDto>> findPushes() {
    var rps = transportReportRepository.findReportsForPushDetection();
    var groups = splitToGroups(rps);
    var pushes = analyzeGroups(groups);
    var cmp = Comparator.comparing(
        (ArrayList<PushEntryDto> entries) -> entries.get(0).reportAndPoints().report().at()
    ).reversed();
    pushes.sort(cmp);
    return pushes;
  }

  // Split all transport reports into groups that should be analyzed separately.
  private static ArrayList<ArrayList<TransportReportAndPointsDto>> splitToGroups(List<TransportReportAndPointsDto> rps) {
    var groups = new ArrayList<ArrayList<TransportReportAndPointsDto>>();
    ArrayList<TransportReportAndPointsDto> curGroup = null;
    for (var rp : rps) {
      if (curGroup == null) {
        curGroup = new ArrayList<>();
      } else {
        assert !curGroup.isEmpty();
        var lastReport = curGroup.get(curGroup.size() - 1).report();
        var curReport = rp.report();
        if (lastReport.userId() != curReport.userId() ||
            !Objects.equals(lastReport.partnerId(), curReport.partnerId()) ||
            DateUtils.addDays(lastReport.at(), 3).before(curReport.at())) {
          groups.add(curGroup);
          curGroup = new ArrayList<>();
        }
      }
      curGroup.add(rp);
    }
    if (curGroup != null)
      groups.add(curGroup);
    return groups;
  }

  private static List<ArrayList<PushEntryDto>> analyzeGroups(ArrayList<ArrayList<TransportReportAndPointsDto>> groups) {
    var pushes = new ArrayList<ArrayList<PushEntryDto>>();

    var now = new Date();
    var startThreshold = DateUtils.addDays(now, -(30 - 3));
    var endThreshold = DateUtils.addDays(now, -3);
    for (var group : groups) {
      assert !group.isEmpty();

      var firstReport = group.get(0).report();
      var lastReport = group.get(group.size() - 1).report();

      // Ignore groups with deleted users.
      if (firstReport.partnerId() == null)
        continue;

      // Ignore reports that are too old, since some reports in a group can be already deleted by our cron, and
      // analyzing the remaining reports might result in a false positive. Similarly, ignore fresh reports, since the
      // trade between players might be not finished yet.
      if (firstReport.at().before(startThreshold) || lastReport.at().after(endThreshold))
        continue;

      // Ignore groups where one player is not always stronger than the other one.
      var isUserAlwaysStronger = group.stream().allMatch(rp -> rp.userPoints() > rp.partnerPoints());
      var isPartnerAlwaysStronger = group.stream().allMatch(rp -> rp.partnerPoints() > rp.userPoints());
      if (!isUserAlwaysStronger && !isPartnerAlwaysStronger)
        continue;

      var push = new ArrayList<PushEntryDto>(group.size());
      var userReceived = 0.0;
      var partnerReceived = 0.0;
      for (var rp : group) {
        var report = rp.report();
        var resources = report.resources();
        var weighted = resources.getMetal() + 1.5 * resources.getCrystal() + 3 * resources.getDeuterium();
        if (report.kind() == TransportKindDto.INCOMING) {
          userReceived += weighted;
        } else {
          assert report.kind() == TransportKindDto.OUTGOING;
          partnerReceived += weighted;
        }
        var entry = new PushEntryDto(rp, (long) userReceived, (long) partnerReceived);
        push.add(entry);
      }

      if ((isUserAlwaysStronger && userReceived > 1.1 * partnerReceived) ||
          (isPartnerAlwaysStronger && partnerReceived > 1.1 * userReceived)) {
        pushes.add(push);
      }
    }

    return pushes;
  }
}
