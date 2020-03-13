package pokecube.core.blocks.berries;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BushBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import pokecube.core.items.berries.BerryManager;

public class BerryFruit extends BushBlock
{
    public static final VoxelShape BERRY_UP   = Block.makeCuboidShape(5.0D, 5.0D, 5.0D, 11.0D, 16.0D, 11.0D);
    public static final VoxelShape BERRY_DOWN = Block.makeCuboidShape(2.5D, 0.0D, 2.5D, 13.5D, 6.0D, 13.5D);

    // Precise selection box
    private static final VoxelShape PECHA_BERRY = VoxelShapes.or(
      Block.makeCuboidShape(5.5, 11.75, 6.75, 10.26, 16, 9.25))
      .simplify();

    private static final VoxelShape ASPEAR_BERRY = VoxelShapes.or(
      Block.makeCuboidShape(5, 0, 5, 11, 6, 11))
      .simplify();

    private static final VoxelShape LEPPA_BERRY = VoxelShapes.or(
      Block.makeCuboidShape(7.4, 13.67, 7.4, 8.6, 14.07, 8.6),
      Block.makeCuboidShape(6, 9.67, 6, 10, 13.67, 10),
      Block.makeCuboidShape(7.65, 14.07, 7.8, 8.35, 16, 8.2))
      .simplify();

    private static final VoxelShape ORAN_BERRY = VoxelShapes.or(
      Block.makeCuboidShape(6.4, 15.0, 6.4, 9.6, 15.4, 9.6),
      Block.makeCuboidShape(5.6, 11.6, 6.4, 6.0, 14.8, 9.6),
      Block.makeCuboidShape(6.4, 11.6, 10.0, 9.6, 14.8, 10.4),
      Block.makeCuboidShape(6.0, 11.2, 6.0, 10.0, 15.2, 10.0),
      Block.makeCuboidShape(6.4, 10.8, 6.4, 9.6, 11.2, 9.6),
      Block.makeCuboidShape(10.0, 11.6, 6.4, 10.4, 14.8, 9.6),
      Block.makeCuboidShape(6.4, 11.6, 5.6, 9.6, 14.8, 6.0),
      Block.makeCuboidShape(7.2, 15.2, 7.2, 8.8, 16.0, 8.8))
      .simplify();

    private static final VoxelShape LUM_BERRY = VoxelShapes.or(
      Block.makeCuboidShape(4, 0, 4, 12, 5, 12))
      .simplify();

    private static final VoxelShape SITRUS_BERRY = VoxelShapes.or(
      Block.makeCuboidShape(6.5, 13.45, 7.46, 9.5, 15.25, 10.46),
      Block.makeCuboidShape(7.25, 15.55, 8.21, 8.75, 16, 9.71),
      Block.makeCuboidShape(6.2, 11.05, 7.16, 9.8, 13.45, 10.76),
      Block.makeCuboidShape(7.1, 15.25, 8.06, 8.9, 15.55, 9.86),
      Block.makeCuboidShape(6.4, 10.45, 7.36, 9.6, 11.05, 10.56))
      .simplify();

    private static final VoxelShape NANAB_BERRY = VoxelShapes.or(
      Block.makeCuboidShape(5.5, 10.49, 7.32, 10.5, 14.04, 9.41),
      Block.makeCuboidShape(7.5, 13.98, 7.8, 8.5, 15.02, 8.84),
      Block.makeCuboidShape(5.26, 14.88, 7.8, 10.74, 16, 8.84),
      Block.makeCuboidShape(5.5, 6.67, 6.21, 7.15, 11.87, 7.84),
      Block.makeCuboidShape(5.5, 6.67, 5.46, 7.15, 9.48, 6.21),
      Block.makeCuboidShape(8.85, 6.67, 5.46, 10.5, 9.48, 6.21),
      Block.makeCuboidShape(7.23, 6.67, 5.46, 8.78, 9.48, 6.21),
      Block.makeCuboidShape(8.85, 6.67, 6.21, 10.5, 11.87, 7.84),
      Block.makeCuboidShape(7.23, 6.67, 6.21, 8.78, 11.87, 7.84),
      Block.makeCuboidShape(5.5, 8.98, 7.84, 7.15, 10.96, 9.59),
      Block.makeCuboidShape(5.5, 6.67, 7.84, 7.15, 8.98, 8.82),
      Block.makeCuboidShape(8.85, 6.67, 7.84, 10.5, 8.98, 8.82),
      Block.makeCuboidShape(7.23, 6.67, 7.84, 8.78, 8.98, 8.82),
      Block.makeCuboidShape(8.85, 8.98, 7.84, 10.5, 10.96, 9.59),
      Block.makeCuboidShape(7.23, 8.98, 7.84, 8.78, 10.96, 9.59))
      .simplify();

    private static final VoxelShape PINAP_BERRY = VoxelShapes.or(
      Block.makeCuboidShape(4, 0, 4, 12, 16, 12))
      .simplify();

    private static final VoxelShape POMEG_BERRY = VoxelShapes.or(
      Block.makeCuboidShape(7.81, 8.07, 7.79, 8.63, 11.97, 8.71),
      Block.makeCuboidShape(6.48, 8.07, 7.79, 7.3, 11.97, 8.71),
      Block.makeCuboidShape(7.81, 8.07, 10.62, 8.63, 11.97, 11.54),
      Block.makeCuboidShape(6.46, 8.07, 10.62, 7.28, 11.97, 11.54),
      Block.makeCuboidShape(6.46, 8.07, 9.19, 7.28, 11.97, 10.11),
      Block.makeCuboidShape(6.48, 8.07, 7.79, 7.3, 11.97, 8.71),
      Block.makeCuboidShape(9.2, 8.07, 7.79, 10.02, 11.97, 8.71),
      Block.makeCuboidShape(9.2, 8.07, 9.19, 10.02, 11.97, 10.11),
      Block.makeCuboidShape(9.2, 8.07, 10.64, 10.02, 11.97, 11.56),
      Block.makeCuboidShape(6.38, 8.81, 7.73, 10.13, 12.71, 11.61),
      Block.makeCuboidShape(6.6, 10.97, 8.18, 9.83, 14.39, 11.16),
      Block.makeCuboidShape(7.67, 14.39, 9, 8.76, 16.04, 10.73))
      .simplify();

    private static final VoxelShape KELPSY_BERRY = VoxelShapes.or(
      Block.makeCuboidShape(5.69, 9.35, 7.46, 10.06, 11.54, 8.66),
      Block.makeCuboidShape(5.69, 6.95, 7.48, 10.06, 9.14, 8.64),
      Block.makeCuboidShape(7.57, 5.79, 7.42, 8.43, 7.52, 8.72),
      Block.makeCuboidShape(7.57, 13.09, 7.42, 8.43, 14.82, 8.72),
      Block.makeCuboidShape(7.84, 14.4, 7.93, 8.19, 16, 8.25),
      Block.makeCuboidShape(5.69, 11.69, 7.45 , 10.06, 13.87, 8.67),
      Block.makeCuboidShape(7.49, 11.25, 7.55, 8.49, 12.25, 8.55),
      Block.makeCuboidShape(7.49, 8.5, 7.55, 8.49, 9.5, 8.55))
      .simplify();

    private static final VoxelShape QUALOT_BERRY = VoxelShapes.or(
      Block.makeCuboidShape(7.76, 10.85, 7.76, 8.24, 15.65, 8.24),
      Block.makeCuboidShape(7.52, 7.72, 7.52, 8.48, 8.68, 8.48),
      Block.makeCuboidShape(5.6, 8.19, 5.6, 10.4, 14.21, 10.4))
      .simplify();

    private static final VoxelShape HONDEW_BERRY = VoxelShapes.or(
      Block.makeCuboidShape(7.45, 14.67, 7.5, 8.55, 16, 8.57),
      Block.makeCuboidShape(5.35, 14.05, 8.58, 10.63, 14.96, 9.5),
      Block.makeCuboidShape(5.35, 12.73, 8.58, 10.63, 13.64, 9.49),
      Block.makeCuboidShape(5.35, 11.43, 8.58, 10.63, 12.34, 9.5),
      Block.makeCuboidShape(5.37, 10.09, 8.62, 10.65, 11, 9.53),
      Block.makeCuboidShape(5.35, 14.05, 6.58, 10.63, 14.96, 7.49),
      Block.makeCuboidShape(5.35, 12.84, 6.58, 10.63, 13.75, 7.49),
      Block.makeCuboidShape(5.35, 11.54, 6.58, 10.63, 12.45, 7.49),
      Block.makeCuboidShape(5.35, 10.19, 6.58, 10.63, 11.1, 7.49),
      Block.makeCuboidShape(8.56, 13.93, 5.39, 9.47, 14.85, 10.67),
      Block.makeCuboidShape(8.56, 12.74, 5.39, 9.47, 13.65, 10.67),
      Block.makeCuboidShape(8.56, 11.52, 5.39, 9.47, 12.43, 10.67),
      Block.makeCuboidShape(8.56, 10.22, 5.39, 9.47, 11.13, 10.67),
      Block.makeCuboidShape(6.51, 13.93, 5.39, 7.42, 14.85, 10.67),
      Block.makeCuboidShape(6.55, 12.74, 5.39, 7.46, 13.65, 10.67),
      Block.makeCuboidShape(6.52, 11.52, 5.39, 7.43, 12.43, 10.67),
      Block.makeCuboidShape(6.54, 10.22, 5.39, 7.46, 11.13, 10.67),
      Block.makeCuboidShape(6.47, 9.42, 8.54, 7.38, 10.33, 9.45),
      Block.makeCuboidShape(8.64, 9.42, 8.54, 9.55, 10.33, 9.45),
      Block.makeCuboidShape(8.64, 9.42, 6.61, 9.55, 10.33, 7.53),
      Block.makeCuboidShape(6.47, 9.42, 6.61, 7.38, 10.33, 7.53),
      Block.makeCuboidShape(5.6, 9.6, 5.6, 10.4, 15.3, 10.4))
      .simplify();

    private static final VoxelShape GREPA_BERRY = VoxelShapes.or(
      Block.makeCuboidShape(6.88, 8.46, 6.88, 9.12, 9.35, 9.12),
      Block.makeCuboidShape(7.55, 8.07, 7.55, 8.45, 8.52, 8.45),
      Block.makeCuboidShape(6.66, 13.11, 6.66, 9.34, 13.79, 9.34),
      Block.makeCuboidShape(7.33, 13.68, 7.33, 8.67, 13.91, 8.67),
      Block.makeCuboidShape(7.78, 11.43, 7.78, 8.22, 16, 8.22),
      Block.makeCuboidShape(7.8, 14.76, 7.89, 8.2, 15.21, 9.68),
      Block.makeCuboidShape(5.76, 8.96, 5.76, 10.24, 13.44, 10.24))
      .simplify();

    private static final VoxelShape TAMATO_BERRY = VoxelShapes.or(
      Block.makeCuboidShape(5.84, 10.16, 5.84, 10.16, 13.84, 10.16),
      Block.makeCuboidShape(6.71, 12.95, 7.89, 9.28, 16, 8.11),
      Block.makeCuboidShape(6.98, 9.2, 6.96, 9.06, 10.16, 9.04),
      Block.makeCuboidShape(7.78, 11.35, 10.15, 8.48, 13.55, 11.25),
      Block.makeCuboidShape(4.9, 11.43, 7.65, 5.86, 13.33, 8.35),
      Block.makeCuboidShape(9.9, 11.35, 7.65, 11.23, 13.49, 8.35),
      Block.makeCuboidShape(7.78, 11.28, 4.81, 8.48, 13.34, 5.91),
      Block.makeCuboidShape(6.37, 13.8, 7.65, 7.16, 14.24, 8.35),
      Block.makeCuboidShape(9.08, 13.8, 7.65, 9.81, 14.21, 8.35),
      Block.makeCuboidShape(7.78, 13.82, 8.93, 8.48, 14.3, 9.86),
      Block.makeCuboidShape(7.78, 13.82, 6.33, 8.48, 14.17, 6.99))
      .simplify();

    private static final VoxelShape ENIGMA_BERRY = VoxelShapes.or(
      Block.makeCuboidShape(6.5, 6.25, 6.5, 9.5, 6.75, 9.5),
      Block.makeCuboidShape(7.75, 15.25, 7.75, 8.25, 16.75, 8.25),
      Block.makeCuboidShape(7, 13.25, 7, 9, 15.25, 9),
      Block.makeCuboidShape(6.25, 6.75, 6.25, 9.75, 11.75, 9.75),
      Block.makeCuboidShape(6.75, 11.75, 6.75, 9.25, 12.75, 9.25),
      Block.makeCuboidShape(7.25, 12.75, 7.25, 8.75, 13.25, 8.75))
      .simplify();

    private static final VoxelShape ROWAP_BERRY = VoxelShapes.or(
      Block.makeCuboidShape(3, 0, 3, 13, 7, 13))
      .simplify();

    public final Integer index;
    private final int    ind;

    public BerryFruit(final Properties builder, final int index)
    {
        super(builder);
        this.index = index;
        this.ind = index;
    }

    @Override
    public VoxelShape getCollisionShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return VoxelShapes.empty();
    }

    @Override
    public ItemStack getItem(final IBlockReader worldIn, final BlockPos pos, final BlockState state)
    {
        return new ItemStack(BerryManager.berryItems.get(this.ind));
    }

    @Override
    public VoxelShape getRenderShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos)
    {
        return state.getShape(worldIn, pos);
    }

    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        if (this.index == 3) { return BerryFruit.PECHA_BERRY; }
        else if (this.index == 5) { return BerryFruit.ASPEAR_BERRY; }
        else if (this.index == 6) { return BerryFruit.LEPPA_BERRY; }
        else if (this.index == 7) { return BerryFruit.ORAN_BERRY; }
        else if (this.index == 9) { return BerryFruit.LUM_BERRY; }
        else if (this.index == 10) { return BerryFruit.SITRUS_BERRY; }
        else if (this.index == 18) { return BerryFruit.NANAB_BERRY; }
        else if (this.index == 20) { return BerryFruit.PINAP_BERRY; }
        else if (this.index == 21) { return BerryFruit.POMEG_BERRY; }
        else if (this.index == 22) { return BerryFruit.KELPSY_BERRY; }
        else if (this.index == 23) { return BerryFruit.QUALOT_BERRY; }
        else if (this.index == 24) { return BerryFruit.HONDEW_BERRY; }
        else if (this.index == 25) { return BerryFruit.GREPA_BERRY; }
        else if (this.index == 26) { return BerryFruit.TAMATO_BERRY; }
        else if (this.index == 60) { return BerryFruit.ENIGMA_BERRY; }
        else if (this.index == 64) { return BerryFruit.ROWAP_BERRY; }
        else return BerryGenManager.trees.containsKey(this.index) ? BerryFruit.BERRY_UP : BerryFruit.BERRY_DOWN;
    }

    @Override
    protected boolean isValidGround(final BlockState state, final IBlockReader worldIn, final BlockPos pos)
    {
        return state.getBlock() instanceof BerryCrop || worldIn.getBlockState(pos.up(2))
                .getBlock() instanceof BerryLeaf;
    }

    @Override
    public ActionResultType onBlockActivated(final BlockState state, final World world, final BlockPos pos,
            final PlayerEntity player, final Hand hand, final BlockRayTraceResult hit)
    {
        if (!world.isRemote) world.destroyBlock(pos, true);
        return ActionResultType.CONSUME;
    }
}
