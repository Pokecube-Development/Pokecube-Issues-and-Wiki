package pokecube.core.ai.tasks.bees.sensors;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import pokecube.api.blocks.IInhabitable;
import pokecube.api.entity.CapabilityInhabitable;
import pokecube.core.ai.tasks.bees.BeeTasks;

public class HiveSensor extends Sensor<Mob>
{
    public static interface IHiveEnterer
    {
        boolean addBee(Mob entityIn, BlockEntity hive);
    }

    public static interface IHiveSpaceCheck
    {
        boolean canAddBee(Mob entityIn, BlockEntity hive);
    }

    public static interface IHiveLocator
    {
        List<BlockPos> getHives(Mob entityIn);
    }

    public static List<IHiveEnterer> hiveEnterers = Lists.newArrayList();

    public static List<IHiveSpaceCheck> hiveSpaceCheckers = Lists.newArrayList();

    public static List<IHiveLocator> hiveLocators = Lists.newArrayList();

    static
    {
        final IHiveEnterer vanillaHives = (entityIn, tile) -> {
            if (!(tile instanceof BeehiveBlockEntity)) return false;
            final IInhabitable habitat = tile.getCapability(CapabilityInhabitable.CAPABILITY).orElse(null);
            return habitat != null && habitat.onEnterHabitat(entityIn);
        };
        HiveSensor.hiveEnterers.add(vanillaHives);

        final IHiveSpaceCheck vanillaCheck = (entityIn, tile) -> {
            if (!(tile instanceof BeehiveBlockEntity)) return false;
            final IInhabitable habitat = tile.getCapability(CapabilityInhabitable.CAPABILITY).orElse(null);
            return habitat != null && habitat.canEnterHabitat(entityIn);
        };
        HiveSensor.hiveSpaceCheckers.add(vanillaCheck);
        final IHiveLocator vanillaLocator = (entityIn) -> {
            final BlockPos blockpos = entityIn.blockPosition();
            final PoiManager pointofinterestmanager = ((ServerLevel) entityIn.level).getPoiManager();
            final Stream<PoiRecord> stream = pointofinterestmanager.getInRange((type) -> {
                return type == PoiType.BEEHIVE || type == PoiType.BEE_NEST;
            }, blockpos, 20, PoiManager.Occupancy.ANY);
            return stream.map(PoiRecord::getPos).filter((pos) -> {
                return HiveSensor.doesHiveHaveSpace(entityIn, pos);
            }).sorted(Comparator.comparingDouble((pos) -> {
                return pos.distSqr(blockpos);
            })).collect(Collectors.toList());
        };
        HiveSensor.hiveLocators.add(vanillaLocator);
    }

    private static List<BlockPos> getNearbyFreeHives(final Mob entityIn)
    {
        final List<BlockPos> hives = Lists.newArrayList();
        final BlockPos blockpos = entityIn.blockPosition();
        HiveSensor.hiveLocators.forEach(l -> hives.addAll(l.getHives(entityIn)));
        hives.sort(Comparator.comparingDouble((pos) -> {
            return pos.distSqr(blockpos);
        }));
        return hives;
    }

    public static boolean doesHiveHaveSpace(final Mob entityIn, final BlockPos pos)
    {
        final BlockEntity tile = entityIn.getLevel().getBlockEntity(pos);
        if (tile != null) for (final IHiveSpaceCheck checker : HiveSensor.hiveSpaceCheckers)
            if (checker.canAddBee(entityIn, tile)) return true;
        return false;
    }

    public static boolean tryAddToBeeHive(final Mob entityIn, final BlockPos hive)
    {
        final BlockEntity tile = entityIn.getLevel().getBlockEntity(hive);
        if (tile != null)
            for (final IHiveEnterer checker : HiveSensor.hiveEnterers) if (checker.addBee(entityIn, tile)) return true;
        return false;
    }

    @Override
    protected void doTick(final ServerLevel worldIn, final Mob entityIn)
    {
        final Brain<?> brain = entityIn.getBrain();
        if (brain.hasMemoryValue(BeeTasks.HIVE_POS.get())) return;
        final List<BlockPos> hives = HiveSensor.getNearbyFreeHives(entityIn);
        Collections.shuffle(hives);
        if (!hives.isEmpty())
        {
            // Randomize this so we don't always pick the same hive if it was
            // cleared for some reason
            brain.eraseMemory(BeeTasks.NO_HIVE_TIMER.get());
            brain.setMemory(BeeTasks.HIVE_POS.get(), GlobalPos.of(entityIn.getLevel().dimension(), hives.get(0)));
        }
        else
        {
            int timer = 0;
            if (brain.hasMemoryValue(BeeTasks.NO_HIVE_TIMER.get()))
                timer = brain.getMemory(BeeTasks.NO_HIVE_TIMER.get()).get();
            brain.setMemory(BeeTasks.NO_HIVE_TIMER.get(), timer + 1);
        }
    }

    @Override
    public Set<MemoryModuleType<?>> requires()
    {
        return Set.of(BeeTasks.HIVE_POS.get(), BeeTasks.NO_HIVE_TIMER.get());
    }

}
