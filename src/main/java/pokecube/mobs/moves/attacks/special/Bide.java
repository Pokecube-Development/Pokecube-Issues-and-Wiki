package pokecube.mobs.moves.attacks.special;

import net.minecraft.entity.LivingEntity;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.moves.damage.PokemobDamageSource;
import pokecube.core.moves.templates.Move_Basic;
import thut.api.Tracker;

public class Bide extends Move_Basic
{

    public Bide()
    {
        super("bide");
    }

    @Override
    public void postAttack(final MovePacket packet)
    {
        super.postAttack(packet);
        if (packet.canceled || packet.failed) return;
        final LivingEntity attacker = packet.attacker.getEntity();
        if (!packet.attacker.getMoveStats().biding)
        {
            attacker.getPersistentData().putLong("bideTime", Tracker.instance().getTick() + PokecubeCore
                    .getConfig().attackCooldown * 5);
            packet.attacker.getMoveStats().biding = true;
            packet.attacker.getMoveStats().PHYSICALDAMAGETAKENCOUNTER = 0;
            packet.attacker.getMoveStats().SPECIALDAMAGETAKENCOUNTER = 0;
        }
        else if (attacker.getPersistentData().getLong("bideTime") < Tracker.instance().getTick())
        {
            attacker.getPersistentData().remove("bideTime");
            final int damage = packet.attacker.getMoveStats().PHYSICALDAMAGETAKENCOUNTER + packet.attacker
                    .getMoveStats().SPECIALDAMAGETAKENCOUNTER;
            packet.attacker.getMoveStats().PHYSICALDAMAGETAKENCOUNTER = 0;
            packet.attacker.getMoveStats().SPECIALDAMAGETAKENCOUNTER = 0;
            packet.attacked.hurt(new PokemobDamageSource(attacker, this), damage);
            packet.attacker.getMoveStats().biding = false;
        }
    }
}
