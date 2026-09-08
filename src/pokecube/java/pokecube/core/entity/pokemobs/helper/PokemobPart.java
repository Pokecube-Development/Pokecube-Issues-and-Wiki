package pokecube.core.entity.pokemobs.helper;

import pokecube.api.entity.pokemob.IPokemob;
import thut.api.entity.multipart.BBPartEntity;
import thut.core.client.render.bbmodel.BBModel;
import thut.core.client.render.model.parts.Part;

public class PokemobPart extends BBPartEntity<PokemobHasParts>
{
    public final IPokemob pokemob;

    public PokemobPart(final PokemobHasParts base, Part part, BBModel model)
    {
        super(base, part, model);
        this.pokemob = base.getPokemob();
    }
}
