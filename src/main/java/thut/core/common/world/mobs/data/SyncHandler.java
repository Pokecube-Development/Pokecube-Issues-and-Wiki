package thut.core.common.world.mobs.data;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import thut.api.world.mobs.data.DataSync;

@Mod.EventBusSubscriber
public class SyncHandler
{
    @CapabilityInject(DataSync.class)
    public static final Capability<DataSync> CAP = null;

    @SubscribeEvent
    public static void EntityUpdate(LivingUpdateEvent event)
    {
        if (event.getEntity().getEntityWorld().isRemote) return;
        final DataSync data = SyncHandler.getData(event.getEntity());
        if (data == null) return;
        PacketDataSync.sync(event.getEntity(), data, event.getEntity().getEntityId(), false);
    }

    public static DataSync getData(Entity mob)
    {
        return mob.getCapability(SyncHandler.CAP, null).orElse(null);
    }

    @SubscribeEvent
    public static void startTracking(StartTracking event)
    {
        if (event.getTarget().getEntityWorld().isRemote) return;
        final DataSync data = SyncHandler.getData(event.getTarget());
        if (data == null) return;
        PacketDataSync.sync((ServerPlayerEntity) event.getEntity(), data, event.getTarget().getEntityId(), true);
    }
}
