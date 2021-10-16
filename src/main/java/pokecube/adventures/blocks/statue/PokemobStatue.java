package pokecube.adventures.blocks.statue;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import pokecube.core.blocks.InteractableHorizontalBlock;
import thut.api.entity.CopyCaps;
import thut.api.entity.ICopyMob;

public class PokemobStatue extends InteractableHorizontalBlock implements IWaterLoggable
{
    static Map<UUID, VoxelShape> CACHE = Maps.newConcurrentMap();

    public static VoxelShape forMob(final LivingEntity mob)
    {
        if (!PokemobStatue.CACHE.containsKey(mob.getUUID()))
        {
            final AxisAlignedBB box = mob.getBoundingBox().move(-mob.getX() + 0.5, -mob.getY(), -mob.getZ() + 0.5);
            PokemobStatue.CACHE.put(mob.getUUID(), VoxelShapes.create(box));
        }
        return PokemobStatue.CACHE.get(mob.getUUID());
    }

    public PokemobStatue(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HorizontalBlock.FACING, Direction.NORTH).setValue(
                BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    public ItemStack getCloneItemStack(final IBlockReader world, final BlockPos pos, final BlockState state)
    {
        @SuppressWarnings("deprecation")
        final ItemStack itemstack = super.getCloneItemStack(world, pos, state);
        final TileEntity tileentity = world.getBlockEntity(pos);
        final CompoundNBT compoundnbt = tileentity.serializeNBT();
        if (!compoundnbt.isEmpty()) itemstack.addTagElement("BlockEntityTag", compoundnbt);
        return itemstack;
    }

    @Override
    public void playerWillDestroy(final World world, final BlockPos pos, final BlockState state,
            final PlayerEntity player)
    {
        final TileEntity tileentity = world.getBlockEntity(pos);
        if (tileentity != null && !world.isClientSide && !player.isCreative())
        {
            final ItemStack itemstack = new ItemStack(this);
            final CompoundNBT compoundnbt = tileentity.serializeNBT();
            if (!compoundnbt.isEmpty()) itemstack.addTagElement("BlockEntityTag", compoundnbt);
            final ItemEntity itementity = new ItemEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    itemstack);
            itementity.setDefaultPickUpDelay();
            world.addFreshEntity(itementity);
        }
        super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(HorizontalBlock.FACING, BlockStateProperties.WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(HorizontalBlock.FACING, context.getHorizontalDirection().getOpposite())
                .setValue(BlockStateProperties.WATERLOGGED, ifluidstate.is(FluidTags.WATER) && ifluidstate
                        .getAmount() == 8);
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new StatueEntity();
    }

    @Override
    public BlockRenderType getRenderShape(final BlockState state)
    {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Deprecated
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        final TileEntity tile = worldIn.getBlockEntity(pos);
        te:
        if (tile != null)
        {
            final ICopyMob mob = CopyCaps.get(tile);
            if (mob == null) break te;
            if (tile instanceof StatueEntity) ((StatueEntity) tile).checkMob();
            if (mob.getCopiedID() != null && mob.getCopiedMob() == null)
            {
                mob.onBaseTick(tile.getLevel(), null);
                if (mob.getCopiedMob() != null)
                {
                    final LivingEntity living = mob.getCopiedMob();
                    living.setUUID(UUID.randomUUID());
                    living.setPos(pos.getX(), pos.getY(), pos.getZ());
                    mob.setCopiedNBT(mob.getCopiedMob().serializeNBT());
                    tile.requestModelDataUpdate();
                }
            }
            if (mob.getCopiedMob() != null) return PokemobStatue.forMob(mob.getCopiedMob());
        }
        return VoxelShapes.block();
    }
}
