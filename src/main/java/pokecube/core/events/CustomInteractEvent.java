package pokecube.core.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class CustomInteractEvent extends EntityInteract
{
    public CustomInteractEvent(final PlayerEntity player, final Hand hand, final Entity target)
    {
        super(player, hand, target);
    }
}
