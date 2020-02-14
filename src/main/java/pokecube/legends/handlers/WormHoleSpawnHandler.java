package pokecube.legends.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.init.BlockInit;
import thut.api.maths.Vector3;

public class WormHoleSpawnHandler
{
    @SubscribeEvent
    public void tickEvent(final WorldTickEvent evt)
    {
        if (evt.phase == Phase.END && evt.side != LogicalSide.CLIENT && !Database.spawnables.isEmpty()) if (evt.world
                .getGameTime() % PokecubeLegends.config.ticksPerPortalSpawn == 0) this.portalSpawnTick(evt.world);
    }

    public void portalSpawnTick(final World world)
    {
        final List<Object> players = new ArrayList<>(world.getPlayers());
        if (players.size() < 1) return;
        final Random rand = new Random();
        final Entity player = (Entity) players.get(rand.nextInt(players.size()));
        final int distance = PokecubeCore.getConfig().maxSpawnRadius;
        final int dx = rand.nextInt(distance) - distance / 2;
        final int dz = rand.nextInt(distance) - distance / 2;
        final Vector3 v = Vector3.getNewVector().set(player).add(dx, 0, dz);
        v.x += dx;
        v.z += dz;
        v.y = world.getHeight(Heightmap.Type.WORLD_SURFACE, (int) v.x, (int) v.z) + 10;
        if (v.isAir(world)) world.setBlockState(v.getPos(), BlockInit.ULTRASPACE_PORTAL.getDefaultState().with(
                HorizontalBlock.HORIZONTAL_FACING, Direction.byHorizontalIndex(rand.nextInt())));

    }
}
