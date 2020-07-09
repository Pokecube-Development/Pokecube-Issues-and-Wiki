package thut.crafts.entity;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import thut.api.entity.IMultiplePassengerEntity.Seat;
import thut.api.entity.blockentity.BlockEntityInteractHandler;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.maths.vecmath.Vector3f;

public class CraftInteractHandler extends BlockEntityInteractHandler
{
    final EntityCraft craft;

    public CraftInteractHandler(final EntityCraft lift)
    {
        super(lift);
        this.craft = lift;
    }

    @Override
    public ActionResultType applyPlayerInteraction(final PlayerEntity player, Vector3d vec, final ItemStack stack,
            final Hand hand)
    {
        if (player.isCrouching()) return ActionResultType.PASS;
        final ActionResultType result = super.applyPlayerInteraction(player, vec, stack, hand);
        boolean passed = result == ActionResultType.SUCCESS;
        passed = passed || this.processInitialInteract(player, player.getHeldItem(hand), hand);
        if (passed) return ActionResultType.SUCCESS;
        vec = vec.add(vec.x > 0 ? -0.01 : 0.01, vec.y > 0 ? -0.01 : 0.01, vec.z > 0 ? -0.01 : 0.01);
        if (this.trace == null)
        {
            final Vector3d playerPos = player.getPositionVector().add(0, player.getEyeHeight(), 0);
            final Vector3d start = playerPos.subtract(this.craft.getPositionVector());
            final RayTraceResult hit = IBlockEntity.BlockEntityFormer.rayTraceInternal(start.add(this.craft
                    .getPositionVector()), vec.add(this.craft.getPositionVector()), this.craft);
            this.trace = hit instanceof BlockRayTraceResult ? (BlockRayTraceResult) hit : null;
        }
        BlockPos pos;
        if (this.trace == null) pos = this.craft.getPosition();
        else pos = this.trace.getPos();
        if (this.trace != null && this.interactInternal(player, pos, stack, hand) == ActionResultType.SUCCESS)
            return ActionResultType.SUCCESS;
        else if (this.craft.rotationYaw != 0) for (int i = 0; i < this.craft.getSeatCount(); i++)
        {
            final Seat seat = this.craft.getSeat(i);
            if (!this.craft.world.isRemote && seat.getEntityId().equals(Seat.BLANK))
            {
                this.craft.setSeatID(i, player.getUniqueID());
                player.startRiding(this.craft);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public ActionResultType interactInternal(final PlayerEntity player, BlockPos pos, final ItemStack stack,
            final Hand hand)
    {
        final BlockState state = this.craft.getFakeWorld().getBlock(pos);
        if (state != null && state.getBlock() instanceof StairsBlock)
        {
            if (this.craft.getSeatCount() == 0)
            {
                final BlockPos.Mutable pos1 = new BlockPos.Mutable();
                final int xMin = this.craft.getMin().getX();
                final int zMin = this.craft.getMin().getZ();
                final int yMin = this.craft.getMin().getY();
                final int sizeX = this.craft.getTiles().length;
                final int sizeY = this.craft.getTiles()[0].length;
                final int sizeZ = this.craft.getTiles()[0][0].length;
                for (int i = 0; i < sizeX; i++)
                    for (int j = 0; j < sizeY; j++)
                        for (int k = 0; k < sizeZ; k++)
                        {
                            pos1.setPos(i + xMin + this.craft.getPosX(), j + yMin + this.craft.getPosY(), k + zMin
                                    + this.craft.getPosZ());
                            final BlockState state1 = this.craft.getFakeWorld().getBlock(pos1);
                            if (state1.getBlock() instanceof StairsBlock)
                            {
                                final Vector3f seat = new Vector3f(i + xMin, j + yMin + 0.5f, k + zMin);
                                this.craft.addSeat(seat);
                            }
                        }
            }
            final BlockPos pos2 = new BlockPos(this.craft.getPositionVector());
            pos = pos.subtract(pos2);
            for (int i = 0; i < this.craft.getSeatCount(); i++)
            {
                final Seat seat = this.craft.getSeat(i);
                final Vector3f seatPos = seat.seat;
                final BlockPos pos1 = new BlockPos(seatPos.x, seatPos.y, seatPos.z);
                if (pos1.equals(pos))
                {
                    if (!player.getEntityWorld().isRemote && !seat.getEntityId().equals(player.getUniqueID()))
                    {
                        this.craft.setSeatID(i, player.getUniqueID());
                        player.startRiding(this.craft);
                        return ActionResultType.SUCCESS;
                    }
                    break;
                }
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean processInitialInteract(final PlayerEntity player, @Nullable final ItemStack stack, final Hand hand)
    {
        if (stack.getItem() == Items.BLAZE_ROD) if (!player.world.isRemote)
        {
            player.sendMessage(new TranslationTextComponent("msg.craft.killed"));
            this.craft.remove();
            return true;
        }
        return false;
    }
}
