package pokecube.core.moves.damage;

import net.minecraft.entity.Entity;
import net.minecraft.util.EntityDamageSource;

public class GenericDamageSource extends EntityDamageSource implements IPokedamage
{

    public GenericDamageSource(final String damageTypeIn, final Entity damageSourceEntityIn)
    {
        super(damageTypeIn, damageSourceEntityIn);
    }

}
