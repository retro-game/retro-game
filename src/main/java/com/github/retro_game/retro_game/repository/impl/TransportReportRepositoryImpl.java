package com.github.retro_game.retro_game.repository.impl;

import com.github.retro_game.retro_game.entity.TransportReport;
import com.github.retro_game.retro_game.entity.TransportReportSortOrder;
import com.github.retro_game.retro_game.entity.User;
import com.github.retro_game.retro_game.repository.TransportReportRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
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
}
