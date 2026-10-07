package pokecube.core.utils.mixin;

import org.spongepowered.asm.mixin.Mutable;

public interface IAttributeMaxAccessor
{
    @Mutable
    default void setMaxValue(double maxValue) {}

    default double maxValue() {return 0;}
}
