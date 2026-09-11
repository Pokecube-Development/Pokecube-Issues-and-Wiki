package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants.AttackCategory;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;

@AbilityProvider(name = "fur-coat")
public class FurCoat extends Ability
{
    @Override
    public void startCombat(IPokemob mob)
    {
        var attr = mob.getEntity().getAttribute(PokecubeAttributes.DEFENSE);
        attr.addOrReplacePermanentModifier(new AttributeModifier(PokecubeAttributes.ABILITY_STAT_MOD, 1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    @Override
    public void endCombat(IPokemob mob)
    {
        var attr = mob.getEntity().getAttribute(PokecubeAttributes.EVASION);
        attr.removeModifier(PokecubeAttributes.ABILITY_STAT_MOD);
    }

    @Override
    public void onRecall(IPokemob mob)
    {
        var attr = mob.getEntity().getAttribute(PokecubeAttributes.EVASION);
        attr.removeModifier(PokecubeAttributes.ABILITY_STAT_MOD);
    }
}
