package pokecube.mobs.abilities.simple;

import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.moves.Move_Base;
import pokecube.core.database.moves.MoveEntry.Category;

public class IceScales extends Ability
{
    @Override
    public void onMoveUse(final IPokemob mob, final MovePacket move)
    {
        final Move_Base attack = move.getMove();

        final IPokemob attacker = move.attacker;
        if (attacker == mob || move.pre || attacker == move.attacked) return;
        if (move.hit && attack.getCategory(move.attacker) == Category.SPECIAL) move.PWR = move.PWR / 2;
    }
}
