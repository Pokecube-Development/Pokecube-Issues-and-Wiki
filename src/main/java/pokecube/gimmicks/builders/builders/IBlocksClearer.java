package pokecube.gimmicks.builders.builders;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/**
 * This interface provdes functions for checking if we need to dig/remove
 * blocks.
 */
public interface IBlocksClearer
{
    /**
     * @return whether we are creative mode, meaning no blocks dropped when
     *         broken.
     */
    boolean isCreative();

    /**
     * Sets the return value for {@link #isCreative()}
     * 
     * @param creative
     */
    void setCreative(boolean creative);

    /**
     * @return the level we work in. The value of this should be set via
     *         {@link #update(ServerLevel)}.
     */
    ServerLevel getLevel();

    /**
     * Updates the instructions, etc. Also sets the return value for
     * {@link #getLevel()}.
     * 
     * @param level
     */
    void update(ServerLevel level);

    /**
     * Attempts to clear the next removal block.
     * 
     * @param dropHandler - sink of dropped items for if not
     *                    {@link #isCreative()}
     * @return whether we removed a block
     */
    boolean tryClear(Consumer<ItemStack> dropHandler);

    /**
     * @return the next location to try to remove, or null if no more removals
     *         to do.
     */
    @Nullable
    BlockPos nextRemoval();

    /**
     * Marks this position as pending for removal. This makes the location be
     * skipped for checks in {@link #nextRemoval()}, and is intended for use
     * with multiple callers working together, but with delays on removal.
     * 
     * @param pos
     */
    void markPendingClear(BlockPos pos);

    /**
     * Unmarks the value as set in {@link #markPendingClear(BlockPos)}
     * 
     * @param pos
     */
    void markCleared(BlockPos pos);
}
