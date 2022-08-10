package pokecube.mobs.abilities.b;

import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.core.database.abilities.Ability;

public class BulletProof extends Ability
{
    private static final String[] bullets = { "AcidSpray", "AuraSphere", "Barrage", "BulletSeed", "EggBomb",
            "ElectroBall", "EnergyBall", "FocusBlast", "GyroBall", "IceBall", "MagnetBomb", "MistBall", "MudBomb",
            "Octazooka", "RockWrecker", "SearingShot", "SeedBomb", "ShadowBall", "SludgeBomb", "WeatherBall",
            "ZapCannon" };

    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (move.pre && mob == move.attacked) for (final String s : BulletProof.bullets)
            if (s.equalsIgnoreCase(move.attack))
            {
                move.canceled = true;
                return;
            }
    }
}
