package thut.api.entity.blockentity;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public abstract class BlockEntityInteractHandler
{
    final IBlockEntity blockEntity;
    final Entity       theEntity;

    protected BlockHitResult trace;

    public BlockEntityInteractHandler(final IBlockEntity entity)
    {
        this.blockEntity = entity;
        this.theEntity = (Entity) entity;
    }

    public BlockHitResult getLastTrace()
    {
        return this.trace;
    }

    public InteractionResult applyPlayerInteraction(final Player player, final Vec3 vec, final ItemStack stack,
            final InteractionHand hand)
    {
        return InteractionResult.PASS;
    }

    public abstract InteractionResult interactInternal(Player player, BlockPos pos, @Nullable ItemStack stack,
            InteractionHand hand);

    public InteractionResult processInitialInteract(final Player player, @Nullable final ItemStack stack,
            final InteractionHand hand)
    {
        return InteractionResult.PASS;
    }
}
