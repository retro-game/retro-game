package com.github.retro_game.retro_game.repository.impl;

import com.github.retro_game.retro_game.dto.*;
import com.github.retro_game.retro_game.entity.TransportReport;
import com.github.retro_game.retro_game.entity.TransportReportSortOrder;
import com.github.retro_game.retro_game.entity.User;
import com.github.retro_game.retro_game.repository.TransportReportRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

class TransportReportRepositoryImpl implements TransportReportRepositoryCustom {
  private final EntityManager entityManager;

  TransportReportRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<TransportReport> findReports(User user, TransportReportSortOrder order, Sort.Direction direction,
                                           Pageable pageable) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<TransportReport> criteriaQuery = criteriaBuilder.createQuery(TransportReport.class);
    Root<TransportReport> root = criteriaQuery.from(TransportReport.class);

    criteriaQuery.where(criteriaBuilder.and(
        criteriaBuilder.equal(root.get("user"), user),
        criteriaBuilder.equal(root.get("deleted"), false)));

    Function<Expression<?>, Order> dirFunc =
        direction == Sort.Direction.ASC ? criteriaBuilder::asc : criteriaBuilder::desc;
    switch (order) {
      case AT: {
        criteriaQuery.orderBy(dirFunc.apply(root.get("at")));
        break;
      }
      case KIND: {
        criteriaQuery.orderBy(dirFunc.apply(root.get("kind")));
        break;
      }
      case PARTNER_NAME: {
        criteriaQuery.orderBy(dirFunc.apply(root.get("partnerName")));
        break;
      }
      case START_COORDINATES: {
        // galaxy, system, position, kind
        criteriaQuery.orderBy(
            dirFunc.apply(root.get("startCoordinates").get("galaxy")),
            dirFunc.apply(root.get("startCoordinates").get("system")),
            dirFunc.apply(root.get("startCoordinates").get("position")),
            dirFunc.apply(root.get("startCoordinates").get("kind")));
        break;
      }
      case TARGET_COORDINATES: {
        // galaxy, system, position, kind
        criteriaQuery.orderBy(
            dirFunc.apply(root.get("targetCoordinates").get("galaxy")),
            dirFunc.apply(root.get("targetCoordinates").get("system")),
            dirFunc.apply(root.get("targetCoordinates").get("position")),
            dirFunc.apply(root.get("targetCoordinates").get("kind")));
        break;
      }
      case RESOURCES: {
        // (metal + crystal) + deuterium
        criteriaQuery.orderBy(dirFunc.apply(
            criteriaBuilder.sum(
                criteriaBuilder.sum(
                    root.get("resources").get("metal"),
                    root.get("resources").get("crystal")),
                root.get("resources").get("deuterium"))));
        break;
      }
    }

    TypedQuery<TransportReport> typedQuery = entityManager.createQuery(criteriaQuery);
    typedQuery.setFirstResult((int) pageable.getOffset());
    typedQuery.setMaxResults(pageable.getPageSize());

    return typedQuery.getResultList();
  }

  @Override
  public List<TransportReportAndPointsDto> findReportsForPushDetection() {
    var sql = """
        select tr.id,
               tr.user_id,
               tr.at,
               tr.kind,
               tr.partner_id,
               tr.partner_name,
               tr.start_galaxy,
               tr.start_system,
               tr.start_position,
               tr.start_kind,
               tr.target_galaxy,
               tr.target_system,
               tr.target_position,
               tr.target_kind,
               tr.metal,
               tr.crystal,
               tr.deuterium,
               uos.points as user_points,
               pos.points as partner_points
        from transport_reports tr
        join overall_statistics uos
        on uos.user_id = tr.user_id
        and uos.at = (select max(at) from overall_statistics where at <= tr.at)
        join overall_statistics pos
        on pos.user_id = tr.partner_id
        and pos.at = (select max(at) from overall_statistics where at <= tr.at)
        where tr.user_id < tr.partner_id
        order by tr.user_id, tr.partner_id, tr.at
        """;
    var query = entityManager.createNativeQuery(sql);
    var rows = query.getResultList();
    var reports = new ArrayList<TransportReportAndPointsDto>(rows.size());
    for (var row : rows) {
      var r = (Object[]) row;
      var id = ((BigInteger) r[0]).longValue();
      var userId = ((BigInteger) r[1]).longValue();
      var at = (Date) r[2];
      var kind = TransportKindDto.values()[(int) r[3]];
      var partnerId = r[4] == null ? null : ((BigInteger) r[4]).longValue();
      var partnerName = (String) r[5];
      var startGalaxy = (int) r[6];
      var startSystem = (int) r[7];
      var startPosition = (int) r[8];
      var startKind = CoordinatesKindDto.values()[(int) r[9]];
      var targetGalaxy = (int) r[10];
      var targetSystem = (int) r[11];
      var targetPosition = (int) r[12];
      var targetKind = CoordinatesKindDto.values()[(int) r[13]];
      var metal = (double) r[14];
      var crystal = (double) r[15];
      var deuterium = (double) r[16];
      var userPoints = ((BigInteger) r[17]).longValue();
      var partnerPoints = ((BigInteger) r[18]).longValue();
      var start = new CoordinatesDto(startGalaxy, startSystem, startPosition, startKind);
      var target = new CoordinatesDto(targetGalaxy, targetSystem, targetPosition, targetKind);
      var resources = new ResourcesDto(metal, crystal, deuterium);
      var report = new TransportReportDto(id, userId, at, kind, partnerId, partnerName, start, target, resources);
      var rp = new TransportReportAndPointsDto(report, userPoints, partnerPoints);
      reports.add(rp);
    }
    return reports;
  }
}
