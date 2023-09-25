package thut.crafts.entity;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
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
import thut.lib.TComponent;

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

        System.out.println(result + " " + passed);
        if (this.interactInternal(player, new BlockPos((int) vec.x, (int) vec.y, (int) vec.z), stack,
                hand) == InteractionResult.SUCCESS)
            return InteractionResult.SUCCESS;
        else
        {
            System.out.println(craft.yRot + " " + this.craft.getSeatCount());
            for (int i = 0; i < this.craft.getSeatCount(); i++)
            {
                final Seat seat = this.craft.getSeat(i);
                if (!this.craft.level.isClientSide && seat.getEntityId().equals(Seat.BLANK))
                {
                    this.craft.setSeatID(i, player.getUUID());
                    player.startRiding(this.craft);
                    return InteractionResult.SUCCESS;
                }
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
            System.out.println(pos + " " + state+" "+this.craft.getSeatCount());
            // First check if a seat exists
            for (int i = 0; i < this.craft.getSeatCount(); i++)
            {
                var seat = this.craft.getSeat(i);
                var seatPos = seat.seat;
                var pos1 = new BlockPos((int) seatPos.x, (int) seatPos.y, (int) seatPos.z);
                if (pos1.equals(pos))
                {
                    System.out.println("Found Seat!");
                    if (!seat.getEntityId().equals(player.getUUID()))
                    {
                        player.startRiding(this.craft);
                        this.craft.setSeatID(i, player.getUUID());
                        System.out.println("Riding!");
                        return InteractionResult.SUCCESS;
                    }
                    return InteractionResult.FAIL;
                }
            }
            // Otherwise add as a seat, then make player ride
            final Vec3f seat = new Vec3f(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
            System.out.println("Adding Seat! "+craft);
            this.craft.addSeat(seat);
            this.craft.setSeatID(craft.getSeatCount() - 1, player.getUUID());
            player.startRiding(this.craft);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult processInitialInteract(final Player player, @Nullable final ItemStack stack,
            final InteractionHand hand)
    {
        if (stack.getItem() == Items.BLAZE_ROD) if (!player.level.isClientSide)
        {
            thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable("msg.craft.killed"));
            this.craft.remove(RemovalReason.KILLED);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
