package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.config.ApotheosisConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTraderSpawner;
import net.minecraft.world.level.LevelReader;

@Mixin(WanderingTraderSpawner.class)
public class WandererSpawnerMixin {

    @Shadow
    private RandomSource random;

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 75))
    public int apoth_replaceMaxChance(int old) {
        return ApotheosisConfig.getServer().enableAdventure ? 90 : old;
    }

    @ModifyConstant(method = "spawn", constant = @Constant(intValue = 10))
    public int apoth_replaceRng(int old) {
        return ApotheosisConfig.getServer().enableAdventure ? 4 : old;
    }

    @ModifyConstant(method = "spawn", constant = @Constant(intValue = 48000))
    public int apoth_replaceDespawnDelay(int old) {
        return ApotheosisConfig.getServer().enableAdventure ? 28000 : old;
    }

    @Inject(at = @At("HEAD"), method = "findSpawnPositionNear", cancellable = true)
    private void apoth_findSpawnPositionNear(LevelReader level, BlockPos pos, int radius, CallbackInfoReturnable<BlockPos> cir) {
        if (ApotheosisConfig.getServer().enableAdventure && ApotheosisConfig.getServer().undergroundTrader && AdventureConfig.undergroundTrader) {
            for (int i = 0; i < 10; ++i) {
                int x = pos.getX() + this.random.nextInt(Math.max(1, radius / 2)) - radius / 4;
                int z = pos.getZ() + this.random.nextInt(Math.max(1, radius / 2)) - radius / 4;
                int y = pos.getY() + this.random.nextInt(5);
                MutableBlockPos spawnPos = new MutableBlockPos(x, y, z);
                for (int j = 1; j <= 7; j++) {
                    spawnPos.set(x, y - j, z);
                    if (!level.getBlockState(spawnPos).isAir()) {
                        spawnPos.set(x, y - j + 1, z);
                        break;
                    }
                }
                if (SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(level, spawnPos, EntityTypes.WANDERING_TRADER)) {
                    cir.setReturnValue(spawnPos.immutable());
                    return;
                }
            }
        }
    }
}
