package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Accessor("invulnerableTime")
    int apoth_getInvulnerableTime();

    @Accessor("invulnerableTime")
    void apoth_setInvulnerableTime(int time);
}
