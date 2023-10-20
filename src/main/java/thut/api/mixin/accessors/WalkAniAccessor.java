package thut.api.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.WalkAnimationState;

@Mixin(WalkAnimationState.class)
public interface WalkAniAccessor
{
    @Accessor("speedOld")
    @Mutable
    void copyCap$setSpeedOld(float maxValue);

    @Accessor("speedOld")
    float copyCap$speedOld();

    @Accessor("position")
    @Mutable
    void copyCap$setPosition(float maxValue);

    @Accessor("position")
    float copyCap$position();

    @Accessor("speed")
    @Mutable
    void copyCap$setSpeed(float maxValue);

    @Accessor("speed")
    float copyCap$speed();
}
