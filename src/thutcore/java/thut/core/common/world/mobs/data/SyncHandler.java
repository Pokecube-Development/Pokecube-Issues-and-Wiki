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
import thut.core.common.network.SyncData;

public class SyncHandler
{
    @SubscribeEvent
    public static void EntityUpdate(final EntityTickEvent.Post event)
    {
        Entity entity = event.getEntity();
        // No running on clients, or for ones with no data
        if (entity.level().isClientSide || !entity.hasData(DataSync_Impl.TYPE)) return;
        // No running for dead ones, or attached ticking ICopyMobs
        if (!entity.isAlive() || entity.getId() < -100) return;
        DataSync data = SyncHandler.getData(entity);
        mainData:
        {
            long tick = Tracker.instance().getTick();
            if (tick == data.getTick()) break mainData;
            data.setTick(tick);
            boolean shouldTick = data.syncNow();
            shouldTick |= (tick % data.tickRate()) == (data.tickOffset() % data.tickRate());
            if (!shouldTick) break mainData;
            SyncData.sync(entity, data, entity.getId(), false);
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
            shouldTick |= (tick % data.tickRate()) == (data.tickOffset() % data.tickRate());
            if (!shouldTick) break copyData;
            SyncData.sync(event.getEntity(), data, entity.getId(), false);
        }
    }

    public static DataSync getData(final Entity mob)
    {
        return ThutCaps.getDataSync(mob);
    }

    @SubscribeEvent
    public static void startTracking(final StartTracking event)
    {
        if (event.getTarget().level().isClientSide || !event.getTarget().hasData(DataSync_Impl.TYPE)) return;
        final DataSync data = SyncHandler.getData(event.getTarget());
        SyncData.sync((ServerPlayer) event.getEntity(), data, event.getTarget().getId(), true);
    }
}
