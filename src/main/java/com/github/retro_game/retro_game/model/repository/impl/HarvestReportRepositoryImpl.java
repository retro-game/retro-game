package com.github.retro_game.retro_game.model.repository.impl;

import com.github.retro_game.retro_game.model.entity.HarvestReport;
import com.github.retro_game.retro_game.model.entity.HarvestReportSortOrder;
import com.github.retro_game.retro_game.model.entity.User;
import com.github.retro_game.retro_game.model.repository.HarvestReportRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.function.Function;

class HarvestReportRepositoryImpl implements HarvestReportRepositoryCustom {
  private final EntityManager entityManager;

  HarvestReportRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<HarvestReport> findReports(User user, HarvestReportSortOrder order, Sort.Direction direction,
                                         Pageable pageable) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<HarvestReport> criteriaQuery = criteriaBuilder.createQuery(HarvestReport.class);
    Root<HarvestReport> root = criteriaQuery.from(HarvestReport.class);

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
      case COORDINATES: {
        // galaxy, system, position, kind
        criteriaQuery.orderBy(
            dirFunc.apply(root.get("coordinates").get("galaxy")),
            dirFunc.apply(root.get("coordinates").get("system")),
            dirFunc.apply(root.get("coordinates").get("position")),
            dirFunc.apply(root.get("coordinates").get("kind")));
        break;
      }
      case NUM_RECYCLERS: {
        criteriaQuery.orderBy(dirFunc.apply(root.get("numRecyclers")));
        break;
      }
      case CAPACITY: {
        criteriaQuery.orderBy(dirFunc.apply(root.get("capacity")));
        break;
      }
      case HARVESTED_RESOURCES: {
        criteriaQuery.orderBy(dirFunc.apply(
            criteriaBuilder.sum(root.get("harvestedMetal"), root.get("harvestedCrystal"))));
        break;
      }
      case REMAINING_RESOURCES: {
        criteriaQuery.orderBy(dirFunc.apply(
            criteriaBuilder.sum(root.get("remainingMetal"), root.get("remainingCrystal"))));
        break;
      }
    }

    TypedQuery<HarvestReport> typedQuery = entityManager.createQuery(criteriaQuery);
    typedQuery.setFirstResult((int) pageable.getOffset());
    typedQuery.setMaxResults(pageable.getPageSize());

    return typedQuery.getResultList();
  }
}
