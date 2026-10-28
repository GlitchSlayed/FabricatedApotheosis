# Handoff Report — Milestone 5: Apothic Attributes Combat Pipeline & Gameplay

- **Date**: 2026-10-27
- **Target Version**: Minecraft 26.3 Fabric (Loom 1.17.21, Loader 0.19.5, YACL 3.9.6)

## 1. Current Repository Tree

```text
FabricatedApotheosis/
├── .github/workflows/build.yml
├── .gitignore
├── AGENTS.md
├── Agents.md -> AGENTS.md
├── LICENSE (MIT - Code)
├── LICENSE_ASSETS (All Rights Reserved - Upstream Assets)
├── NOTICE (Third-Party Attributions)
├── README.md
├── build.gradle (with verifyNoDuplicateClasses & DuplicatesStrategy.FAIL)
├── gradle.properties
├── settings.gradle
├── gradlew
├── gradlew.bat
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties
├── docs/
│   ├── ARCHITECTURE.md
│   ├── DAMAGE_PIPELINE.md
│   ├── DECISIONS.md
│   ├── DEPENDENCY_MATRIX.md
│   ├── FEATURE_PARITY.md
│   ├── HANDOFF_REPORT.md
│   ├── PORTING_SURFACE.md
│   ├── REFERENCE_INDEX.md
│   ├── STATUS.md
│   ├── TEST_PLAN.md
│   ├── UPSTREAM_LOCK.md
│   └── VANILLA_OVERLAP.md
├── reference -> Refrences (symlink, excluded from git)
├── Refrences/ (read-only reference jars and classes, excluded from git)
├── upstream/ (cloned upstream 26.1.2 sources, excluded from git)
├── src/
│   ├── main/
│   │   ├── java/dev/shadowsoffire/
│   │   │   ├── apotheosis/
│   │   │   │   ├── Apotheosis.java
│   │   │   │   ├── adventure/AdventureModule.java
│   │   │   │   ├── attributes/
│   │   │   │   │   ├── AttributesModule.java
│   │   │   │   │   ├── api/
│   │   │   │   │   │   ├── ALCombatRules.java
│   │   │   │   │   │   ├── ALObjects.java
│   │   │   │   │   │   ├── AbilityCooldowns.java
│   │   │   │   │   │   ├── AttributeEvents.java
│   │   │   │   │   │   ├── AttributeHelper.java
│   │   │   │   │   │   ├── AttributesUtil.java
│   │   │   │   │   │   ├── AuxDmgTracker.java
│   │   │   │   │   │   ├── CooldownTracker.java
│   │   │   │   │   │   └── LEInvoker.java
│   │   │   │   │   ├── effects/
│   │   │   │   │   │   ├── BleedingEffect.java
│   │   │   │   │   │   ├── DetonationEffect.java
│   │   │   │   │   │   ├── FlyingEffect.java
│   │   │   │   │   │   ├── GrievousEffect.java
│   │   │   │   │   │   ├── KnowledgeEffect.java
│   │   │   │   │   │   ├── SunderingEffect.java
│   │   │   │   │   │   └── VitalityEffect.java
│   │   │   │   │   ├── format/
│   │   │   │   │   │   └── AttributeFormatting.java
│   │   │   │   │   └── payload/
│   │   │   │   │       └── CritParticlePayload.java
│   │   │   │   ├── config/
│   │   │   │   │   ├── ApotheosisConfig.java
│   │   │   │   │   ├── ClientConfig.java
│   │   │   │   │   ├── ConfigImpact.java
│   │   │   │   │   ├── ConfigMigration.java
│   │   │   │   │   ├── ServerConfig.java
│   │   │   │   │   └── network/
│   │   │   │   │       └── ApotheosisConfigPayload.java
│   │   │   │   ├── enchanting/EnchantingModule.java
│   │   │   │   ├── mixin/
│   │   │   │   │   ├── AbstractArrowAccessor.java
│   │   │   │   │   ├── AbstractArrowMixin.java
│   │   │   │   │   ├── BlockMixin.java
│   │   │   │   │   ├── CombatRulesMixin.java
│   │   │   │   │   ├── EntityAccessor.java
│   │   │   │   │   ├── EntityMixin.java
│   │   │   │   │   ├── LivingEntityAccessor.java
│   │   │   │   │   ├── LivingEntityMixin.java
│   │   │   │   │   ├── NearestAttackableTargetGoalMixin.java
│   │   │   │   │   ├── PlayerMixin.java
│   │   │   │   │   ├── ServerPlayerGameModeMixin.java
│   │   │   │   │   └── ThrownTridentMixin.java
│   │   │   │   └── spawners/SpawnersModule.java
│   │   │   └── placebo/
│   │   └── resources/
│   │       ├── apotheosis.mixins.json
│   │       ├── fabric.mod.json
│   │       └── data/
│   │           ├── apothic_attributes/
│   │           │   ├── damage_type/
│   │           │   └── tags/damage_type/
│   │           ├── c/tags/damage_type/
│   │           ├── minecraft/tags/damage_type/
│   │           └── neoforge/tags/damage_type/
│   ├── client/
│   └── test/
```

---

## 2. Implemented Features & Architecture

### Damage Processing Pipeline (`docs/DAMAGE_PIPELINE.md`)
The authoritative 11-stage damage processing pipeline maps every upstream NeoForge event and priority to targeted Fabric mixins:
1. **Dodge & Cancellation**: Deterministic per-tick hash roll (`AttributeEvents.checkDodge`), melee reach bounds check, and projectile impact dodge (`AbstractArrowMixin`).
2. **Attack Scaling & Velocity**: First-tick arrow damage & velocity scaling with `"apothic_attributes.arrow.done"` loop prevention tag; thrown trident quad-scaling (`ThrownTridentMixin`, `AbstractArrowAccessor`).
3. **Critical Strikes & Multicrit**: Multi-tier critical strikes degradation loop ($0.85\times$ per tier above 100%), vanilla jump critical upgrade ($\max(1.5\text{F}, \text{critDamage})$), and S2C crit particle network packet (`CritParticlePayload`).
4. **Resistance & Sundering**: Sundering amplifier scaling damage taken by 20% per level.
5. **Armor & Piercing**: `ALCombatRules.getDamageAfterArmor` implementing custom A-value formula, toughness bypass resistance ($\min(0.02 \times \text{toughness}, 0.60)$), negative effective armor factor ($0.015 \times (-\text{armor}) \times \text{damage}$), and armor shred/pierce.
6. **Protection & Piercing**: `ALCombatRules.getDamageAfterProtection` implementing protection shred/pierce and 80% maximum protection cap.
7. **Absorption & Health**: Overheal cap ($50\%$ max HP) without destroying external absorption (`AttributesUtil`).
8. **Life Steal & Overheal**: Post-damage life steal and overheal conversion in `actuallyHurt` using pre-damage health capture (`ALObjects.Attachments.PRE_DAMAGE_HEALTH`).
9. **Auxiliary Damage**: Non-reentrant auxiliary damage execution (`AuxDmgTracker`, `AttributeEvents`), invulnerability time preservation, sweeping attack ratio scaling, and current-HP, fire, and cold damage with post-callbacks.
10. **Draw Speed & Cooldown Reduction**: `DRAW_SPEED` use-duration tick acceleration in `updateUsingItem`, persistent ability cooldown tracking (`CooldownTracker`, `AbilityCooldowns`) with CDR formula (`applyCDR`).
11. **Flight Mechanics & Mobility**: `canGlide` / `canGlideUsing` for `ELYTRA_FLIGHT`, and safe creative flight grant tracking (`had_flight_granted`) preserving external/creative flight states.
12. **Experience Scaling**: Mob experience in `getExperienceReward` and block experience in `popExperience` scaled by `EXPERIENCE_GAINED` via `AttributesUtil.CURRENT_BREAKER`.
13. **Mob Effects**: 7 registered mob effects (`BleedingEffect`, `DetonationEffect`, `FlyingEffect`, `GrievousEffect`, `KnowledgeEffect`, `SunderingEffect`, `VitalityEffect`).

---

## 3. Verification & Test Results

1. **Automated Unit Tests**:
   - `CombatRulesTest.testAValueFormula()`: Passed.
   - `CombatRulesTest.testArmorReduction()`: Passed.
   - `CombatRulesTest.testArmorPierceAndShred()`: Passed.
   - `CombatRulesTest.testNegativeArmorAmplification()`: Passed.
   - `CombatRulesTest.testProtectionPierceAndShred()`: Passed.
   - `CombatRulesTest.testMulticritCalculation()`: Passed.
   - `CombatRulesTest.testCooldownReductionMath()`: Passed.
   - `CombatRulesTest.testCooldownTrackerPrune()`: Passed.
   - `CombatRulesTest.testDodgeChanceDeterminism()`: Passed.
   - `CombatRulesTest.testAuxDmgTrackerNoRecursion()`: Passed.
   - `AttributesTest`: All 6 attribute tests passed.
   - `ApotheosisConfigTest`: All config tests passed.
   - `Placebo` tests: All dynamic registry and serialization tests passed.
   - **Total test suite**: 23 tests executed, 23 passed (`./gradlew test`).

2. **Compilation & Packaging**:
   - Aggregate compilation (`compileJava`, `compileClientJava`, `compileTestJava`) succeeded.
   - Aggregate jar build (`./gradlew build`) succeeded.
   - Duplicate class defense task (`verifyNoDuplicateClasses`) passed with 0 violations.

3. **Dedicated Server Launch**:
   - Executed `./gradlew runServer --args="--nogui"`.
   - Log output:
     `[Apotheosis/Attributes] Apothic Attributes module initialized successfully (all attributes registered and attached)`
     `[Server thread/INFO] (Minecraft) Preparing level "world"`
     `[Server thread/INFO] (Minecraft) Preparing spawn area: 100%`
     `[Server thread/INFO] (Minecraft) Done (0.287s)! For help, type "help"`
   - Server shut down cleanly.
   - Zero mixin injection errors; 100% absence of client GUI/screen classes on dedicated server.

---

## 4. Next Concrete Implementation Task

Proceed to **Milestone 6: Apothic Spawners Core**:
1. Implement spawner stat modifications hooking into Minecraft 26.3 `BaseSpawner` (min/max spawn delay, spawn count, max nearby entities, required player range, spawn range).
2. Implement spawner modifier items and data map definitions (sugar, clock, ghast tear, blaze rod, fermented spider eye, dragon egg, etc.).
3. Implement Silk Touch spawner capture and custom drops loot table handling.
4. Implement redstone control and condition-ignoring spawner modifiers.
