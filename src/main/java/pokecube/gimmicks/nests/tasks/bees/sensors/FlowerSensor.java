package pokecube.gimmicks.nests.tasks.bees.sensors;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.brain.MemoryModules;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.gimmicks.nests.tasks.bees.BeeTasks;

public class FlowerSensor extends Sensor<LivingEntity>
{
    public static final Predicate<BlockState> flowerPredicate = (state) -> {
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
    protected void doTick(final ServerLevel worldIn, final LivingEntity entityIn)
    {
        final Brain<?> brain = entityIn.getBrain();
        if (brain.hasMemoryValue(BeeTasks.FLOWER_POS.get())) return;
        final List<NearBlock> blocks = BrainUtils.getNearBlocks(entityIn);
        if (blocks == null) return;
        for (final NearBlock b : blocks) if (FlowerSensor.flowerPredicate.test(b.getState())
                && entityIn.getRandom().nextFloat() < this.flowerSenseChance)
        {
            brain.eraseMemory(BeeTasks.NO_FLOWER_TIME.get());
            brain.setMemory(BeeTasks.FLOWER_POS.get(), GlobalPos.of(entityIn.getLevel().dimension(), b.getPos()));
            return;
        }
        // we returned earlier if we had found a flower, so here we increment
        // the timer since last flower found.
        int timer = 0;
        if (brain.hasMemoryValue(BeeTasks.NO_FLOWER_TIME.get()))
            timer = brain.getMemory(BeeTasks.NO_FLOWER_TIME.get()).get();
        brain.setMemory(BeeTasks.NO_FLOWER_TIME.get(), timer + 1);
    }

    @Override
    public Set<MemoryModuleType<?>> requires()
    {
        return Set.of(BeeTasks.FLOWER_POS.get(), MemoryModules.VISIBLE_BLOCKS.get(), BeeTasks.NO_FLOWER_TIME.get());
    }

}
