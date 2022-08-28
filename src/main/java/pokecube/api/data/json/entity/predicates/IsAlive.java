package pokecube.api.data.json.entity.predicates;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.json.common.BasePredicate;
import pokecube.api.utils.PokeType;

public class IsAlive extends BasePredicate<LivingEntity>
{
    String type;
    PokeType _type = null;

    @Override
    public void init()
    {
        _type = PokeType.getType(type);
    }

    @Override
    public boolean test(LivingEntity t)
    {
        return t.isAlive();
    }
}
