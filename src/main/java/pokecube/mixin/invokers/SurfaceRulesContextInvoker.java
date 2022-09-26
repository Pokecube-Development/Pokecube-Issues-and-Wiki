package pokecube.mixin.invokers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.level.levelgen.SurfaceRules;

@Mixin(SurfaceRules.Context.class)
public interface SurfaceRulesContextInvoker
{
    @Invoker("getMinSurfaceLevel")
    int invokeGetMinSurfaceLevel();
}
