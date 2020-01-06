package pokecube.core.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

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
    public ItemStack getBerryStack(IBlockReader world, BlockPos pos);
}
