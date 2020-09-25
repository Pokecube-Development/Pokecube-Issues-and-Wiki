package thut.api.entity.blockentity;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public abstract class BlockEntityInteractHandler
{
    final IBlockEntity blockEntity;
    final Entity       theEntity;

    protected BlockRayTraceResult trace;

    public BlockEntityInteractHandler(final IBlockEntity entity)
    {
        this.blockEntity = entity;
        this.theEntity = (Entity) entity;
    }

    public BlockRayTraceResult getLastTrace()
    {
        return this.trace;
    }

    public ActionResultType applyPlayerInteraction(final PlayerEntity player, final Vector3d vec, final ItemStack stack,
            final Hand hand)
    {
        final Vector3d playerPos = player.getEyePosition(1);
        final Vector3d start = playerPos;
        final Vector3d end = playerPos.add(player.getLookVec().scale(4.5));
        this.trace = null;
        RayTraceResult trace2 = IBlockEntity.BlockEntityFormer.rayTraceInternal(start, end, this.blockEntity);
        if (trace2 instanceof BlockRayTraceResult) this.trace = (BlockRayTraceResult) trace2;
        final World realWorld = this.theEntity.getEntityWorld();
        BlockState state;
        final World world = this.blockEntity.getFakeWorld() instanceof World ? (World) this.blockEntity.getFakeWorld()
                : realWorld;
        if (this.trace != null) switch (this.trace.getType())
        {
        case BLOCK:
            // In this case, we have attempt to interact with the block on
            // the entity
            state = this.blockEntity.getFakeWorld().getBlock(this.trace.getPos());

            return state.onBlockActivated(world, player, hand, this.trace);
        case ENTITY:
            break;
        case MISS:
            // In this case, we trace the normal world and see what we get
            final RayTraceContext context = new RayTraceContext(start, end, BlockMode.OUTLINE, FluidMode.NONE, player);
            trace2 = realWorld.rayTraceBlocks(context);
            if (trace2 instanceof BlockRayTraceResult)
            {
                state = realWorld.getBlockState(((BlockRayTraceResult) trace2).getPos());
                return state.onBlockActivated(realWorld, player, hand, (BlockRayTraceResult) trace2);
            }
            break;
        default:
            break;
        }
        return ActionResultType.PASS;
    }

    public abstract ActionResultType interactInternal(PlayerEntity player, BlockPos pos, @Nullable ItemStack stack,
            Hand hand);

    public ActionResultType processInitialInteract(final PlayerEntity player, @Nullable final ItemStack stack,
            final Hand hand)
    {
        return ActionResultType.PASS;
    }
}
