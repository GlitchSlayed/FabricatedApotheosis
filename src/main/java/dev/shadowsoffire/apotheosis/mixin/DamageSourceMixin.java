package dev.shadowsoffire.apotheosis.mixin;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.apotheosis.util.DamageSourceExtension;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

@Mixin(DamageSource.class)
public class DamageSourceMixin implements DamageSourceExtension {

    @Unique
    @Nullable
    private Set<TagKey<DamageType>> apoth$extraTypes = null;

    @Inject(at = @At("RETURN"), method = "is(Lnet/minecraft/tags/TagKey;)Z", cancellable = true)
    public void apoth_isSourceInExtraTags(TagKey<DamageType> tag, CallbackInfoReturnable<Boolean> cir) {
        if (this.apoth$extraTypes != null && this.apoth$extraTypes.contains(tag)) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public void addTag(TagKey<DamageType> tag) {
        this.apoth$getOrCreateExtraTypes().add(tag);
    }

    @Override
    public void removeTag(TagKey<DamageType> tag) {
        if (this.apoth$extraTypes != null) {
            this.apoth$extraTypes.remove(tag);
        }
    }

    @Unique
    private Set<TagKey<DamageType>> apoth$getOrCreateExtraTypes() {
        if (this.apoth$extraTypes == null) {
            this.apoth$extraTypes = new HashSet<>();
        }
        return this.apoth$extraTypes;
    }

}
