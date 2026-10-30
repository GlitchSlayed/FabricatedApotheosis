package dev.shadowsoffire.apotheosis.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public interface LivingEntityInvoker {

    @Invoker("checkTotemDeathProtection")
    boolean callCheckTotemDeathProtection(DamageSource damageSource);

    @Invoker("getDeathSound")
    SoundEvent callGetDeathSound();

    @Invoker("getSoundVolume")
    float callGetSoundVolume();

    @Invoker("onEffectUpdated")
    void callOnEffectUpdated(MobEffectInstance effectInstance, boolean forced, @Nullable Entity entity);
}
