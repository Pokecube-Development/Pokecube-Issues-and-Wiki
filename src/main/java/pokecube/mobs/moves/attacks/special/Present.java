package pokecube.mobs.moves.attacks.special;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;
import thut.core.common.ThutCore;

public class Present extends Move_Basic
{

    public Present()
    {
        super("present");
    }

    @Override
    public int getPWR(IPokemob user, Entity target)
    {
        final double rand = ThutCore.newRandom().nextDouble();
        if (rand < 0.4) return 40;
        if (rand < 0.7) return 80;
        if (rand < 0.8) return 120;
        return 0;
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
