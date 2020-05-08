package pokecube.mobs.moves.attacks.special;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.templates.Move_Basic;

public class Teleport extends Move_Basic
{
    public Teleport()
    {
        super("teleport");
    }

    @Override
    public void postAttack(MovePacket packet)
    {
        final IPokemob attacker = packet.attacker;
        Entity attacked = packet.attacked;
        final Entity target = attacker.getEntity().getAttackTarget();
        if (attacked == attacker.getEntity() && target != null) attacked = target;
        final IPokemob attackedMob = CapabilityPokemob.getPokemobFor(attacked);
        if (attacked instanceof MobEntity) ((MobEntity) attacked).setAttackTarget(null);
        else if (attackedMob != null) attackedMob.getEntity().setAttackTarget(null);
        super.postAttack(packet);
    }
}
