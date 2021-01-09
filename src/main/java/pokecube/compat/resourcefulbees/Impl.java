package pokecube.compat.resourcefulbees;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.PointOfInterest;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.idle.bees.BeeTasks;
import pokecube.core.ai.tasks.idle.bees.sensors.HiveSensor;
import pokecube.core.ai.tasks.idle.bees.sensors.HiveSensor.IHiveEnterer;
import pokecube.core.ai.tasks.idle.bees.sensors.HiveSensor.IHiveLocator;
import pokecube.core.ai.tasks.idle.bees.sensors.HiveSensor.IHiveSpaceCheck;

public class Impl
{

    public static void register()
    {
        try
        {
            final Class<?> APIARYTILE = Impl.class.getClassLoader().loadClass(
                    "com.resourcefulbees.resourcefulbees.tileentity.multiblocks.apiary.ApiaryTileEntity");
            final Method IS_FULL_BEES = APIARYTILE.getMethod("isFullOfBees");
            final Method CAN_HOLD_BEES = APIARYTILE.getMethod("isAllowedBee");
            final Method TRY_ENTER_HIVE = APIARYTILE.getMethod("tryEnterHive", Entity.class, boolean.class,
                    boolean.class);
            final Method COUNT_BEES = APIARYTILE.getMethod("getBeeCount");
            final Method MAX_BEES = APIARYTILE.getMethod("getMaxBees");

            final IHiveEnterer apiaryEnterer = (entityIn, tile) ->
            {
                if (!APIARYTILE.isInstance(tile)) return false;
                try
                {
                    final int num = (int) COUNT_BEES.invoke(tile);
                    final int maxNum = (int) MAX_BEES.invoke(tile);
                    if (maxNum >= num) return false;
                    final Brain<?> brain = entityIn.getBrain();
                    final Optional<Boolean> hasNectar = brain.getMemory(BeeTasks.HAS_NECTAR);
                    final boolean nectar = hasNectar.isPresent() && hasNectar.get();
                    // Try to enter the hive
                    return (boolean) TRY_ENTER_HIVE.invoke(tile, entityIn, nectar, false);
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.error("ERROR WITH ENTERING APIARY!");
                    PokecubeCore.LOGGER.error(e);
                    return false;
                }
            };
            HiveSensor.hiveEnterers.add(apiaryEnterer);
            final IHiveSpaceCheck apiaryChecker = (entityIn, tile) ->
            {
                if (!APIARYTILE.isInstance(tile)) return false;
                try
                {
                    return (boolean) CAN_HOLD_BEES.invoke(tile) && !(boolean) IS_FULL_BEES.invoke(tile);
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.error("ERROR WITH CHECKING APIARY!");
                    PokecubeCore.LOGGER.error(e);
                    return false;
                }
            };
            HiveSensor.hiveSpaceCheckers.add(apiaryChecker);
            final PointOfInterestType POI = ForgeRegistries.POI_TYPES.getValue(new ResourceLocation("resourcefulbees",
                    "tiered_beehive_poi"));
            final IHiveLocator apiaryLocator = (entityIn) ->
            {
                final BlockPos blockpos = entityIn.getPosition();
                final PointOfInterestManager pointofinterestmanager = ((ServerWorld) entityIn.world)
                        .getPointOfInterestManager();
                final Stream<PointOfInterest> stream = pointofinterestmanager.func_219146_b((type) ->
                {
                    return type == POI;
                }, blockpos, 20, PointOfInterestManager.Status.ANY);
                return stream.map(PointOfInterest::getPos).filter((pos) ->
                {
                    return HiveSensor.doesHiveHaveSpace(entityIn, pos);
                }).sorted(Comparator.comparingDouble((pos) ->
                {
                    return pos.distanceSq(blockpos);
                })).collect(Collectors.toList());
            };
            HiveSensor.hiveLocators.add(apiaryLocator);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("ERROR WITH RESOURCEFULBEES SUPPORT!");
            PokecubeCore.LOGGER.error(e);
        }

    }

}
