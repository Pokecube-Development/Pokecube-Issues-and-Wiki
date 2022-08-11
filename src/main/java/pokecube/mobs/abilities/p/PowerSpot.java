package pokecube.mobs.abilities.p;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.utils.EntityTools;

public class PowerSpot extends Ability
{
    @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
        final LivingEntity target = EntityTools.getCoreLiving(move.attacked);
        if (target == null) return;
        final IPokemob targetMob = PokemobCaps.getPokemobFor(target);
        if (targetMob == null) return;

        final IPokemob attacker = move.attacker;

        if (move.pre || attacker == move.attacked) return;
        if (targetMob != null) move.PWR *= 1.3;
    }
}
