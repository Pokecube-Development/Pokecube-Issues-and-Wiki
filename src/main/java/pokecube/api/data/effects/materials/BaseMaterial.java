package pokecube.api.data.effects.materials;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.effects.actions.BaseAction;

public interface BaseMaterial extends BaseAction
{
    default void init(LivingEntity mob)
    {}
}
