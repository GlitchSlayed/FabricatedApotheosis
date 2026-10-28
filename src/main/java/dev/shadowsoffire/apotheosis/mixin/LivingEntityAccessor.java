package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {

    @Accessor("lastHurt")
    float apoth_getLastHurt();

    @Accessor("lastHurt")
    void apoth_setLastHurt(float lastHurt);
}
