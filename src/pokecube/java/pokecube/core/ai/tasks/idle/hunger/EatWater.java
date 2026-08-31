package pokecube.core.ai.tasks.idle.hunger;

import java.util.function.Predicate;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.WaterFluid;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;

public class EatWater extends EatBlockBase
{
    private static final Predicate<BlockState> checker = (b2) -> b2.getFluidState().getType() instanceof WaterFluid;

    @Override
    public EatResult eat(final IPokemob pokemob, final NearBlock block)
    {
        if (!pokemob.filterFeeder()) return EatResult.NOEAT;

        final Mob entity = pokemob.getEntity();
        double diff = 1.5;
        diff = Math.max(diff, entity.getBbWidth());
        final double dist = block.pos().distManhattan(entity.blockPosition());
        this.setWalkTo(entity, block.pos(), 1, 0);
        if (dist > diff) return EatResult.PATHING;

        final ServerLevel world = (ServerLevel) entity.level();
        final BlockState current = world.getBlockState(block.pos());

        if (!EatWater.checker.test(current)) return EatResult.NOEAT;

        pokemob.eat(EatWater.class);// Set indicator of having eaten something, this fires the event

        return EatResult.EATEN;
    }

    @Override
    public boolean isValid(final NearBlock block)
    {
        return EatWater.checker.test(block.state());
    }

}