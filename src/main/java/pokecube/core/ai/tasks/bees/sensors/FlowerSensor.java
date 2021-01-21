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
        if (state.isIn(BlockTags.BEE_GROWABLES)) return true;
        if (state.isIn(BlockTags.TALL_FLOWERS))
        {
            if (state.isIn(Blocks.SUNFLOWER)) return state.get(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER;
            else return true;
        }
        else return state.isIn(BlockTags.SMALL_FLOWERS);
    };

    private final float flowerSenseChance = 0.5f;

    @Override
    protected void update(final ServerWorld worldIn, final LivingEntity entityIn)
    {
        final Brain<?> brain = entityIn.getBrain();
        if (brain.hasMemory(BeeTasks.FLOWER_POS)) return;
        final List<NearBlock> blocks = BrainUtils.getNearBlocks(entityIn);
        if (blocks == null) return;
        for (final NearBlock b : blocks)
            if (FlowerSensor.flowerPredicate.test(b.getState()) && entityIn.getRNG()
                    .nextFloat() < this.flowerSenseChance)
            {
                brain.removeMemory(BeeTasks.NO_FLOWER_TIME);
                brain.setMemory(BeeTasks.FLOWER_POS, GlobalPos.getPosition(entityIn.getEntityWorld().getDimensionKey(),
                        b.getPos()));
                return;
            }
        // we returned earlier if we had found a flower, so here we increment
        // the timer since last flower found.
        int timer = 0;
        if (brain.hasMemory(BeeTasks.NO_FLOWER_TIME)) timer = brain.getMemory(BeeTasks.NO_FLOWER_TIME).get();
        brain.setMemory(BeeTasks.NO_FLOWER_TIME, timer + 1);
    }

    @Override
    public Set<MemoryModuleType<?>> getUsedMemories()
    {
        return FlowerSensor.MEMS;
    }

}
