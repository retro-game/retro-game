package com.github.retro_game.retro_game.repository.impl;

import com.github.retro_game.retro_game.entity.Body;
import com.github.retro_game.retro_game.entity.CoordinatesKind;
import com.github.retro_game.retro_game.entity.User;
import com.github.retro_game.retro_game.repository.BodyRepositoryCustom;
import org.springframework.lang.Nullable;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

class BodyRepositoryImpl implements BodyRepositoryCustom {
  private EntityManager entityManager;

  public BodyRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<Body> findByUserForEmpire(User user, @Nullable Integer galaxy, @Nullable Integer system,
                                        @Nullable Integer position, @Nullable CoordinatesKind kind) {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Body> criteriaQuery = criteriaBuilder.createQuery(Body.class);
    Root<Body> root = criteriaQuery.from(Body.class);

    Predicate predicate = criteriaBuilder.equal(root.get("user"), user);
    if (galaxy != null) {
      predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("coordinates").get("galaxy"), galaxy));
    }
    if (system != null) {
      predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("coordinates").get("system"), system));
    }
    if (position != null) {
      predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("coordinates").get("position"),
          position));
    }
    if (kind != null) {
      predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("coordinates").get("kind"), kind));
    }
    criteriaQuery.where(predicate);

    TypedQuery<Body> typedQuery = entityManager.createQuery(criteriaQuery);
    return typedQuery.getResultList();
  }
}
