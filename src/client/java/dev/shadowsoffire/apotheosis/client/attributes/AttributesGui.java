package dev.shadowsoffire.apotheosis.client.attributes;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

import dev.shadowsoffire.apotheosis.attributes.api.ALObjects;
import dev.shadowsoffire.apotheosis.attributes.format.AttributeFormatting;
import dev.shadowsoffire.apotheosis.client.mixin.AbstractContainerScreenAccessor;
import dev.shadowsoffire.apotheosis.client.mixin.AbstractRecipeBookScreenAccessor;
import dev.shadowsoffire.apotheosis.config.ApotheosisConfig;
import dev.shadowsoffire.placebo.PlaceboClient;
import dev.shadowsoffire.placebo.util.Offset;
import dev.shadowsoffire.placebo.util.Offset.AnchorPoint;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;

public class AttributesGui implements Renderable, GuiEventListener, NarratableEntry {

    public static final Identifier TEXTURES = Identifier.fromNamespaceAndPath("apothic_attributes", "textures/gui/attributes_gui.png");
    public static final WidgetSprites SWORD_BUTTON_SPRITES = new WidgetSprites(
        Identifier.fromNamespaceAndPath("apothic_attributes", "sword"),
        Identifier.fromNamespaceAndPath("apothic_attributes", "sword_highlighted")
    );

    public static final int ENTRY_HEIGHT = 22;
    public static final int MAX_ENTRIES = 6;
    public static final int WIDTH = 131;
    public static final int HEIGHT = 166;
    private static final int TEX_W = 256;
    private static final int TEX_H = 256;

    public static boolean wasOpen = false;
    protected static float scrollOffset = 0;
    protected static boolean hideUnchanged = false;

    protected final InventoryScreen parent;
    protected final Player player;
    protected final Font font = Minecraft.getInstance().font;
    protected final ImageButton toggleBtn;
    @Nullable
    protected final ImageButton recipeBookButton;
    protected final HideUnchangedButton hideUnchangedBtn;

    protected int leftPos, topPos;
    protected boolean scrolling;
    protected int startIndex;
    protected List<AttributeInstance> data = new ArrayList<>();
    protected boolean open = false;
    protected long lastRenderTick = -1;

    public AttributesGui(InventoryScreen parent) {
        this.parent = parent;
        this.player = Minecraft.getInstance().player;
        this.refreshData();

        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) parent;
        this.leftPos = accessor.apoth_getLeftPos() - WIDTH;
        this.topPos = accessor.apoth_getTopPos();

        this.toggleBtn = new ImageButton(0, 0, 10, 10, SWORD_BUTTON_SPRITES, btn -> {
            this.toggleVisibility();
        }, Component.translatable("apothic_attributes.gui.show_attributes")) {
            @Override
            public void setFocused(boolean pFocused) {}
        };

        if (this.parent.children().size() > 1) {
            GuiEventListener btn = this.parent.children().get(0);
            this.recipeBookButton = btn instanceof ImageButton imgBtn ? imgBtn : null;
        } else {
            this.recipeBookButton = null;
        }

        this.hideUnchangedBtn = new HideUnchangedButton(0, 0);
        this.updateButtonPosition();
    }

    public ImageButton getToggleBtn() {
        return this.toggleBtn;
    }

    public HideUnchangedButton getHideUnchangedBtn() {
        return this.hideUnchangedBtn;
    }

    public boolean isOpen() {
        return this.open;
    }

    private void updateButtonPosition() {
        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) this.parent;
        Offset offset = new Offset(
            AnchorPoint.TOP_RIGHT,
            ApotheosisConfig.getClient().attributesGuiOffsetX,
            ApotheosisConfig.getClient().attributesGuiOffsetY
        );
        ButtonPlacement.positionGuiButton(this.toggleBtn, offset, accessor.apoth_getLeftPos(), accessor.apoth_getTopPos());
    }

    public void refreshData() {
        if (this.player == null) return;
        this.data.clear();
        BuiltInRegistries.ATTRIBUTE.listElements()
            .map(this.player::getAttribute)
            .filter(Objects::nonNull)
            .filter(ai -> {
                Identifier id = ai.getAttribute().unwrapKey().map(k -> k.identifier()).orElse(null);
                return !ApotheosisConfig.getClient().isAttributeHidden(id);
            })
            .filter(ai -> !hideUnchanged || (ai.getBaseValue() != ai.getValue()))
            .forEach(this.data::add);
        this.data.sort(this::compareAttrs);
        this.startIndex = (int) (scrollOffset * this.getOffScreenRows() + 0.5D);
    }

    public void toggleVisibility() {
        this.open = !this.open;
        if (this.open && this.parent instanceof AbstractRecipeBookScreenAccessor rba) {
            RecipeBookComponent<?> rbc = rba.apoth_getRecipeBookComponent();
            if (rbc != null && rbc.isVisible()) {
                rbc.toggleVisibility();
            }
        }
        this.hideUnchangedBtn.visible = this.open;

        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) this.parent;
        int newLeftPos;
        if (this.open && this.parent.width >= 379) {
            newLeftPos = 177 + (this.parent.width - accessor.apoth_getImageWidth() - 200) / 2;
        } else {
            newLeftPos = (this.parent.width - accessor.apoth_getImageWidth()) / 2;
        }

        accessor.apoth_setLeftPos(newLeftPos);
        this.leftPos = accessor.apoth_getLeftPos() - WIDTH;
        this.topPos = accessor.apoth_getTopPos();

        if (this.recipeBookButton != null) {
            this.recipeBookButton.setX(accessor.apoth_getLeftPos() + 104);
            this.recipeBookButton.setY(this.parent.height / 2 - 22);
        }
        this.hideUnchangedBtn.setX(this.leftPos + 7);
        this.hideUnchangedBtn.setY(this.topPos + 151);
    }

    protected int compareAttrs(AttributeInstance a1, AttributeInstance a2) {
        String name = I18n.get(a1.getAttribute().value().getDescriptionId());
        String name2 = I18n.get(a2.getAttribute().value().getDescriptionId());
        return name.compareTo(name2);
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        if (!this.open) return false;
        if (this.hideUnchangedBtn.isMouseOver(pMouseX, pMouseY)) {
            return false;
        }
        return this.isHovering(0, 0, WIDTH, HEIGHT, pMouseX, pMouseY);
    }

    public List<Rect2i> getExclusionAreas() {
        List<Rect2i> areas = new ArrayList<>();
        areas.add(new Rect2i(this.toggleBtn.getX(), this.toggleBtn.getY(), this.toggleBtn.getWidth(), this.toggleBtn.getHeight()));
        if (this.open) {
            areas.add(new Rect2i(this.leftPos, this.topPos, WIDTH, HEIGHT));
        }
        return areas;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float partialTicks) {
        this.updateButtonPosition();
        if (this.parent instanceof AbstractRecipeBookScreenAccessor rba) {
            RecipeBookComponent<?> rbc = rba.apoth_getRecipeBookComponent();
            if (rbc != null && rbc.isVisible()) this.open = false;
        }
        wasOpen = this.open;
        if (!this.open) return;

        if (this.lastRenderTick != PlaceboClient.ticks) {
            this.lastRenderTick = PlaceboClient.ticks;
            this.refreshData();
        }

        int left = this.leftPos;
        int top = this.topPos;
        gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, left, top, 0, 0, WIDTH, HEIGHT, TEX_W, TEX_H);
        int scrollbarPos = (int) (117 * scrollOffset);
        gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, left + 111, top + 16 + scrollbarPos, 244, this.isScrollBarActive() ? 0 : 15, 12, 15, TEX_W, TEX_H);

        int idx = this.startIndex;
        while (idx < this.startIndex + MAX_ENTRIES && idx < this.data.size()) {
            this.renderEntry(gfx, this.data.get(idx), this.leftPos + 8, this.topPos + 16 + ENTRY_HEIGHT * (idx - this.startIndex), mouseX, mouseY);
            idx++;
        }
        this.renderTooltip(gfx, mouseX, mouseY);
        gfx.text(font, Component.translatable("apothic_attributes.gui.attributes"), this.leftPos + 8, this.topPos + 5, 0xFF404040, false);
        gfx.text(font, Component.translatable("text.apothic_attributes.hide_unchanged"), this.leftPos + 20, this.topPos + 152, 0xFF404040, false);
    }

    protected void renderTooltip(GuiGraphicsExtractor gfx, int mouseX, int mouseY) {
        AttributeInstance inst = this.getHoveredSlot(mouseX, mouseY);
        if (inst != null) {
            Holder<Attribute> holder = inst.getAttribute();
            Attribute attr = holder.value();
            boolean isDynamic = holder.is(ALObjects.Tags.DYNAMIC_BASE_ATTRIBUTES);

            List<Component> list = new ArrayList<>();
            MutableComponent name = Component.translatable(attr.getDescriptionId()).withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withUnderlined(true));

            if (isDynamic) {
                name.append(CommonComponents.SPACE);
                name.append(Component.translatable("apothic_attributes.gui.dynamic").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withUnderlined(false)));
            }

            TooltipFlag flag = Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
            if (flag.isAdvanced()) {
                Style style = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withUnderlined(false);
                name.append(Component.literal(" [" + BuiltInRegistries.ATTRIBUTE.getKey(attr) + "]").withStyle(style));
            }

            list.add(name);

            String key = attr.getDescriptionId() + ".desc";
            if (Language.getInstance().has(key)) {
                list.add(Component.translatable(key).withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
            } else if (flag.isAdvanced()) {
                list.add(Component.literal(key).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }

            int color = AttributeFormatting.getValueColor(inst, 0xAAAAAA);

            Component valueComp = AttributeFormatting.toValueComponent(holder, null, inst.getValue(), flag).withColor(color);
            Component baseComp = AttributeFormatting.toValueComponent(holder, null, inst.getBaseValue(), flag).withStyle(ChatFormatting.GRAY);

            if (!isDynamic) {
                list.add(CommonComponents.EMPTY);
                list.add(Component.translatable("apothic_attributes.gui.current", valueComp).withStyle(ChatFormatting.GRAY));

                Component base = Component.translatable("apothic_attributes.gui.base", baseComp).withStyle(ChatFormatting.GRAY);

                if (attr instanceof RangedAttribute ra) {
                    Component min = AttributeFormatting.toValueComponent(holder, null, ra.getMinValue(), flag);
                    min = Component.translatable("apothic_attributes.gui.min", min);
                    Component max = AttributeFormatting.toValueComponent(holder, null, ra.getMaxValue(), flag);
                    max = Component.translatable("apothic_attributes.gui.max", max);
                    list.add(Component.translatable("%s \u2507 %s \u2507 %s", base, min, max).withStyle(ChatFormatting.GRAY));
                } else {
                    list.add(base);
                }
            }

            List<ClientTooltipComponent> finalTooltip = new ArrayList<>(list.size());
            for (Component txt : list) {
                this.addComp(txt, finalTooltip);
            }

            if (inst.getModifiers().stream().anyMatch(modif -> modif.amount() != 0)) {
                this.addComp(CommonComponents.EMPTY, finalTooltip);
                this.addComp(Component.translatable("apothic_attributes.gui.modifiers").withStyle(ChatFormatting.GOLD), finalTooltip);

                Map<Identifier, ModifierSource<?>> modifiersToSources = new HashMap<>();
                for (ModifierSourceType<?> type : ModifierSourceType.getTypes()) {
                    type.extract(this.player, (modif, source) -> modifiersToSources.put(modif.id(), source));
                }

                MutableComponent[] opValues = new MutableComponent[3];
                double[] numericValues = new double[3];

                for (Operation op : Operation.values()) {
                    double baseValue = op == Operation.ADD_MULTIPLIED_TOTAL ? 1 : 0;
                    List<AttributeModifier> modifiers = inst.getModifiers().stream()
                        .filter(m -> m.operation() == op)
                        .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
                    double opValue = modifiers.stream().mapToDouble(AttributeModifier::amount).reduce(baseValue, (res, elem) -> op == Operation.ADD_MULTIPLIED_TOTAL ? res * (1 + elem) : res + elem);

                    modifiers.sort(ModifierSourceType.compareBySource(modifiersToSources));
                    for (AttributeModifier modif : modifiers) {
                        if (modif.amount() != 0) {
                            Component comp = AttributeFormatting.toComponent(holder, modif, flag);
                            var src = modifiersToSources.get(modif.id());
                            finalTooltip.add(new AttributeModifierComponent(src, comp, this.font, this.leftPos - 16));
                        }
                    }

                    color = AttributeFormatting.getValueColor(attr, opValue, baseValue, 0xAAAAAA);
                    Component valueComp2 = AttributeFormatting.toValueComponent(holder, op, opValue, flag).withStyle(Style.EMPTY.withColor(color));
                    MutableComponent comp = Component.translatable("apothic_attributes.gui." + op.name().toLowerCase(Locale.ROOT), valueComp2).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);

                    opValues[op.ordinal()] = comp;
                    numericValues[op.ordinal()] = opValue;
                }

                this.addComp(CommonComponents.EMPTY, finalTooltip);
                this.addComp(Component.translatable("apothic_attributes.gui.formula").withStyle(ChatFormatting.GOLD), finalTooltip);

                Component base = isDynamic ? Component.translatable("apothic_attributes.gui.formula.base") : baseComp;
                Component value = isDynamic ? Component.translatable("apothic_attributes.gui.formula.value") : valueComp;

                Component formula = AttributeFormatting.buildFormula(base, value, numericValues, attr);
                this.addComp(formula, finalTooltip);
            } else if (isDynamic) {
                this.addComp(CommonComponents.EMPTY, finalTooltip);
                this.addComp(Component.translatable("apothic_attributes.gui.no_modifiers").withStyle(ChatFormatting.GOLD), finalTooltip);
            }

            int maxWidth = finalTooltip.stream().map(c -> c.getWidth(this.font)).max(Integer::compare).orElse(0);
            gfx.tooltip(font, finalTooltip, this.leftPos - 16 - maxWidth, mouseY, DefaultTooltipPositioner.INSTANCE, null, false);
        }
    }

    private void addComp(Component comp, List<ClientTooltipComponent> finalTooltip) {
        if (comp == CommonComponents.EMPTY) {
            finalTooltip.add(ClientTooltipComponent.create(comp.getVisualOrderText()));
        } else {
            for (FormattedText fTxt : this.font.getSplitter().splitLines(comp, this.leftPos - 16, comp.getStyle())) {
                finalTooltip.add(ClientTooltipComponent.create(Language.getInstance().getVisualOrder(fTxt)));
            }
        }
    }

    private void renderEntry(GuiGraphicsExtractor gfx, AttributeInstance inst, int x, int y, int mouseX, int mouseY) {
        boolean hover = this.getHoveredSlot(mouseX, mouseY) == inst;
        gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, x, y, 142, hover ? ENTRY_HEIGHT : 0, 100, ENTRY_HEIGHT, TEX_W, TEX_H);

        Component txt = Component.translatable(inst.getAttribute().value().getDescriptionId());
        int splitWidth = 60;
        List<FormattedCharSequence> lines = this.font.split(txt, splitWidth);
        while (lines.size() > 2) {
            splitWidth += 10;
            lines = this.font.split(txt, splitWidth);
        }

        Matrix3x2fStack pose = gfx.pose();
        pose.pushMatrix();
        float scale = 1;
        int maxWidth = lines.stream().map(this.font::width).max(Integer::compareTo).orElse(0);
        if (maxWidth > 66) {
            scale = 66F / maxWidth;
            pose.scale(scale, scale);
        }

        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            float width = this.font.width(line) * scale;
            float lineX = (x + 1 + (68 - width) / 2) / scale;
            float lineY = (y + (lines.size() == 1 ? 7 : 2) + i * 10) / scale;
            gfx.text(font, line, (int) lineX, (int) lineY, 0xFF404040, false);
        }
        pose.popMatrix();

        pose.pushMatrix();
        MutableComponent value = AttributeFormatting.toValueComponent(inst.getAttribute(), null, inst.getValue(), TooltipFlag.Default.NORMAL);

        if (inst.getAttribute().is(ALObjects.Tags.DYNAMIC_BASE_ATTRIBUTES)) {
            value = Component.literal("\uFFFD");
        }

        scale = 1;
        if (this.font.width(value) > 27) {
            scale = 27F / this.font.width(value);
            pose.scale(scale, scale);
        }

        int color = AttributeFormatting.getValueColor(inst, 0xFFFFFF) | 0xFF000000;
        gfx.text(font, value, (int) ((x + 72 + (27 - this.font.width(value) * scale) / 2) / scale), (int) ((y + 7) / scale), color, true);
        pose.popMatrix();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double pMouseX = event.x();
        double pMouseY = event.y();
        if (!this.open || !this.isScrollBarActive()) return false;
        this.scrolling = false;
        int left = this.leftPos + 111;
        int top = this.topPos + 15;
        if (pMouseX >= left && pMouseX < left + 12 && pMouseY >= top && pMouseY < top + 155) {
            this.scrolling = true;
            int i = this.topPos + 15;
            int j = i + 138;
            scrollOffset = ((float) pMouseY - i - 7.5F) / (j - i - 15.0F);
            scrollOffset = Mth.clamp(scrollOffset, 0.0F, 1.0F);
            this.startIndex = (int) (scrollOffset * this.getOffScreenRows() + 0.5D);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double pDragX, double pDragY) {
        double pMouseY = event.y();
        if (!this.open) return false;
        if (this.scrolling && this.isScrollBarActive()) {
            int i = this.topPos + 15;
            int j = i + 138;
            scrollOffset = ((float) pMouseY - i - 7.5F) / (j - i - 15.0F);
            scrollOffset = Mth.clamp(scrollOffset, 0.0F, 1.0F);
            this.startIndex = (int) (scrollOffset * this.getOffScreenRows() + 0.5D);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        if (!this.open) return false;
        if (this.isScrollBarActive()) {
            int i = this.getOffScreenRows();
            scrollOffset = (float) (scrollOffset - pScrollY / i);
            scrollOffset = Mth.clamp(scrollOffset, 0.0F, 1.0F);
            this.startIndex = (int) (scrollOffset * i + 0.5D);
            return true;
        }
        return false;
    }

    private boolean isScrollBarActive() {
        return this.data.size() > MAX_ENTRIES;
    }

    protected int getOffScreenRows() {
        return Math.max(0, this.data.size() - MAX_ENTRIES);
    }

    @Nullable
    public AttributeInstance getHoveredSlot(int mouseX, int mouseY) {
        for (int i = 0; i < MAX_ENTRIES; i++) {
            if (this.startIndex + i < this.data.size()) {
                if (this.isHovering(8, 14 + ENTRY_HEIGHT * i, 100, ENTRY_HEIGHT, mouseX, mouseY)) {
                    return this.data.get(this.startIndex + i);
                }
            }
        }
        return null;
    }

    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        pMouseX -= i;
        pMouseY -= j;
        return pMouseX >= pX - 1 && pMouseX < pX + pWidth + 1 && pMouseY >= pY - 1 && pMouseY < pY + pHeight + 1;
    }

    public class HideUnchangedButton extends AbstractButton {

        public HideUnchangedButton(int pX, int pY) {
            super(pX, pY, 10, 10, Component.translatable("button.apothic_attributes.hide_unchanged"));
            this.visible = false;
        }

        @Override
        public void onPress(InputWithModifiers input) {
            hideUnchanged = !hideUnchanged;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor gfx, int pMouseX, int pMouseY, float pPartialTick) {
            int u = 131, v = 20;
            int vOffset = hideUnchanged ? 0 : 10;
            if (this.isHovered()) {
                vOffset += 20;
            }
            gfx.blit(RenderPipelines.GUI_TEXTURED, TEXTURES, this.getX(), this.getY(), u, v + vOffset, 10, 10, TEX_W, TEX_H);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
            this.defaultButtonNarrationText(pNarrationElementOutput);
        }
    }

    @Override
    public void setFocused(boolean pFocused) {}

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return NarratableEntry.NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {}
}
