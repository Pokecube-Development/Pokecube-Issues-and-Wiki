package pokecube.core.ai.logic;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import pokecube.api.entity.pokemob.IPokemob;

/**
 * Manages interactions with materials for the pokemob. This is what is used to make some mobs despawn in high light, or
 * take damage from certain materials.
 */
public class LogicInMaterials extends LogicBase
{
    public LogicInMaterials(final IPokemob entity)
    {
        super(entity);
    }

    @Override
    public void tick(final Level world)
    {
        super.tick(world);
        if (world instanceof ServerLevel && this.entity.tickCount % 20 == 0)
            this.pokemob.getPokedexEntry().materialActions.forEach(a -> a.applyEffect(entity));
    }
}
