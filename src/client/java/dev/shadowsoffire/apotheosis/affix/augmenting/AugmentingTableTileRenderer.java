package dev.shadowsoffire.apotheosis.affix.augmenting;

import java.util.ArrayList;
import java.util.List;

import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.shadowsoffire.apotheosis.affix.augmenting.AugmentingTableTile.AnimationStage;
import dev.shadowsoffire.apotheosis.client.AdventureModuleClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class AugmentingTableTileRenderer implements BlockEntityRenderer<AugmentingTableTile, AugmentingTableTileRenderer.State> {

    public AugmentingTableTileRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AugmentingTableTile blockEntity, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.time = blockEntity.time;
        state.partialTicks = partialTicks;
        state.stage = blockEntity.stage;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        // Visual refinement of in-world star cube animation is deferred to Milestone 10.
    }

    public static class State extends BlockEntityRenderState {
        public int time;
        public float partialTicks;
        public AnimationStage stage;
    }

}
