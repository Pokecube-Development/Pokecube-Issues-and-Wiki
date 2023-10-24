package thut.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import thut.api.entity.event.LevelEntityEvent;

@Mixin(ServerLevel.class)
public class LevelEntityEventServer
{
    @Inject(method = "broadcastEntityEvent", at = @At(value = "RETURN"))
    public void pokecube$onBroadcastEntityEvent(Entity entity, byte key, CallbackInfo cb)
    {
        MinecraftForge.EVENT_BUS.post(new LevelEntityEvent(entity, key));
    }
}
