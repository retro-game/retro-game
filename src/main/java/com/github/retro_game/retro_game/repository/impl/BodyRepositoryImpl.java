package com.github.retro_game.retro_game.repository.impl;

import com.github.retro_game.retro_game.entity.Body;
import com.github.retro_game.retro_game.entity.Coordinates;
import com.github.retro_game.retro_game.entity.CoordinatesKind;
import com.github.retro_game.retro_game.entity.User;
import com.github.retro_game.retro_game.repository.BodyRepositoryCustom;
import org.springframework.lang.Nullable;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class BodyRepositoryImpl implements BodyRepositoryCustom {
  private final EntityManager entityManager;

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

  @Override
  public Optional<Coordinates> findFirstFreeHomeworldSlot(int numGalaxies, int numSystems,
                                                          Collection<Integer> allowedPositions) {
    var positions = allowedPositions.stream().map(String::valueOf).collect(Collectors.joining(","));
    var sql = String.format("""
        select g, s, p
        from generate_series(1, ?1) as g
        cross join generate_series(1, ?2) s
        cross join (select unnest(array[%s]) as p) positions
        where not exists (
          select 1
          from bodies b
          where b.galaxy = g
          and b.system = s
          and b.position = p
          and b.kind = 0
        )
        order by g, s, p
        limit 1
        """, positions);

    var query = entityManager.createNativeQuery(sql);
    query.setParameter(1, numGalaxies);
    query.setParameter(2, numSystems);

    Object[] res;
    try {
      res = (Object[]) query.getSingleResult();
    } catch (NoResultException e) {
      return Optional.empty();
    }

    var galaxy = (int) res[0];
    var system = (int) res[1];
    var position = (int) res[2];
    var coords = new Coordinates(galaxy, system, position, CoordinatesKind.PLANET);
    return Optional.of(coords);
  }
}
