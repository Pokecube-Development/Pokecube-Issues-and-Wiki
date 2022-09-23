package pokecube.mixin.features;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.levelgen.SurfaceSystem;

@Mixin(SurfaceSystem.class)
public interface SurfaceSystemInvoker
{
    @Invoker("erodedBadlandsExtension")
    void invokeErodedBadlandsExtension(BlockColumn p_189955_, int p_189956_, int p_189957_, int p_189958_,
            LevelHeightAccessor p_189959_);
}
