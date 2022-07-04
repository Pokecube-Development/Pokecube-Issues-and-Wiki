package pokecube.world.gen_old;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WorldStructDatafixer
{
    public static void insertFixer()
    {
        MinecraftForge.EVENT_BUS.register(WorldStructDatafixer.class);
    }

    @SubscribeEvent
    public static void onChunkLoad(final ChunkEvent.Load event)
    {
//        TODO see if we still need this...
//        final Set<String> stale = Sets.newHashSet();
//        for (final String s : event.getChunk().getStructureStarts().keySet())
//            if (event.getChunk().getStructureStarts().get(s) == null) stale.add(s);
//        if (!stale.isEmpty())
//        {
//            IChunk chunk = event.getChunk();
//            if (chunk instanceof ChunkPrimerWrapper) chunk = ((ChunkPrimerWrapper) chunk).getWrapped();
//            PokecubeCore.LOGGER.warn("Removing error structure starts: " + stale);
//            final Map<String, LongSet> refs = Maps.newHashMap(chunk.getStructureReferences());
//            final Map<String, StructureStart> starts = Maps.newHashMap(chunk.getStructureStarts());
//            for (final String s : stale)
//            {
//                refs.remove(s);
//                starts.remove(s);
//            }
//            chunk.setStructureReferences(refs);
//            chunk.setStructureStarts(starts);
//        }
    }

}