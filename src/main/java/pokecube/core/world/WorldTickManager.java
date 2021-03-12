package pokecube.core.world;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.world.WorldEvent;
import pokecube.core.PokecubeCore;

public class WorldTickManager
{
    private static class WorldData
    {
        private final List<IWorldTickListener> data = Lists.newArrayList();

        private final ServerWorld world;

        private final List<IWorldTickListener> pendingRemove = Lists.newArrayList();
        private final List<IWorldTickListener> pendingAdd    = Lists.newArrayList();

        private boolean ticking = false;

        public WorldData(final ServerWorld world)
        {
            this.world = world;
        }

        public void onWorldTickEnd()
        {
            this.ticking = true;
            for (final IWorldTickListener data : this.data)
                data.onTickEnd(this.world);
            this.ticking = false;
            for (final IWorldTickListener data : this.pendingRemove)
                this.removeData(data);
            for (final IWorldTickListener data : this.pendingAdd)
                this.addData(data);
            this.pendingRemove.clear();
            this.pendingAdd.clear();
        }

        public void onWorldTickStart()
        {
            this.ticking = true;
            for (final IWorldTickListener data : this.data)
                data.onTickStart(this.world);
            this.ticking = false;
            for (final IWorldTickListener data : this.pendingRemove)
                this.removeData(data);
            for (final IWorldTickListener data : this.pendingAdd)
                this.addData(data);
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
            for (final IWorldTickListener data : this.data)
                data.onDetach(this.world);
        }
    }

    public static class StaticData
    {
        public final Predicate<RegistryKey<World>> valid;

        public final Supplier<IWorldTickListener> data;

        public StaticData(final Supplier<IWorldTickListener> data, final Predicate<RegistryKey<World>> valid)
        {
            this.data = data;
            this.valid = valid;
        }
    }

    public static List<StaticData> staticData = Lists.newArrayList();

    static Map<RegistryKey<World>, WorldData> dataMap = Maps.newHashMap();

    public static Map<RegistryKey<World>, List<IPathHelper>> pathHelpers = Maps.newHashMap();

    public static void registerStaticData(final Supplier<IWorldTickListener> data,
            final Predicate<RegistryKey<World>> valid)
    {
        WorldTickManager.staticData.add(new StaticData(data, valid));
    }

    public static void addWorldData(final RegistryKey<World> key, final IWorldTickListener data)
    {
        final WorldData holder = WorldTickManager.dataMap.get(key);
        if (holder == null)
        {
            PokecubeCore.LOGGER.error("Adding Data before load???");
            return;
        }
        holder.addData(data);
    }

    public static void removeWorldData(final RegistryKey<World> key, final IWorldTickListener data)
    {
        final WorldData holder = WorldTickManager.dataMap.get(key);
        if (holder == null)
        {
            PokecubeCore.LOGGER.error("Removing Data before load???");
            return;
        }
        holder.removeData(data);
    }

    public static void onWorldLoad(final WorldEvent.Load event)
    {
        if (event.getWorld().isClientSide()) return;
        if (!(event.getWorld() instanceof ServerWorld)) return;
        final ServerWorld world = (ServerWorld) event.getWorld();
        final RegistryKey<World> key = world.dimension();
        if (WorldTickManager.dataMap.containsKey(key)) WorldTickManager.dataMap.get(key).detach();
        final WorldData data = new WorldData(world);
        WorldTickManager.dataMap.put(key, data);
        WorldTickManager.staticData.forEach(s ->
        {
            if (s.valid.test(key)) data.addData(s.data.get());
        });
        WorldTickManager.pathHelpers.put(key, Lists.newArrayList());
    }

    public static void onWorldUnload(final WorldEvent.Unload event)
    {
        if (event.getWorld().isClientSide()) return;
        if (!(event.getWorld() instanceof ServerWorld)) return;
        final ServerWorld world = (ServerWorld) event.getWorld();
        final RegistryKey<World> key = world.dimension();
        if (WorldTickManager.dataMap.containsKey(key)) WorldTickManager.dataMap.remove(key).detach();
        WorldTickManager.pathHelpers.remove(key);
    }

    public static void onWorldTick(final WorldTickEvent event)
    {
        if (event.world instanceof ServerWorld)
        {
            final RegistryKey<World> key = event.world.dimension();
            final WorldData data = WorldTickManager.dataMap.get(key);
            if (data == null)
            {
                PokecubeCore.LOGGER.error("Ticking world before load???");
                return;
            }
            if (event.phase == Phase.END) data.onWorldTickEnd();
            else data.onWorldTickStart();
        }
    }
}
