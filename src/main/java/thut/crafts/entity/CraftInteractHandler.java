package thut.crafts.entity;

import javax.annotation.Nullable;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import thut.api.entity.IMultiplePassengerEntity.Seat;
import thut.api.entity.blockentity.BlockEntityInteractHandler;
import thut.api.maths.vecmath.Vec3f;

public class CraftInteractHandler extends BlockEntityInteractHandler
{
    final EntityCraft craft;

    public CraftInteractHandler(final EntityCraft lift)
    {
        super(lift);
        this.craft = lift;
    }

    @Override
    public InteractionResult applyPlayerInteraction(final Player player, Vec3 vec, final ItemStack stack,
            final InteractionHand hand)
    {
        if (player.isCrouching()) return InteractionResult.PASS;
        final InteractionResult result = super.applyPlayerInteraction(player, vec, stack, hand);
        boolean passed = result == InteractionResult.SUCCESS;
        passed = passed
                || this.processInitialInteract(player, player.getItemInHand(hand), hand) == InteractionResult.SUCCESS;
        if (passed) return InteractionResult.SUCCESS;
        vec = vec.subtract(craft.position());

        if (this.interactInternal(player, new BlockPos(vec), stack, hand) == InteractionResult.SUCCESS)
            return InteractionResult.SUCCESS;
        else if (this.craft.yRot != 0) for (int i = 0; i < this.craft.getSeatCount(); i++)
        {
            final Seat seat = this.craft.getSeat(i);
            if (!this.craft.level.isClientSide && seat.getEntityId().equals(Seat.BLANK))
            {
                this.craft.setSeatID(i, player.getUUID());
                player.startRiding(this.craft);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult interactInternal(final Player player, BlockPos pos, final ItemStack stack,
            final InteractionHand hand)
    {
        final BlockState state = this.craft.getFakeWorld().getBlockRelative(pos);
        if (state != null && state.getBlock() instanceof StairBlock)
        {
            if (this.craft.getSeatCount() == 0)
            {
                final BlockPos.MutableBlockPos pos1 = new BlockPos.MutableBlockPos();
                final int sizeX = this.craft.getTiles().length;
                final int sizeY = this.craft.getTiles()[0].length;
                final int sizeZ = this.craft.getTiles()[0][0].length;
                for (int i = 0; i < sizeX; i++) for (int j = 0; j < sizeY; j++) for (int k = 0; k < sizeZ; k++)
                {
                    pos1.set(i + this.craft.getX(), j + this.craft.getY(), k + this.craft.getZ());
                    final BlockState state1 = this.craft.getFakeWorld().getBlock(pos1);
                    if (state1.getBlock() instanceof StairBlock)
                    {
                        final Vec3f seat = new Vec3f(i + 0.5f, j + 0.5f, k + 0.5f);
                        this.craft.addSeat(seat);
                    }
                }
            }

            for (int i = 0; i < this.craft.getSeatCount(); i++)
            {
                final Seat seat = this.craft.getSeat(i);
                final Vec3f seatPos = seat.seat;
                final BlockPos pos1 = new BlockPos(seatPos.x, seatPos.y, seatPos.z);
                if (pos1.equals(pos))
                {
                    if (!player.getLevel().isClientSide && !seat.getEntityId().equals(player.getUUID()))
                    {
                        this.craft.setSeatID(i, player.getUUID());
                        player.startRiding(this.craft);
                        return InteractionResult.SUCCESS;
                    }
                    break;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult processInitialInteract(final Player player, @Nullable final ItemStack stack,
            final InteractionHand hand)
    {
        if (stack.getItem() == Items.BLAZE_ROD) if (!player.level.isClientSide)
        {
            player.sendMessage(new TranslatableComponent("msg.craft.killed"), Util.NIL_UUID);
            this.craft.remove(RemovalReason.KILLED);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
