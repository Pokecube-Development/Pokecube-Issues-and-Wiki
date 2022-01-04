package thut.core.common.world.mobs.data;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.ThutCaps;
import thut.api.Tracker;
import thut.api.world.mobs.data.DataSync;

public class SyncHandler
{
    @SubscribeEvent
    public static void EntityUpdate(final LivingUpdateEvent event)
    {
        if (event.getEntity().getCommandSenderWorld().isClientSide) return;
        final DataSync data = SyncHandler.getData(event.getEntity());
        if (data == null) return;
        long tick = Tracker.instance().getTick();
        if (tick == data.getTick()) return;
        data.setTick(tick);
        if (!data.syncNow() && tick % data.tickRate() != data.tickOffset() % data.tickRate()) return;
        PacketDataSync.sync(event.getEntity(), data, event.getEntity().getId(), false);
    }

    public static DataSync getData(final Entity mob)
    {
        return mob.getCapability(ThutCaps.DATASYNC, null).orElse(null);
    }

    @SubscribeEvent
    public static void startTracking(final StartTracking event)
    {
        if (event.getTarget().getCommandSenderWorld().isClientSide) return;
        final DataSync data = SyncHandler.getData(event.getTarget());
        if (data == null) return;
        PacketDataSync.sync((ServerPlayer) event.getEntity(), data, event.getTarget().getId(), true);
    }
}
