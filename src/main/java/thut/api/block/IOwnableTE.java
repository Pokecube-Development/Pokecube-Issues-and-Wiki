package thut.api.block;

import java.util.UUID;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import thut.api.IOwnable;

public interface IOwnableTE extends IOwnable
{
    default boolean canEdit(final LivingEntity editor)
    {
        final UUID owner = this.getOwnerId();
        if (owner == null) return true;
        if (editor == null) return false;
        return editor.getUniqueID().equals(owner) || editor instanceof PlayerEntity && ((PlayerEntity) editor)
                .isCreative();
    }

    default void setPlacer(final LivingEntity placer)
    {
        this.setOwner(placer);
    }
}
