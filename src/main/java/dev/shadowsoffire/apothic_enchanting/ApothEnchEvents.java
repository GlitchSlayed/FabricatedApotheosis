package dev.shadowsoffire.apothic_enchanting;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.datafixers.util.Pair;

import dev.shadowsoffire.apothic_enchanting.command.DumpEnchantmentInfoCommand;
import dev.shadowsoffire.apothic_enchanting.enchantments.ChainsawTask;
import dev.shadowsoffire.apothic_enchanting.enchantments.components.BerserkingComponent;
import dev.shadowsoffire.apothic_enchanting.enchantments.components.BoonComponent;
import dev.shadowsoffire.apothic_enchanting.enchantments.components.ReflectiveComponent;
import dev.shadowsoffire.apothic_enchanting.event.AnvilCraftEvent;
import dev.shadowsoffire.apothic_enchanting.event.AnvilUpdateEvent;
import dev.shadowsoffire.apothic_enchanting.objects.EnderLeadItem;
import dev.shadowsoffire.apothic_enchanting.objects.ExtractionTomeItem;
import dev.shadowsoffire.apothic_enchanting.objects.ImprovedScrappingTomeItem;
import dev.shadowsoffire.apothic_enchanting.objects.ScrappingTomeItem;
import dev.shadowsoffire.apothic_enchanting.table.infusion.InfusionRecipeCache;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class ApothEnchEvents {

    public static void init() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            ItemStack s = player.getItemInHand(hand);
            Pair<LevelBasedValue, Integer> bonemealCost = EnchantmentHelper.getHighestLevel(s, Ench.EnchantEffects.BONEMEAL_CROPS);
            if (bonemealCost != null && !player.isShiftKeyDown() && BoneMealItem.growCrop(s.copy(), world, hitResult.getBlockPos())) {
                int cost = (int) bonemealCost.getFirst().calculate(bonemealCost.getSecond());
                if (cost > 0) {
                    s.hurtAndBreak(cost, player, hand);
                }
                if (!world.isClientSide()) {
                    s.causeUseVibration(player, GameEvent.ITEM_INTERACT_FINISH);
                    world.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, hitResult.getBlockPos(), 15);
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof EnderLeadItem lead && entity instanceof LivingEntity living) {
                InteractionResult result = lead.interactLivingEntity(stack, player, living, hand);
                if (result.consumesAction()) {
                    return result;
                }
            }
            return InteractionResult.PASS;
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            ChainsawTask.attemptChainsaw(world, player, pos, state);
            if (world instanceof ServerLevel sl) {
                BoonComponent.provideBenefits(sl, pos, state, player, player.getMainHandItem());
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<CommandSourceStack> apoth = Commands.literal("apoth");
            DumpEnchantmentInfoCommand.register(apoth);
            dispatcher.register(apoth);
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            InfusionRecipeCache.rebuildFromServer(server);
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            InfusionRecipeCache.clear();
        });

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            if (player.level().getServer() != null) {
                InfusionRecipeCache.rebuildFromServer(player.level().getServer());
            }
        });
    }

    public static void onAnvilUpdate(AnvilUpdateEvent e) {
        ItemStack left = e.getLeft();

        if (left.isEnchanted() && e.getRight().getItem() == Ench.Items.PRISMATIC_WEB.value()) {
            ItemStack stack = left.copy();
            ItemEnchantments.Mutable enchants = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(stack));

            enchants.removeIf(h -> h.is(EnchantmentTags.CURSE));
            EnchantmentHelper.setEnchantments(stack, enchants.toImmutable());

            e.setXpCost(30);
            e.setMaterialCost(1);
            e.setOutput(stack);
            return;
        }

        if (left.getCount() == 1 && (left.getItem() == Items.CHIPPED_ANVIL || left.getItem() == Items.DAMAGED_ANVIL) && e.getRight().is(ConventionalItemTags.STORAGE_BLOCKS_IRON)) {
            Item target = left.getItem() == Items.CHIPPED_ANVIL ? Items.DAMAGED_ANVIL : Items.ANVIL;
            ItemStack out = left.transmuteCopy(target);
            e.setOutput(out);
            e.setXpCost(5);
            e.setMaterialCost(1);
            return;
        }

        if (ScrappingTomeItem.updateAnvil(e)) return;
        if (ImprovedScrappingTomeItem.updateAnvil(e)) return;
        if (ExtractionTomeItem.updateAnvil(e)) return;
    }

    public static void onAnvilTake(AnvilCraftEvent.Post e) {
        ExtractionTomeItem.updateRepair(e);
    }

    public static void clampAnvilCombine(AnvilUpdateEvent e) {
        ItemStack output = e.getOutput();
        if (output.isEmpty()) return;

        ItemEnchantments outputEnchants = EnchantmentHelper.getEnchantmentsForCrafting(output);
        if (outputEnchants.isEmpty()) return;

        ItemEnchantments leftEnchants = EnchantmentHelper.getEnchantmentsForCrafting(e.getLeft());
        ItemEnchantments rightEnchants = EnchantmentHelper.getEnchantmentsForCrafting(e.getRight());

        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(outputEnchants);
        boolean modified = false;
        for (Holder<Enchantment> ench : outputEnchants.keySet()) {
            int leftLevel = leftEnchants.getLevel(ench);
            int rightLevel = rightEnchants.getLevel(ench);
            if (leftLevel <= 0 || rightLevel <= 0) continue;

            int cap = ApothicEnchanting.getEnchInfo(ench).getMaxAnvilCombineLevel(ench);
            int outputLevel = outputEnchants.getLevel(ench);
            int capped = Math.max(leftLevel, Math.min(outputLevel, cap));
            if (capped < outputLevel) {
                mutable.set(ench, capped);
                modified = true;
            }
        }

        if (modified) {
            ItemStack clamped = output.copy();
            EnchantmentHelper.setEnchantments(clamped, mutable.toImmutable());
            e.setOutput(clamped);
        }
    }

    public static float handleLifeMending(LivingEntity entity, float amount) {
        if (entity.getType() == EntityTypes.ARMOR_STAND) return amount;
        if (entity.level().isClientSide()) return amount;
        if (amount <= 0F) return amount;

        var opt = EnchantmentHelper.getRandomItemWith(Ench.EnchantEffects.REPAIR_WITH_HP, entity, ItemStack::isDamaged);
        if (opt.isPresent()) {
            var itemInUse = opt.get();
            ItemStack stack = itemInUse.itemStack();
            MutableFloat duraPerHp = new MutableFloat();
            EnchantmentHelper.runIterationOnItem(stack, (ench, level) -> {
                ench.value().modifyItemFilteredCount(Ench.EnchantEffects.REPAIR_WITH_HP, (ServerLevel) entity.level(), level, stack, duraPerHp);
            });
            if (duraPerHp.floatValue() > 0) {
                float cost = 1F / duraPerHp.floatValue();
                int maxRestore = Math.min(Mth.floor(amount / cost), stack.getDamageValue());
                amount -= maxRestore * cost;
                stack.setDamageValue(stack.getDamageValue() - maxRestore);
                if (itemInUse.inSlot() != null && itemInUse.owner() != null) {
                    itemInUse.owner().setItemSlot(itemInUse.inSlot(), stack);
                }
            }
        }
        return amount;
    }

    public static void handleReflectiveDefenses(LivingEntity user, ServerLevel level, DamageSource source, float blockedDamage) {
        Entity attacker = source.getDirectEntity();
        ItemStack shield = user.getUseItem();
        Pair<ReflectiveComponent, Integer> reflect = EnchantmentHelper.getHighestLevel(shield, Ench.EnchantEffects.REFLECTIVE);
        if (reflect != null) {
            float chance = reflect.getFirst().procChance().calculate(reflect.getSecond());
            if (user.level().getRandom().nextFloat() <= chance) {
                DamageSource src = user.level().damageSources().indirectMagic(user, user);
                if (attacker instanceof LivingEntity livingAttacker && !attacker.level().isClientSide()) {
                    float ratio = reflect.getFirst().reflectRatio().calculate(reflect.getSecond());
                    livingAttacker.hurtServer((ServerLevel) livingAttacker.level(), src, ratio * blockedDamage);
                    shield.hurtAndBreak(10, user, user.getUsedItemHand());
                }
            }
        }
    }

    public static float modifyBreakSpeed(Player p, BlockState state, BlockPos pos, float speed) {
        if (!p.onGround()) {
            MutableBoolean flag = new MutableBoolean(false);
            EnchantmentHelper.runIterationOnEquipment(p, (ench, level, item) -> {
                if (ench.value().effects().has(Ench.EnchantEffects.STABLE_FOOTING)) {
                    flag.setTrue();
                }
            });

            if (flag.booleanValue()) {
                speed *= 5F;
            }
        }

        ItemStack stack = p.getMainHandItem();
        if (!stack.isEmpty()) {
            Pair<LevelBasedValue, Integer> fervor = EnchantmentHelper.getHighestLevel(stack, Ench.EnchantEffects.MINERS_FERVOR);
            if (fervor != null && stack.getDestroySpeed(state) > 1.0F) {
                float hardness = state.getDestroySpeed(p.level(), pos);
                if (hardness > 0) {
                    speed = Math.min(29.9999F, fervor.getFirst().calculate(fervor.getSecond())) * hardness;
                }
            }
        }
        return speed;
    }

    public static void handleDropsToXp(LivingEntity entity, DamageSource source, List<ItemEntity> drops) {
        if (!(entity instanceof Player) && source.getEntity() instanceof Player p && !p.level().isClientSide()) {
            ItemStack stack = p.getWeaponItem();
            MutableFloat knowledge = new MutableFloat();
            EnchantmentHelper.runIterationOnItem(stack, (ench, level) -> {
                ench.value().modifyItemFilteredCount(Ench.EnchantEffects.DROPS_TO_XP, (ServerLevel) p.level(), level, stack, knowledge);
            });

            if (knowledge.floatValue() > 0) {
                int totalXp = 0;
                Iterator<ItemEntity> it = drops.iterator();
                while (it.hasNext()) {
                    ItemEntity i = it.next();
                    if (!i.getItem().is(Ench.Tags.CANNOT_BE_CONVERTED_TO_XP)) {
                        totalXp += i.getItem().getCount() * knowledge.floatValue();
                        it.remove();
                    }
                }

                while (totalXp > 0) {
                    int i = ExperienceOrb.getExperienceValue(totalXp);
                    totalXp -= i;
                    p.level().addFreshEntity(new ExperienceOrb(p.level(), entity.getX(), entity.getY(), entity.getZ(), i));
                }
            }
        }
    }
}
