package thut.api.entity.blockentity;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

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
