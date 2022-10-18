package pokecube.mixin.invokers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.levelgen.SurfaceSystem;

@Mixin(SurfaceSystem.class)
public interface SurfaceSystemInvoker
{
    @Invoker("erodedBadlandsExtension")
    void invokeErodedBadlandsExtension(BlockColumn p_189955_, int p_189956_, int p_189957_, int p_189958_,
            LevelHeightAccessor p_189959_);

    @Invoker("frozenOceanExtension")
    void invokeFrozenOceanExtension(int p_189935_, Biome p_189936_, BlockColumn p_189937_,
            BlockPos.MutableBlockPos p_189938_, int p_189939_, int p_189940_, int p_189941_);
}
