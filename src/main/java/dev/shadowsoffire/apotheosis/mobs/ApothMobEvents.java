package dev.shadowsoffire.apotheosis.mobs;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.attributes.api.AttributesUtil;
import dev.shadowsoffire.apotheosis.Apoth.Attachments;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.mobs.registries.AugmentRegistry;
import dev.shadowsoffire.apotheosis.mobs.registries.EliteRegistry;
import dev.shadowsoffire.apotheosis.mobs.registries.InvaderRegistry;
import dev.shadowsoffire.apotheosis.mobs.types.Augmentation;
import dev.shadowsoffire.apotheosis.mobs.types.Elite;
import dev.shadowsoffire.apotheosis.mobs.types.Invader;
import dev.shadowsoffire.apotheosis.mobs.util.SurfaceType;
import dev.shadowsoffire.apotheosis.net.BossSpawnPayload;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment.Target;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugmentRegistry;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import dev.shadowsoffire.placebo.network.PayloadHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;

/**
 * Apotheosis mob processing events.
 */
public class ApothMobEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger("Apotheosis/Mobs");

    public static final String APOTH_MINIBOSS = "apoth.miniboss";
    public static final String APOTH_MINIBOSS_PLAYER = APOTH_MINIBOSS + ".player";

    /**
     * Intercepts mob spawn finalization. Returns true if the mob should proceed with vanilla finalizeSpawn;
     * false if the mob spawn was consumed/cancelled (e.g. replaced by an Invader).
     */
    public static boolean onFinalizeSpawn(Mob mob, ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, SpawnGroupData spawnData) {
        Player player = level.getNearestPlayer(mob.getX(), mob.getY(), mob.getZ(), -1, false);
        if (player == null) {
            return true;
        }

        RandomSource rand = level.getRandom();
        GenContext ctx = GenContext.forPlayerAtPos(rand, player, mob.blockPosition());

        if (trySpawnInvader(mob, level, reason, ctx, player)) {
            mob.discard();
            return false;
        }

        tryAugmentations(level, mob, reason, ctx);

        if (trySpawnElite(mob, level, reason, ctx, player)) {
            return false;
        }

        return true;
    }

    private static boolean trySpawnInvader(Mob mob, ServerLevelAccessor sLevel, EntitySpawnReason reason, GenContext ctx, Player player) {
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION || !(mob instanceof Monster)) {
            return false;
        }

        long gameTime = sLevel.getLevel().getGameTime();
        Long cooldownEnd = dev.shadowsoffire.apotheosis.util.ModAttachments.getData(player, Attachments.INVADER_COOLDOWN);
        if (cooldownEnd != null && cooldownEnd > gameTime) {
            return false;
        }

        ResourceKey<DimensionType> dimId = sLevel.getLevel().dimensionTypeRegistration().unwrapKey().orElse(null);
        InvaderSpawnRules rules = InvaderSpawnRules.getRules(dimId);
        if (rules == null) {
            return false;
        }

        Float chance = rules.spawnChances().get(ctx.tier());
        if (chance == null || ctx.rand().nextFloat() > chance) {
            return false;
        }

        SurfaceType surface = rules.surfaceType();
        BlockPos pos = mob.blockPosition();

        if (surface.test(sLevel, pos)) {
            Invader item = InvaderRegistry.INSTANCE.getRandomItem(ctx);
            if (item == null) {
                LOGGER.error("Attempted to spawn an Invader in dimension {} but no bosses were available.", dimId);
                return false;
            }

            if (!item.basicData().canSpawn(mob, sLevel, reason)) {
                return false;
            }

            Mob boss = item.createBoss(sLevel, pos, ctx);
            if (rules.autoAggro().orElse(AdventureConfig.bossAutoAggro) && !player.isCreative()) {
                boss.setTarget(player);
            }

            if (canSpawn(sLevel, boss, player.distanceToSqr(boss))) {
                sLevel.addFreshEntityWithPassengers(boss);
                sendInvaderSpawnNotification(sLevel.getLevel(), boss);

                long end = gameTime + rules.cooldown().orElse(AdventureConfig.bossSpawnCooldown);
                applyClusteredCooldown(sLevel.getLevel(), player, ctx.tier(), boss, end);
                return true;
            }
        }

        return false;
    }

    public static void sendInvaderSpawnNotification(ServerLevel sLevel, Mob invader) {
        Component name = getName(invader);
        DynamicHolder<LootRarity> rarity = getRarity(invader);

        if (name != null && rarity.isBound()) {
            sLevel.players().forEach(p -> {
                if (isWithinAnnounceRange(p, invader)) {
                    p.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("info.apotheosis.boss_spawn", name, (int) invader.getX(), (int) invader.getY())));
                    PayloadHelper.sendToPlayer(p, new BossSpawnPayload(invader.blockPosition(), rarity));
                }
            });
        }
    }

    private static void applyClusteredCooldown(ServerLevel level, Player trigger, WorldTier tier, Mob boss, long end) {
        applyCooldown(trigger, end);
        for (ServerPlayer p : level.players()) {
            if (p != trigger && WorldTier.getTier(p) == tier && isWithinAnnounceRange(p, boss)) {
                applyCooldown(p, end);
            }
        }
    }

    private static void applyCooldown(Player player, long end) {
        Long current = dev.shadowsoffire.apotheosis.util.ModAttachments.getData(player, Attachments.INVADER_COOLDOWN);
        if (current == null || end > current) {
            dev.shadowsoffire.apotheosis.util.ModAttachments.setData(player, Attachments.INVADER_COOLDOWN, end);
        }
    }

    private static boolean isWithinAnnounceRange(Player player, Entity target) {
        Vec3 tPos = new Vec3(target.getX(), player.getY(), target.getZ());
        return player.distanceToSqr(tPos) <= AdventureConfig.bossAnnounceRange * AdventureConfig.bossAnnounceRange;
    }

    public static void tryAugmentations(ServerLevelAccessor level, Mob mob, EntitySpawnReason type, GenContext ctx) {
        float healthPct = mob.getHealth() / mob.getMaxHealth();

        for (TierAugment aug : TierAugmentRegistry.getAugments(ctx.tier(), Target.MONSTERS)) {
            aug.apply(level, mob);
        }
        dev.shadowsoffire.apotheosis.util.ModAttachments.setData(mob, Attachments.TIER_AUGMENTS_APPLIED, true);

        for (Augmentation aug : AugmentRegistry.getAll()) {
            if (aug.canApply(level, mob, type, ctx)) {
                if (ctx.rand().nextFloat() <= aug.chance()) {
                    aug.apply(mob, ctx);
                }
            }
        }

        mob.setHealth(healthPct * mob.getMaxHealth());
    }

    private static boolean trySpawnElite(Mob mob, ServerLevelAccessor sLevel, EntitySpawnReason reason, GenContext ctx, Player player) {
        Elite item = EliteRegistry.INSTANCE.getRandomItem(ctx, mob);
        if (item == null) {
            return false;
        }

        if (!item.basicData().canSpawn(mob, sLevel, reason)) {
            return false;
        }

        if (ctx.rand().nextFloat() <= item.getChance()) {
            AttributesUtil.getPersistentData(mob).putString(Elite.MINIBOSS_KEY, EliteRegistry.INSTANCE.getKey(item).toString());
            AttributesUtil.getPersistentData(mob).putString(Elite.PLAYER_KEY, player.getUUID().toString());
            return !item.basicData().finalizeSpawn();
        }

        return false;
    }

    public static void delayedEliteMobs(Mob mob, ServerLevel level) {
        CompoundTag data = AttributesUtil.getPersistentData(mob);
        if (data.contains(Elite.MINIBOSS_KEY) && data.contains(Elite.PLAYER_KEY)) {
            String key = data.getString(Elite.MINIBOSS_KEY).orElse("");
            try {
                UUID playerId = UUID.fromString(data.getString(Elite.PLAYER_KEY).orElseThrow());
                Player player = level.getPlayerByUUID(playerId);
                if (player == null) {
                    player = level.getNearestPlayer(mob, -1);
                }

                if (player != null) {
                    GenContext ctx = GenContext.forPlayerAtPos(level.getRandom(), player, mob.blockPosition());
                    Elite item = EliteRegistry.INSTANCE.getValue(Identifier.tryParse(key));
                    if (item != null) {
                        item.transformMiniboss(level, mob, ctx);
                    }
                }
            }
            catch (Exception ex) {
                LOGGER.error("Failure while initializing the Apothic Elite " + key, ex);
            }
        }
    }

    private static boolean canSpawn(LevelAccessor world, Mob entity, double playerDist) {
        if (playerDist > entity.getType().getCategory().getDespawnDistance() * entity.getType().getCategory().getDespawnDistance() && entity.removeWhenFarAway(playerDist)) {
            return false;
        }
        return entity.checkSpawnRules(world, EntitySpawnReason.NATURAL) && entity.checkSpawnObstruction(world);
    }

    @Nullable
    private static Component getName(Mob boss) {
        return boss.getSelfAndPassengers().filter(e -> AttributesUtil.getPersistentData(e).contains(Invader.BOSS_KEY)).findFirst().map(Entity::getCustomName).orElse(null);
    }

    @Nullable
    private static DynamicHolder<LootRarity> getRarity(Mob boss) {
        return boss.getSelfAndPassengers()
            .filter(e -> AttributesUtil.getPersistentData(e).contains(Invader.BOSS_KEY))
            .findFirst()
            .map(ApothMobEvents::getRarityHolder)
            .orElse(RarityRegistry.INSTANCE.emptyHolder());
    }

    private static DynamicHolder<LootRarity> getRarityHolder(Entity entity) {
        Identifier id = Identifier.tryParse(AttributesUtil.getPersistentData(entity).getString(Invader.RARITY_KEY).orElse(""));
        return RarityRegistry.INSTANCE.holder(id);
    }

}
