package pokecube.legends.blocks.containers;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.piglin.PiglinTasks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import pokecube.legends.tileentity.GenericBarrelTile;

public class GenericBarrel extends ContainerBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty   OPEN   = BlockStateProperties.OPEN;

    public GenericBarrel(final Properties props)
    {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(GenericBarrel.FACING, Direction.NORTH).setValue(
                GenericBarrel.OPEN, Boolean.valueOf(false)));
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Override
    public void setPlacedBy(final World world, final BlockPos pos, final BlockState state,
            @Nullable final LivingEntity livingEntity, final ItemStack stack)
    {
        if (stack.hasCustomHoverName())
        {
            final TileEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof GenericBarrelTile) ((GenericBarrelTile) tileentity).setCustomName(stack
                    .getHoverName());
        }
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> state)
    {
        state.add(GenericBarrel.FACING, GenericBarrel.OPEN);
    }

    @Override
    public ActionResultType use(final BlockState state, final World world, final BlockPos pos,
            final PlayerEntity player, final Hand hand, final BlockRayTraceResult blockRayTraceResult)
    {
        if (world.isClientSide) return ActionResultType.SUCCESS;
        else
        {
            final TileEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof GenericBarrelTile)
            {
                player.openMenu((GenericBarrelTile) tileentity);
                player.awardStat(Stats.OPEN_BARREL);
                PiglinTasks.angerNearbyPiglins(player, true);
            }
            return ActionResultType.CONSUME;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(final BlockState state, final World world, final BlockPos pos, final BlockState state2,
            final boolean remove)
    {
        if (!state.is(state2.getBlock()))
        {
            final TileEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof IInventory)
            {
                InventoryHelper.dropContents(world, pos, (IInventory) tileentity);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, state2, remove);
        }
    }

    @Override
    public void tick(final BlockState state, final ServerWorld world, final BlockPos pos, final Random random)
    {
        final TileEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof GenericBarrelTile) ((GenericBarrelTile) tileentity).recheckOpen();
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(final BlockState state, final Mirror mirror)
    {
        return state.rotate(mirror.getRotation(state.getValue(GenericBarrel.FACING)));
    }

    @Override
    public TileEntity newBlockEntity(final IBlockReader reader)
    {
        return new GenericBarrelTile();
    }

    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext blockItemUseContext)
    {
        return this.defaultBlockState().setValue(GenericBarrel.FACING, blockItemUseContext.getNearestLookingDirection()
                .getOpposite());
    }

    @Override
    public BlockRenderType getRenderShape(final BlockState state)
    {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean hasAnalogOutputSignal(final BlockState state)
    {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(final BlockState state, final World world, final BlockPos pos)
    {
        return Container.getRedstoneSignalFromBlockEntity(world.getBlockEntity(pos));
    }

    @Override
    public BlockState rotate(final BlockState state, final Rotation rotation)
    {
        return state.setValue(GenericBarrel.FACING, rotation.rotate(state.getValue(GenericBarrel.FACING)));
    }
}
