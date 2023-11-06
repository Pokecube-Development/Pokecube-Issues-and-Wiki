package thut.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import thut.api.entity.event.LevelEntityEvent;
import thut.core.common.ThutCore;

@Mixin(Level.class)
public class LevelEntityEventClient
{
    @Inject(method = "broadcastEntityEvent", at = @At(value = "RETURN"))
    public void pokecube$onBroadcastEntityEvent(Entity entity, byte key, CallbackInfo cb)
    {
        ThutCore.FORGE_BUS.post(new LevelEntityEvent(entity, key));
    }
}
