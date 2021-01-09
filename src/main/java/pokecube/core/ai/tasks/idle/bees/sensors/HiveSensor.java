package pokecube.core.ai.tasks.idle.bees.sensors;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.PointOfInterest;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.tasks.idle.bees.BeeTasks;

public class HiveSensor extends Sensor<LivingEntity>
{
    private static final Set<MemoryModuleType<?>> MEMS = ImmutableSet.of(BeeTasks.HIVE_POS, BeeTasks.NO_HIVE_TIMER);

    public static interface IHiveEnterer
    {
        boolean addBee(LivingEntity entityIn, TileEntity hive);
    }

    public static interface IHiveSpaceCheck
    {
        boolean canAddBee(LivingEntity entityIn, TileEntity hive);
    }

    public static interface IHiveLocator
    {
        List<BlockPos> getHives(LivingEntity entityIn);
    }

    public static List<IHiveEnterer> hiveEnterers = Lists.newArrayList();

    public static List<IHiveSpaceCheck> hiveSpaceCheckers = Lists.newArrayList();

    public static List<IHiveLocator> hiveLocators = Lists.newArrayList();

    static
    {
        final IHiveEnterer vanillaHives = (entityIn, tile) ->
        {
            if (!(tile instanceof BeehiveTileEntity)) return false;
            final BeehiveTileEntity hive = (BeehiveTileEntity) tile;
            final int num = hive.bees.size();
            final Brain<?> brain = entityIn.getBrain();
            final Optional<Boolean> hasNectar = brain.getMemory(BeeTasks.HAS_NECTAR);
            final boolean nectar = hasNectar.isPresent() && hasNectar.get();
            // Try to enter the hive
            hive.tryEnterHive(entityIn, nectar);
            // If this changed, then we added correctly.
            final boolean added = num < hive.bees.size();
            // BeehiveTileEntity checks this boolean directly for if
            // there is nectar in the bee.
            if (added) hive.bees.get(num).entityData.putBoolean("HasNectar", nectar);
            return added;
        };
        HiveSensor.hiveEnterers.add(vanillaHives);

        final IHiveSpaceCheck vanillaCheck = (entityIn, tile) ->
        {
            if (!(tile instanceof BeehiveTileEntity)) return false;
            final BeehiveTileEntity hive = (BeehiveTileEntity) tile;
            return !hive.isFullOfBees();
        };
        HiveSensor.hiveSpaceCheckers.add(vanillaCheck);

        final IHiveLocator vanillaLocator = (entityIn) ->
        {
            final BlockPos blockpos = entityIn.getPosition();
            final PointOfInterestManager pointofinterestmanager = ((ServerWorld) entityIn.world)
                    .getPointOfInterestManager();
            final Stream<PointOfInterest> stream = pointofinterestmanager.func_219146_b((type) ->
            {
                return type == PointOfInterestType.BEEHIVE || type == PointOfInterestType.BEE_NEST;
            }, blockpos, 20, PointOfInterestManager.Status.ANY);
            return stream.map(PointOfInterest::getPos).filter((pos) ->
            {
                return HiveSensor.doesHiveHaveSpace(entityIn, pos);
            }).sorted(Comparator.comparingDouble((pos) ->
            {
                return pos.distanceSq(blockpos);
            })).collect(Collectors.toList());
        };
        HiveSensor.hiveLocators.add(vanillaLocator);
    }

    private static List<BlockPos> getNearbyFreeHives(final LivingEntity entityIn)
    {
        final List<BlockPos> hives = Lists.newArrayList();
        final BlockPos blockpos = entityIn.getPosition();
        HiveSensor.hiveLocators.forEach(l -> hives.addAll(l.getHives(entityIn)));
        hives.sort(Comparator.comparingDouble((pos) ->
        {
            return pos.distanceSq(blockpos);
        }));
        return hives;
    }

    public static boolean doesHiveHaveSpace(final LivingEntity entityIn, final BlockPos pos)
    {
        final TileEntity tile = entityIn.getEntityWorld().getTileEntity(pos);
        if (tile != null) for (final IHiveSpaceCheck checker : HiveSensor.hiveSpaceCheckers)
            if (checker.canAddBee(entityIn, tile)) return true;
        return false;
    }

    public static boolean tryAddToBeeHive(final LivingEntity entityIn, final BlockPos hive)
    {
        final TileEntity tile = entityIn.getEntityWorld().getTileEntity(hive);
        if (tile != null) for (final IHiveEnterer checker : HiveSensor.hiveEnterers)
            if (checker.addBee(entityIn, tile)) return true;
        return false;
    }

    @Override
    protected void update(final ServerWorld worldIn, final LivingEntity entityIn)
    {
        final Brain<?> brain = entityIn.getBrain();
        if (brain.hasMemory(BeeTasks.HIVE_POS)) return;
        final List<BlockPos> hives = HiveSensor.getNearbyFreeHives(entityIn);
        Collections.shuffle(hives);
        if (!hives.isEmpty())
        {
            // Randomize this so we don't always pick the same hive if it was
            // cleared for some reason
            brain.removeMemory(BeeTasks.NO_HIVE_TIMER);
            brain.setMemory(BeeTasks.HIVE_POS, GlobalPos.getPosition(entityIn.getEntityWorld().getDimensionKey(), hives
                    .get(0)));
        }
        else
        {
            int timer = 0;
            if (brain.hasMemory(BeeTasks.NO_HIVE_TIMER)) timer = brain.getMemory(BeeTasks.NO_HIVE_TIMER).get();
            brain.setMemory(BeeTasks.NO_HIVE_TIMER, timer + 1);
        }
    }

    @Override
    public Set<MemoryModuleType<?>> getUsedMemories()
    {
        return HiveSensor.MEMS;
    }

}
