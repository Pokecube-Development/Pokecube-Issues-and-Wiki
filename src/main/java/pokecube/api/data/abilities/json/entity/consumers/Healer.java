package pokecube.api.data.abilities.json.entity.consumers;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.abilities.json.common.BaseConsumer;

public class Healer extends BaseConsumer<LivingEntity>
{
    float amt;
    boolean fraction = false;

    @Override
    public void accept(LivingEntity t)
    {
        if (fraction)
        {
            t.setHealth(t.getHealth() + t.getHealth() * amt);
        }
        else
        {
            t.setHealth(t.getHealth() + amt);
        }
    }

}
