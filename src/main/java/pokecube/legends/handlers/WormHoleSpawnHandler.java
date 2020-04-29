package pokecube.legends.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.UltraSpacePortal;
import pokecube.legends.init.BlockInit;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;

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
        if (!SpawnHandler.canSpawnInWorld(world)) return;
        final List<Object> players = new ArrayList<>(world.getPlayers());
        if (players.size() < 1) return;
        final Random rand = new Random();
        final Entity player = (Entity) players.get(rand.nextInt(players.size()));
        final int distance = PokecubeCore.getConfig().maxSpawnRadius;
        final int dx = rand.nextInt(distance) - distance / 2;
        final int dz = rand.nextInt(distance) - distance / 2;
        final Vector3 v = Vector3.getNewVector().set(player).add(dx, 0, dz);

        // Only spawn this if the nearby area is actually loaded.
        if (!TerrainManager.isAreaLoaded(world, v, 8)) return;

        v.y = world.getHeight(Heightmap.Type.WORLD_SURFACE, (int) v.x, (int) v.z) + 10;
        if (v.isAir(world)) ((UltraSpacePortal) BlockInit.ULTRASPACE_PORTAL).place(world, v.getPos(), Direction
                .byHorizontalIndex(rand.nextInt()));
    }
}
