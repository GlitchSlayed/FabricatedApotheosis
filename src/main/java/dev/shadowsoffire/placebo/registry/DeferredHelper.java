package dev.shadowsoffire.placebo.registry;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Entry point for registration of game elements with built-in duplicate detection.
 */
public class DeferredHelper {

    protected final String modid;
    protected final Set<Identifier> registeredKeys = Collections.synchronizedSet(new HashSet<>());

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
        if (!this.registeredKeys.add(id)) {
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
        return this.custom(BuiltInRegistries.ITEM, name, () -> factory.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, this.id(name)))));
    }

    public Holder.Reference<Block> block(String name, Supplier<Block> supplier) {
        Identifier id = this.id(name);
        this.checkDuplicate(id, BuiltInRegistries.BLOCK);
        return Registry.registerForHolder(BuiltInRegistries.BLOCK, ResourceKey.create(Registries.BLOCK, id), supplier.get());
    }

    public <T extends BlockEntity> Holder.Reference<BlockEntityType<T>> blockEntity(String name, BlockEntityType.BlockEntitySupplier<T> supplier, Supplier<Block[]> validBlocks) {
        return this.custom(BuiltInRegistries.BLOCK_ENTITY_TYPE, name, () -> new BlockEntityType<>(supplier, Set.of(validBlocks.get())));
    }

    public <T extends AbstractContainerMenu> Holder.Reference<MenuType<T>> menu(String name, MenuType.MenuSupplier<T> supplier) {
        return this.custom(BuiltInRegistries.MENU, name, () -> new MenuType<>(supplier, net.minecraft.world.flag.FeatureFlags.VANILLA_SET));
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

    public Holder.Reference<SimpleParticleType> particle(String name) {
        return this.custom(BuiltInRegistries.PARTICLE_TYPE, name, FabricParticleTypes::simple);
    }

    public Holder.Reference<SoundEvent> sound(String name) {
        Identifier id = this.id(name);
        return this.custom(BuiltInRegistries.SOUND_EVENT, name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
