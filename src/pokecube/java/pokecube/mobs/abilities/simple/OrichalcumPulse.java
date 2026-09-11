package pokecube.mobs.abilities.simple;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.moves.PokemobTerrainEffects;
import pokecube.core.moves.damage.attributes.PokecubeAttributes;
import thut.api.Tracker;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;
import thut.core.common.ThutCore;
import thut.core.common.network.TerrainUpdate;

@AbilityProvider(name = "orichalcum-pulse")
public class OrichalcumPulse extends Ability
{
    @Override
    public void onAgress(IPokemob mob, LivingEntity target)
    {
        final IPokemob targetMob = PokemobCaps.getPokemobFor(target);
        if (targetMob != null)
        {
            final Level world = mob.getEntity().level();
            final TerrainSegment segment = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
            final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemob_effects");

            int duration = 300 + ThutCore.newRandom().nextInt(600);
            teffect.setEffectDuration(PokemobTerrainEffects.WeatherEffectType.SUN,
                    duration + Tracker.instance().getTick(), mob);
            if (world instanceof ServerLevel) TerrainUpdate.sendTerrainToWatching(segment);
        }
    }

    @Override
    public void preMoveUse(final IPokemob mob, final MoveApplication move)
    {
        final TerrainSegment segment = TerrainManager.getInstance().getTerrainForEntity(mob.getEntity());
        final PokemobTerrainEffects teffect = (PokemobTerrainEffects) segment.geTerrainEffect("pokemob_effects");
        if (!areWeUser(mob, move)) return;

        var attr = mob.getEntity().getAttribute(PokecubeAttributes.ATTACK);

        if (teffect.isEffectActive(PokemobTerrainEffects.WeatherEffectType.SUN) && !attr.hasModifier(PokecubeAttributes.ABILITY_STAT_MOD))
        {
            attr.addOrReplacePermanentModifier(new AttributeModifier(PokecubeAttributes.ABILITY_STAT_MOD, 5461.0/4096.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else if (attr.hasModifier(PokecubeAttributes.ABILITY_STAT_MOD)) {
            attr.removeModifier(PokecubeAttributes.ABILITY_STAT_MOD);
        }
    }
}