package thut.core.common.world.mobs.data;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import thut.api.ThutCaps;
import thut.api.Tracker;
import thut.api.entity.ICopyMob;
import thut.api.world.mobs.data.DataSync;

public class SyncHandler
{
    @SubscribeEvent
    public static void EntityUpdate(final EntityTickEvent.Post event)
    {
        if (event.getEntity().level().isClientSide || !event.getEntity().hasData(DataSync_Impl.TYPE)) return;
        if (!event.getEntity().isAlive()) return;
        Entity entity = event.getEntity();
        DataSync data = SyncHandler.getData(entity);
        mainData:
        {
            long tick = Tracker.instance().getTick();
            if (tick == data.getTick()) break mainData;
            data.setTick(tick);
            boolean shouldTick = data.syncNow();
            shouldTick |= tick % data.tickRate() == data.tickOffset() % data.tickRate();
            if (!shouldTick) break mainData;
            PacketDataSync.sync(entity, data, entity.getId(), false);
        }
        ICopyMob copy = ThutCaps.getCopyMob(event.getEntity());
        copyData:
        {
            if (copy == null || copy.getCopiedMob() == null) break copyData;
            entity = copy.getCopiedMob();
            data = SyncHandler.getData(entity);
            if (data == null) break copyData;
            long tick = Tracker.instance().getTick();
            if (tick == data.getTick()) break copyData;
            data.setTick(tick);
            boolean shouldTick = data.syncNow();
            shouldTick |= tick % data.tickRate() == data.tickOffset() % data.tickRate();
            if (!shouldTick) break copyData;
            PacketDataSync.sync(event.getEntity(), data, entity.getId(), false);
        }
    }

    public static DataSync getData(final Entity mob)
    {
        return ThutCaps.getDataSync(mob);
    }

    @SubscribeEvent
    public static void startTracking(final StartTracking event)
    {
        if (event.getTarget().level().isClientSide) return;
        final DataSync data = SyncHandler.getData(event.getTarget());
        if (data == null) return;
        PacketDataSync.sync((ServerPlayer) event.getEntity(), data, event.getTarget().getId(), true);
    }
}
