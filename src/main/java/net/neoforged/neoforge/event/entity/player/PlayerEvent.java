package net.neoforged.neoforge.event.entity.player;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class PlayerEvent {

    public static class BreakSpeed {
        private final Player entity;
        private final BlockState state;
        private final Optional<BlockPos> position;
        private final float originalSpeed;
        private float newSpeed;

        public BreakSpeed(Player entity, BlockState state, float originalSpeed, Optional<BlockPos> position) {
            this.entity = entity;
            this.state = state;
            this.originalSpeed = originalSpeed;
            this.newSpeed = originalSpeed;
            this.position = position;
        }

        public Player getEntity() {
            return this.entity;
        }

        public BlockState getState() {
            return this.state;
        }

        public Optional<BlockPos> getPosition() {
            return this.position;
        }

        public float getOriginalSpeed() {
            return this.originalSpeed;
        }

        public float getNewSpeed() {
            return this.newSpeed;
        }

        public void setNewSpeed(float newSpeed) {
            this.newSpeed = newSpeed;
        }
    }

    public static class HarvestCheck {
        private final Player entity;
        private final BlockState targetBlock;
        private boolean canHarvest;

        public HarvestCheck(Player entity, BlockState targetBlock, boolean canHarvest) {
            this.entity = entity;
            this.targetBlock = targetBlock;
            this.canHarvest = canHarvest;
        }

        public Player getEntity() {
            return this.entity;
        }

        public BlockState getTargetBlock() {
            return this.targetBlock;
        }

        public boolean canHarvest() {
            return this.canHarvest;
        }

        public void setCanHarvest(boolean canHarvest) {
            this.canHarvest = canHarvest;
        }
    }
}
