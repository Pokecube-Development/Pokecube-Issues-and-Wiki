package thut.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.WalkAnimationState;
import thut.api.entity.ICopyMob;

@Mixin(WalkAnimationState.class)
public abstract class WalkAniAccessor implements ICopyMob.WalkAccess
{
    @Accessor("speedOld")
    @Mutable
    public abstract void copyCap$setSpeedOld(float speedOld);

    @Accessor("speedOld")
    public abstract float copyCap$speedOld();

    @Accessor("position")
    @Mutable
    public abstract void copyCap$setPosition(float position);

    @Accessor("speed")
    @Mutable
    public abstract void copyCap$setSpeed(float maxValue);
}
