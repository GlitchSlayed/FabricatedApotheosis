package dev.shadowsoffire.apotheosis.mixin;

import java.util.ArrayDeque;
import java.util.Deque;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apotheosis.gen.ItemFrameGemsProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplateMixin {

    @Unique
    private static final ThreadLocal<Deque<StructurePlaceSettings>> apoth$CURRENT_SETTINGS = ThreadLocal.withInitial(ArrayDeque::new);

    @Unique
    private static final ThreadLocal<Deque<ServerLevelAccessor>> apoth$CURRENT_LEVEL = ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method = "placeInWorld", at = @At("HEAD"))
    private void apoth$captureSettings(ServerLevelAccessor level, BlockPos pos, BlockPos pivot, StructurePlaceSettings settings, RandomSource random, int flags, CallbackInfoReturnable<Boolean> cir) {
        apoth$CURRENT_SETTINGS.get().push(settings);
        apoth$CURRENT_LEVEL.get().push(level);
    }

    @Inject(method = "placeInWorld", at = @At("RETURN"))
    private void apoth$releaseSettings(ServerLevelAccessor level, BlockPos pos, BlockPos pivot, StructurePlaceSettings settings, RandomSource random, int flags, CallbackInfoReturnable<Boolean> cir) {
        Deque<StructurePlaceSettings> sStack = apoth$CURRENT_SETTINGS.get();
        if (!sStack.isEmpty()) {
            sStack.pop();
        }
        Deque<ServerLevelAccessor> lStack = apoth$CURRENT_LEVEL.get();
        if (!lStack.isEmpty()) {
            lStack.pop();
        }
    }

    @ModifyArg(
        method = "placeEntities",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;createEntityIgnoreException(Lnet/minecraft/util/ProblemReporter;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/nbt/CompoundTag;)Ljava/util/Optional;"
        ),
        index = 2
    )
    private CompoundTag apoth$processEntityNbt(CompoundTag nbt) {
        StructurePlaceSettings settings = apoth$CURRENT_SETTINGS.get().peek();
        if (settings != null && !settings.isIgnoreEntities()) {
            for (StructureProcessor proc : settings.getProcessors()) {
                if (proc instanceof ItemFrameGemsProcessor ifgp) {
                    BlockPos pos = nbt.read("block_pos", BlockPos.CODEC).orElseGet(() -> {
                        ListTag posList = nbt.getListOrEmpty("Pos");
                        if (posList.size() >= 3) {
                            return BlockPos.containing(posList.getDoubleOr(0, 0), posList.getDoubleOr(1, 0), posList.getDoubleOr(2, 0));
                        }
                        return null;
                    });
                    if (pos != null) {
                        ServerLevelAccessor level = apoth$CURRENT_LEVEL.get().peek();
                        if (level != null) {
                            ifgp.processEntity(level, pos, nbt, settings.getRandom(pos));
                        }
                    }
                }
            }
        }
        return nbt;
    }
}
