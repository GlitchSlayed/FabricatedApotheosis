package dev.shadowsoffire.apotheosis.mixin;

import java.util.List;
import net.minecraft.commands.Commands;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import dev.shadowsoffire.placebo.dynreg.DynamicRegistry;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void apoth$captureRegistries(ReloadableServerRegistries.LoadResult loadResult, FeatureFlagSet featureFlagSet, Commands.CommandSelection commandSelection, List<?> postponedTags, PermissionSet permissionSet, List<?> newComponents, CallbackInfo ci) {
        DynamicRegistry.setActiveLookup(loadResult.lookupWithUpdatedTags());
    }
}
