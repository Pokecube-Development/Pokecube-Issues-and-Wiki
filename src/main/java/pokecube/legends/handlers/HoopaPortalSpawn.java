package pokecube.legends.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraftforge.common.util.FakePlayer;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.core.eventhandlers.MoveEventsHandler.UseContext;
import pokecube.core.eventhandlers.SpawnHandler;
import pokecube.core.impl.PokecubeMod;
import pokecube.legends.blocks.customblocks.PortalWarp;
import pokecube.legends.init.BlockInit;
import thut.api.level.terrain.TerrainManager;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public class HoopaPortalSpawn 
{
    public static void portalSpawnTick(final Level world, final int distance)
    {
        if (!SpawnHandler.canSpawnInWorld(world)) return;
        final List<Object> players = new ArrayList<>(world.players());
        if (players.size() < 1) return;
        final Random rand = ThutCore.newRandom();
        final Entity player = (Entity) players.get(rand.nextInt(players.size()));
        final int dx = rand.nextInt(distance) - distance / 2;
        final int dz = rand.nextInt(distance) - distance / 2;
        final Vector3 v = new Vector3().set(player).add(dx, 0, dz);

        // Only spawn this if the nearby area is actually loaded.
        if (!TerrainManager.isAreaLoaded(world, v, 8)) return;

        v.y = world.getHeight(Types.WORLD_SURFACE, (int) v.x, (int) v.z) + 2;
        if (v.isAir(world))
        {

            final PortalWarp block = (PortalWarp) BlockInit.MIRAGE_SPOTS.get();
            final Vector3 fakePos = new Vector3().set(v);

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

            final UseContext context = MoveEventsHandler.getContext(world, placer, block.defaultBlockState(), v);
            final BlockState state = BlockInit.MIRAGE_SPOTS.get().getStateForPlacement(context);
            final BlockPos placePos = context.getClickedPos();
            ((PortalWarp) BlockInit.MIRAGE_SPOTS.get()).place(world, v.getPos(), Direction.from2DDataValue(
                    rand.nextInt()));
            // Only place if valid, this fixes #478
            if (state != null) block.place(world, placePos, context.getHorizontalDirection());

        }
    }
}
