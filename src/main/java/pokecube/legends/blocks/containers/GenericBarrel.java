package pokecube.legends.blocks.containers;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import pokecube.legends.tileentity.GenericBarrelTile;

public class GenericBarrel extends BaseEntityBlock
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
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state)
    {
        return new GenericBarrelTile(pos, state);
    }

    @Override
    public void setPlacedBy(final Level world, final BlockPos pos, final BlockState state,
            @Nullable final LivingEntity livingEntity, final ItemStack stack)
    {
        if (stack.hasCustomHoverName())
        {
            final BlockEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof GenericBarrelTile) ((GenericBarrelTile) tileentity).setCustomName(stack
                    .getHoverName());
        }
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> state)
    {
        state.add(GenericBarrel.FACING, GenericBarrel.OPEN);
    }

    @Override
    public InteractionResult use(final BlockState state, final Level world, final BlockPos pos,
            final Player player, final InteractionHand hand, final BlockHitResult blockRayTraceResult)
    {
        if (world.isClientSide) return InteractionResult.SUCCESS;
        else
        {
            final BlockEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof GenericBarrelTile)
            {
                player.openMenu((GenericBarrelTile) tileentity);
                player.awardStat(Stats.OPEN_BARREL);
                PiglinAi.angerNearbyPiglins(player, true);
            }
            return InteractionResult.CONSUME;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(final BlockState state, final Level world, final BlockPos pos, final BlockState state2,
            final boolean remove)
    {
        if (!state.is(state2.getBlock()))
        {
            final BlockEntity tileentity = world.getBlockEntity(pos);
            if (tileentity instanceof Container)
            {
                Containers.dropContents(world, pos, (Container) tileentity);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, world, pos, state2, remove);
        }
    }

    @Override
    public void tick(final BlockState state, final ServerLevel world, final BlockPos pos, final Random random)
    {
        final BlockEntity tileentity = world.getBlockEntity(pos);
        if (tileentity instanceof GenericBarrelTile) ((GenericBarrelTile) tileentity).recheckOpen();
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(final BlockState state, final Mirror mirror)
    {
        return state.rotate(mirror.getRotation(state.getValue(GenericBarrel.FACING)));
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext blockItemUseContext)
    {
        return this.defaultBlockState().setValue(GenericBarrel.FACING, blockItemUseContext.getNearestLookingDirection()
                .getOpposite());
    }

    @Override
    public RenderShape getRenderShape(final BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    public boolean hasAnalogOutputSignal(final BlockState state)
    {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(final BlockState state, final Level world, final BlockPos pos)
    {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(world.getBlockEntity(pos));
    }

    @Override
    public BlockState rotate(final BlockState state, final Rotation rotation)
    {
        return state.setValue(GenericBarrel.FACING, rotation.rotate(state.getValue(GenericBarrel.FACING)));
    }
}
