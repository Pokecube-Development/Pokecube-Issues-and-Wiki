package pokecube.mobs.moves.attacks.special;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class Present extends Move_Basic
{

    public Present()
    {
        super("present");
    }

    @Override
    public void postAttack(MovePacket packet)
    {
        super.postAttack(packet);
        if (packet.canceled || packet.failed) return;
        if (packet.PWR == 0 && packet.attacked instanceof LivingEntity)
        {
            final LivingEntity toHeal = (LivingEntity) packet.attacked;
            final float health = Math.min(toHeal.getHealth() + 80, toHeal.getMaxHealth());
            toHeal.setHealth(health);
        }
    }
}
