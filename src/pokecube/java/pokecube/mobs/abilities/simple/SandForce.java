package pokecube.mobs.abilities.simple;

import net.minecraft.world.level.Level;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.utils.PokeType;
import pokecube.core.moves.PokemobTerrainEffects;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;
import thut.api.maths.Vector3;

@AbilityProvider(name = "sand-force")
public class SandForce extends Ability
{
    @Override
    // Apply 30% rock, steel and ground move boost in the sand.
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        final Level world = mob.getEntity().level();
        final TerrainSegment segment = TerrainManager.getInstance().getTerrian(world, new Vector3());
        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemob_effects");

        if (!areWeUser(mob, move)) return;

        if (teffect.isEffectActive(PokemobTerrainEffects.WeatherEffectType.SAND) && ( mob.isType(PokeType.getType("ground")) || mob.isType(PokeType.getType("rock")) || mob.isType(PokeType.getType("steel")) ))
            move.pwr *= 1.3;

    }
}
