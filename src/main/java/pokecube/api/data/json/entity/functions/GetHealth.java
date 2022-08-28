package pokecube.api.data.json.entity.functions;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.json.common.BaseFunction;

public class GetHealth extends BaseFunction<LivingEntity, Float>
{
    @Override
    public Float apply(LivingEntity t)
    {
        return t.getHealth();
    }
}
