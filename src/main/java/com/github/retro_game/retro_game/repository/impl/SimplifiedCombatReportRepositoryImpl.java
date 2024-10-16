package com.github.retro_game.retro_game.repository.impl;

import com.github.retro_game.retro_game.entity.SimplifiedCombatReport;
import com.github.retro_game.retro_game.entity.SimplifiedCombatReportSortOrder;
import com.github.retro_game.retro_game.entity.User;
import com.github.retro_game.retro_game.repository.SimplifiedCombatReportRepositoryCustom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.function.Function;

class SimplifiedCombatReportRepositoryImpl implements SimplifiedCombatReportRepositoryCustom {
  private final EntityManager entityManager;

  SimplifiedCombatReportRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<SimplifiedCombatReport> findReports(User user, SimplifiedCombatReportSortOrder order,
                                                  Sort.Direction direction, Pageable pageable) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<SimplifiedCombatReport> criteriaQuery = criteriaBuilder.createQuery(SimplifiedCombatReport.class);
    Root<SimplifiedCombatReport> root = criteriaQuery.from(SimplifiedCombatReport.class);

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
      case RESULT: {
        criteriaQuery.orderBy(dirFunc.apply(root.get("result")));
        break;
      }
      case ATTACKERS_LOSS: {
        criteriaQuery.orderBy(dirFunc.apply(root.get("attackersLoss")));
        break;
      }
      case DEFENDERS_LOSS: {
        criteriaQuery.orderBy(dirFunc.apply(root.get("defendersLoss")));
        break;
      }
      case PLUNDER: {
        // (metal + crystal) + deuterium
        criteriaQuery.orderBy(dirFunc.apply(
            criteriaBuilder.sum(
                criteriaBuilder.sum(
                    root.get("plunder").get("metal"),
                    root.get("plunder").get("crystal")),
                root.get("plunder").get("deuterium"))));
        break;
      }
      case DEBRIS: {
        // metal + crystal
        criteriaQuery.orderBy(dirFunc.apply(criteriaBuilder.sum(root.get("debrisMetal"), root.get("debrisCrystal"))));
        break;
      }
      case MOON_CHANCE: {
        criteriaQuery.orderBy(dirFunc.apply(root.get("moonChance")));
        break;
      }
    }

    TypedQuery<SimplifiedCombatReport> typedQuery = entityManager.createQuery(criteriaQuery);
    typedQuery.setFirstResult((int) pageable.getOffset());
    typedQuery.setMaxResults(pageable.getPageSize());

    return typedQuery.getResultList();
  }
}
