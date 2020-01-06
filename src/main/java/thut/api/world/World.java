package thut.api.world;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import thut.api.world.blocks.Block;
import thut.api.world.mobs.Mob;
import thut.api.world.utils.Vector;

/**
 * This is a World, it is made of Blocks on a discrete grid, and is the home to
 * Mobs.
 *
 * @author Thutmose
 */
public interface World
{
    /**
     * @param mob
     * @return if the mob added successfully.
     */
    boolean addMob(Mob mob);

    /**
     * Gets the block for the given position.
     *
     * @param position
     * @return
     */
    @Nonnull
    Block getBlock(Vector<Integer> position);

    /**
     * This is the level of this world, generally the base world is 0, lower
     * ones are negative.
     *
     * @return
     */
    int getLevel();

    /**
     * This gets the mob by UUID
     *
     * @param id
     * @return
     */
    @Nullable
    Mob getMob(UUID id);

    /**
     * @param mob
     * @return if the mob was removed successfully
     */
    boolean removeMob(Mob mob);
}
