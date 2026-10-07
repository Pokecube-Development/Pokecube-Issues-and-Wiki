package pokecube.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import pokecube.core.utils.mixin.IAttributeMaxAccessor;

@Mixin(RangedAttribute.class)
public interface AttributeMaxAccessor extends IAttributeMaxAccessor
{
    @Accessor("maxValue")
    @Mutable
    void setMaxValue(double maxValue);

    @Accessor("maxValue")
    double maxValue();
}
