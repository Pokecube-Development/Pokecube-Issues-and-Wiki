package thut.api.world;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.level.LevelEvent;
import thut.core.common.ThutCore;

public class WorldTickManager
{
    private static class WorldData
    {
        private final List<IWorldTickListener> data = Lists.newArrayList();

        private final ServerLevel world;

        private final List<IWorldTickListener> pendingRemove = Lists.newArrayList();
        private final List<IWorldTickListener> pendingAdd = Lists.newArrayList();

        private boolean ticking = false;

        public WorldData(final ServerLevel world)
        {
            this.world = world;
        }

        public void onWorldTickEnd()
        {
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
            this.ticking = true;
            for (final IWorldTickListener data : this.data) data.onTickStart(this.world);
            this.ticking = false;
            for (final IWorldTickListener data : this.pendingRemove) this.removeData(data);
            for (final IWorldTickListener data : this.pendingAdd) this.addData(data);
            this.pendingRemove.clear();
            this.pendingAdd.clear();
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

    public static void onWorldUnload(final LevelEvent.Unload event)
    {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        final ResourceKey<Level> key = level.dimension();
        if (WorldTickManager.dataMap.containsKey(key)) WorldTickManager.dataMap.remove(key).detach();
        WorldTickManager.pathHelpers.remove(key);
    }

    public static void onWorldTick(final LevelTickEvent event)
    {
        if (event.level instanceof ServerLevel)
        {
            
            // Uncomment to produce server lag for testing.
//            if (event.level.getRandom().nextDouble() > 0.9)
//            {
//                long start = System.nanoTime();
//                int wait = event.level.getRandom().nextInt(1000000, 100000000);
//                while (System.nanoTime() < start + wait)
//                {}
//                System.out.println("FORCED LAGGED: " + (wait / 1e9d));
//            }
            
            final ResourceKey<Level> key = event.level.dimension();
            final WorldData data = WorldTickManager.dataMap.get(key);
            if (data == null)
            {
                ThutCore.LOGGER.error("Ticking world before load???");
                return;
            }
            if (event.phase == Phase.END) data.onWorldTickEnd();
            else data.onWorldTickStart();
        }
    }
}
