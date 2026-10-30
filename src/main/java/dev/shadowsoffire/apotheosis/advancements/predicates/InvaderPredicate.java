package dev.shadowsoffire.apotheosis.advancements.predicates;

import com.mojang.serialization.MapCodec;

import dev.shadowsoffire.apotheosis.attributes.api.AttributesUtil;
import net.minecraft.advancements.predicates.entity.EntitySubPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Checks that the target entity was spawned by an Invader.
 */
public class InvaderPredicate implements EntitySubPredicate {

    public static final InvaderPredicate INSTANCE = new InvaderPredicate();

    public static final MapCodec<InvaderPredicate> CODEC = MapCodec.unit(INSTANCE);

    public static final String BOSS_KEY = "apoth.boss";

    @Override
    public boolean matches(Entity entity, ServerLevel level, Vec3 position) {
        return AttributesUtil.getPersistentData(entity).contains(BOSS_KEY);
    }

}
