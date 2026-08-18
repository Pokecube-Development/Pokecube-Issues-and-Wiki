package thut.api.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import thut.api.Tracker;
import thut.core.common.ThutCore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

@EventBusSubscriber
public class WorldTickManager
{
    public static class DelayedTask implements Runnable
    {
        private final long tick;
        private final Runnable runnable;

        public DelayedTask(long runTick, Runnable runnable)
        {
            this.tick = runTick;
            this.runnable = runnable;
        }

        public long getTick()
        {
            return this.tick;
        }

        public void run()
        {
            this.runnable.run();
        }
    }

    private static class WorldData
    {
        private final List<IWorldTickListener> data = Lists.newArrayList();

        private final ServerLevel world;

        private final List<IWorldTickListener> pendingRemove = Lists.newArrayList();
        private final List<IWorldTickListener> pendingAdd = Lists.newArrayList();
        private final List<DelayedTask> pendingDelayed = new ArrayList<>();
        private final List<DelayedTask> delayed = new ArrayList<>();

        private boolean ticking = false;
        private long lastEndTick;
        private long lastStartTick;

        public WorldData(final ServerLevel world)
        {
            this.world = world;
        }

        public void onWorldTickEnd()
        {
            long tick = Tracker.instance().getTick();
            if (tick == lastEndTick) return;
            lastEndTick = tick;
            this.ticking = true;
            for (final IWorldTickListener data : this.data) data.onTickEnd(this.world);
            this.ticking = false;
            for (final IWorldTickListener data : this.pendingRemove) this.removeData(data);
            for (final IWorldTickListener data : this.pendingAdd) this.addData(data);
            this.pendingRemove.clear();
            this.pendingAdd.clear();
        }

        public void onWorldTickStart()
        {
            long tick = Tracker.instance().getTick();
            if (tick == lastStartTick) return;
            lastStartTick = tick;

            this.ticking = true;
            for (final IWorldTickListener data : this.data) data.onTickStart(this.world);
            this.ticking = false;
            for (final IWorldTickListener data : this.pendingRemove) this.removeData(data);
            for (final IWorldTickListener data : this.pendingAdd) this.addData(data);
            this.pendingRemove.clear();
            this.pendingAdd.clear();

            synchronized (pendingDelayed)
            {
                this.delayed.addAll(pendingDelayed);
                pendingDelayed.clear();
            }

            delayed.removeIf(task -> {
                if (task.getTick() > Tracker.instance().getTick()) return false;
                try
                {
                    task.run();
                }
                catch (Exception e)
                {
                    ThutCore.LOGGER.error("Error running a delayed task!", e);
                }
                return true;
            });
        }

        public void addData(final IWorldTickListener data)
        {
            if (this.data.contains(data)) return;
            data.onAttach(this.world);
            if (!this.ticking) this.data.add(data);
            else this.pendingAdd.add(data);
        }

        public void removeData(final IWorldTickListener data)
        {
            if (!this.ticking)
            {
                if (!this.data.remove(data)) return;
                data.onDetach(this.world);
            }
            else this.pendingRemove.add(data);
        }

        public void addDelayedTask(DelayedTask task)
        {
            synchronized (pendingDelayed)
            {
                this.pendingDelayed.add(task);
            }
        }

        public void detach()
        {
            for (final IWorldTickListener data : this.data) data.onDetach(this.world);
        }
    }

    public static class StaticData
    {
        public final Predicate<ResourceKey<Level>> valid;

        public final Supplier<IWorldTickListener> data;

        public StaticData(final Supplier<IWorldTickListener> data, final Predicate<ResourceKey<Level>> valid)
        {
            this.data = data;
            this.valid = valid;
        }
    }

    public static List<StaticData> staticData = Lists.newArrayList();

    static Map<ResourceKey<Level>, WorldData> dataMap = Maps.newHashMap();

    public static Map<ResourceKey<Level>, List<IPathHelper>> pathHelpers = Maps.newHashMap();

    public static void registerStaticData(final Supplier<IWorldTickListener> data,
            final Predicate<ResourceKey<Level>> valid)
    {
        WorldTickManager.staticData.add(new StaticData(data, valid));
    }

    public static void addWorldData(final ResourceKey<Level> key, final IWorldTickListener data)
    {
        final WorldData holder = WorldTickManager.dataMap.get(key);
        if (holder == null)
        {
            ThutCore.LOGGER.error("Adding Data before load???");
            return;
        }
        holder.addData(data);
    }

    public static void scheduleTask(final ResourceKey<Level> key, final DelayedTask task)
    {
        final WorldData holder = WorldTickManager.dataMap.get(key);
        if (holder == null)
        {
            ThutCore.LOGGER.error("Adding Data before load???");
            return;
        }
        holder.addDelayedTask(task);
    }

    public static void removeWorldData(final ResourceKey<Level> key, final IWorldTickListener data)
    {
        final WorldData holder = WorldTickManager.dataMap.get(key);
        if (holder == null)
        {
            ThutCore.LOGGER.error("Removing Data before load???");
            return;
        }
        holder.removeData(data);
    }

    @SubscribeEvent
    public static void onWorldLoad(final LevelEvent.Load event)
    {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        final ResourceKey<Level> key = level.dimension();
        if (WorldTickManager.dataMap.containsKey(key)) WorldTickManager.dataMap.get(key).detach();
        final WorldData data = new WorldData(level);
        WorldTickManager.dataMap.put(key, data);
        WorldTickManager.staticData.forEach(s -> {
            if (s.valid.test(key)) data.addData(s.data.get());
        });
        WorldTickManager.pathHelpers.put(key, Lists.newArrayList());
    }

    @SubscribeEvent
    public static void onWorldUnload(final LevelEvent.Unload event)
    {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        final ResourceKey<Level> key = level.dimension();
        if (WorldTickManager.dataMap.containsKey(key)) WorldTickManager.dataMap.remove(key).detach();
        WorldTickManager.pathHelpers.remove(key);
    }

    @SubscribeEvent
    public static void onWorldTickPost(final LevelTickEvent.Post event)
    {
        if (event.getLevel() instanceof ServerLevel)
        {
            final ResourceKey<Level> key = event.getLevel().dimension();
            final WorldData data = WorldTickManager.dataMap.get(key);
            if (data == null)
            {
                ThutCore.LOGGER.error("Ticking world before load???");
                return;
            }
            data.onWorldTickEnd();

        }
    }

    @SubscribeEvent
    public static void onWorldTickPre(final LevelTickEvent.Pre event)
    {
        if (event.getLevel() instanceof ServerLevel)
        {
            final ResourceKey<Level> key = event.getLevel().dimension();
            final WorldData data = WorldTickManager.dataMap.get(key);
            if (data == null)
            {
                ThutCore.LOGGER.error("Ticking world before load???");
                return;
            }
            data.onWorldTickStart();
        }
    }
}
