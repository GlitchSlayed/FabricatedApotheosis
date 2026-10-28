package dev.shadowsoffire.placebo.registry;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import dev.shadowsoffire.placebo.menu.MenuUtil;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Entry point for registration of game elements with built-in duplicate detection.
 */
public class DeferredHelper {

    protected final String modid;
    protected final Set<ResourceKey<?>> registeredKeys = Collections.synchronizedSet(new HashSet<>());

    protected DeferredHelper(String modid) {
        this.modid = modid;
    }

    public static DeferredHelper create(String modid) {
        return new DeferredHelper(modid);
    }

    public String getModid() {
        return this.modid;
    }

    public Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(this.modid, path);
    }

    protected void checkDuplicate(Identifier id, Registry<?> registry) {
        ResourceKey<?> key = ResourceKey.create(registry.key(), id);
        if (!this.registeredKeys.add(key)) {
            throw new IllegalStateException("Duplicate registration detected for key '" + id + "' in registry '" + registry.key().identifier() + "'!");
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Holder.Reference<T> custom(Registry<?> registry, String name, Supplier<T> supplier) {
        Identifier id = this.id(name);
        this.checkDuplicate(id, registry);
        T value = supplier.get();
        Holder.Reference<T> ref = Registry.registerForHolder((Registry<T>) registry, id, value);
        if (!ref.isBound()) {
            try {
                java.lang.reflect.Method bindValueMethod = Holder.Reference.class.getDeclaredMethod("bindValue", Object.class);
                bindValueMethod.setAccessible(true);
                bindValueMethod.invoke(ref, value);
            } catch (Exception e) {
                throw new RuntimeException("Failed to bind value to holder for " + id, e);
            }
        }
        return ref;
    }

    public Holder.Reference<Attribute> attribute(String name, Supplier<Attribute> supplier) {
        return this.custom(BuiltInRegistries.ATTRIBUTE, name, supplier);
    }

    public Holder.Reference<Item> item(String name, Function<Item.Properties, Item> factory) {
        return this.item(name, factory, UnaryOperator.identity());
    }

    public Holder.Reference<Item> item(String name, Function<Item.Properties, Item> factory, UnaryOperator<Item.Properties> props) {
        Identifier id = this.id(name);
        this.checkDuplicate(id, BuiltInRegistries.ITEM);
        Item.Properties p = new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id));
        if (props != null) p = props.apply(p);
        Item it = factory.apply(p);
        return Registry.registerForHolder(BuiltInRegistries.ITEM, ResourceKey.create(Registries.ITEM, id), it);
    }

    public Holder.Reference<Block> block(String name, Supplier<Block> supplier) {
        Identifier id = this.id(name);
        this.checkDuplicate(id, BuiltInRegistries.BLOCK);
        return Registry.registerForHolder(BuiltInRegistries.BLOCK, ResourceKey.create(Registries.BLOCK, id), supplier.get());
    }

    public Holder.Reference<Block> block(String name, Function<BlockBehaviour.Properties, Block> factory, UnaryOperator<BlockBehaviour.Properties> props) {
        Identifier id = this.id(name);
        this.checkDuplicate(id, BuiltInRegistries.BLOCK);
        BlockBehaviour.Properties p = BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, id));
        if (props != null) p = props.apply(p);
        Block b = factory.apply(p);
        return Registry.registerForHolder(BuiltInRegistries.BLOCK, ResourceKey.create(Registries.BLOCK, id), b);
    }

    public Holder.Reference<Item> blockItem(String name, Holder<Block> block, BiFunction<Block, Item.Properties, Item> factory, UnaryOperator<Item.Properties> props) {
        Identifier id = this.id(name);
        this.checkDuplicate(id, BuiltInRegistries.ITEM);
        Item.Properties p = new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id));
        if (props != null) p = props.apply(p);
        Item it = factory.apply(block.value(), p);
        return Registry.registerForHolder(BuiltInRegistries.ITEM, ResourceKey.create(Registries.ITEM, id), it);
    }

    public Holder.Reference<Item> blockItem(String name, Holder<Block> block, Function<Item.Properties, Item> factory, UnaryOperator<Item.Properties> props) {
        return this.item(name, factory, props);
    }

    public Holder.Reference<Item> blockItem(String name, Holder<Block> block) {
        return this.blockItem(name, block, BlockItem::new, UnaryOperator.identity());
    }

    public Holder.Reference<Item> blockItem(String name, Holder<Block> block, UnaryOperator<Item.Properties> props) {
        return this.blockItem(name, block, BlockItem::new, props);
    }

    public <T extends BlockEntity> Holder.Reference<BlockEntityType<T>> blockEntity(String name, BlockEntityType.BlockEntitySupplier<T> supplier, Supplier<Block[]> validBlocks) {
        return this.custom(BuiltInRegistries.BLOCK_ENTITY_TYPE, name, () -> new BlockEntityType<>(supplier, Set.of(validBlocks.get())));
    }

    public <T extends BlockEntity> BlockEntityType<T> blockEntity(String name, BlockEntityType.BlockEntitySupplier<T> supplier, Holder<Block> validBlock) {
        Identifier id = this.id(name);
        BlockEntityType<T> type = new BlockEntityType<>(supplier, Set.of(validBlock.value()));
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, type);
    }

    public <T extends AbstractContainerMenu> Holder.Reference<MenuType<T>> menu(String name, MenuType.MenuSupplier<T> supplier) {
        return this.custom(BuiltInRegistries.MENU, name, () -> new MenuType<>(supplier, net.minecraft.world.flag.FeatureFlags.VANILLA_SET));
    }

    public <T extends AbstractContainerMenu> MenuType<T> menuWithPos(String name, MenuUtil.PosFactory<T> factory) {
        Identifier id = this.id(name);
        return Registry.register(BuiltInRegistries.MENU, id, MenuUtil.posType(factory));
    }

    public <T extends AbstractContainerMenu, D> MenuType<T> menuWithData(String name, ExtendedMenuType.ExtendedFactory<T, D> factory, StreamCodec<? super RegistryFriendlyByteBuf, D> codec) {
        Identifier id = this.id(name);
        return Registry.register(BuiltInRegistries.MENU, id, MenuUtil.bufType(factory, codec));
    }

    public Holder.Reference<MobEffect> effect(String name, Supplier<MobEffect> supplier) {
        return this.custom(BuiltInRegistries.MOB_EFFECT, name, supplier);
    }

    public Holder.Reference<Potion> potion(String name, Supplier<Potion> supplier) {
        return this.custom(BuiltInRegistries.POTION, name, supplier);
    }

    public <T> Holder.Reference<DataComponentType<T>> dataComponent(String name, Consumer<DataComponentType.Builder<T>> builderConsumer) {
        DataComponentType.Builder<T> builder = DataComponentType.builder();
        builderConsumer.accept(builder);
        return this.custom(BuiltInRegistries.DATA_COMPONENT_TYPE, name, builder::build);
    }

    public <T> DataComponentType<T> component(String name, Consumer<DataComponentType.Builder<T>> builderConsumer) {
        Identifier id = this.id(name);
        DataComponentType.Builder<T> builder = DataComponentType.builder();
        builderConsumer.accept(builder);
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, builder.build());
    }

    public <T> DataComponentType<T> enchantmentEffect(String name, Consumer<DataComponentType.Builder<T>> builderConsumer) {
        Identifier id = this.id(name);
        DataComponentType.Builder<T> builder = DataComponentType.builder();
        builderConsumer.accept(builder);
        return Registry.register(BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, id, builder.build());
    }

    public Holder.Reference<SimpleParticleType> particle(String name) {
        return this.custom(BuiltInRegistries.PARTICLE_TYPE, name, FabricParticleTypes::simple);
    }

    public SimpleParticleType simpleParticle(String name, boolean overrideLimiter) {
        Identifier id = this.id(name);
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, id, FabricParticleTypes.simple(overrideLimiter));
    }

    public Holder.Reference<SoundEvent> sound(String name) {
        Identifier id = this.id(name);
        return this.custom(BuiltInRegistries.SOUND_EVENT, name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public <T extends Recipe<?>> RecipeType<T> recipe(String name) {
        Identifier id = this.id(name);
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, id, new RecipeType<T>() {
            @Override
            public String toString() {
                return id.toString();
            }
        });
    }

    public <T extends Recipe<?>> Holder.Reference<RecipeSerializer<T>> recipeSerializer(String name, Supplier<RecipeSerializer<T>> supplier) {
        return this.custom(BuiltInRegistries.RECIPE_SERIALIZER, name, supplier);
    }

    public Holder.Reference<CreativeModeTab> creativeTab(String name, Consumer<CreativeModeTab.Builder> consumer) {
        CreativeModeTab.Builder builder = FabricCreativeModeTab.builder();
        consumer.accept(builder);
        return this.custom(BuiltInRegistries.CREATIVE_MODE_TAB, name, builder::build);
    }

    public <T extends CriterionTrigger<?>> T criteriaTrigger(String name, T trigger) {
        Identifier id = this.id(name);
        return Registry.register(BuiltInRegistries.TRIGGER_TYPES, id, trigger);
    }
}
