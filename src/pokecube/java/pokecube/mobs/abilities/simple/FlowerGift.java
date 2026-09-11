package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.database.Database;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;

@AbilityProvider(name = "flower-gift")
public class FlowerGift extends Ability
{
    private static PokedexEntry overcast = Database.getEntry("cherrim-overcast");
    private static PokedexEntry sunshine = Database.getEntry("cherrim-sunshine");

    @Override
    public void onUpdate(IPokemob mob)
    {
        final TerrainSegment segment = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemob_effects");

        if (teffect.isEffectActive(PokemobTerrainEffects.WeatherEffectType.SUN))
            mob.changeForm(sunshine);
        else
            mob.changeForm(overcast);
    }

    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        if (!areWeUser(mob, move)) return;
        final TerrainSegment segment = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemob_effects");

        if (teffect.isEffectActive(PokemobTerrainEffects.WeatherEffectType.SUN))
        {
            var attackAttr = mob.getEntity().getAttribute(PokecubeAttributes.ATTACK);
            var spdefenseAttr = mob.getEntity().getAttribute(PokecubeAttributes.SPDEFENSE);
            attackAttr.addOrReplacePermanentModifier(new AttributeModifier(PokecubeAttributes.ABILITY_STAT_MOD, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            spdefenseAttr.addOrReplacePermanentModifier(new AttributeModifier(PokecubeAttributes.ABILITY_STAT_MOD, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }
}
