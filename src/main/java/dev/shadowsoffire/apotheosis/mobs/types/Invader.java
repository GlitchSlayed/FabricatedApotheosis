package dev.shadowsoffire.apotheosis.mobs.types;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apoth.LootCategories;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.ItemAffixes;
import dev.shadowsoffire.apotheosis.compat.enchanting.ApothicEnchantingCompat;
import dev.shadowsoffire.apotheosis.attributes.api.AttributesUtil;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.mixin.EntityInvoker;
import dev.shadowsoffire.apotheosis.mixin.NearestAttackableTargetGoalAccessor;
import dev.shadowsoffire.apotheosis.mobs.InvaderSpawnRules;
import dev.shadowsoffire.apotheosis.mobs.util.BasicBossData;
import dev.shadowsoffire.apotheosis.mobs.util.BossStats;
import dev.shadowsoffire.apotheosis.tiers.Constraints;
import dev.shadowsoffire.apotheosis.tiers.Constraints.Constrained;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights.Weighted;
import dev.shadowsoffire.apotheosis.util.NameHelper;
import dev.shadowsoffire.apothic_attributes.modifiers.EquipmentSlotCompat;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import dev.shadowsoffire.placebo.json.ChancedEffectInstance;
import dev.shadowsoffire.placebo.json.RandomAttributeModifier;
import dev.shadowsoffire.placebo.systems.gear.GearSet;
import dev.shadowsoffire.placebo.systems.gear.GearSetRegistry;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.AABB;

/**
 * An Invader is a preset entity with per-rarity stats that will spawn with a full gear set and an equipped affix item.
 */
public record Invader(BasicBossData basicData, EntityType<?> entity, AABB size, Map<LootRarity, BossStats> stats) implements Constrained, Weighted {

    public static final String BOSS_KEY = "apoth.boss";
    public static final String RARITY_KEY = BOSS_KEY + ".rarity";

    public static final Codec<Invader> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            BasicBossData.CODEC.fieldOf("basic_data").forGetter(Invader::basicData),
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(Invader::entity),
            BasicBossData.AABB_CODEC.fieldOf("size").forGetter(Invader::size),
            LootRarity.mapCodec(BossStats.CODEC).fieldOf("stats").forGetter(Invader::stats))
        .apply(inst, Invader::new));

    public static final Predicate<Goal> IS_VILLAGER_ATTACK = a -> a instanceof NearestAttackableTargetGoalAccessor acc && acc.getTargetType() == Villager.class;

    public BasicBossData basicData() {
        return this.basicData;
    }

    @Override
    public TieredWeights weights() {
        return this.basicData.weights();
    }

    @Override
    public Constraints constraints() {
        return this.basicData.constraints();
    }

    public Mob createBoss(ServerLevelAccessor world, BlockPos pos, GenContext ctx) {
        return this.createBoss(world, pos, ctx, null);
    }

    public Mob createBoss(ServerLevelAccessor level, BlockPos pos, GenContext ctx, @Nullable LootRarity rarity) {
        Optional<CompoundTag> nbt = this.basicData.nbt();
        CompoundTag fakeNbt = nbt.map(CompoundTag::copy).orElse(new CompoundTag());
        fakeNbt.putString("id", EntityType.getKey(this.entity).toString());
        Mob entity = (Mob) EntityType.loadEntityRecursive(this.entity, fakeNbt, level.getLevel(), net.minecraft.world.entity.EntitySpawnReason.EVENT, net.minecraft.world.entity.EntityProcessor.NOP);

        this.initBoss(entity, ctx, rarity);

        if (nbt.isPresent()) {
            try (net.minecraft.util.ProblemReporter.ScopedCollector reporter = new net.minecraft.util.ProblemReporter.ScopedCollector(entity.problemPath(), Apotheosis.LOGGER)) {
                ((EntityInvoker) entity).callReadAdditionalSaveData(net.minecraft.world.level.storage.TagValueInput.create(reporter, level.registryAccess(), nbt.get()));
            }
        }

        entity.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, ctx.rand().nextFloat() * 360.0F, 0.0F);

        if (this.basicData.hasMount()) {
            entity = this.basicData.createMount(level, pos, entity);
        }

        entity.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, ctx.rand().nextFloat() * 360.0F, 0.0F);

        return entity;
    }

    public void initBoss(Mob mob, GenContext ctx, @Nullable LootRarity rarity) {
        RandomSource rand = ctx.rand();

        if (rarity == null) {
            rarity = LootRarity.random(ctx, this.stats.keySet());
        }

        BossStats bStats = this.stats.get(rarity);
        int duration = mob instanceof Creeper ? 6000 : Integer.MAX_VALUE;

        for (ChancedEffectInstance inst : bStats.effects()) {
            if (rand.nextFloat() <= inst.chance()) {
                mob.addEffect(inst.create(rand, duration));
            }
        }

        for (RandomAttributeModifier modif : bStats.modifiers()) {
            modif.apply(rand, mob);
        }

        mob.getGoalSelector().getAvailableGoals().removeIf(IS_VILLAGER_ATTACK);

        this.basicData.applyEntityName(rand, mob);

        GearSet set = this.basicData.applyGearSet(mob, ctx);

        if (set != null) {
            boolean anyValid = false;

            for (EquipmentSlot t : EquipmentSlot.values()) {
                ItemStack s = mob.getItemBySlot(t);
                if (!s.isEmpty() && !LootCategory.forItem(s).isNone()) {
                    anyValid = true;
                    break;
                }
            }

            if (!anyValid) {
                Apotheosis.LOGGER.error("Attempted to apply boss gear set " + GearSetRegistry.INSTANCE.getKey(set) + " but it had no valid affix loot items generated.");
            }
        }
        else {
            ItemStack affixItem = LootController.createRandomLootItem(ctx, rarity);
            LootCategory cat = LootCategory.forItem(affixItem);
            EquipmentSlot slot = Arrays.stream(EquipmentSlot.values())
                .filter(eSlot -> cat.getSlots().test(EquipmentSlotCompat.fromVanilla(eSlot)))
                .findAny()
                .orElse(EquipmentSlot.MAINHAND);
            mob.setItemSlot(slot, affixItem);
        }

        EquipmentSlot[] slots = EquipmentSlot.values();

        EquipmentSlot guaranteed = slots[rand.nextInt(6)];
        int tries = 50;

        ItemStack temp = mob.getItemBySlot(guaranteed);
        while (temp.isEmpty() || LootCategory.forItem(temp) == LootCategories.NONE) {
            guaranteed = slots[rand.nextInt(6)];
            temp = mob.getItemBySlot(guaranteed);

            if (tries-- <= 0) {
                break;
            }
        }

        for (EquipmentSlot s : EquipmentSlot.values()) {
            ItemStack stack = mob.getItemBySlot(s);
            if (stack.isEmpty()) {
                continue;
            }

            if (s == guaranteed) {
                boolean cursed = false;
                if (mob.level() instanceof ServerLevelAccessor sl) {
                    ResourceKey<DimensionType> dimId = sl.getLevel().dimensionTypeRegistration().unwrapKey().orElse(null);
                    InvaderSpawnRules rules = InvaderSpawnRules.getRules(dimId);
                    cursed = (rules == null ? Optional.<Boolean>empty() : rules.cursed()).orElse(AdventureConfig.curseBossItems);
                }

                mob.setDropChance(s, 2F);
                mob.setItemSlot(s, modifyBossItem(stack, mob.getName(), ctx, rarity, bStats.enchLevels().primary(), mob.level().registryAccess(), cursed));
                mob.setCustomName(mob.getName().copy().withStyle(Style.EMPTY.withColor(rarity.color())));
            }
            else if (rand.nextFloat() < bStats.enchantChance()) {
                enchantBossItem(rand, stack, bStats.enchLevels().secondary(), true, mob.level().registryAccess());
                mob.setItemSlot(s, stack);
            }
        }

        AttributesUtil.getPersistentData(mob).putBoolean(BOSS_KEY, true);
        AttributesUtil.getPersistentData(mob).putString(RARITY_KEY, RarityRegistry.INSTANCE.getKey(rarity).toString());
        mob.setHealth(mob.getMaxHealth());

        if (AdventureConfig.bossGlowOnSpawn) {
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 3600, 0, true, false));
        }

        this.basicData.appendBonusLoot(mob);
    }

    public static void enchantBossItem(RandomSource rand, ItemStack stack, int level, boolean treasure, RegistryAccess reg) {
        Stream<Holder<Enchantment>> available = reg.lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT).map(HolderSet::stream).orElse(Stream.empty());
        List<EnchantmentInstance> ench = EnchantmentHelper.selectEnchantment(rand, stack, level, available);
        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(stack));
        ench.stream().filter(d -> !d.enchantment().is(EnchantmentTags.CURSE)).forEach(i -> builder.upgrade(i.enchantment(), i.level()));
        EnchantmentHelper.setEnchantments(stack, builder.toImmutable());
    }

    public static ItemStack modifyBossItem(ItemStack stack, Component bossName, GenContext ctx, LootRarity rarity, int enchLevel, RegistryAccess reg, boolean cursed) {
        RandomSource rand = ctx.rand();
        if (enchLevel > 0) {
            enchantBossItem(rand, stack, enchLevel, true, reg);
        }
        NameHelper.setItemName(rand, stack);
        stack = LootController.createLootItem(stack, LootCategory.forItem(stack), rarity, ctx);

        ItemAffixes.Builder builder = stack.getOrDefault(Components.AFFIXES, ItemAffixes.EMPTY).toBuilder();
        for (DynamicHolder<Affix> afx : builder.keySet()) {
            builder.upgrade(afx, Math.min(Affix.STANDARD_MAX_LEVEL, builder.getLevel(afx) + Mth.nextFloat(rand, 0.1F, 0.25F)));
        }
        AffixHelper.setAffixes(stack, builder.build());

        Component bossOwnerName = Component.translatable(NameHelper.ownershipFormat, bossName);
        Component name = AffixHelper.getName(stack);
        if (name != null && name.getContents() instanceof TranslatableContents tc) {
            String oldKey = tc.getKey();
            String newKey = "misc.apotheosis.affix_name.two".equals(oldKey) ? "misc.apotheosis.affix_name.three" : "misc.apotheosis.affix_name.four";
            Object[] newArgs = new Object[tc.getArgs().length + 1];
            newArgs[0] = bossOwnerName;
            for (int i = 1; i < newArgs.length; i++) {
                newArgs[i] = tc.getArgs()[i - 1];
            }
            Component copy = Component.translatable(newKey, newArgs).withStyle(name.getStyle().withItalic(false));

            var rfbb = new RegistryFriendlyByteBuf(Unpooled.buffer(), reg);
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode(rfbb, copy);
            Component deserialized = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(rfbb);

            AffixHelper.setName(stack, deserialized);
        }

        ItemEnchantments.Mutable enchMap = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (Object2IntMap.Entry<Holder<Enchantment>> e : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()) {
            if (e.getKey() != null) {
                enchMap.upgrade(e.getKey(), Math.min(ApothicEnchantingCompat.getMaxLevel(e.getKey().value()), e.getIntValue() + rand.nextInt(2)));
            }
        }

        if (cursed) {
            List<Holder.Reference<Enchantment>> curses = reg.lookupOrThrow(Registries.ENCHANTMENT).listElements().filter(e -> e.is(EnchantmentTags.CURSE) && e.is(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT)).toList();
            if (!curses.isEmpty()) {
                Holder<Enchantment> curse = curses.get(rand.nextInt(curses.size()));
                enchMap.upgrade(curse, Mth.nextInt(rand, 1, ApothicEnchantingCompat.getMaxLevel(curse.value())));
            }
        }

        EnchantmentHelper.setEnchantments(stack, enchMap.toImmutable());
        stack.set(Components.FROM_BOSS, true);
        return stack;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BasicBossData basicData;
        private EntityType<? extends Mob> entity;
        private AABB size;
        private Map<LootRarity, BossStats> stats = new HashMap<>();

        public Builder basicData(UnaryOperator<BasicBossData.Builder> config) {
            this.basicData = config.apply(BasicBossData.builder()).build();
            return this;
        }

        public Builder entity(EntityType<? extends Mob> entity) {
            this.entity = entity;
            return this;
        }

        public Builder size(double width, double height) {
            this.size = new AABB(0, 0, 0, width, height, width);
            return this;
        }

        public Builder stats(LootRarity rarity, UnaryOperator<BossStats.Builder> config) {
            this.stats.put(rarity, config.apply(BossStats.builder()).build());
            return this;
        }

        public Invader build() {
            if (this.basicData == null) {
                throw new IllegalStateException("BasicBossData must be set");
            }
            if (this.entity == null) {
                throw new IllegalStateException("Entity type must be set");
            }
            if (this.size == null) {
                throw new IllegalStateException("Size must be set");
            }
            if (this.stats.isEmpty()) {
                throw new IllegalStateException("Stats must not be empty");
            }
            return new Invader(this.basicData, this.entity, this.size, this.stats);
        }
    }

}
