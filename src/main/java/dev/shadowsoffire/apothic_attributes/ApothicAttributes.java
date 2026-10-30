package dev.shadowsoffire.apothic_attributes;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;

public class ApothicAttributes {

    public static final String MODID = "apothic_attributes";
    public static float localAtkStrength = 1.0f;

    public static float getLocalAtkStrength(Entity entity) {
        if (entity instanceof Player p) {
            return p.getAttackStrengthScale(0.5f);
        }
        return localAtkStrength;
    }

    public static TooltipFlag getTooltipFlag() {
        return TooltipFlag.NORMAL;
    }

    public static Identifier loc(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
