package pokecube.core.events;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class CustomInteractEvent extends EntityInteract
{
    public CustomInteractEvent(final Player player, final InteractionHand hand, final Entity target)
    {
        super(player, hand, target);
    }
}
