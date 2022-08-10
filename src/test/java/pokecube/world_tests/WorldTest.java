package pokecube.world_tests;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.compress.utils.Lists;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pokecube.core.PokecubeCore;
import thut.api.maths.Cruncher.SquareLoopCruncher;
import thut.core.common.ThutCore;

@Mod.EventBusSubscriber
public class WorldTest
{
    public static int average(List<Integer> pos)
    {
        if (pos.size() == 0) return -1;
        int sum = 0;
        for (var i : pos) sum += i;
        return sum / pos.size();
    }

    public static int stdev(List<Integer> pos)
    {
        int average = average(pos);
        if (average == -1) return -1;
        int sum = 0;
        for (var i : pos) sum += Math.pow((i - average), 2);
        return (int) Math.sqrt(sum / pos.size());
    }

    public static class Logger
    {
        public static class LogEntry implements Comparable<LogEntry>
        {
            public ResourceLocation name;
            public int last_n = 0;
            public Set<BlockPos> points = new HashSet<>();

            public boolean locate(int max_n, ServerLevel level, BlockPos pos,
                    Registry<ConfiguredStructureFeature<?, ?>> registry, int min_dt, int radius)
            {
                if (max_n == 25310) ThutCore.LOGGER.info("Checking {}", name);
                final ResourceKey<ConfiguredStructureFeature<?, ?>> structure = ResourceKey
                        .create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, name);
                var holder = registry.getHolderOrThrow(structure);
                HolderSet<ConfiguredStructureFeature<?, ?>> holderset = HolderSet.direct(holder);
                long time = System.nanoTime();
                Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> thing = level.getChunkSource().getGenerator()
                        .findNearestMapFeature(level, holderset, pos, radius, false);
                long dt = (long) ((System.nanoTime() - time) / 1e3);
                if (thing != null)
                {
                    last_n = 0;
                    this.points.add(thing.getFirst());
                }
                if (max_n == 25310) ThutCore.LOGGER.info("dt: {} us ({})", dt, this.points.isEmpty());
                if (dt < min_dt) return false;
                return true;
            }

            public int[] computeDistances()
            {
                int[] ret =
                { 0, 0 };
                List<Integer> distances = Lists.newArrayList();
                for (var pos : points)
                {
                    double min = Double.MAX_VALUE;
                    for (var pos2 : points)
                    {
                        if (pos2 == pos) continue;
                        double dist = pos2.distSqr(pos);
                        min = Math.min(dist, min);
                    }
                    if (min != Double.MAX_VALUE) distances.add((int) Math.sqrt(min));
                }

                if (points.size() == 1)
                {
                    distances.add((int) Math.sqrt(points.iterator().next().distSqr(BlockPos.ZERO)));
                }

                ret[0] = average(distances);
                ret[1] = stdev(distances);
                return ret;
            }

            @Override
            public int compareTo(LogEntry o)
            {
                return name.compareTo(o.name);
            }
        }

        public List<LogEntry> entries = new ArrayList<>();
        public List<ResourceLocation> all_Checked = new ArrayList<>();

        SquareLoopCruncher searcher = new SquareLoopCruncher();

        int n = 0;

        int x = -50;
        int z = -50;
        int r = 50000;

        BlockPos pos = new BlockPos(0, 0, 0);

        public boolean tick(ServerLevel level)
        {
            if (level.players().isEmpty()) return false;
            var registry = level.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
            if (this.entries.isEmpty())
            {
                var list = registry.stream().toList();
                for (var feature : list)
                {
                    var name = registry.getKey(feature);
                    if (name.toString().startsWith("pokecube"))
                    {
                        LogEntry entry = new LogEntry();
                        entry.name = name;
                        this.entries.add(entry);
                        all_Checked.add(name);
                    }
                }
                this.entries.sort(null);
            }
            int min_found = Integer.MAX_VALUE;
            int max_found = Integer.MIN_VALUE;
            ResourceLocation min = null;
            ResourceLocation max = null;

            int step = 12 * 16;

            BlockPos pos = searcher.getNext(level.getSharedSpawnPos(), step);
            if (n % 10 == 0) ThutCore.LOGGER.info("\n\nChecking Structures {} {}\n", pos, level.dimension().location());
            long time = System.nanoTime();

            int num = 25000;
            if (n == 0) num = 25310;
            int _num = num;

            this.entries.removeIf(e -> !e.locate(_num, level, pos, registry, 0, step / 24));
            long dt = System.nanoTime() - time;

            boolean any_found = false;

            for (var entry : this.entries)
            {
                if (entry.points.isEmpty()) continue;
                any_found = true;
                if (entry.points.size() < min_found) min = entry.name;
                if (entry.points.size() > max_found) max = entry.name;
                min_found = Math.min(min_found, entry.points.size());
                max_found = Math.max(max_found, entry.points.size());
            }
            if (n % 10 == 0) ThutCore.LOGGER.info(
                    "\nMin Found: {} ({}), Max Found: {} ({}), took: {} ms, checking {}, iter {}, r {}\n", min_found,
                    min, max_found, max, dt / 1e6, entries.size(), n, searcher._radius * step);
            n++;
            return (any_found && min_found >= 100) || step * searcher._radius > 100000;
        }
    }

    static Logger LOG = null;

    @SubscribeEvent
    public static void onChat(final ServerChatEvent chat)
    {
        if (chat.getMessage().startsWith("Start:Debug_Structures"))
        {
            LOG = new Logger();
        }
        if (chat.getMessage().startsWith("Status:Debug_Structures"))
        {
            PokecubeCore.LOGGER.info("Structures Found:");
            Set<ResourceLocation> found = Sets.newHashSet();
            for (var entry : LOG.entries)
            {
                var name = entry.name;
                if (entry.points.isEmpty())
                {
                    continue;
                }
                int[] vars = entry.computeDistances();
                PokecubeCore.LOGGER.info("{}\t{}\t{}\t{}", name, vars[0], vars[1], entry.points.size());
                found.add(name);
            }
            PokecubeCore.LOGGER.info("Structures Missing:");
            for (var name : LOG.all_Checked)
            {
                if (!found.contains(name)) PokecubeCore.LOGGER.info(name);
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(final WorldTickEvent event)
    {
        if (LOG != null && event.world instanceof ServerLevel level && event.phase == Phase.END)
        {
            boolean done = LOG.tick(level);
            if (done)
            {
                PokecubeCore.LOGGER.info("Structures Found:");
                for (var entry : LOG.entries)
                {
                    var name = entry.name;
                    if (entry.points.isEmpty())
                    {
                        continue;
                    }
                    int[] vars = entry.computeDistances();
                    PokecubeCore.LOGGER.info("{}\t{}\t{}\t{}", name, vars[0], vars[1], entry.points.size());
                    LOG.all_Checked.remove(name);
                }
                PokecubeCore.LOGGER.info("Structures Missing:");
                for (var name : LOG.all_Checked)
                {
                    PokecubeCore.LOGGER.info(name);
                }
                LOG = null;
            }
        }
    }
}