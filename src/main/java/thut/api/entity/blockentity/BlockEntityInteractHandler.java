package thut.api.entity.blockentity;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public abstract class BlockEntityInteractHandler
{
    final IBlockEntity blockEntity;
    final Entity       theEntity;

    public BlockEntityInteractHandler(final IBlockEntity entity)
    {
        this.blockEntity = entity;
        this.theEntity = (Entity) entity;
    }

    public ActionResultType applyPlayerInteraction(final PlayerEntity player, Vec3d vec,
            @Nullable final ItemStack stack, final Hand hand)
    {
        vec = vec.add(vec.x > 0 ? -0.01 : 0.01, vec.y > 0 ? -0.01 : 0.01, vec.z > 0 ? -0.01 : 0.01);
        final Vec3d playerPos = player.getPositionVector().add(0, player.isServerWorld() ? player.getEyeHeight() : 0,
                0);
        final Vec3d start = playerPos;
        final Vec3d end = playerPos.add(player.getLookVec().scale(4.5));
        BlockRayTraceResult trace = null;
        final RayTraceResult trace2 = IBlockEntity.BlockEntityFormer.rayTraceInternal(start, end, this.blockEntity);
        if (trace2 instanceof BlockRayTraceResult) trace = (BlockRayTraceResult) trace2;
        BlockPos pos;
        if (trace == null) pos = this.theEntity.getPosition();
        else pos = trace.getPos();
        BlockState state = this.blockEntity.getFakeWorld().getBlock(pos);
        boolean activate = state != null && state.onBlockActivated((World) this.blockEntity.getFakeWorld(), player,
                hand, trace);
        if (activate || state == null) return ActionResultType.SUCCESS;
        else if (trace == null || !state.getMaterial().isSolid())
        {
            final Vec3d playerLook = playerPos.add(player.getLookVec().scale(4));

            final RayTraceContext context = new RayTraceContext(playerPos, playerLook,
                    RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, player);

            final RayTraceResult result = this.theEntity.getEntityWorld().rayTraceBlocks(context);

            if (result instanceof BlockRayTraceResult)
            {
                trace = (BlockRayTraceResult) result;
                pos = trace.getPos();
                state = this.theEntity.getEntityWorld().getBlockState(pos);
                final ItemUseContext context2 = new ItemUseContext(player, hand, trace);
                if (player.isSneaking() && !stack.isEmpty())
                {
                    final ActionResultType itemUse = ForgeHooks.onPlaceItemIntoWorld(context2);
                    if (itemUse != ActionResultType.PASS) return itemUse;
                }
                activate = state.onBlockActivated(this.theEntity.getEntityWorld(), player, hand, trace);
                if (activate) return ActionResultType.SUCCESS;
                else if (!player.isSneaking() && !stack.isEmpty())
                {
                    final ActionResultType itemUse = ForgeHooks.onPlaceItemIntoWorld(context2);
                    if (itemUse != ActionResultType.PASS) return itemUse;
                }
            }
            return ActionResultType.PASS;
        }
        return ActionResultType.PASS;
    }

    public abstract ActionResultType interactInternal(PlayerEntity player, BlockPos pos, @Nullable ItemStack stack,
            Hand hand);

    public boolean processInitialInteract(final PlayerEntity player, @Nullable final ItemStack stack, final Hand hand)
    {
        return false;
    }
}
