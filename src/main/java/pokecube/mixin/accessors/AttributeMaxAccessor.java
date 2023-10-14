package pokecube.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;

@Mixin(RangedAttribute.class)
public interface AttributeMaxAccessor
{
    @Accessor("maxValue")
    @Mutable
    void setMaxValue(double maxValue);

    @Accessor("maxValue")
    double maxValue();
}
