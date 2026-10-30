package net.neoforged.neoforge.event.entity.living;

import java.util.Collection;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;

public class LivingDropsEvent {

    private final LivingEntity entity;
    private final DamageSource source;
    private final Collection<ItemEntity> drops;

    public LivingDropsEvent(LivingEntity entity, DamageSource source, Collection<ItemEntity> drops) {
        this.entity = entity;
        this.source = source;
        this.drops = drops;
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    public DamageSource getSource() {
        return this.source;
    }

    public Collection<ItemEntity> getDrops() {
        return this.drops;
    }
}
