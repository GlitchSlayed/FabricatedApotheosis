package dev.shadowsoffire.apothic_enchanting.table;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class EnchantmentTableItemHandler extends SimpleContainer {

    public static AttachmentType<EnchantmentTableItemHandler> TYPE;

    public EnchantmentTableItemHandler() {
        super(1);
    }

    public static void bootstrap() {
        if (TYPE == null) {
            try {
                TYPE = AttachmentRegistry.<EnchantmentTableItemHandler>builder()
                    .initializer(EnchantmentTableItemHandler::new)
                    .persistent(ItemStack.OPTIONAL_CODEC.xmap(
                        stack -> {
                            EnchantmentTableItemHandler h = new EnchantmentTableItemHandler();
                            h.setItem(0, stack);
                            return h;
                        },
                        h -> h.getItem(0)
                    ))
                    .buildAndRegister(Identifier.fromNamespaceAndPath("apothic_enchanting", "enchantment_table_item_handler"));
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return index == 0 && (stack.is(Items.LAPIS_LAZULI) || stack.is(ItemTags.LAPIS_ORES));
    }
}
