package pokecube.mobs.abilities.p;

import net.minecraft.entity.LivingEntity;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;
import pokecube.core.utils.EntityTools;

public class PowerSpot extends Ability
{
    @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
        final LivingEntity target = EntityTools.getCoreLiving(move.attacked);
        if (target == null) return;
        final IPokemob targetMob = CapabilityPokemob.getPokemobFor(target);
        if (targetMob == null) return;

        final IPokemob attacker = move.attacker;

        if (move.pre || attacker == move.attacked) return;
        if (targetMob != null) move.PWR *= 1.3;
    }
}
