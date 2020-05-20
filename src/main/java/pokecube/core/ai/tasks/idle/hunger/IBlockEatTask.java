package pokecube.core.ai.tasks.idle.hunger;

import java.util.List;

import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.interfaces.IPokemob;

public interface IBlockEatTask
{
    default EatResult tryEat(final IPokemob pokemob, final List<NearBlock> blocks)
    {
        if (blocks != null) for (final NearBlock b : blocks)
            if (this.isValid(b))
            {
                final EatResult result = this.eat(pokemob, b);
                if (result == EatResult.NOEAT) continue;
                else return result;
            }
        return EatResult.NOEAT;
    }

    EatResult eat(IPokemob pokemob, NearBlock block);

    boolean isValid(NearBlock block);
}
