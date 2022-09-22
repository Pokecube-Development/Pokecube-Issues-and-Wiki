package pokecube.mixin;

import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AtomicDouble;

import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries.Keys;

@Mixin(Entity.class)
public class MixinEntityFluidFix
{
    Map<TagKey<Fluid>, Set<FluidType>> revMap = Maps.newHashMap();

    private Entity _self()
    {
        return (Entity) ((Object) this);
    }

    @Inject(method = "updateFluidHeightAndDoFluidPushing", at = @At("HEAD"), cancellable = true)
    public void fixForgeBrokenFluidTagChecks1(TagKey<Fluid> tag, double depth, CallbackInfoReturnable<Boolean> info)
    {
        Entity us = _self();
        Set<FluidType> types = revMap.computeIfAbsent(tag, t -> {
            var reg = us.getLevel().registryAccess().registryOrThrow(Keys.FLUIDS);
            var tagged = reg.getTagOrEmpty(tag);
            Set<FluidType> ret = Sets.newHashSet();
            tagged.forEach(h -> ret.add(h.get().getFluidType()));
            return ret;
        });
        // This does this pushing first, then returns true.
        us.updateFluidHeightAndDoFluidPushing();
        boolean valid = false;
        for (FluidType type : types)
        {
            valid = us.getFluidTypeHeight(type) > 0;
            if (valid) break;
        }
        if (valid) info.setReturnValue(valid);
    }

    @Inject(method = "getFluidHeight", at = @At("HEAD"), cancellable = true)
    public void fixForgeBrokenFluidTagChecks2(TagKey<Fluid> tag, CallbackInfoReturnable<Double> info)
    {
        Entity us = _self();
        Set<FluidType> types = revMap.computeIfAbsent(tag, t -> {
            var reg = us.getLevel().registryAccess().registryOrThrow(Keys.FLUIDS);
            var tagged = reg.getTagOrEmpty(tag);
            Set<FluidType> ret = Sets.newHashSet();
            tagged.forEach(h -> ret.add(h.get().getFluidType()));
            return ret;
        });
        AtomicDouble d = new AtomicDouble(0);
        types.forEach(y -> d.addAndGet(us.getFluidTypeHeight(y)));
        if (d.get() > 0) info.setReturnValue(d.get());
    }

    @Inject(method = "isEyeInFluid", at = @At("HEAD"), cancellable = true)
    public void fixForgeBrokenFluidTagChecks3(TagKey<Fluid> tag, CallbackInfoReturnable<Boolean> info)
    {
        Entity us = _self();
        Set<FluidType> types = revMap.computeIfAbsent(tag, t -> {
            var reg = us.getLevel().registryAccess().registryOrThrow(Keys.FLUIDS);
            var tagged = reg.getTagOrEmpty(tag);
            Set<FluidType> ret = Sets.newHashSet();
            tagged.forEach(h -> ret.add(h.get().getFluidType()));
            return ret;
        });
        boolean valid = false;
        for (FluidType type : types)
        {
            valid = us.getEyeInFluidType() == type;
            if (valid) break;
        }
        if (valid) info.setReturnValue(valid);
    }

    @Inject(method = "isInLava", at = @At("HEAD"), cancellable = true)
    public void fixForgeBrokenFluidTagChecks4(CallbackInfoReturnable<Boolean> info)
    {
        Entity us = _self();
        if (us.firstTick) return;
        TagKey<Fluid> tag = FluidTags.LAVA;
        Set<FluidType> types = revMap.computeIfAbsent(tag, t -> {
            var reg = us.getLevel().registryAccess().registryOrThrow(Keys.FLUIDS);
            var tagged = reg.getTagOrEmpty(tag);
            Set<FluidType> ret = Sets.newHashSet();
            tagged.forEach(h -> ret.add(h.get().getFluidType()));
            return ret;
        });
        AtomicDouble d = new AtomicDouble(0);
        types.forEach(y -> d.addAndGet(us.getFluidTypeHeight(y)));
        if (d.get() > 0) info.setReturnValue(true);
    }
}
