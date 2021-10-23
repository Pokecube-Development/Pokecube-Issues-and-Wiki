package pokecube.adventures.blocks.statue;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.core.blocks.InteractableHorizontalBlock;
import thut.api.entity.CopyCaps;
import thut.api.entity.ICopyMob;

public class PokemobStatue extends InteractableHorizontalBlock implements EntityBlock, SimpleWaterloggedBlock
{
    static Map<UUID, VoxelShape> CACHE = Maps.newConcurrentMap();

    public static VoxelShape forMob(final LivingEntity mob)
    {
        if (!PokemobStatue.CACHE.containsKey(mob.getUUID()))
        {
            final AABB box = mob.getBoundingBox().move(-mob.getX() + 0.5, -mob.getY(), -mob.getZ() + 0.5);
            PokemobStatue.CACHE.put(mob.getUUID(), Shapes.create(box));
        }
        return PokemobStatue.CACHE.get(mob.getUUID());
    }

    public PokemobStatue(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HorizontalDirectionalBlock.FACING,
                Direction.NORTH).setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    public ItemStack getCloneItemStack(final BlockGetter world, final BlockPos pos, final BlockState state)
    {
        @SuppressWarnings("deprecation")
        final ItemStack itemstack = super.getCloneItemStack(world, pos, state);
        final BlockEntity tileentity = world.getBlockEntity(pos);
        final CompoundTag compoundnbt = tileentity.serializeNBT();
        if (!compoundnbt.isEmpty()) itemstack.addTagElement("BlockEntityTag", compoundnbt);
        return itemstack;
    }

    @Override
    public void playerWillDestroy(final Level world, final BlockPos pos, final BlockState state, final Player player)
    {
        final BlockEntity tileentity = world.getBlockEntity(pos);
        if (tileentity != null && !world.isClientSide && !player.isCreative())
        {
            final ItemStack itemstack = new ItemStack(this);
            final CompoundTag compoundnbt = tileentity.serializeNBT();
            if (!compoundnbt.isEmpty()) itemstack.addTagElement("BlockEntityTag", compoundnbt);
            final ItemEntity itementity = new ItemEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    itemstack);
            itementity.setDefaultPickUpDelay();
            world.addFreshEntity(itementity);
        }
        super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(HorizontalDirectionalBlock.FACING, BlockStateProperties.WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection()
                .getOpposite()).setValue(BlockStateProperties.WATERLOGGED, ifluidstate.is(FluidTags.WATER)
                        && ifluidstate.getAmount() == 8);
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state)
    {
        return new StatueEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(final BlockState state)
    {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Deprecated
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        final BlockEntity tile = worldIn.getBlockEntity(pos);
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
        return Shapes.block();
    }
}
