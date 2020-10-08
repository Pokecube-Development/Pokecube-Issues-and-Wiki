package pokecube.mobs.abilities.p;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.moves.MovePacket;

public class PunkRock extends Ability
{
	private static final String[] Sounds = { "Boomburst", "BugBuzz", "DisarmingVoice", "EchoedVoice", "Overdrive",
            "RelicSong", "Round", "Snarl", "SparklingAria", "Uproar"};

 /*   @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
	    if (move.pre && mob == move.attacked) for (final String s : PunkRock.Sounds)
        if (s.equalsIgnoreCase(move.attack))
        {
            move.PWR *= 0.3;
            return;
        }
    }*/
}
