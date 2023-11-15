package pokecube.gimmicks.builders.builders;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public interface IBlocksClearer
{
    boolean tryClear(ServerLevel level);

    BlockPos nextRemoval(ServerLevel level);

    void markPendingClear(BlockPos pos);

    void markCleared(BlockPos pos);
}
