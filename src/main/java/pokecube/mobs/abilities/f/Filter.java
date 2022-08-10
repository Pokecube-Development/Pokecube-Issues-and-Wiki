package pokecube.mobs.abilities.f;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.database.abilities.Ability;
import pokecube.core.utils.PokeType;

public class Filter extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (mob == move.attacked && move.pre) if (PokeType.getAttackEfficiency(move.attackType, mob.getType1(), mob
                .getType2()) > 1) move.superEffectMult = 0.75f;
    }
}
