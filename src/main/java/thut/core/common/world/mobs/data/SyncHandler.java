package thut.core.common.world.mobs.data;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.world.mobs.data.DataSync;

public class SyncHandler
{
    @CapabilityInject(DataSync.class)
    public static final Capability<DataSync> CAP = null;

    @SubscribeEvent
    public static void EntityUpdate(final LivingUpdateEvent event)
    {
        if (event.getEntity().getCommandSenderWorld().isClientSide) return;
        final DataSync data = SyncHandler.getData(event.getEntity());
        if (data == null) return;
        PacketDataSync.sync(event.getEntity(), data, event.getEntity().getId(), false);
    }

    public static DataSync getData(final Entity mob)
    {
        return mob.getCapability(SyncHandler.CAP, null).orElse(null);
    }

    @SubscribeEvent
    public static void startTracking(final StartTracking event)
    {
        if (event.getTarget().getCommandSenderWorld().isClientSide) return;
        final DataSync data = SyncHandler.getData(event.getTarget());
        if (data == null) return;
        PacketDataSync.sync((ServerPlayerEntity) event.getEntity(), data, event.getTarget().getId(), true);
    }
}
