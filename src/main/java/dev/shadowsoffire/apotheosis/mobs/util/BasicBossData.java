package dev.shadowsoffire.apotheosis.mobs.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.Attachments;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.attachments.BonusLootTables;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.util.NameHelper;
import dev.shadowsoffire.placebo.dynreg.tag.DynamicHolderSet;
import dev.shadowsoffire.placebo.json.NBTAdapter;
import dev.shadowsoffire.placebo.systems.gear.GearSet;
import dev.shadowsoffire.placebo.systems.gear.GearSetRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

/**
 * Basic boss information, shared between Elite and Invader.
 */
public record BasicBossData(
    TieredWeights weights,
    Constraints constraints,
    Component name,
    BonusLootTables bonusLoot,
    Map<WorldTier, DynamicHolderSet<GearSet>> gearSets,
    Optional<CompoundTag> nbt,
    Optional<SupportingEntity> mount,
    List<SupportingEntity> support,
    boolean finalizeSpawn,
    List<SpawnCondition> spawnConditions) {

    public static final String NAME_GEN = "use_name_generation";

    public static final Codec<BasicBossData> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            TieredWeights.CODEC.fieldOf("weights").forGetter(BasicBossData::weights),
            Constraints.CODEC.optionalFieldOf("constraints", Constraints.EMPTY).forGetter(BasicBossData::constraints),
            ComponentSerialization.CODEC.optionalFieldOf("name", CommonComponents.EMPTY).forGetter(BasicBossData::name),
            BonusLootTables.CODEC.optionalFieldOf("bonus_loot", BonusLootTables.EMPTY).forGetter(BasicBossData::bonusLoot),
            WorldTier.mapCodec(DynamicHolderSet.codec(GearSetRegistry.INSTANCE)).codec().optionalFieldOf("valid_gear_sets", Map.of()).forGetter(BasicBossData::gearSets),
            NBTAdapter.EITHER_CODEC.optionalFieldOf("nbt").forGetter(BasicBossData::nbt),
            SupportingEntity.CODEC.optionalFieldOf("mount").forGetter(BasicBossData::mount),
            SupportingEntity.CODEC.listOf().optionalFieldOf("supporting_entities", Collections.emptyList()).forGetter(BasicBossData::support),
            Codec.BOOL.optionalFieldOf("finalize", false).forGetter(BasicBossData::finalizeSpawn),
            SpawnCondition.CODEC.listOf().optionalFieldOf("spawn_conditions", Collections.emptyList()).forGetter(BasicBossData::spawnConditions))
        .apply(inst, BasicBossData::new));

    public static final Codec<AABB> AABB_CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.DOUBLE.fieldOf("width").forGetter(a -> Math.abs(a.maxX - a.minX)),
            Codec.DOUBLE.fieldOf("height").forGetter(a -> Math.abs(a.maxY - a.minY)))
        .apply(inst, (width, height) -> new AABB(0, 0, 0, width, height, width)));

    public boolean hasGearSets(WorldTier tier) {
        DynamicHolderSet<GearSet> set = this.gearSets.get(tier);
        return set != null && set.size() > 0;
    }

    public boolean hasNbt() {
        return this.nbt.isPresent();
    }

    public boolean hasMount() {
        return this.mount.isPresent();
    }

    public void applyEntityName(RandomSource rand, Mob mob) {
        String nameStr = this.name.getString();

        if (NAME_GEN.equals(nameStr)) {
            NameHelper.setEntityName(rand, mob);
        }
        else if (!nameStr.isBlank()) {
            mob.setCustomName(this.name);
        }

        if (mob.hasCustomName()) {
            mob.setCustomNameVisible(true);
        }
    }

    public void appendBonusLoot(Mob mob) {
        if (!this.bonusLoot.isEmpty()) {
            BonusLootTables existing = dev.shadowsoffire.apotheosis.util.ModAttachments.getData(mob, Attachments.BONUS_LOOT_TABLES);
            if (existing == null) {
                existing = BonusLootTables.EMPTY;
            }
            dev.shadowsoffire.apotheosis.util.ModAttachments.setData(mob, Attachments.BONUS_LOOT_TABLES, existing.mergeWith(this.bonusLoot));
        }
    }

    @Nullable
    public GearSet applyGearSet(Mob mob, GenContext ctx) {
        DynamicHolderSet<GearSet> sets = this.gearSets.get(ctx.tier());
        if (sets == null || sets.size() == 0) {
            return null;
        }

        GearSet set = GearSetRegistry.INSTANCE.getRandomSet(ctx.rand(), ctx.luck(), sets);
        if (set != null) {
            set.apply(mob);
        }
        return set;
    }

    /**
     * Creates the mounted entity stored in this boss data. This method does not add the entity to the level.
     */
    public Mob createMount(ServerLevelAccessor level, BlockPos pos, Mob rider) {
        if (!this.hasMount()) {
            Apotheosis.LOGGER.error("BasicBossData#createMount called when hasMount() was false!");
            return rider;
        }

        Mob mountedEntity = this.mount.get().create(level.getLevel(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        if (mountedEntity != null) {
            rider.startRiding(mountedEntity, true, true);
        }
        return mountedEntity != null ? mountedEntity : rider;
    }

    public boolean canSpawn(Mob mob, ServerLevelAccessor level, EntitySpawnReason type) {
        return SpawnCondition.checkAll(this.spawnConditions, mob, level, type);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TieredWeights weights;
        private Constraints constraints = Constraints.EMPTY;
        private Component name = CommonComponents.EMPTY;
        private BonusLootTables bonusLoot = BonusLootTables.EMPTY;
        private Map<WorldTier, DynamicHolderSet<GearSet>> gearSets = new HashMap<>();
        private Optional<CompoundTag> nbt = Optional.empty();
        private Optional<SupportingEntity> mount = Optional.empty();
        private List<SupportingEntity> support = new ArrayList<>();
        private boolean finalizeSpawn = false;
        private List<SpawnCondition> exclusions = new ArrayList<>();

        public Builder weights(TieredWeights weights) {
            this.weights = weights;
            return this;
        }

        public Builder weights(UnaryOperator<TieredWeights.Builder> config) {
            return this.weights(config.apply(TieredWeights.builder()).build());
        }

        public Builder constraints(Constraints constraints) {
            this.constraints = constraints;
            return this;
        }

        public Builder constraints(UnaryOperator<Constraints.Builder> config) {
            return this.constraints(config.apply(Constraints.builder()).build());
        }

        public Builder name(Component name) {
            this.name = name;
            return this;
        }

        public Builder name(String name) {
            return this.name(Component.translatable(name));
        }

        public Builder generatedName() {
            return this.name(NAME_GEN);
        }

        public Builder bonusLoot(BonusLootTables bonusLoot) {
            this.bonusLoot = bonusLoot;
            return this;
        }

        public Builder gearSet(WorldTier tier, DynamicHolderSet<GearSet> gearSets) {
            this.gearSets.put(tier, gearSets);
            return this;
        }

        public Builder nbt(CompoundTag nbt) {
            this.nbt = Optional.of(nbt);
            return this;
        }

        public Builder mount(SupportingEntity mount) {
            this.mount = Optional.of(mount);
            return this;
        }

        public Builder support(SupportingEntity... support) {
            this.support.addAll(List.of(support));
            return this;
        }

        public Builder finalizeSpawn(boolean finalizeSpawn) {
            this.finalizeSpawn = finalizeSpawn;
            return this;
        }

        public Builder conditions(SpawnCondition... conditions) {
            this.exclusions.addAll(List.of(conditions));
            return this;
        }

        public BasicBossData build() {
            if (this.weights == null) {
                throw new IllegalStateException("Weights must be set");
            }
            return new BasicBossData(this.weights, this.constraints, this.name, this.bonusLoot, this.gearSets, this.nbt, this.mount, this.support, this.finalizeSpawn, this.exclusions);
        }
    }
}
