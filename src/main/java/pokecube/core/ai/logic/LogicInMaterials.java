package pokecube.core.ai.logic;

import net.minecraft.world.level.Level;
import pokecube.api.entity.pokemob.IPokemob;
import thut.api.maths.Vector3;

/**
 * Manages interactions with materials for the pokemob. This is what is used to
 * make some mobs despawn in high light, or take damage from certain materials.
 */
public class LogicInMaterials extends LogicBase
{
    Vector3 v = new Vector3();

    public LogicInMaterials(final IPokemob entity)
    {
        super(entity);
    }

    @Override
    public void tick(final Level world)
    {
        super.tick(world);
        if (this.entity.tickCount % 20 == 0)
            this.pokemob.getPokedexEntry().materialActions.forEach(a -> a.applyEffect(entity));
    }
}
