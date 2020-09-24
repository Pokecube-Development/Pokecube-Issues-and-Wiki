package pokecube.legends.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.handlers.events.MoveEventsHandler;
import pokecube.core.handlers.events.MoveEventsHandler.UseContext;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.customblocks.UltraSpacePortal;
import pokecube.legends.init.BlockInit;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;

public class WormHoleSpawnHandler
{
    @SubscribeEvent
    public void tickEvent(final WorldTickEvent evt)
    {
        if (evt.phase == Phase.END && evt.side != LogicalSide.CLIENT && !Database.spawnables.isEmpty()) if (evt.world
                .getGameTime() % PokecubeLegends.config.ticksPerPortalSpawn == 0) WormHoleSpawnHandler.portalSpawnTick(
                        evt.world, PokecubeCore.getConfig().maxSpawnRadius);
    }

    public static void portalSpawnTick(final World world, final int distance)
    {
        if (!SpawnHandler.canSpawnInWorld(world)) return;
        final List<Object> players = new ArrayList<>(world.getPlayers());
        if (players.size() < 1) return;
        final Random rand = new Random();
        final Entity player = (Entity) players.get(rand.nextInt(players.size()));
        final int dx = rand.nextInt(distance) - distance / 2;
        final int dz = rand.nextInt(distance) - distance / 2;
        final Vector3 v = Vector3.getNewVector().set(player).add(dx, 0, dz);

        // Only spawn this if the nearby area is actually loaded.
        if (!TerrainManager.isAreaLoaded(world, v, 8)) return;

        v.y = world.getHeight(Heightmap.Type.WORLD_SURFACE, (int) v.x, (int) v.z) + 2;
        if (v.isAir(world))
        {

            final UltraSpacePortal block = (UltraSpacePortal) BlockInit.ULTRASPACE_PORTAL.get();
            final Vector3 fakePos = Vector3.getNewVector().set(v);

            // Randomize the position a bit so the orientation of portal is
            // randomized
            final double rng1 = Math.random();
            final double rng2 = Math.random();
            if (rng1 < 1 / 3) fakePos.x += 1;
            if (rng1 > 2 / 3) fakePos.x -= 1;
            if (rng2 < 1 / 3) fakePos.z += 1;
            if (rng2 > 2 / 3) fakePos.z -= 1;

            final FakePlayer placer = PokecubeMod.getFakePlayer(world);
            fakePos.moveEntity(placer);

            final UseContext context = MoveEventsHandler.getContext(world, placer, block.getDefaultState(), v);
            final BlockState state = BlockInit.ULTRASPACE_PORTAL.get().getStateForPlacement(context);
            final BlockPos placePos = context.getPos();
            ((UltraSpacePortal) BlockInit.ULTRASPACE_PORTAL.get()).place(world, v.getPos(), Direction.byHorizontalIndex(
                    rand.nextInt()));
            // Only place if valid, this fixes #478
            if (state != null) block.place(world, placePos, context.getFace());

        }
    }
}
