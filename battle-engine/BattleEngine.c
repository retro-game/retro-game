#include <math.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

#include <jni.h>

#define BATTLE_ENGINE_PREFIX "com/github/retro_game/retro_game/battleengine"

#define BATTLE_OUTCOME_CLASS BATTLE_ENGINE_PREFIX "/BattleOutcome"
#define COMBATANT_CLASS BATTLE_ENGINE_PREFIX "/Combatant"
#define COMBATANT_OUTCOME_CLASS BATTLE_ENGINE_PREFIX "/CombatantOutcome"
#define UNIT_CHARACTERISTICS_CLASS BATTLE_ENGINE_PREFIX "/UnitCharacteristics"

#define MAX_ROUNDS 6

/* Lehmer RNG. */
#define RANDOM_MULTIPLIER 48271UL
#define RANDOM_MODULUS 2147483647UL
#define RANDOM_MAX (RANDOM_MODULUS - 1)
#define RANDOM_NEXT(r) ((uint32_t)((uint64_t)(r)*RANDOM_MULTIPLIER % RANDOM_MODULUS))

struct jni_battle_outcome {
  jclass class;
  jmethodID constructor;
};

struct jni_combatant {
  jclass class;
  jmethodID get_weapons_technology;
  jmethodID get_shielding_technology;
  jmethodID get_armor_technology;
  jmethodID get_unit_groups;
};

struct jni_combatant_outcome {
  jclass class;
  jmethodID constructor;
};

struct jni_unit_characteristics {
  jclass class;
  jmethodID get_weapons;
  jmethodID get_shield;
  jmethodID get_armor;
  jmethodID get_rapid_fire;
};

struct jni {
  JNIEnv *env;
  struct jni_battle_outcome battle_outcome;
  struct jni_combatant combatant;
  struct jni_combatant_outcome combatant_outcome;
  struct jni_unit_characteristics unit_characteristics;
};

struct unit_characteristics {
  float weapons;
  float shield;
  float armor;
  int *rapid_fire;
};

/* Unit group = units with the same kind. */
struct unit_group_stats {
  long times_fired;
  long times_was_shot;
  long shield_damage_dealt;
  long hull_damage_dealt;
  long shield_damage_taken;
  long hull_damage_taken;
  long num_remaining_units;
};

struct combatant {
  struct unit_group_stats *stats;
  uint8_t weapons_technology;
  uint8_t shielding_technology;
  uint8_t armor_technology;
  int *unit_groups;
};

struct unit {
  float shield;
  float hull;
  uint8_t kind;
  uint8_t combatant_id;
};

struct party {
  struct combatant *combatants;
  struct unit *units;
  size_t num_alive;
};

static bool init_jni(struct jni *jni, JNIEnv *env);

static bool init_jni_battle_outcome(struct jni *jni);

static bool init_jni_combatant(struct jni *jni);

static bool init_jni_combatant_outcome(struct jni *jni);

static bool init_jni_unit_characteristics(struct jni *jni);

static bool init_units_characteristics(struct jni *jni, jobjectArray characteristics_array);

static bool load_combatants(struct jni *jni, struct combatant **combatants, size_t *num_combatants,
                            jobjectArray combatants_array);

static jobject create_battle_outcome(struct jni *jni, const struct combatant *restrict attackers, size_t num_attackers,
                                     const struct combatant *restrict defenders, size_t num_defenders,
                                     size_t num_rounds);

static jobjectArray create_combatants_outcomes(struct jni *jni, const struct combatant *combatants,
                                               size_t num_combatants, size_t num_rounds);

static jobject create_combatant_outcome(struct jni *jni, const struct combatant *combatant, size_t num_rounds);

static size_t fight(struct combatant *restrict attackers, size_t num_attackers, struct combatant *restrict defenders,
                    size_t num_defenders, uint32_t *random);

static struct party *create_party(struct combatant *combatants, size_t num_combatants);

static void restore_shields(struct party *party);

static void fire(struct party *restrict attackers_party, struct party *restrict defenders_party, size_t round,
                 uint32_t *random);

static void update_units(struct combatant *combatants, struct party *party, size_t round);

static void update_combatants(struct combatant *combatants, size_t num_combatants, struct party *party);

static struct unit_characteristics *units_characteristics;
static size_t num_kinds;

JNIEXPORT jboolean JNICALL Java_com_github_retro_1game_retro_1game_battleengine_BattleEngine_init(
    JNIEnv *env, jobject battle_engine, jobjectArray characteristics_array) {
  (void)battle_engine;
  struct jni jni;
  return init_jni(&jni, env) && init_units_characteristics(&jni, characteristics_array);
}

JNIEXPORT jobject JNICALL Java_com_github_retro_1game_retro_1game_battleengine_BattleEngine_fight(
    JNIEnv *env, jobject battle_engine, jobjectArray attackers_array, jobjectArray defenders_array, jint seed) {
  (void)battle_engine;

  struct jni jni;
  if (!init_jni(&jni, env)) {
    return NULL;
  }

  struct combatant *attackers;
  size_t num_attackers;
  if (!load_combatants(&jni, &attackers, &num_attackers, attackers_array)) {
    return NULL;
  }

  struct combatant *defenders;
  size_t num_defenders;
  if (!load_combatants(&jni, &defenders, &num_defenders, defenders_array)) {
    free(attackers);
    return NULL;
  }

  uint32_t random = seed == 0 ? 1 : (uint32_t)seed;

  size_t num_rounds = fight(attackers, num_attackers, defenders, num_defenders, &random);

  jobject battle_outcome = create_battle_outcome(&jni, attackers, num_attackers, defenders, num_defenders, num_rounds);

  free(defenders);
  free(attackers);
  return battle_outcome;
}

bool init_jni(struct jni *jni, JNIEnv *env) {
  jni->env = env;
  return init_jni_battle_outcome(jni) && init_jni_combatant(jni) && init_jni_combatant_outcome(jni) &&
         init_jni_unit_characteristics(jni);
}

bool init_jni_battle_outcome(struct jni *jni) {
  jni->battle_outcome.class = (*jni->env)->FindClass(jni->env, BATTLE_OUTCOME_CLASS);
  if (jni->battle_outcome.class == NULL) {
    return false;
  }
  jni->battle_outcome.constructor =
      (*jni->env)->GetMethodID(jni->env, jni->battle_outcome.class, "<init>",
                               "(I[L" COMBATANT_OUTCOME_CLASS ";[L" COMBATANT_OUTCOME_CLASS ";)V");
  return jni->battle_outcome.constructor != NULL;
}

bool init_jni_combatant(struct jni *jni) {
  jni->combatant.class = (*jni->env)->FindClass(jni->env, COMBATANT_CLASS);
  if (jni->combatant.class == NULL) {
    return false;
  }
  jni->combatant.get_weapons_technology =
      (*jni->env)->GetMethodID(jni->env, jni->combatant.class, "getWeaponsTechnology", "()I");
  jni->combatant.get_shielding_technology =
      (*jni->env)->GetMethodID(jni->env, jni->combatant.class, "getShieldingTechnology", "()I");
  jni->combatant.get_armor_technology =
      (*jni->env)->GetMethodID(jni->env, jni->combatant.class, "getArmorTechnology", "()I");
  jni->combatant.get_unit_groups = (*jni->env)->GetMethodID(jni->env, jni->combatant.class, "getUnitGroups", "()[I");
  return jni->combatant.get_weapons_technology != NULL && jni->combatant.get_shielding_technology != NULL &&
         jni->combatant.get_armor_technology != NULL && jni->combatant.get_unit_groups != NULL;
}

bool init_jni_combatant_outcome(struct jni *jni) {
  jni->combatant_outcome.class = (*jni->env)->FindClass(jni->env, COMBATANT_OUTCOME_CLASS);
  if (jni->combatant_outcome.class == NULL) {
    return false;
  }
  jni->combatant_outcome.constructor =
      (*jni->env)->GetMethodID(jni->env, jni->combatant_outcome.class, "<init>", "(I[J)V");
  return jni->combatant_outcome.constructor != NULL;
}

bool init_jni_unit_characteristics(struct jni *jni) {
  jni->unit_characteristics.class = (*jni->env)->FindClass(jni->env, UNIT_CHARACTERISTICS_CLASS);
  if (jni->unit_characteristics.class == NULL) {
    return false;
  }
  jni->unit_characteristics.get_weapons =
      (*jni->env)->GetMethodID(jni->env, jni->unit_characteristics.class, "getWeapons", "()F");
  jni->unit_characteristics.get_shield =
      (*jni->env)->GetMethodID(jni->env, jni->unit_characteristics.class, "getShield", "()F");
  jni->unit_characteristics.get_armor =
      (*jni->env)->GetMethodID(jni->env, jni->unit_characteristics.class, "getArmor", "()F");
  jni->unit_characteristics.get_rapid_fire =
      (*jni->env)->GetMethodID(jni->env, jni->unit_characteristics.class, "getRapidFire", "()[I");
  return jni->unit_characteristics.get_weapons != NULL && jni->unit_characteristics.get_shield != NULL &&
         jni->unit_characteristics.get_armor != NULL && jni->unit_characteristics.get_rapid_fire != NULL;
}

bool init_units_characteristics(struct jni *jni, jobjectArray characteristics_array) {
  size_t num_elems = (size_t)(*jni->env)->GetArrayLength(jni->env, characteristics_array);

  size_t total_size = num_elems * sizeof(struct unit_characteristics) + num_elems * num_elems * sizeof(int);
  struct unit_characteristics *characteristics = malloc(total_size);
  if (characteristics == NULL) {
    return false;
  }

  int *rf = (int *)&characteristics[num_elems];

  for (size_t i = 0; i < num_elems; i++, rf += num_elems) {
    jobject object = (*jni->env)->GetObjectArrayElement(jni->env, characteristics_array, (jsize)i);
    if (object == NULL) {
      free(characteristics);
      return false;
    }

    jobject rf_array_object = (*jni->env)->CallObjectMethod(jni->env, object, jni->unit_characteristics.get_rapid_fire);
    if (rf_array_object == NULL) {
      free(characteristics);
      return false;
    }

    int *array = (*jni->env)->GetIntArrayElements(jni->env, rf_array_object, NULL);
    memcpy(rf, array, num_elems * sizeof(*rf));
    (*jni->env)->ReleaseIntArrayElements(jni->env, rf_array_object, array, 0);

    struct unit_characteristics *c = &characteristics[i];
    c->weapons = (*jni->env)->CallFloatMethod(jni->env, object, jni->unit_characteristics.get_weapons);
    c->shield = (*jni->env)->CallFloatMethod(jni->env, object, jni->unit_characteristics.get_shield);
    c->armor = (*jni->env)->CallFloatMethod(jni->env, object, jni->unit_characteristics.get_armor);
    c->rapid_fire = rf;
  }

  units_characteristics = characteristics;
  num_kinds = num_elems;
  return true;
}

bool load_combatants(struct jni *jni, struct combatant **combatants, size_t *num_combatants,
                     jobjectArray combatants_array) {
  size_t num_elems = (size_t)(*jni->env)->GetArrayLength(jni->env, combatants_array);
  size_t total_size = num_elems * sizeof(struct combatant) + num_elems * num_kinds * sizeof(int) +
                      num_elems * MAX_ROUNDS * num_kinds * sizeof(struct unit_group_stats);
  struct combatant *cs = malloc(total_size);
  if (cs == NULL) {
    return false;
  }

  int *unit_groups = (int *)&cs[num_elems];

  struct unit_group_stats *stats = (struct unit_group_stats *)&unit_groups[num_elems * num_kinds];
  memset(stats, 0, num_elems * MAX_ROUNDS * num_kinds * sizeof(*stats));

  for (size_t i = 0; i < num_elems; i++, unit_groups += num_kinds, stats += MAX_ROUNDS * num_kinds) {
    jobject object = (*jni->env)->GetObjectArrayElement(jni->env, combatants_array, (jsize)i);
    if (object == NULL) {
      free(cs);
      return false;
    }

    jobject array_object = (*jni->env)->CallObjectMethod(jni->env, object, jni->combatant.get_unit_groups);
    if (array_object == NULL) {
      free(cs);
      return false;
    }

    int *array = (*jni->env)->GetIntArrayElements(jni->env, array_object, NULL);
    memcpy(unit_groups, array, num_kinds * sizeof(*unit_groups));
    (*jni->env)->ReleaseIntArrayElements(jni->env, array_object, array, 0);

    struct combatant *c = &cs[i];
    c->weapons_technology =
        (uint8_t)(*jni->env)->CallIntMethod(jni->env, object, jni->combatant.get_weapons_technology);
    c->shielding_technology =
        (uint8_t)(*jni->env)->CallIntMethod(jni->env, object, jni->combatant.get_shielding_technology);
    c->armor_technology = (uint8_t)(*jni->env)->CallIntMethod(jni->env, object, jni->combatant.get_armor_technology);
    c->unit_groups = unit_groups;
    c->stats = stats;
  }

  *combatants = cs;
  *num_combatants = num_elems;
  return true;
}

jobject create_battle_outcome(struct jni *jni, const struct combatant *restrict attackers, size_t num_attackers,
                              const struct combatant *restrict defenders, size_t num_defenders, size_t num_rounds) {
  jobjectArray attackers_array = create_combatants_outcomes(jni, attackers, num_attackers, num_rounds);
  jobjectArray defenders_array = create_combatants_outcomes(jni, defenders, num_defenders, num_rounds);
  return (*jni->env)->NewObject(jni->env, jni->battle_outcome.class, jni->battle_outcome.constructor, num_rounds,
                                attackers_array, defenders_array);
}

jobjectArray create_combatants_outcomes(struct jni *jni, const struct combatant *combatants, size_t num_combatants,
                                        size_t num_rounds) {
  jobjectArray array = (*jni->env)->NewObjectArray(jni->env, (jsize)num_combatants, jni->combatant_outcome.class, NULL);
  for (size_t i = 0; i < num_combatants; i++) {
    jobject outcome = create_combatant_outcome(jni, &combatants[i], num_rounds);
    (*jni->env)->SetObjectArrayElement(jni->env, array, (jsize)i, outcome);
  }
  return array;
}

jobject create_combatant_outcome(struct jni *jni, const struct combatant *combatant, size_t num_rounds) {
  size_t length = num_rounds * num_kinds * (sizeof(*combatant->stats) / sizeof(long));
  jlongArray stats = (*jni->env)->NewLongArray(jni->env, (jsize)length);
  (*jni->env)->SetLongArrayRegion(jni->env, stats, 0, (jsize)length, (long *)combatant->stats);
  return (*jni->env)->NewObject(jni->env, jni->combatant_outcome.class, jni->combatant_outcome.constructor, num_kinds,
                                stats);
}

size_t fight(struct combatant *restrict attackers, size_t num_attackers, struct combatant *restrict defenders,
             size_t num_defenders, uint32_t *random) {
  size_t round = 0;

  struct party *attackers_party = create_party(attackers, num_attackers);
  if (attackers_party == NULL) {
    goto out;
  }

  struct party *defenders_party = create_party(defenders, num_defenders);
  if (defenders_party == NULL) {
    goto out_attackers_party;
  }

  do {
    restore_shields(attackers_party);
    restore_shields(defenders_party);

    fire(attackers_party, defenders_party, round, random);
    fire(defenders_party, attackers_party, round, random);

    update_units(attackers, attackers_party, round);
    update_units(defenders, defenders_party, round);

    round++;
  } while (round < MAX_ROUNDS && attackers_party->num_alive > 0 && defenders_party->num_alive > 0);

  update_combatants(attackers, num_attackers, attackers_party);
  update_combatants(defenders, num_defenders, defenders_party);

  free(defenders_party->units);
  free(defenders_party);
out_attackers_party:
  free(attackers_party->units);
  free(attackers_party);
out:
  return round;
}

struct party *create_party(struct combatant *combatants, size_t num_combatants) {
  struct party *party = malloc(sizeof(*party));
  if (party == NULL) {
    return NULL;
  }

  size_t total_units = 0;
  for (size_t i = 0; i < num_combatants; i++) {
    const struct combatant *combatant = &combatants[i];
    for (size_t kind = 0; kind < num_kinds; kind++) {
      total_units += (size_t)combatant->unit_groups[kind];
    }
  }

  struct unit *units = malloc(total_units * sizeof(*units));
  if (units == NULL) {
    free(party);
    return NULL;
  }

  party->combatants = combatants;
  party->units = units;
  party->num_alive = total_units;

  for (size_t i = 0; i < num_combatants; i++) {
    const struct combatant *combatant = &combatants[i];
    for (uint8_t kind = 0; kind < num_kinds; kind++) {
      float max_hull = 0.1f * units_characteristics[kind].armor * (1.0f + 0.1f * combatant->armor_technology);
      for (int j = 0; j < combatant->unit_groups[kind]; j++) {
        struct unit *unit = units++;
        unit->hull = max_hull;
        unit->kind = kind;
        unit->combatant_id = (uint8_t)i;
      }
    }
  }

  return party;
}

void restore_shields(struct party *party) {
  const struct combatant *combatants = party->combatants;
  struct unit *units = party->units;
  for (size_t i = 0; i < party->num_alive; i++) {
    struct unit *unit = &units[i];
    unit->shield =
        units_characteristics[unit->kind].shield * (1.0f + 0.1f * combatants[unit->combatant_id].shielding_technology);
  }
}

void fire(struct party *restrict attackers_party, struct party *restrict defenders_party, size_t round,
          uint32_t *random) {
  uint32_t r = *random;

  struct combatant *attackers = attackers_party->combatants;
  const struct unit *shooters = attackers_party->units;
  size_t num_shooters = attackers_party->num_alive;

  struct combatant *defenders = defenders_party->combatants;
  struct unit *targets = defenders_party->units;
  size_t num_targets = defenders_party->num_alive;

  for (size_t i = 0; i < num_shooters; i++) {
    const struct unit *shooter = &shooters[i];
    uint8_t shooter_kind = shooter->kind;
    const struct unit_characteristics *shooter_characteristics = &units_characteristics[shooter_kind];
    struct combatant *attacker = &attackers[shooter->combatant_id];
    struct unit_group_stats *shooter_stats = &attacker->stats[round * num_kinds + shooter_kind];

    float damage =
        shooter_characteristics->weapons * (1.0f + 0.1f * attackers[shooter->combatant_id].weapons_technology);
    uint32_t rapid_fire;

    do {
      r = RANDOM_NEXT(r);
      struct unit *target = &targets[r % num_targets];
      uint8_t target_kind = target->kind;
      const struct unit_characteristics *target_characteristics = &units_characteristics[target_kind];
      struct combatant *defender = &defenders[target->combatant_id];
      struct unit_group_stats *target_stats = &defender->stats[round * num_kinds + target_kind];

      shooter_stats->times_fired++;
      target_stats->times_was_shot++;

      if (target->hull != 0.0f) {
        float hull = target->hull;
        float hull_damage = damage - target->shield;

        if (hull_damage < 0.0f) {
          float max_shield = target_characteristics->shield * (1.0f + 0.1f * defender->shielding_technology);
          float shield_damage = 0.01f * floorf(100.0f * damage / max_shield) * max_shield;
          target->shield -= shield_damage;
          shooter_stats->shield_damage_dealt += (long)shield_damage;
          target_stats->shield_damage_taken += (long)shield_damage;
        } else {
          shooter_stats->shield_damage_dealt += (long)target->shield;
          target_stats->shield_damage_taken += (long)target->shield;
          target->shield = 0.0f;
          if (hull_damage > hull) {
            hull_damage = hull;
          }
          hull -= hull_damage;
          shooter_stats->hull_damage_dealt += (long)hull_damage;
          target_stats->hull_damage_taken += (long)hull_damage;
        }

        if (hull != 0.0f) {
          float max_hull = 0.1f * target_characteristics->armor * (1.0f + 0.1f * defender->armor_technology);
          if (hull < 0.7f * max_hull) {
            r = RANDOM_NEXT(r);
            if (hull < (1.0f / (float)RANDOM_MAX) * (float)r * max_hull) {
              hull = 0.0f;
            }
          }
        }
        target->hull = hull;
      }

      rapid_fire = (uint32_t)shooter_characteristics->rapid_fire[target_kind];
    } while (rapid_fire != 0 && (r = RANDOM_NEXT(r)) % rapid_fire != 0);
  }

  *random = r;
}

void update_units(struct combatant *combatants, struct party *party, size_t round) {
  struct unit *units = party->units;
  size_t num_alive = 0;
  for (size_t i = 0; i < party->num_alive; i++) {
    struct unit *unit = &units[i];
    if (unit->hull != 0.0f) {
      units[num_alive++] = *unit;
      struct combatant *combatant = &combatants[unit->combatant_id];
      combatant->stats[round * num_kinds + unit->kind].num_remaining_units++;
    }
  }
  party->num_alive = num_alive;
}

void update_combatants(struct combatant *combatants, size_t num_combatants, struct party *party) {
  for (size_t i = 0; i < num_combatants; i++) {
    struct combatant *combatant = &combatants[i];
    memset(combatant->unit_groups, 0, num_kinds * sizeof(*combatant->unit_groups));
  }

  for (size_t i = 0; i < party->num_alive; i++) {
    struct unit *unit = &party->units[i];
    combatants[unit->combatant_id].unit_groups[unit->kind]++;
  }
}
