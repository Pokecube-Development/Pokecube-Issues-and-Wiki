package pokecube.mobs.moves.attacks;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.moves.MoveProvider;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.Damage;
import pokecube.api.moves.utils.MoveApplication.HealProvider;

@MoveProvider(name = "present")
public class Present implements HealProvider
{
    @Override
    public void applyHealing(Damage t)
    {
        MoveApplication packet = t.move();
        if (packet.canceled || packet.failed) return;
        if (packet.pwr == 0 && packet.getTarget() != null)
        {
            final LivingEntity toHeal = packet.getTarget();
            final float health = Math.min(toHeal.getHealth() + 80, toHeal.getMaxHealth());
            toHeal.setHealth(health);
        }
    }
}
