package pokecube.compat.minecolonies;

import java.util.Locale;
import java.util.Set;

import com.google.common.collect.Sets;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerChunkProvider;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.PokecubeCore;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainSegment;
import thut.api.terrain.TerrainSegment.ISubBiomeChecker;

public class Impl
{
    private static boolean reged = false;

    private static IMinecoloniesAPI instance;
    private static Set<String>      logged = Sets.newHashSet();

    public static void register()
    {
        if (Impl.reged) return;
        Impl.reged = true;
        TerrainSegment.defaultChecker = new TerrainChecker(TerrainSegment.defaultChecker);
        Impl.instance = IMinecoloniesAPI.getInstance();
        PokecubeAdv.config.customTrainers.add(AbstractEntityCitizen.class);
    }

    public static class TerrainChecker extends PokecubeTerrainChecker
    {
        ISubBiomeChecker parent;

        public TerrainChecker(final ISubBiomeChecker parent)
        {
            this.parent = parent;
        }

        @Override
        public int getSubBiome(final IWorld world, final Vector3 v, final TerrainSegment segment,
                final boolean caveAdjusted)
        {
            check:
            if (caveAdjusted) if (world.getChunkProvider() instanceof ServerChunkProvider)
            {
                if (!Impl.instance.getColonyManager().isCoordinateInAnyColony(world.getWorld(), v.getPos()))
                    break check;

                final IColony colony = Impl.instance.getColonyManager().getClosestColony(world.getWorld(), v.getPos());
                final Vec3d vec = new Vec3d(v.x, v.y, v.z);
                for (final IBuilding b : colony.getBuildingManager().getBuildings().values())
                {
                    String type = b.getSchematicName();
                    type = type.toLowerCase(Locale.ROOT);
                    if (Impl.logged.add(type)) PokecubeCore.LOGGER.info("Minecolonies Structure: " + type);
                    if (PokecubeTerrainChecker.structureSubbiomeMap.containsKey(type))
                        type = PokecubeTerrainChecker.structureSubbiomeMap.get(type);
                    else continue;
                    final AxisAlignedBB box = b.getTargetableArea(colony.getWorld());
                    if (box.contains(vec)) return BiomeType.getBiome(type, true).getType();
                }
            }
            return this.parent.getSubBiome(world, v, segment, caveAdjusted);
        }
    }
}
