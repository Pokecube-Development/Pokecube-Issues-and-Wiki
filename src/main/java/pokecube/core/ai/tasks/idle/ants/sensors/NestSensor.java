package pokecube.core.ai.tasks.idle.ants.sensors;

import java.util.Optional;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.village.PointOfInterestManager.Status;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.poi.PointsOfInterest;
import pokecube.core.ai.tasks.idle.ants.AntTasks;
import pokecube.core.blocks.nests.NestTile;

public class NestSensor extends Sensor<MobEntity>
{
    private static final Set<MemoryModuleType<?>> MEMS = ImmutableSet.of(AntTasks.NEST_POS, AntTasks.NO_HIVE_TIMER);

    @Override
    protected void update(final ServerWorld worldIn, final MobEntity entityIn)
    {
        final Brain<?> brain = entityIn.getBrain();
        if (brain.hasMemory(AntTasks.NEST_POS)) return;

        final PointOfInterestManager pois = worldIn.getPointOfInterestManager();
        final BlockPos pos = entityIn.getPosition();
        final Random rand = new Random();
        final Optional<BlockPos> opt = pois.getRandom(p -> p == PointsOfInterest.NEST.get(), p -> this.validNest(p,
                worldIn, entityIn), Status.ANY, pos, 16, rand);
        if (opt.isPresent())
        {
            // Randomize this so we don't always pick the same hive if it was
            // cleared for some reason
            brain.removeMemory(AntTasks.NO_HIVE_TIMER);
            brain.setMemory(AntTasks.NEST_POS, GlobalPos.getPosition(entityIn.getEntityWorld().getDimensionKey(), opt
                    .get()));
        }
        else
        {
            int timer = 0;
            if (brain.hasMemory(AntTasks.NO_HIVE_TIMER)) timer = brain.getMemory(AntTasks.NO_HIVE_TIMER).get();
            brain.setMemory(AntTasks.NO_HIVE_TIMER, timer + 1);
        }
    }

    private boolean validNest(final BlockPos p, final ServerWorld worldIn, final MobEntity entityIn)
    {
        final TileEntity tile = worldIn.getTileEntity(p);
        if (!(tile instanceof NestTile)) return false;
        final NestTile nest = (NestTile) tile;
        return nest.tag.getBoolean("_is_ant_nest_");
    }

    @Override
    public Set<MemoryModuleType<?>> getUsedMemories()
    {
        return NestSensor.MEMS;
    }

}
