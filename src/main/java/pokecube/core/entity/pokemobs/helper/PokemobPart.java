package pokecube.core.entity.pokemobs.helper;

import pokecube.core.interfaces.IPokemob;
import thut.api.entity.GenericPartEntity;

public class PokemobPart extends GenericPartEntity<PokemobHasParts>
{
    public final IPokemob pokemob;

    public PokemobPart(final PokemobHasParts base, final float width, final float height, final float x, final float y,
            final float z, final String id)
    {
        super(base, width, height, x, y, z, id);
        this.pokemob = base.pokemobCap;
    }
}
