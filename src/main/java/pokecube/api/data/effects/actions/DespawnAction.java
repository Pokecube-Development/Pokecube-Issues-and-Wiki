package pokecube.api.data.effects.actions;

import net.minecraft.world.entity.LivingEntity;

public class DespawnAction implements BaseAction
{
    @Override
    public void applyEffect(LivingEntity mob)
    {
        mob.discard();
    }

    @Override
    public void init()
    {}
}
