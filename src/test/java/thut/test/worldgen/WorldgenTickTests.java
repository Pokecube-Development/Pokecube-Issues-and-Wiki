package thut.test.worldgen;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import thut.test.Tests;

public class WorldgenTickTests
{
    public static int radius = 1000;

    public static void onWorldTick(final WorldTickEvent event)
    {
        Tests.WORLGENTICKTEST = false;
        if (!Tests.WORLGENTICKTEST) return;
        if (event.phase == Phase.START) return;
        if (!(event.world instanceof ServerWorld)) return;
        if (event.world.getDimensionKey() != World.OVERWORLD) return;
        final int n = 5;
        // ThutCore.LOGGER.info("World Tick {}",
        // event.world.getDimensionKey().getLocation());
        if (event.world.getGameTime() % n != 0) return;
        final Random rand = new Random();
        final int x = WorldgenTickTests.radius - rand.nextInt(WorldgenTickTests.radius * 2);
        final int z = WorldgenTickTests.radius - rand.nextInt(WorldgenTickTests.radius * 2);
        final IChunk chunk = event.world.getChunk(x, z);
        chunk.getStatus();
//        ThutCore.LOGGER.info("Chunk: {}, World: {}", chunk.getStatus(), event.world.getDimensionKey().getLocation());
    }
}
