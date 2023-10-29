package pokecube.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.LivingEntity;
import pokecube.core.eventhandlers.EventsHandler;

@Mixin(LivingEntity.class)
public class EntityDeathMixin
{
    @Inject(method = "tickDeath", at = @At(value = "HEAD"))
    public void pokecube$preTickDeath(CallbackInfo cb)
    {
        LivingEntity e = (LivingEntity) ((Object) this);
        EventsHandler.preTickLivingDeath(e);
    }

    @Inject(method = "tickDeath", at = @At(value = "RETURN"))
    public void pokecube$postTickDeath(CallbackInfo cb)
    {
        LivingEntity e = (LivingEntity) ((Object) this);
        EventsHandler.postTickLivingDeath(e);
    }
}
