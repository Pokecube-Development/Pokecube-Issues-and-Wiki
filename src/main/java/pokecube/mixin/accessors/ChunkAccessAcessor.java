package pokecube.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;

@Mixin(ChunkAccess.class)
public interface ChunkAccessAcessor
{
    @Accessor("levelHeightAccessor")
    LevelHeightAccessor getLevelHeightAccessor();
}
