package dev.shadowsoffire.apotheosis.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apotheosis.gen.ItemFrameGemsProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureEntityInfo;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplateMixin {

    @Shadow
    @Final
    private List<StructureEntityInfo> entityInfoList;

    @Unique
    private static final ThreadLocal<StructurePlaceSettings> apoth$CURRENT_SETTINGS = new ThreadLocal<>();

    @Inject(method = "placeInWorld", at = @At("HEAD"))
    private void apoth$captureSettings(ServerLevelAccessor level, BlockPos pos, BlockPos pivot, StructurePlaceSettings settings, RandomSource random, int flags, CallbackInfoReturnable<Boolean> cir) {
        apoth$CURRENT_SETTINGS.set(settings);
    }

    @Inject(method = "placeInWorld", at = @At("RETURN"))
    private void apoth$releaseSettings(ServerLevelAccessor level, BlockPos pos, BlockPos pivot, StructurePlaceSettings settings, RandomSource random, int flags, CallbackInfoReturnable<Boolean> cir) {
        apoth$CURRENT_SETTINGS.remove();
    }

    @Inject(method = "placeEntities", at = @At("HEAD"))
    private void apoth$processEntities(ServerLevelAccessor level, BlockPos pos, Mirror mirror, Rotation rotation, BlockPos pivot, BoundingBox box, boolean finalizeEntities, ProblemReporter reporter, CallbackInfo ci) {
        StructurePlaceSettings settings = apoth$CURRENT_SETTINGS.get();
        if (settings != null && !settings.isIgnoreEntities()) {
            for (StructureProcessor proc : settings.getProcessors()) {
                if (proc instanceof ItemFrameGemsProcessor ifgp) {
                    for (StructureEntityInfo info : this.entityInfoList) {
                        BlockPos transformedPos = StructureTemplate.transform(info.blockPos, mirror, rotation, pivot).offset(pos);
                        if (box == null || box.isInside(transformedPos)) {
                            ifgp.processEntity(level, transformedPos, info.nbt, settings.getRandom(transformedPos));
                        }
                    }
                }
            }
        }
    }
}
