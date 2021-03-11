package pokecube.compat.minecolonies;

import java.util.Locale;
import java.util.Set;

import com.google.common.collect.Sets;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event.Result;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.events.TrainerInteractEvent.CanInteract;
import pokecube.core.PokecubeCore;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainSegment;
import thut.api.terrain.TerrainSegment.ISubBiomeChecker;

public class Impl
{
    private static IMinecoloniesAPI instance;
    private static Set<String>      logged = Sets.newHashSet();

    public static void register()
    {
        TerrainSegment.defaultChecker = new TerrainChecker(TerrainSegment.defaultChecker);
        Impl.instance = IMinecoloniesAPI.getInstance();

        // Register the type mapper for minecolonies citizens
        TypeTrainer.registerTypeMapper((mob, spawn) ->
        {
            return mob instanceof AbstractEntityCitizen ? TypeTrainer.merchant : null;
        });

        MinecraftForge.EVENT_BUS.addListener(Impl::onTrainerGuiCheck);

        // TODO check here for mine related stuff:
        // https://github.com/ldtteam/minecolonies/blob/34a42edeeddcb4c078ad25032a3a87d0015dc960/src/main/java/com/minecolonies/coremod/colony/buildings/workerbuildings/BuildingMiner.java
    }

    private static void onTrainerGuiCheck(final CanInteract event)
    {
        if (event.action.holder instanceof AbstractEntityCitizen)
        {
            // Lets default this to false.
            event.setResult(Result.DENY);
            final AbstractEntityCitizen cit = (AbstractEntityCitizen) event.action.holder;
            // Now we check if the citizen's colony considers the player
            // "important", if so, then allow.
            if (cit.getCitizenColonyHandler().getColony().getImportantMessageEntityPlayers().contains(event
                    .getEntityLiving())) event.setResult(Result.ALLOW);
        }
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
            if (!(world instanceof World)) return -1;
            final World rworld = (World) world;
            check:
            if (caveAdjusted) if (world.getChunkSource() instanceof ServerChunkProvider)
            {
                if (!Impl.instance.getColonyManager().isCoordinateInAnyColony(rworld, v.getPos())) break check;

                final IColony colony = Impl.instance.getColonyManager().getClosestColony(rworld, v.getPos());
                if (colony == null || colony.getBuildingManager() == null || colony.getBuildingManager()
                        .getBuildings() == null) break check;
                final Vector3d vec = new Vector3d(v.x, v.y, v.z);
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
