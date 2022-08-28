package pokecube.api.data.json.entity.consumers;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.json.common.BaseConsumer;

public class SetHealth extends BaseConsumer<LivingEntity>
{
    float amt;
    boolean fraction = false;

    @Override
    public void accept(LivingEntity t)
    {
        if (fraction)
        {
            t.setHealth(t.getHealth() * amt);
        }
        else
        {
            t.setHealth(amt);
        }
    }
}
