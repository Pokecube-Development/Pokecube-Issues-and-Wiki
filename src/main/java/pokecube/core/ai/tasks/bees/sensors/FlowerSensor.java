package pokecube.core.ai.tasks.bees.sensors;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.tasks.bees.BeeTasks;

public class FlowerSensor extends Sensor<LivingEntity>
{
    private static final Set<MemoryModuleType<?>> MEMS = ImmutableSet.of(BeeTasks.FLOWER_POS,
            MemoryModules.VISIBLE_BLOCKS, BeeTasks.NO_FLOWER_TIME);

    public static final Predicate<BlockState> flowerPredicate = (state) ->
    {
        if (state.is(BlockTags.BEE_GROWABLES)) return true;
        if (state.is(BlockTags.TALL_FLOWERS))
        {
            if (state.is(Blocks.SUNFLOWER)) return state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER;
            else return true;
        }
        else return state.is(BlockTags.SMALL_FLOWERS);
    };

    private final float flowerSenseChance = 0.5f;

    @Override
    protected void doTick(final ServerWorld worldIn, final LivingEntity entityIn)
    {
        final Brain<?> brain = entityIn.getBrain();
        if (brain.hasMemoryValue(BeeTasks.FLOWER_POS)) return;
        final List<NearBlock> blocks = BrainUtils.getNearBlocks(entityIn);
        if (blocks == null) return;
        for (final NearBlock b : blocks)
            if (FlowerSensor.flowerPredicate.test(b.getState()) && entityIn.getRandom()
                    .nextFloat() < this.flowerSenseChance)
            {
                brain.eraseMemory(BeeTasks.NO_FLOWER_TIME);
                brain.setMemory(BeeTasks.FLOWER_POS, GlobalPos.of(entityIn.getCommandSenderWorld().dimension(),
                        b.getPos()));
                return;
            }
        // we returned earlier if we had found a flower, so here we increment
        // the timer since last flower found.
        int timer = 0;
        if (brain.hasMemoryValue(BeeTasks.NO_FLOWER_TIME)) timer = brain.getMemory(BeeTasks.NO_FLOWER_TIME).get();
        brain.setMemory(BeeTasks.NO_FLOWER_TIME, timer + 1);
    }

    @Override
    public Set<MemoryModuleType<?>> requires()
    {
        return FlowerSensor.MEMS;
    }

}
