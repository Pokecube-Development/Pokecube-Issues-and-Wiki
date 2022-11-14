package pokecube.api.data.effects.materials;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries.Keys;

public class Fluid extends BaseMaterialAction
{
    public String fluid;

    public TagKey<net.minecraft.world.level.material.Fluid> _tag = null;

    public Fluid()
    {}

    @Override
    public void init()
    {
        super.init();
        _tag = TagKey.create(Keys.FLUIDS, new ResourceLocation(fluid));
    }

    @Override
    public boolean shouldApply(LivingEntity mob)
    {
        return mob.getFluidHeight(_tag) > 0;
    }
}
