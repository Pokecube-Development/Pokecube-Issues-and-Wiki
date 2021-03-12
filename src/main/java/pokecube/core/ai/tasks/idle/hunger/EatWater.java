package pokecube.core.ai.tasks.idle.hunger;

import java.util.function.Predicate;

import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.fluid.WaterFluid;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.interfaces.IPokemob;

public class EatWater extends EatBlockBase
{
    public static final ResourceLocation FOODTAG = new ResourceLocation(PokecubeCore.MODID, "pokemob_redstone_food");

    private static final Predicate<BlockState> checker = (b2) -> b2.getFluidState().getType() instanceof WaterFluid;

    @Override
    public EatResult eat(final IPokemob pokemob, final NearBlock block)
    {
        if (!pokemob.filterFeeder()) return EatResult.NOEAT;

        final MobEntity entity = pokemob.getEntity();
        double diff = 1.5;
        diff = Math.max(diff, entity.getBbWidth());
        final double dist = block.getPos().distManhattan(entity.blockPosition());
        this.setWalkTo(entity, block.getPos(), 1, 0);
        if (dist > diff) return EatResult.PATHING;

        final ServerWorld world = (ServerWorld) entity.getCommandSenderWorld();
        final BlockState current = world.getBlockState(block.getPos());

        if (!EatWater.checker.test(current)) return EatResult.NOEAT;

        pokemob.applyHunger(-PokecubeCore.getConfig().pokemobLifeSpan / 4);

        return EatResult.EATEN;
    }

    @Override
    public boolean isValid(final NearBlock block)
    {
        return EatWater.checker.test(block.getState());
    }

}