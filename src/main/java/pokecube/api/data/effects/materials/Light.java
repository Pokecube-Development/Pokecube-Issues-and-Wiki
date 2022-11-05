package pokecube.api.data.effects.materials;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class Light extends BaseMaterialAction
{
    float max_light = 0.5f;

    public Light()
    {}

    @Override
    public boolean shouldApply(LivingEntity mob)
    {
        if (!(mob.getLevel() instanceof ServerLevel level)) return false;
        if (!level.isDay()) return false;
        float br = level.getLightEngine().getRawBrightness(mob.getOnPos(), 0) / 15f;
        return br > max_light;
    }
}
