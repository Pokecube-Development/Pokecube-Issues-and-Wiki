package pokecube.core.blocks.pc;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import pokecube.core.blocks.tms.TMBlock;
import pokecube.core.inventory.pc.PCContainer;
import pokecube.core.inventory.pc.PCInventory;

public class PCBlock extends HorizontalBlock
{
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
    final boolean                         top;
    final boolean                         needsBase;

    public PCBlock(final Properties properties, final boolean top)
    {
        this(properties, top, true);
    }

    public PCBlock(final Properties properties, final boolean top, final boolean needsBase)
    {
        super(properties);
        this.top = top;
        this.needsBase = needsBase;
        this.setDefaultState(this.stateContainer.getBaseState().with(PCBlock.FACING, Direction.NORTH));
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new PCTile();
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(PCBlock.FACING);
    }

    @Override
    public VoxelShape getRenderShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos)
    {
        return TMBlock.RENDERSHAPE;
    }

    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        return this.getDefaultState().with(PCBlock.FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Override
    public boolean onBlockActivated(final BlockState state, final World world, final BlockPos pos,
            final PlayerEntity player, final Hand hand, final BlockRayTraceResult hit)
    {
        if (this.top)
        {
            if (!this.needsBase || world.getBlockState(pos.down()).getBlock() instanceof PCBlock)
                if (player instanceof ServerPlayerEntity) player.openContainer(new SimpleNamedContainerProvider((id,
                        playerInventory, playerIn) -> new PCContainer(id, playerInventory, PCInventory.getPC(playerIn)),
                        player.getDisplayName()));
            return true;
        }
        else
        {
            return false;
        }
    }
}
