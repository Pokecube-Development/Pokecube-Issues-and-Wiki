package thut.test.worldgen;

import java.util.Random;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
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
        final ServerWorld world = (ServerWorld) event.world;
        if (world.getDimensionKey() != World.OVERWORLD) return;
        final int n = world.getPlayers().isEmpty() ? 5 : 20;
        // ThutCore.LOGGER.info("World Tick {}",
        // event.world.getDimensionKey().getLocation());
        if (world.getGameTime() % n != 0) return;

        final Random rand = new Random();
        final int x = WorldgenTickTests.radius - rand.nextInt(WorldgenTickTests.radius * 2);
        final int z = WorldgenTickTests.radius - rand.nextInt(WorldgenTickTests.radius * 2);
        if (world.getPlayers().isEmpty())
        {
            // final IChunk chunk = world.getChunk(x, z);
            // chunk.getStatus();
        }
        else
        {
            final ServerPlayerEntity player = world.getPlayers().get(0);
            player.setPosition(x << 4, 100, z << 4);
        }
        // ThutCore.LOGGER.info("Chunk: {}, World: {}", chunk.getStatus(),
        // event.world.getDimensionKey().getLocation());
    }
}
