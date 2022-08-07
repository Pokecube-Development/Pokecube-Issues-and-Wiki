package pokecube.world_tests;

import java.util.ArrayList;
import java.util.List;

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
import thut.core.common.ThutCore;

@Mod.EventBusSubscriber
public class WorldTest
{
    public static class Logger
    {
        public static class LogEntry implements Comparable<LogEntry>
        {
            public ResourceLocation name;
            public int last_n = 0;
            public List<Integer> distances = new ArrayList<>();

            public boolean locate(ServerLevel level, BlockPos pos, Registry<ConfiguredStructureFeature<?, ?>> registry)
            {
                ThutCore.LOGGER.info("Checking {}", name);
                final ResourceKey<ConfiguredStructureFeature<?, ?>> structure = ResourceKey
                        .create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, name);
                var holder = registry.getHolderOrThrow(structure);
                HolderSet<ConfiguredStructureFeature<?, ?>> holderset = HolderSet.direct(holder);
                long time = System.currentTimeMillis();
                Pair<BlockPos, Holder<ConfiguredStructureFeature<?, ?>>> thing = level.getChunkSource().getGenerator()
                        .findNearestMapFeature(level, holderset, pos, 100, false);
                long dt = System.currentTimeMillis() - time;
                if (thing == null && dt < 5) last_n += 10;
                if (thing != null)
                {
                    last_n = 0;
                    this.distances.add((int) Math.sqrt(thing.getFirst().distSqr(pos)));
                }
                ThutCore.LOGGER.info("dt: {} ms", dt);
                if (!this.distances.isEmpty()) return true;
                return last_n++ < 5;
            }

            @Override
            public int compareTo(LogEntry o)
            {
                return name.compareTo(o.name);
            }
        }

        public List<LogEntry> entries = new ArrayList<>();
        public List<ResourceLocation> all_Checked = new ArrayList<>();

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
            pos = new BlockPos(x * r, 0, z * r);
            x++;
            if (x > 50)
            {
                x = -50;
                z++;
            }
            ThutCore.LOGGER.info("\n\nChecking Structures\n");
            long time = System.currentTimeMillis();
            this.entries.removeIf(e -> !e.locate(level, pos, registry));
            long dt = System.currentTimeMillis() - time;
            for (var entry : this.entries)
            {
                if (entry.distances.size() < min_found) min = entry.name;
                if (entry.distances.size() > max_found) max = entry.name;
                min_found = Math.min(min_found, entry.distances.size());
                max_found = Math.max(max_found, entry.distances.size());
            }
            ThutCore.LOGGER.info("\nMin Found: {} ({}), Max Found: {} ({}), took: {} s\n", min_found, min, max_found,
                    max, dt / 1000);
            return min_found >= 10;
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
                    PokecubeCore.LOGGER.info("{}\t{}\t{}\t{}", name, average(entry.distances), stdev(entry.distances),
                            entry.distances.size());
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
}
