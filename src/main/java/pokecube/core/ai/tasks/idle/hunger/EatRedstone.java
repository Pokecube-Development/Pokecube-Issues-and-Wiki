package pokecube.core.ai.tasks.idle.hunger;

import java.util.function.Predicate;

import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
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

        final MobEntity entity = pokemob.getEntity();
        double diff = 3;
        diff = Math.max(diff, entity.getWidth());
        final double dist = block.getPos().manhattanDistance(entity.getPosition());
        this.setWalkTo(entity, block.getPos(), 1, 0);
        if (dist > diff) return EatResult.PATHING;

        final ServerWorld world = (ServerWorld) entity.getEntityWorld();
        final BlockState current = world.getBlockState(block.getPos());
        if (!EatRedstone.checker.test(current)) return EatResult.NOEAT;

        pokemob.setHungerTime(pokemob.getHungerTime() - PokecubeCore.getConfig().pokemobLifeSpan / 4);

        return EatResult.EATEN;
    }

    @Override
    public boolean isValid(final NearBlock block)
    {
        return EatRedstone.checker.test(block.getState());
    }

}