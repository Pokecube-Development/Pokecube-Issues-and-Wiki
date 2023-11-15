package pokecube.gimmicks.builders.builders;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public interface IBlocksClearer
{
    boolean isCreative();

    void setCreative(boolean creative);

    ServerLevel getLevel();

    void update(ServerLevel level);

    boolean tryClear(ServerLevel level, Consumer<ItemStack> dropHandler);

    BlockPos nextRemoval(ServerLevel level);

    void markPendingClear(BlockPos pos);

    void markCleared(BlockPos pos);
}
