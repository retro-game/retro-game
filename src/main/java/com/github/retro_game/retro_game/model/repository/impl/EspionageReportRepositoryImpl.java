package com.github.retro_game.retro_game.model.repository.impl;

import com.github.retro_game.retro_game.model.entity.EspionageReport;
import com.github.retro_game.retro_game.model.entity.EspionageReportSortOrder;
import com.github.retro_game.retro_game.model.entity.User;
import com.github.retro_game.retro_game.model.repository.EspionageReportRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.function.Function;

class EspionageReportRepositoryImpl implements EspionageReportRepositoryCustom {
  private final EntityManager entityManager;

  EspionageReportRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<EspionageReport> findReports(User user, EspionageReportSortOrder order, Sort.Direction direction,
                                           Pageable pageable) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<EspionageReport> criteriaQuery = criteriaBuilder.createQuery(EspionageReport.class);
    Root<EspionageReport> root = criteriaQuery.from(EspionageReport.class);

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
      case ENEMY_NAME: {
        criteriaQuery.orderBy(dirFunc.apply(root.get("enemyName")));
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
      case ACTIVITY: {
        criteriaQuery.orderBy(dirFunc.apply(root.get("activity")));
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
      case FLEET: {
        criteriaQuery.orderBy(dirFunc.apply(root.get("fleet")));
        break;
      }
      case DEFENSE: {
        criteriaQuery.orderBy(dirFunc.apply(root.get("defense")));
        break;
      }
    }

    TypedQuery<EspionageReport> typedQuery = entityManager.createQuery(criteriaQuery);
    typedQuery.setFirstResult((int) pageable.getOffset());
    typedQuery.setMaxResults(pageable.getPageSize());

    return typedQuery.getResultList();
  }
}
