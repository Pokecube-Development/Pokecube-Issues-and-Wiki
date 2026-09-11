package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;

@AbilityProvider(name = "snow-cloak")
public class SnowCloak extends Ability
{
    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        final TerrainSegment segment = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemob_effects");
        if (!areWeUser(mob, move)) return;

        var attr = mob.getEntity().getAttribute(PokecubeAttributes.EVASION);

        if (teffect.isEffectActive(PokemobTerrainEffects.WeatherEffectType.HAIL) && !attr.hasModifier(PokecubeAttributes.ABILITY_STAT_MOD))
        {
            attr.addOrReplacePermanentModifier(new AttributeModifier(PokecubeAttributes.ABILITY_STAT_MOD, 1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else if (attr.hasModifier(PokecubeAttributes.ABILITY_STAT_MOD)) {
            attr.removeModifier(PokecubeAttributes.ABILITY_STAT_MOD);
        }
    }
}