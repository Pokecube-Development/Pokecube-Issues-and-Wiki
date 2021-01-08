package pokecube.core.ai.tasks.idle.bees.sensors;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

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

    private List<BlockPos> getNearbyFreeHives(final LivingEntity entityIn)
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
    }

    public static boolean doesHiveHaveSpace(final LivingEntity entityIn, final BlockPos pos)
    {
        final TileEntity tileentity = entityIn.world.getTileEntity(pos);
        if (tileentity instanceof BeehiveTileEntity) return !((BeehiveTileEntity) tileentity).isFullOfBees();
        else return false;
    }

    @Override
    protected void update(final ServerWorld worldIn, final LivingEntity entityIn)
    {
        final Brain<?> brain = entityIn.getBrain();
        if (brain.hasMemory(BeeTasks.HIVE_POS)) return;
        final List<BlockPos> hives = this.getNearbyFreeHives(entityIn);
        if (!hives.isEmpty())
        {
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
