#include <algorithm>
#include <cassert>
#include <cmath>
#include <cstdint>
#include <cstdio>
#include <limits>
#include <memory>
#include <new>
#include <numeric>
#include <optional>
#include <type_traits>
#include <utility>

#include <jni.h>

namespace {

// Lehmer RNG
// Using this simple RNG improves the performance of the battle engine by a wide margin.
// Keep in sync with the RNG in the java battle engine.
// TODO: We need a RNG that returns 64-bit integers.
struct Random {
  static constexpr std::uint32_t multiplier{48271u};
  static constexpr std::uint32_t modulus{2147483647u};
  static constexpr std::uint32_t max{modulus - 1u};

  static constexpr std::uint32_t next(std::uint32_t r) {
    return static_cast<std::uint64_t>(r) * static_cast<std::uint64_t>(multiplier) %
           static_cast<std::uint64_t>(modulus);
  }
};

// JNI

#define RETRO_GAME_PREFIX "com/github/retro_game/retro_game/"

#define BATTLE_OUTCOME_CLASS_NAME RETRO_GAME_PREFIX "battleengine/BattleOutcome"
#define COMBATANT_CLASS_NAME RETRO_GAME_PREFIX "battleengine/Combatant"
#define COMBATANT_OUTCOME_CLASS_NAME RETRO_GAME_PREFIX "battleengine/CombatantOutcome"
#define UNIT_ATTRIBUTES_CLASS_NAME RETRO_GAME_PREFIX "battleengine/UnitAttributes"
#define UNIT_GROUP_STATS_CLASS_NAME RETRO_GAME_PREFIX "battleengine/UnitGroupStats"
#define UNIT_KIND_CLASS_NAME RETRO_GAME_PREFIX "entity/UnitKind"

// The JNI types must be defined as follows, otherwise our code might break.
static_assert(std::is_same_v<jfloat, float>);
static_assert(std::is_same_v<jint, int>);
static_assert(std::is_same_v<jlong, long>);

struct Jni {
  JNIEnv *env;

  struct Long {
    jclass clazz;
    jmethodID longValue;
  } long_;

  struct List {
    jclass clazz;
    jmethodID iterator;
    jmethodID size;
  } list;

  struct ArrayList {
    jclass clazz;
    jmethodID init;
    jmethodID add;
  } arrayList;

  struct Set {
    jclass clazz;
    jmethodID iterator;
  } set;

  struct EnumMap {
    jclass clazz;
    jmethodID init;
    jmethodID entrySet;
    jmethodID put;
  } enumMap;

  struct MapEntry {
    jclass clazz;
    jmethodID getKey;
    jmethodID getValue;
  } mapEntry;

  struct Iterator {
    jclass clazz;
    jmethodID hasNext;
    jmethodID next;
  } iterator;

  struct UnitKind {
    jclass clazz;
    jmethodID ordinal;
    jmethodID values;
  } unitKind;

  struct UnitAttributes {
    jclass clazz;
    jfieldID weapons;
    jfieldID shield;
    jfieldID armor;
    jfieldID rapidFire;
  } unitAttributes;

  struct Combatant {
    jclass clazz;
    jmethodID getWeaponsTechnology;
    jmethodID getShieldingTechnology;
    jmethodID getArmorTechnology;
    jmethodID getUnitGroups;
  } combatant;

  struct BattleOutcome {
    jclass clazz;
    jmethodID init;
  } battleOutcome;

  struct CombatantOutcome {
    jclass clazz;
    jmethodID init;
  } combatantOutcome;

  struct UnitGroupStats {
    jclass clazz;
    jmethodID init;
  } unitGroupStats;

  explicit Jni(JNIEnv *env) : env(env) {}

  bool init() {
    bool ok = true;
    ok &= initLong();
    ok &= initList();
    ok &= initArrayList();
    ok &= initSet();
    ok &= initEnumMap();
    ok &= initMapEntry();
    ok &= initIterator();
    ok &= initUnitKind();
    ok &= initUnitAttributes();
    ok &= initCombatant();
    ok &= initBattleOutcome();
    ok &= initCombatantOutcome();
    ok &= initUnitGroupStats();
    return ok;
  }

  bool initLong() {
    const char *className = "java/lang/Long";
    bool ok = findClass(long_.clazz, className);
    if (!ok)
      return false;
    ok &= getMethod(long_.longValue, long_.clazz, className, "longValue", "()J");
    return ok;
  }

  bool initList() {
    const char *className = "java/util/List";
    bool ok = findClass(list.clazz, className);
    if (!ok)
      return false;
    ok &= getMethod(list.iterator, list.clazz, className, "iterator", "()Ljava/util/Iterator;");
    ok &= getMethod(list.size, list.clazz, className, "size", "()I");
    return ok;
  }

  bool initArrayList() {
    const char *className = "java/util/ArrayList";
    bool ok = findClass(arrayList.clazz, className);
    if (!ok)
      return false;
    ok &= getMethod(arrayList.init, arrayList.clazz, className, "<init>", "(I)V");
    ok &= getMethod(arrayList.add, arrayList.clazz, className, "add", "(Ljava/lang/Object;)Z");
    return ok;
  }

  bool initSet() {
    const char *className = "java/util/Set";
    bool ok = findClass(set.clazz, className);
    if (!ok)
      return false;
    ok &= getMethod(set.iterator, set.clazz, className, "iterator", "()Ljava/util/Iterator;");
    return ok;
  }

  bool initEnumMap() {
    const char *className = "java/util/EnumMap";
    bool ok = findClass(enumMap.clazz, className);
    if (!ok)
      return false;
    ok &= getMethod(enumMap.init, enumMap.clazz, className, "<init>", "(Ljava/lang/Class;)V");
    ok &= getMethod(enumMap.entrySet, enumMap.clazz, className, "entrySet", "()Ljava/util/Set;");
    ok &= getMethod(enumMap.put, enumMap.clazz, className, "put",
                    "(Ljava/lang/Enum;Ljava/lang/Object;)Ljava/lang/Object;");
    return ok;
  }

  bool initMapEntry() {
    const char *className = "java/util/Map$Entry";
    bool ok = findClass(mapEntry.clazz, className);
    if (!ok)
      return false;
    ok &= getMethod(mapEntry.getKey, mapEntry.clazz, className, "getKey", "()Ljava/lang/Object;");
    ok &=
        getMethod(mapEntry.getValue, mapEntry.clazz, className, "getValue", "()Ljava/lang/Object;");
    return ok;
  }

  bool initIterator() {
    bool ok = findClass(iterator.clazz, "java/util/Iterator");
    if (!ok)
      return false;
    ok &= getMethod(iterator.hasNext, iterator.clazz, "java/util/Iterator", "hasNext", "()Z");
    ok &= getMethod(iterator.next, iterator.clazz, "java/util/Iterator", "next",
                    "()Ljava/lang/Object;");
    return ok;
  }

  bool initUnitKind() {
    bool ok = findClass(unitKind.clazz, UNIT_KIND_CLASS_NAME);
    if (!ok)
      return false;
    ok &= getMethod(unitKind.ordinal, unitKind.clazz, UNIT_KIND_CLASS_NAME, "ordinal", "()I");
    ok &= getStaticMethod(unitKind.values, unitKind.clazz, UNIT_KIND_CLASS_NAME, "values",
                          "()[L" UNIT_KIND_CLASS_NAME ";");
    return ok;
  }

  bool initUnitAttributes() {
    bool ok = findClass(unitAttributes.clazz, UNIT_ATTRIBUTES_CLASS_NAME);
    if (!ok)
      return false;
    ok &= getField(unitAttributes.weapons, unitAttributes.clazz, UNIT_ATTRIBUTES_CLASS_NAME,
                   "weapons", "F");
    ok &= getField(unitAttributes.shield, unitAttributes.clazz, UNIT_ATTRIBUTES_CLASS_NAME,
                   "shield", "F");
    ok &= getField(unitAttributes.armor, unitAttributes.clazz, UNIT_ATTRIBUTES_CLASS_NAME, "armor",
                   "F");
    ok &= getField(unitAttributes.rapidFire, unitAttributes.clazz, UNIT_ATTRIBUTES_CLASS_NAME,
                   "rapidFire", "[I");
    return ok;
  }

  bool initCombatant() {
    bool ok = findClass(combatant.clazz, COMBATANT_CLASS_NAME);
    if (!ok)
      return false;
    ok &= getMethod(combatant.getWeaponsTechnology, combatant.clazz, COMBATANT_CLASS_NAME,
                    "getWeaponsTechnology", "()I");
    ok &= getMethod(combatant.getShieldingTechnology, combatant.clazz, COMBATANT_CLASS_NAME,
                    "getShieldingTechnology", "()I");
    ok &= getMethod(combatant.getArmorTechnology, combatant.clazz, COMBATANT_CLASS_NAME,
                    "getArmorTechnology", "()I");
    ok &= getMethod(combatant.getUnitGroups, combatant.clazz, COMBATANT_CLASS_NAME, "getUnitGroups",
                    "()Ljava/util/EnumMap;");
    return ok;
  }

  bool initBattleOutcome() {
    bool ok = findClass(battleOutcome.clazz, BATTLE_OUTCOME_CLASS_NAME);
    if (!ok)
      return false;
    ok &= getMethod(battleOutcome.init, battleOutcome.clazz, BATTLE_OUTCOME_CLASS_NAME, "<init>",
                    "(ILjava/util/List;Ljava/util/List;)V");
    return ok;
  }

  bool initCombatantOutcome() {
    bool ok = findClass(combatantOutcome.clazz, COMBATANT_OUTCOME_CLASS_NAME);
    if (!ok)
      return false;
    ok &= getMethod(combatantOutcome.init, combatantOutcome.clazz, COMBATANT_OUTCOME_CLASS_NAME,
                    "<init>", "(Ljava/util/List;)V");
    return ok;
  }

  bool initUnitGroupStats() {
    bool ok = findClass(unitGroupStats.clazz, UNIT_GROUP_STATS_CLASS_NAME);
    if (!ok)
      return false;
    ok &= getMethod(unitGroupStats.init, unitGroupStats.clazz, UNIT_GROUP_STATS_CLASS_NAME,
                    "<init>", "(JJJFFFF)V");
    return ok;
  }

  bool findClass(jclass &clazz, const char *name) {
    jclass c = env->FindClass(name);
    if (!c) {
      std::fprintf(stderr, "BattleEngine: Cannot find class '%s'\n", name);
      return false;
    }
    clazz = c;
    return true;
  }

  bool getField(jfieldID &field, jclass clazz, const char *clazzName, const char *fieldName,
                const char *fieldSig) {
    jfieldID f = env->GetFieldID(clazz, fieldName, fieldSig);
    if (!f) {
      std::fprintf(stderr, "BattleEngine: Cannot get field '%s' of class '%s'\n", fieldName,
                   clazzName);
      return false;
    }
    field = f;
    return true;
  }

  bool getMethod(jmethodID &method, jclass clazz, const char *clazzName, const char *methodName,
                 const char *methodSig) {
    jmethodID m = env->GetMethodID(clazz, methodName, methodSig);
    if (!m) {
      std::fprintf(stderr, "BattleEngine: Cannot get method '%s' of class '%s'\n", methodName,
                   clazzName);
      return false;
    }
    method = m;
    return true;
  }

  bool getStaticMethod(jmethodID &method, jclass clazz, const char *clazzName,
                       const char *methodName, const char *methodSig) {
    jmethodID m = env->GetStaticMethodID(clazz, methodName, methodSig);
    if (!m) {
      std::fprintf(stderr, "BattleEngine: Cannot get static method '%s' of class '%s'\n",
                   methodName, clazzName);
      return false;
    }
    method = m;
    return true;
  }

  // Long

  long Long_longValue(jobject obj) const { return env->CallLongMethod(obj, long_.longValue); }

  // List

  jobject List_iterator(jobject obj) const { return env->CallObjectMethod(obj, list.iterator); }

  int List_size(jobject obj) const { return env->CallIntMethod(obj, list.size); }

  // ArrayList

  jobject ArrayList_init(int initialCapacity) const {
    return env->NewObject(arrayList.clazz, arrayList.init, initialCapacity);
  }

  bool ArrayList_add(jobject obj, jobject e) const {
    return env->CallBooleanMethod(obj, arrayList.add, e);
  }

  // Set

  jobject Set_iterator(jobject obj) const { return env->CallObjectMethod(obj, set.iterator); }

  // EnumMap

  jobject EnumMap_init(jclass keyType) const {
    return env->NewObject(enumMap.clazz, enumMap.init, keyType);
  }

  jobject EnumMap_entrySet(jobject obj) const {
    return env->CallObjectMethod(obj, enumMap.entrySet);
  }

  jobject EnumMap_put(jobject obj, jobject key, jobject value) const {
    return env->CallObjectMethod(obj, enumMap.put, key, value);
  }

  // Map.Entry

  jobject MapEntry_getKey(jobject obj) const { return env->CallObjectMethod(obj, mapEntry.getKey); }

  jobject MapEntry_getValue(jobject obj) const {
    return env->CallObjectMethod(obj, mapEntry.getValue);
  }

  // Iterator

  bool Iterator_hasNext(jobject obj) const { return env->CallBooleanMethod(obj, iterator.hasNext); }

  jobject Iterator_next(jobject obj) const { return env->CallObjectMethod(obj, iterator.next); }

  // UnitKind

  int UnitKind_ordinal(jobject obj) const { return env->CallIntMethod(obj, unitKind.ordinal); }

  jobjectArray UnitKind_values() const {
    return static_cast<jobjectArray>(env->CallStaticObjectMethod(unitKind.clazz, unitKind.values));
  }

  // UnitAttributes

  float UnitAttributes_weapons(jobject obj) const {
    return env->GetFloatField(obj, unitAttributes.weapons);
  }

  float UnitAttributes_shield(jobject obj) const {
    return env->GetFloatField(obj, unitAttributes.shield);
  }

  float UnitAttributes_armor(jobject obj) const {
    return env->GetFloatField(obj, unitAttributes.armor);
  }

  jintArray UnitAttributes_rapidFire(jobject obj) const {
    return static_cast<jintArray>(env->GetObjectField(obj, unitAttributes.rapidFire));
  }

  // Combatant

  int Combatant_getWeaponsTechnology(jobject obj) const {
    return env->CallIntMethod(obj, combatant.getWeaponsTechnology);
  }

  int Combatant_getShieldingTechnology(jobject obj) const {
    return env->CallIntMethod(obj, combatant.getShieldingTechnology);
  }

  int Combatant_getArmorTechnology(jobject obj) const {
    return env->CallIntMethod(obj, combatant.getArmorTechnology);
  }

  jobject Combatant_getUnitGroups(jobject obj) const {
    return env->CallObjectMethod(obj, combatant.getUnitGroups);
  }

  // BattleOutcome

  jobject BattleOutcome_init(int numRounds, jobject attackersOutcomes,
                             jobject defendersOutcomes) const {
    return env->NewObject(battleOutcome.clazz, battleOutcome.init, numRounds, attackersOutcomes,
                          defendersOutcomes);
  }

  // CombatantOutcome

  jobject CombatantOutcome_init(jobject unitGroupsStats) const {
    return env->NewObject(combatantOutcome.clazz, combatantOutcome.init, unitGroupsStats);
  }

  // UnitGroupStats

  jobject UnitGroupStats_init(long numRemainingUnits, long timesFired, long timesWasShot,
                              float shieldDamageDealt, float hullDamageDealt,
                              float shieldDamageTaken, float hullDamageTaken) const {
    return env->NewObject(unitGroupStats.clazz, unitGroupStats.init, numRemainingUnits, timesFired,
                          timesWasShot, shieldDamageDealt, hullDamageDealt, shieldDamageTaken,
                          hullDamageTaken);
  }
};

// Battle Engine

constexpr unsigned maxRounds = 6;

struct UnitAttributes {
  float weapons;
  float shield;
  float armor;

  // A mapping: unit kind -> the number of rapid fire against that kind.
  unsigned *rapidFire;
};

// Initialized by initUnitsAttributes().
UnitAttributes *g_unitsAttributes;
std::uint8_t g_numKinds;

struct UnitGroupStats {
  std::uint64_t numRemainingUnits{};
  std::uint64_t timesFired{};
  std::uint64_t timesWasShot{};
  float shieldDamageDealt{};
  float hullDamageDealt{};
  float shieldDamageTaken{};
  float hullDamageTaken{};
};

struct Combatant {
  float weaponsTechnology;
  float shieldingTechnology;
  float armorTechnology;

  // A mapping: unit kind -> the number of units of that kind.
  std::unique_ptr<std::uint64_t[]> unitGroups;

  // A mapping: unit kind -> the stats of units of that kind.
  std::unique_ptr<UnitGroupStats[]> stats;
};

struct Combatants {
  std::unique_ptr<Combatant[]> combatants;
  std::uint8_t num;
};

struct Unit {
  float shield;
  float hull;
  std::uint8_t kind;
  std::uint8_t combatantId;
};

struct Party {
  std::unique_ptr<Unit[]> units;
  std::uint64_t numAlive;
};

// Initializes units' attributes.
// initUnitsAttributes() should be called only once during the initialization of battle engine.
bool initUnitsAttributes(const Jni &jni, jobjectArray unitsAttributesArray) {
  int len = jni.env->GetArrayLength(unitsAttributesArray);
  assert(len >= 0);
  if (len > std::numeric_limits<std::uint8_t>::max()) {
    std::fputs("BattleEngine: Too many unit kinds\n", stderr);
    return false;
  }
  auto numKinds = static_cast<std::uint8_t>(len);

  std::unique_ptr<UnitAttributes[]> unitsAttributes(new (std::nothrow) UnitAttributes[numKinds]);
  if (!unitsAttributes) {
    std::fputs("BattleEngine: Allocating memory for unitsAttributes failed\n", stderr);
    return false;
  }

  std::unique_ptr<unsigned[]> rapidFire(new (std::nothrow) unsigned[numKinds * numKinds]);
  if (!rapidFire) {
    std::fputs("BattleEngine: Allocating memory for rapidFire failed\n", stderr);
    return false;
  }

  auto *rf = rapidFire.get();
  for (std::uint8_t kind = 0; kind < numKinds; ++kind, rf += numKinds) {
    jobject elem = jni.env->GetObjectArrayElement(unitsAttributesArray, kind);
    if (!elem) {
      std::fprintf(stderr, "BattleEngine: Element %u of unitsAttributes cannot be null\n", kind);
      return false;
    }

    jintArray rapidFireField = jni.UnitAttributes_rapidFire(elem);
    if (!rapidFireField) {
      std::fputs("BattleEngine: rapidFire field in UnitAttributes cannot be null\n", stderr);
      return false;
    }

    int *rapidFireArray = jni.env->GetIntArrayElements(rapidFireField, /*isCopy=*/nullptr);
    bool valid =
        std::all_of(rapidFireArray, rapidFireArray + numKinds, [](int n) { return n >= 0; });
    if (valid)
      std::copy_n(rapidFireArray, numKinds, rf);
    jni.env->ReleaseIntArrayElements(rapidFireField, rapidFireArray, /*mode=*/0);
    if (!valid) {
      std::fputs("BattleEngine: Rapid fire cannot be negative\n", stderr);
      return false;
    }

    auto &attrs = unitsAttributes[kind];
    attrs.weapons = jni.UnitAttributes_weapons(elem);
    attrs.shield = jni.UnitAttributes_shield(elem);
    attrs.armor = jni.UnitAttributes_armor(elem);
    attrs.rapidFire = rf;
  }

  rapidFire.release();
  g_unitsAttributes = unitsAttributes.release();
  g_numKinds = numKinds;
  return true;
}

std::optional<Combatant> loadCombatant(const Jni &jni, jobject combatantObj) {
  int weaponsTechnology = jni.Combatant_getWeaponsTechnology(combatantObj);
  int shieldingTechnology = jni.Combatant_getShieldingTechnology(combatantObj);
  int armorTechnology = jni.Combatant_getArmorTechnology(combatantObj);
  if (weaponsTechnology < 0 || shieldingTechnology < 0 || armorTechnology < 0) {
    std::fputs("BattleEngine: Combat technologies cannot be negative\n", stderr);
    return {};
  }

  std::unique_ptr<std::uint64_t[]> unitGroups(new (std::nothrow) std::uint64_t[g_numKinds]);
  if (!unitGroups) {
    std::fputs("BattleEngine: Allocating memory for unitGroups failed\n", stderr);
    return {};
  }
  std::fill_n(unitGroups.get(), g_numKinds, std::uint64_t{0u});

  jobject unitGroupsObj = jni.Combatant_getUnitGroups(combatantObj);
  if (!unitGroupsObj) {
    std::fputs("BattleEngine: unitGroups of Combatant cannot be null\n", stderr);
    return {};
  }

  jobject entrySetObj = jni.EnumMap_entrySet(unitGroupsObj);
  assert(entrySetObj);

  jobject iteratorObj = jni.Set_iterator(entrySetObj);
  assert(iteratorObj);

  while (jni.Iterator_hasNext(iteratorObj)) {
    jobject entryObj = jni.Iterator_next(iteratorObj);
    assert(entryObj);

    jobject keyObj = jni.MapEntry_getKey(entryObj);
    if (!keyObj) {
      std::fputs("BattleEngine: Unit kind cannot be null\n", stderr);
      return {};
    }

    jobject valueObj = jni.MapEntry_getValue(entryObj);
    if (!valueObj) {
      std::fputs("BattleEngine: Unit count cannot be null\n", stderr);
      return {};
    }

    int kind = jni.UnitKind_ordinal(keyObj);
    assert(kind >= 0);
    if (kind > g_numKinds) {
      std::fprintf(stderr, "BattleEngine: Unit kind out of bounds: kind=%i numKinds=%u\n", kind,
                   g_numKinds);
      return {};
    }
    auto k = static_cast<unsigned>(kind);

    long count = jni.Long_longValue(valueObj);
    if (count < 0l) {
      std::fputs("BattleEngine: Unit count cannot be negative\n", stderr);
      return {};
    }
    auto num = static_cast<std::uint64_t>(count);

    unitGroups[k] = num;
  }

  // UnitGroupStats has a constructor that zeroes all fields.
  std::unique_ptr<UnitGroupStats[]> stats(new (std::nothrow)
                                              UnitGroupStats[g_numKinds * maxRounds]);
  if (!stats) {
    std::fputs("BattleEngine: Allocating memory for stats failed\n", stderr);
    return {};
  }

  return Combatant{
      .weaponsTechnology = static_cast<float>(weaponsTechnology),
      .shieldingTechnology = static_cast<float>(shieldingTechnology),
      .armorTechnology = static_cast<float>(armorTechnology),
      .unitGroups = std::move(unitGroups),
      .stats = std::move(stats),
  };
}

std::optional<Combatants> loadCombatants(const Jni &jni, jobject combatantsList) {
  int size = jni.List_size(combatantsList);
  assert(size >= 0);

  // Each unit has an uint8_t field representing its owner, and thus we cannot handle more than 256
  // combatants on one side.
  if (size > std::numeric_limits<std::uint8_t>::max()) {
    std::fputs("BattleEngine: Too many combatants\n", stderr);
    return {};
  }

  auto numCombatants = static_cast<std::uint8_t>(size);

  std::unique_ptr<Combatant[]> combatants(new (std::nothrow) Combatant[numCombatants]);
  if (!combatants) {
    std::fputs("BattleEngine: Allocating memory for combatants failed\n", stderr);
    return {};
  }

  jobject iteratorObj = jni.List_iterator(combatantsList);
  assert(iteratorObj);

  std::uint8_t i = 0;
  while (jni.Iterator_hasNext(iteratorObj)) {
    jobject combatantObj = jni.Iterator_next(iteratorObj);
    assert(combatantObj);

    auto combatant = loadCombatant(jni, combatantObj);
    if (!combatant)
      return {};

    combatants[i++] = std::move(*combatant);
  }
  assert(i == numCombatants);

  return Combatants{
      .combatants = std::move(combatants),
      .num = numCombatants,
  };
}

std::optional<Party> createParty(Combatants &combatants) {
  auto *cs = combatants.combatants.get();

  std::uint64_t totalUnits = std::accumulate(
      cs, cs + combatants.num, std::uint64_t{0u}, [](std::uint64_t lhs, const Combatant &c) {
        const auto *unitGroups = c.unitGroups.get();
        return lhs + std::accumulate(unitGroups, unitGroups + g_numKinds, std::uint64_t{0u});
      });

  std::unique_ptr<Unit[]> units(new (std::nothrow) Unit[totalUnits]);
  if (!units) {
    std::fputs("BattleEngine: Allocating memory for units failed\n", stderr);
    return {};
  }

  // Initialize each unit. We don't initialize shields here, we will do it in restoreShields().
  auto *u = units.get();
  for (std::uint8_t i = 0; i < combatants.num; ++i) {
    const Combatant &combatant = cs[i];
    for (std::uint8_t kind = 0; kind < g_numKinds; ++kind) {
      float maxHull =
          0.1f * g_unitsAttributes[kind].armor * (1.0f + 0.1f * combatant.armorTechnology);
      for (std::uint64_t j = 0; j < combatant.unitGroups[kind]; ++j) {
        u->hull = maxHull;
        u->kind = kind;
        u->combatantId = i;
        ++u;
      }
    }
  }

  return Party{
      .units = std::move(units),
      .numAlive = totalUnits,
  };
}

void restoreShields(const Combatants &combatants, Party &party) {
  const Combatant *cs = combatants.combatants.get();
  for (std::uint64_t i = 0; i < party.numAlive; ++i) {
    Unit &unit = party.units[i];
    const Combatant &combatant = cs[unit.combatantId];
    unit.shield =
        g_unitsAttributes[unit.kind].shield * (1.0f + 0.1f * combatant.shieldingTechnology);
  }
}

void fire(Combatants &attackers, Combatants &defenders, Party &attackersParty,
          Party &defendersParty, unsigned round, std::uint32_t &random) {
  std::uint32_t r = random;

  Combatant *atts = attackers.combatants.get();
  const Unit *shooters = attackersParty.units.get();
  std::uint64_t numShooters = attackersParty.numAlive;

  Combatant *defs = defenders.combatants.get();
  Unit *targets = defendersParty.units.get();
  std::uint64_t numTargets = defendersParty.numAlive;

  // Each shooter fires at one or more random targets.
  for (std::uint64_t i = 0; i < numShooters; ++i) {
    const Unit &shooter = shooters[i];
    std::uint8_t shooterKind = shooter.kind;
    const UnitAttributes &shooterAttrs = g_unitsAttributes[shooterKind];
    Combatant &attacker = atts[shooter.combatantId];
    UnitGroupStats &shooterStats = attacker.stats[round * g_numKinds + shooterKind];

    float damage = shooterAttrs.weapons * (1.0f + 0.1f * attacker.weaponsTechnology);

    unsigned rapidFire;
    do {
      // Pick a random target.
      r = Random::next(r);
      Unit &target = targets[r % numTargets];
      std::uint8_t targetKind = target.kind;
      const UnitAttributes &targetAttrs = g_unitsAttributes[targetKind];
      Combatant &defender = defs[target.combatantId];
      UnitGroupStats &targetStats = defender.stats[round * g_numKinds + targetKind];

      ++shooterStats.timesFired;
      ++targetStats.timesWasShot;

      float shield = target.shield;
      float hull = target.hull;

      // Is the target alive?
      if (hull != 0.0f) {
        float hullDamage = damage - shield;

        // Does the shooter break through the shield at all?
        if (hullDamage < 0.0f) {
          // All damage absorbed by the shield. Calculate the shield damage including the bouncing
          // effect.
          float maxShield = targetAttrs.shield * (1.0f + 0.1f * defender.shieldingTechnology);
          float shieldDamage = 0.01f * floorf(100.0f * damage / maxShield) * maxShield;
          shield -= shieldDamage;

          shooterStats.shieldDamageDealt += shieldDamage;
          targetStats.shieldDamageTaken += shieldDamage;
        } else {
          // We break through the shield.
          float shieldDamage = shield;
          shield = 0.0f;

          if (hullDamage > hull)
            hullDamage = hull;
          hull -= hullDamage;

          shooterStats.shieldDamageDealt += shieldDamage;
          shooterStats.hullDamageDealt += hullDamage;
          targetStats.shieldDamageTaken += shieldDamage;
          targetStats.hullDamageTaken += hullDamage;
        }

        if (hull != 0.0f) {
          // If the target's hull is less than 70%, the target might explode.
          float maxHull = 0.1f * targetAttrs.armor * (1.0f + 0.1f * defender.armorTechnology);
          if (hull < 0.7f * maxHull) {
            r = Random::next(r);
            if (hull < (1.0f / static_cast<float>(Random::max) * static_cast<float>(r) * maxHull)) {
              // The target explodes, we mark it as dead.
              hull = 0.0f;
            }
          }
        }

        target.shield = shield;
        target.hull = hull;
      }

      rapidFire = shooterAttrs.rapidFire[targetKind];
    } while (rapidFire != 0 && (r = Random::next(r)) % rapidFire != 0);
  }

  random = r;
}

// Remove (place at the end) the dead units, and update the stats.
void updateUnits(Combatants &combatants, Party &party, unsigned round) {
  Combatant *cs = combatants.combatants.get();
  Unit *units = party.units.get();
  std::uint64_t n = 0;
  for (std::uint64_t i = 0; i < party.numAlive; ++i) {
    Unit &unit = units[i];
    if (unit.hull != 0.0f) {
      units[n++] = unit;
      Combatant &combatant = cs[unit.combatantId];
      ++combatant.stats[round * g_numKinds + unit.kind].numRemainingUnits;
    }
  }
  party.numAlive = n;
}

jobject createCombatantOutcome(const Jni &jni, const Combatant &combatant, unsigned numRounds) {
  const UnitGroupStats *stats = combatant.stats.get();

  jobject combatantStatsObj = jni.ArrayList_init(static_cast<int>(numRounds));
  assert(combatantStatsObj);

  jobjectArray valuesObj = jni.UnitKind_values();
  assert(valuesObj);

  for (unsigned round = 0; round < numRounds; ++round) {
    jobject roundStatsObj = jni.EnumMap_init(jni.unitKind.clazz);
    for (std::uint8_t kind = 0; kind < g_numKinds; ++kind) {
      jobject kindObj = jni.env->GetObjectArrayElement(valuesObj, kind);
      assert(kindObj);

      const auto *s = &stats[round * g_numKinds + kind];
      auto numRemainingUnits = static_cast<long>(s->numRemainingUnits);
      auto timesFired = static_cast<long>(s->timesFired);
      auto timesWasShot = static_cast<long>(s->timesWasShot);
      jobject statsObj =
          jni.UnitGroupStats_init(numRemainingUnits, timesFired, timesWasShot, s->shieldDamageDealt,
                                  s->hullDamageDealt, s->shieldDamageTaken, s->hullDamageTaken);
      assert(statsObj);

      jobject prevObj = jni.EnumMap_put(roundStatsObj, kindObj, statsObj);
      (void)prevObj;
      assert(!prevObj);
    }
    jni.ArrayList_add(combatantStatsObj, roundStatsObj);
  }

  jobject outcomeObj = jni.CombatantOutcome_init(combatantStatsObj);
  assert(outcomeObj);
  return outcomeObj;
}

jobject createCombatantOutcomes(const Jni &jni, const Combatants &combatants, unsigned numRounds) {
  jobject outcomesObj = jni.ArrayList_init(combatants.num);
  assert(outcomesObj);
  for (std::uint8_t i = 0; i < combatants.num; ++i) {
    const Combatant &combatant = combatants.combatants[i];
    jobject outcomeObj = createCombatantOutcome(jni, combatant, numRounds);
    assert(outcomeObj);
    jni.ArrayList_add(outcomesObj, outcomeObj);
  }
  return outcomesObj;
}

jobject createBattleOutcome(const Jni &jni, const Combatants &attackers,
                            const Combatants &defenders, unsigned numRounds) {
  static_assert(maxRounds <= std::numeric_limits<int>::max());
  auto nRounds = static_cast<int>(numRounds);
  jobject attackersOutcomes = createCombatantOutcomes(jni, attackers, numRounds);
  jobject defendersOutcomes = createCombatantOutcomes(jni, defenders, numRounds);
  jobject battleOutcome = jni.BattleOutcome_init(nRounds, attackersOutcomes, defendersOutcomes);
  assert(battleOutcome);
  return battleOutcome;
}

jobject fight(const Jni &jni, jobject attackersList, jobject defendersList, jint seed) {
  // Our RNG needs a positive seed.
  // Keep the calculation of the seed in sync with the java battle engine.
  if (seed < 0)
    seed = -seed;
  if (seed < 0 || seed == 0)
    seed = 1;
  auto random = static_cast<std::uint32_t>(seed);

  auto attackers = loadCombatants(jni, attackersList);
  if (!attackers)
    return nullptr;

  auto defenders = loadCombatants(jni, defendersList);
  if (!defenders)
    return nullptr;

  auto attackersParty = createParty(*attackers);
  if (!attackersParty)
    return nullptr;

  auto defendersParty = createParty(*defenders);
  if (!defendersParty)
    return nullptr;

  unsigned round = 0u;
  while (round < maxRounds && attackersParty->numAlive > 0u && defendersParty->numAlive > 0u) {
    restoreShields(*attackers, *attackersParty);
    restoreShields(*defenders, *defendersParty);

    fire(*attackers, *defenders, *attackersParty, *defendersParty, round, random);
    fire(*defenders, *attackers, *defendersParty, *attackersParty, round, random);

    updateUnits(*attackers, *attackersParty, round);
    updateUnits(*defenders, *defendersParty, round);

    ++round;
  }

  unsigned numRounds = round;
  return createBattleOutcome(jni, *attackers, *defenders, numRounds);
}

} // namespace

extern "C" JNIEXPORT jboolean JNICALL
Java_com_github_retro_1game_retro_1game_battleengine_NativeBattleEngine_init(
    JNIEnv *env, [[maybe_unused]] jobject battleEngine, jobjectArray unitsAttributesArray) {
  Jni jni(env);
  if (!jni.init())
    return false;

  return initUnitsAttributes(jni, unitsAttributesArray);
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_github_retro_1game_retro_1game_battleengine_NativeBattleEngine_fight(
    JNIEnv *env, [[maybe_unused]] jobject battleEngine, jobject attackersList,
    jobject defendersList, jint seed) {
  Jni jni(env);
  if (!jni.init())
    return nullptr;

  return fight(jni, attackersList, defendersList, seed);
}
