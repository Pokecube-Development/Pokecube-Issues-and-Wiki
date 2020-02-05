package pokecube.core.blocks.berries;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BushBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BerryFruit extends BushBlock
{
    public static final VoxelShape BERRY_UP   = Block.makeCuboidShape(5.0D, 5.0D, 5.0D, 11.0D, 16.0D, 11.0D);
    public static final VoxelShape BERRY_DOWN = Block.makeCuboidShape(5.0D, 0.0D, 5.0D, 11.0D, 11.0D, 11.0D);

    public final Integer           index;

    public BerryFruit(final Properties builder, final int index)
    {
        super(builder);
        this.index = index;
    }

    @Override
    public VoxelShape getCollisionShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        return state.getShape(worldIn, pos, context);
    }

    @Override
    public ResourceLocation getLootTable()
    {
        return super.getLootTable();
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
        return BerryGenManager.trees.containsKey(this.index) ? BerryFruit.BERRY_UP : BerryFruit.BERRY_DOWN;
    }

    @Override
    protected boolean isValidGround(final BlockState state, final IBlockReader worldIn, final BlockPos pos)
    {
        return state.getBlock() instanceof BerryCrop || worldIn.getBlockState(pos.up(2)).getBlock() instanceof BerryLeaf
                || true;
    }

    @Override
    public ActionResultType onBlockActivated(final BlockState state, final World world, final BlockPos pos,
            final PlayerEntity player, final Hand hand, final BlockRayTraceResult hit)
    {
        if (!world.isRemote) world.destroyBlock(pos, true);
        return ActionResultType.CONSUME;
    }
}
