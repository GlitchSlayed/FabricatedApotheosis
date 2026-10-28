package dev.shadowsoffire.apotheosis.client.attributes;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Encapsulates the visual representation and provenance details of an attribute modifier source.
 */
public abstract class ModifierSource<T> {

    protected final ModifierSourceType<T> type;
    protected final T data;

    public ModifierSource(ModifierSourceType<T> type, T data) {
        this.type = type;
        this.data = data;
    }

    public ModifierSourceType<T> getType() {
        return this.type;
    }

    public T getData() {
        return this.data;
    }

    public abstract Component getDisplayName();

    public abstract void render(GuiGraphicsExtractor gfx, Font font, int x, int y);

    public static class ItemModifierSource extends ModifierSource<ItemStack> {

        protected final EquipmentSlot slot;

        public ItemModifierSource(ItemStack data, EquipmentSlot slot) {
            super(ModifierSourceType.EQUIPMENT, data);
            this.slot = slot;
        }

        public EquipmentSlot getSlot() {
            return this.slot;
        }

        @Override
        public Component getDisplayName() {
            return Component.literal("[" + this.slot.getSerializedName() + "] ").append(this.data.getHoverName());
        }

        @Override
        public void render(GuiGraphicsExtractor gfx, Font font, int x, int y) {
            var pose = gfx.pose();
            pose.pushMatrix();
            float scale = 0.5F;
            pose.scale(scale, scale);
            pose.translate(1 + x / scale, 1 + y / scale);
            gfx.fakeItem(this.data, 0, 0);
            pose.popMatrix();
        }
    }

    public static class EffectModifierSource extends ModifierSource<MobEffectInstance> {

        public EffectModifierSource(MobEffectInstance data) {
            super(ModifierSourceType.MOB_EFFECT, data);
        }

        @Override
        public Component getDisplayName() {
            return this.data.getEffect().value().getDisplayName();
        }

        @Override
        public void render(GuiGraphicsExtractor gfx, Font font, int x, int y) {
            Holder<MobEffect> effect = this.data.getEffect();
            Identifier sprite = Hud.getMobEffectSprite(effect);
            var pose = gfx.pose();
            pose.pushMatrix();
            float scale = 0.5F;
            pose.scale(scale, scale);
            pose.translate(x / scale, y / scale);
            gfx.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, 0, 0, 18, 18);
            pose.popMatrix();
        }
    }
}
