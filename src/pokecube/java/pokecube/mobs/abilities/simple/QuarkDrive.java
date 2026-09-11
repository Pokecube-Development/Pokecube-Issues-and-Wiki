package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;

@AbilityProvider(name = "quark-drive")
public class QuarkDrive extends Ability
{
    @Override
    public void onAgress(IPokemob mob, LivingEntity target)
    {
        final TerrainSegment segment = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemob_effects");
        final boolean electricTerrain = teffect.isEffectActive(PokemobTerrainEffects.TerrainEffectType.ELECTRIC);

        if (electricTerrain) {
            var boost = PokecubeAttributes.ATTACK;
            int stat = mob.getStat(Stats.ATTACK, true);
            int tmp;
            double increaseRatio = 0.3;
            if ((tmp = mob.getStat(Stats.SPATTACK, true)) > stat) {
                stat = tmp;
                boost = PokecubeAttributes.SPATTACK;
            }
            if ((tmp = mob.getStat(Stats.DEFENSE, true)) > stat) {
                stat = tmp;
                boost = PokecubeAttributes.DEFENSE;
            }
            if ((tmp = mob.getStat(Stats.SPDEFENSE, true)) > stat) {
                stat = tmp;
                boost = PokecubeAttributes.SPDEFENSE;
            }
            if ((tmp = mob.getStat(Stats.VIT, true)) > stat) {
                stat = tmp;
                boost = PokecubeAttributes.VIT;
                increaseRatio = 0.5;
            }
            var attr = mob.getEntity().getAttribute(boost);
            attr.addOrReplacePermanentModifier(new AttributeModifier(PokecubeAttributes.ABILITY_STAT_MOD, increaseRatio, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    @Override
    public void endCombat(IPokemob mob)
    {
        var attr = mob.getEntity().getAttribute(PokecubeAttributes.ATTACK);
        attr.removeModifier(PokecubeAttributes.ABILITY_STAT_MOD);
    }

    @Override
    public void onRecall(IPokemob mob)
    {
        var attr = mob.getEntity().getAttribute(PokecubeAttributes.ATTACK);
        attr.removeModifier(PokecubeAttributes.ABILITY_STAT_MOD);
    }
}
