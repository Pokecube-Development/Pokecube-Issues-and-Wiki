package pokecube.mobs.moves.attacks;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pokecube.api.data.moves.MoveProvider;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.PostMoveUse;

@MoveProvider(name = "knock-off")
public class KnockOff implements PostMoveUse
{
    @Override
    public void applyPostMove(Damage t)
    {
        MoveApplication packet = t.move();
        if (packet.canceled || packet.failed) return;
        if (packet.getUser() == packet.getTarget()) return;

        var target = packet.getTarget();
        final Level level = target.level();

        if (target == null) return;

        ItemStack heldItem = target.getItemInHand(InteractionHand.MAIN_HAND);

        // drop target's held item into the world.
        ItemEntity entityitem = new ItemEntity(level, target.getX(), target.getY() + 0.5, target.getZ(), heldItem);
        entityitem.setPickUpDelay(40);
        entityitem.setDeltaMovement(entityitem.getDeltaMovement().multiply(0, 1, 0));
        level.addFreshEntity(entityitem);

        target.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        target.getPersistentData().putBoolean("pokecube:itemUsedOrLost", true);
    }
}
