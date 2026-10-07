package pokecube.core.utils.mixin;

import net.minecraft.world.level.LevelHeightAccessor;

public interface IChunkAccessAcessor
{
    default LevelHeightAccessor getLevelHeightAccessor() {return null;}
}
