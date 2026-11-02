package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;

@Mixin(Entity.class)
public interface EntityInvoker {

    @Invoker("readAdditionalSaveData")
    void callReadAdditionalSaveData(ValueInput input);
}
