package thut.core.common.world.mobs.data;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.ThutCaps;
import thut.api.Tracker;
import thut.api.entity.ICopyMob;
import thut.api.world.mobs.data.DataSync;

public class SyncHandler
{
    @SubscribeEvent
    public static void EntityUpdate(final LivingUpdateEvent event)
    {
        if (event.getEntity().getLevel().isClientSide) return;
        Entity entity = event.getEntity();
        DataSync data = SyncHandler.getData(entity);
        mainData:
        {
            if (data == null) break mainData;
            long tick = Tracker.instance().getTick();
            if (tick == data.getTick()) break mainData;
            data.setTick(tick);
            if (!data.syncNow() && tick % data.tickRate() != data.tickOffset() % data.tickRate()) break mainData;
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
            if (!data.syncNow() && tick % data.tickRate() != data.tickOffset() % data.tickRate()) break copyData;
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
        if (event.getTarget().getLevel().isClientSide) return;
        final DataSync data = SyncHandler.getData(event.getTarget());
        if (data == null) return;
        PacketDataSync.sync((ServerPlayer) event.getEntity(), data, event.getTarget().getId(), true);
    }
}
