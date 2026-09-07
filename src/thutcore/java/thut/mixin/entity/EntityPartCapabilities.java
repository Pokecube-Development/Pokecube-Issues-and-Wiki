package thut.mixin.entity;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.capabilities.EntityCapability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thut.api.entity.multipart.GenericPartEntity;

@Mixin(Entity.class)
public class EntityPartCapabilities
{
    @Inject(method = "getCapability*", at = @At(value = "HEAD"), cancellable = true)
    public <T, C> void thutcore$getCapabilityContexted(EntityCapability<T, C> capability, C context,
            CallbackInfoReturnable<T> cbr)
    {
        Object us = (Object) this;
        if (us instanceof GenericPartEntity<?> parted)
        {
            cbr.setReturnValue(parted.getCapability(capability, context));
        }
    }

    @Inject(method = "getCapability*", at = @At(value = "HEAD"), cancellable = true)
    public <T> void thutcore$getCapabilityContexted(EntityCapability<T, Void> capability, CallbackInfoReturnable<T> cbr)
    {
        Object us = (Object) this;
        if (us instanceof GenericPartEntity<?> parted)
        {
            cbr.setReturnValue(parted.getCapability(capability));
        }
    }
}
