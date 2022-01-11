package pokecube.core.ai.tasks.idle.hunger;

import java.util.function.Predicate;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.interfaces.IPokemob;
import thut.api.item.ItemList;

public class EatRedstone extends EatBlockBase
{
    public static final ResourceLocation FOODTAG = new ResourceLocation(PokecubeCore.MODID, "pokemob_redstone_food");

    private static final Predicate<BlockState> checker = (b2) -> ItemList.is(EatRedstone.FOODTAG, b2);

    @Override
    public EatResult eat(final IPokemob pokemob, final NearBlock block)
    {
        if (!pokemob.isElectrotroph()) return EatResult.NOEAT;

        final Mob entity = pokemob.getEntity();
        double diff = 3;
        diff = Math.max(diff, entity.getBbWidth());
        final double dist = block.getPos().distManhattan(entity.blockPosition());
        this.setWalkTo(entity, block.getPos(), 1, 0);
        if (dist > diff) return EatResult.PATHING;

        final ServerLevel world = (ServerLevel) entity.getLevel();
        final BlockState current = world.getBlockState(block.getPos());
        if (!EatRedstone.checker.test(current)) return EatResult.NOEAT;

        pokemob.applyHunger(-PokecubeCore.getConfig().pokemobLifeSpan / 4);

        return EatResult.EATEN;
    }

    @Override
    public boolean isValid(final NearBlock block)
    {
        return EatRedstone.checker.test(block.getState());
    }

}