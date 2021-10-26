package pokecube.core.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;

/**
 * have blocks which are to be eaten by pokemon as berries implement this
 * interface.
 *
 * @author Thutmose
 */
public interface IBerryFruitBlock
{
    /**
     * @param world
     *            - world we are in
     * @param pos
     *            - block pos with block
     * @return - berry from pos and world
     */
    public ItemStack getBerryStack(BlockGetter world, BlockPos pos);
}
