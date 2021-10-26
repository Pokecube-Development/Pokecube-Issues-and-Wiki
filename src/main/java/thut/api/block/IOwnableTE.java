package thut.api.block;

import java.util.UUID;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import thut.api.IOwnable;

public interface IOwnableTE extends IOwnable
{
    default boolean canEdit(final LivingEntity editor)
    {
        final UUID owner = this.getOwnerId();
        if (owner == null) return true;
        if (editor == null) return false;
        return editor.getUUID().equals(owner) || editor instanceof Player && ((Player) editor)
                .isCreative();
    }

    default void setPlacer(final LivingEntity placer)
    {
        this.setOwner(placer);
        this.setOwner(placer.getUUID());
    }
}
