package pokecube.gimmicks.builders.builders;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * This interface provides functions for allowing building structures or
 * buildings
 */
public interface IBlocksBuilder
{
    /**
     * Used to mark the placement status. {@link CanPlace#NO} means to ignore
     * the block for placement.
     */
    public static enum CanPlace
    {
        YES, NO, NEED_ITEM;
    }

    /**
     * Record holding information about the placement status. If valid is
     * {@link CanPlace#YES}, then itemSlot may be the slot
     * {@link IItemHandlerModifiable} which is involved in the placement.
     */
    public static record PlaceInfo(CanPlace valid, StructureBlockInfo info, int itemSlot)
    {
    }

    /**
     * Record holding suppliers and consumers for providing a Bill of Materials
     * (BoM) involved in our builder. BoMProvider() should return the itemstack
     * to populate with the BoM, and BoMConsumer() is used to provide the
     * feedback. If not told to provide only needed, the entire list of items
     * get put in neededStacks.
     */
    public static record BoMRecord(Supplier<ItemStack> BoMProvider, Consumer<ItemStack> BoMConsumer,
            List<ItemStack> neededStacks)
    {
        public BoMRecord(Supplier<ItemStack> BoMProvider, Consumer<ItemStack> BoMConsumer)
        {
            this(BoMProvider, BoMConsumer, new ArrayList<>());
        }
    }

    /**
     * @return whether we are creative mode, meaning no need for items to place.
     */
    boolean isCreative();

    /**
     * @param creative sets the return value for {@link #isCreative()}
     */
    void setCreative(boolean creative);

    /**
     * @return whether we are still valid to build. When we are done, this
     *         should be false.
     */
    boolean validBuilder();

    /**
     * Clears the list of {@link BoMRecord} which was added to in
     * {@link #addBoMRecord(BoMRecord)}
     */
    void clearBoMRecords();

    /**
     * Adds a {@link BoMRecord} to provide feedback to.
     * 
     * @param BoM
     */
    void addBoMRecord(BoMRecord BoM);

    /**
     * Provides feedback to the given {@link BoMRecord}
     * 
     * @param record
     * @param onlyNeeded
     */
    void provideBoM(BoMRecord record, boolean onlyNeeded);

    /**
     * checks status of the given {@link BoMRecord}. If the "Needed Items" of
     * the {@link BoMRecord} had their required number replaced with "-", then
     * that item should be ignored for placement.
     * 
     * @param record
     */
    void checkBoM(BoMRecord record);

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
     * 
     * @param info       - block to check
     * @param itemSource - source of items for placement if not
     *                   {@link #isCreative()}, if null, and not
     *                   {@link #isCreative()}, we will not be able to place
     *                   blocks.
     * @return a {@link PlaceInfo} containing whether we can place the block.
     */
    @Nonnull
    PlaceInfo canPlace(StructureBlockInfo info, @Nullable IItemHandlerModifiable itemSource);

    /**
     * @param itemSource - If we are not {@link #isCreative()} this is needed to
     *                   check if we have the correct item to place, checked via
     *                   {@link #canPlace(StructureBlockInfo, IItemHandlerModifiable)}
     * @return a {@link PlaceInfo} containing whether we an place a next block.
     *         If this returns null, then it means we are out of placements to
     *         check at all.
     */
    @Nullable
    PlaceInfo getNextPlacement(@Nullable IItemHandlerModifiable itemSource);

    /**
     * Attempts to place the block held in the {@link PlaceInfo}. It will do an
     * additional check to
     * {@link #canPlace(StructureBlockInfo, IItemHandlerModifiable)} to confirm
     * that the placement is valid, and if not {@link #isCreative()}, it will
     * consume the item from the provided itemSource.
     * 
     * @param placement  - context of block to place
     * @param itemSource - inventory to pull items from, if not
     *                   {@link #isCreative()} and this is null, then the
     *                   placement will most likely not occur.
     * @return whether an item was placed.
     */
    boolean tryPlace(PlaceInfo placement, @Nullable IItemHandlerModifiable itemSource);

    /**
     * Marks the position as pending for a build. This makes it get skipped in
     * checks for {@link #getNextPlacement(IItemHandlerModifiable)}, and is
     * intended for use if multiple callers are working together, but with
     * delays on building.
     * 
     * @param pos
     */
    void markPendingBuild(BlockPos pos);

    /**
     * Unmarks the value as set in {@link #markPendingBuild(BlockPos)}
     * 
     * @param pos
     */
    void markBuilt(BlockPos pos);

    /**
     * Populates requested with a list of needed itemstacks. It will walk
     * forwards along the next list of placements, and add those to the list. If
     * the same item is required each time, it will not re-add it to the list,
     * so the list will contain up to number items, but maybe as low as 1.
     * 
     * @param requested - list to populate
     * @param number    - maximum number of steps ahead to check for population.
     */
    void getNextNeeded(List<ItemStack> requested, int number);
}
