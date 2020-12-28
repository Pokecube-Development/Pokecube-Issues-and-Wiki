package thut.test.worldgen;

import java.util.Random;

import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import thut.core.common.ThutCore;
import thut.test.Tests;

public class WorldgenTickTests
{
    public static int radius = 1000;

    public static void onWorldTick(final WorldTickEvent event)
    {
        Tests.WORLGENTICKTEST = false;
        if (!Tests.WORLGENTICKTEST) return;
        ThutCore.LOGGER.info("World Tick");
        if (event.phase == Phase.START) return;
        if (!(event.world instanceof ServerWorld)) return;
        final int n = 20;
        if (event.world.getGameTime() % n != 0) return;
        final Random rand = new Random();
        final int x = WorldgenTickTests.radius - rand.nextInt(WorldgenTickTests.radius * 2);
        final int z = WorldgenTickTests.radius - rand.nextInt(WorldgenTickTests.radius * 2);
        ThutCore.LOGGER.info(event.world.getChunk(x, z));
    }
}
