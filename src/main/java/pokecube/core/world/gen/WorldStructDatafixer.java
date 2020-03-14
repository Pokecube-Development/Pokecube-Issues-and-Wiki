package pokecube.core.world.gen;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.chunk.ChunkPrimerWrapper;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.PokecubeCore;

public class WorldStructDatafixer
{
    public static void insertFixer()
    {
        MinecraftForge.EVENT_BUS.register(WorldStructDatafixer.class);
    }

    @SubscribeEvent
    public static void onChunkLoad(final ChunkEvent.Load event)
    {
        final Set<String> stale = Sets.newHashSet();
        for (final String s : event.getChunk().getStructureStarts().keySet())
            if (event.getChunk().getStructureStarts().get(s) == null) stale.add(s);
        if (!stale.isEmpty())
        {
            IChunk chunk = event.getChunk();
            if (chunk instanceof ChunkPrimerWrapper) chunk = ((ChunkPrimerWrapper) chunk).func_217336_u();
            PokecubeCore.LOGGER.warn("Removing error structure starts: " + stale);
            final Map<String, LongSet> refs = Maps.newHashMap(chunk.getStructureReferences());
            final Map<String, StructureStart> starts = Maps.newHashMap(chunk.getStructureStarts());
            for (final String s : stale)
            {
                refs.remove(s);
                starts.remove(s);
            }
            chunk.setStructureReferences(refs);
            chunk.setStructureStarts(starts);
        }
    }

}