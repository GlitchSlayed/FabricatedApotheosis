package dev.shadowsoffire.apotheosis.mobs.types;

import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.mixin.EntityInvoker;
import dev.shadowsoffire.apotheosis.mobs.registries.EliteRegistry.IEntityMatch;
import dev.shadowsoffire.apotheosis.mobs.util.AffixData;
import dev.shadowsoffire.apotheosis.mobs.util.BasicBossData;
import dev.shadowsoffire.apotheosis.mobs.util.BossStats;
import dev.shadowsoffire.apotheosis.mobs.util.SupportingEntity;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.Constraints.Constrained;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights.Weighted;
import dev.shadowsoffire.placebo.json.ChancedEffectInstance;
import dev.shadowsoffire.placebo.json.RandomAttributeModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;

public record Elite(BasicBossData basicData, float chance, HolderSet<EntityType<?>> entities, BossStats stats, AffixData afxData) implements Constrained, Weighted, IEntityMatch {

    public static final String MINIBOSS_KEY = "apoth.miniboss";
    public static final String PLAYER_KEY = MINIBOSS_KEY + ".player";

    public static final Codec<Elite> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            BasicBossData.CODEC.fieldOf("basic_data").forGetter(Elite::basicData),
            Codec.floatRange(0, 1).fieldOf("success_chance").forGetter(Elite::chance),
            RegistryCodecs.holderSet(Registries.ENTITY_TYPE).fieldOf("entities").forGetter(Elite::entities),
            BossStats.CODEC.fieldOf("stats").forGetter(Elite::stats),
            AffixData.CODEC.optionalFieldOf("affix_data", AffixData.DEFAULT).forGetter(Elite::afxData))
        .apply(inst, Elite::new));

    @Override
    public TieredWeights weights() {
        return this.basicData.weights();
    }

    @Override
    public Constraints constraints() {
        return this.basicData.constraints();
    }

    public float getChance() {
        return this.chance;
    }

    @Override
    public HolderSet<EntityType<?>> getEntities() {
        return this.entities;
    }

    public void transformMiniboss(ServerLevelAccessor level, Mob mob, GenContext ctx) {
        Vec3 pos = mob.getPosition(0);

        Optional<CompoundTag> optNbt = this.basicData.nbt();
        if (optNbt.isPresent()) {
            CompoundTag nbt = optNbt.get();
            if (nbt.contains(Entity.TAG_PASSENGERS)) {
                ListTag passengers = nbt.getListOrEmpty(Entity.TAG_PASSENGERS);
                for (int i = 0; i < passengers.size(); ++i) {
                    Entity entity = EntityType.loadEntityRecursive(passengers.getCompoundOrEmpty(i), level.getLevel(), new net.minecraft.world.entity.EntitySpawnRequest(net.minecraft.world.entity.EntitySpawnReason.EVENT, false), net.minecraft.world.entity.EntityProcessor.NOP);
                    if (entity != null) {
                        entity.setPos(pos);
                        level.addFreshEntityWithPassengers(entity);
                        entity.startRiding(mob, true, true);
                    }
                }
            }
        }

        mob.setPos(pos);
        this.initElite(mob, ctx);

        if (optNbt.isPresent()) {
            try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(mob.problemPath(), dev.shadowsoffire.apotheosis.Apotheosis.LOGGER)) {
                ((EntityInvoker) mob).callReadAdditionalSaveData(TagValueInput.create(reporter, level.registryAccess(), optNbt.get()));
            }
        }

        if (this.basicData.hasMount()) {
            BlockPos bPos = BlockPos.containing(pos);
            this.basicData.createMount(level, bPos, mob);
        }

        for (SupportingEntity support : this.basicData.support()) {
            Mob sup = support.create(level.getLevel(), pos.x, pos.y, pos.z);
            if (sup != null) {
                level.addFreshEntityWithPassengers(sup);
            }
        }
    }

    public void initElite(Mob mob, GenContext ctx) {
        RandomSource rand = ctx.rand();

        int duration = mob instanceof Creeper ? 6000 : Integer.MAX_VALUE;

        for (ChancedEffectInstance inst : this.stats.effects()) {
            if (rand.nextFloat() <= inst.chance()) {
                mob.addEffect(inst.create(rand, duration));
            }
        }

        for (RandomAttributeModifier modif : this.stats.modifiers()) {
            modif.apply(rand, mob);
        }

        this.basicData.applyEntityName(rand, mob);
        this.basicData.applyGearSet(mob, ctx);

        EquipmentSlot affixedSlot = this.afxData.applyTo(mob, ctx, this.stats.enchLevels().primary(), true);

        for (EquipmentSlot s : EquipmentSlot.values()) {
            ItemStack stack = mob.getItemBySlot(s);
            if (stack.isEmpty()) {
                continue;
            }

            if (s != affixedSlot && rand.nextFloat() < this.stats.enchantChance()) {
                Invader.enchantBossItem(rand, stack, this.stats.enchLevels().secondary(), true, mob.level().registryAccess());
                mob.setItemSlot(s, stack);
            }
        }

        mob.setHealth(mob.getMaxHealth());
        this.basicData.appendBonusLoot(mob);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BasicBossData basicData;
        private float chance = 0F;
        private HolderSet<EntityType<?>> entities;
        private BossStats stats;
        private AffixData afxData = AffixData.DEFAULT;

        public Builder basicData(UnaryOperator<BasicBossData.Builder> config) {
            this.basicData = config.apply(BasicBossData.builder()).build();
            return this;
        }

        public Builder chance(float chance) {
            this.chance = chance;
            return this;
        }

        public Builder entities(HolderSet<EntityType<?>> entities) {
            if (this.entities == null) {
                this.entities = entities;
            }
            else {
                this.entities = HolderSet.direct(Stream.concat(this.entities.stream(), entities.stream()).toList());
            }
            return this;
        }

        public Builder entities(TagKey<EntityType<?>> entities) {
            return this.entities(BuiltInRegistries.ENTITY_TYPE.get(entities).orElseThrow());
        }

        @SafeVarargs
        @SuppressWarnings("deprecation")
        public final Builder entities(EntityType<? extends Mob>... entities) {
            return this.entities(HolderSet.direct(EntityType::builtInRegistryHolder, entities));
        }

        public Builder stats(UnaryOperator<BossStats.Builder> config) {
            this.stats = config.apply(BossStats.builder()).build();
            return this;
        }

        public Builder affixes(float chance, Set<LootRarity> rarities) {
            this.afxData = new AffixData(chance, rarities);
            return this;
        }

        public Elite build() {
            if (this.basicData == null) {
                throw new IllegalStateException("BasicBossData must be set");
            }
            if (this.chance <= 0) {
                throw new IllegalStateException("Chance value must be positive");
            }
            if (this.entities == null) {
                throw new IllegalStateException("Entities must be set");
            }
            if (this.stats == null) {
                throw new IllegalStateException("Stats must be set");
            }
            return new Elite(this.basicData, this.chance, this.entities, this.stats, this.afxData);
        }
    }
}
