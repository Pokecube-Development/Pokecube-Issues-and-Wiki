package pokecube.legends.conditions;

import net.minecraft.world.entity.Entity;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;

public class OrCondition extends AbstractCondition
{
    AbstractCondition A;
    AbstractCondition B;

    public OrCondition(AbstractCondition A, AbstractCondition B)
    {
        this.A = A;
        this.B = B;
        if (A.getEntry() != B.getEntry())
        {
            PokecubeAPI.LOGGER.error("Warning, two conditions handed in with different entries to an OR condition!");
        }
    }

    @Override
    public final PokedexEntry getEntry()
    {
        return A.getEntry();
    }

    @Override
    protected boolean hasRequirements(final Entity trainer)
    {
        return A.hasRequirements(trainer) || B.hasRequirements(trainer);
    }
}
