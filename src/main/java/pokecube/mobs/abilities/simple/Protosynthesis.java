package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.core.moves.MovesUtils;
@AbilityProvider(name = "protosynthesis")
public class Protosynthesis extends Ability {
    @Override
    public void onAgress(IPokemob mob, LivingEntity target) {
        final Level world = mob.getEntity().getLevel();
        final boolean rain = world.isRaining();
        if (!rain) {
            byte boost = IMoveConstants.ATTACK;
            int stat = mob.getStat(Stats.ATTACK, true);
            int tmp;
            if ((tmp = mob.getStat(Stats.SPATTACK, true)) > stat) {
                stat = tmp;
                boost = IMoveConstants.SPATACK;
            }
            if ((tmp = mob.getStat(Stats.DEFENSE, true)) > stat) {
                stat = tmp;
                boost = IMoveConstants.DEFENSE;
            }
            if ((tmp = mob.getStat(Stats.SPDEFENSE, true)) > stat) {
                stat = tmp;
                boost = IMoveConstants.SPDEFENSE;
            }
            if ((tmp = mob.getStat(Stats.VIT, true)) > stat) {
                stat = tmp;
                boost = IMoveConstants.VIT;
            }
            MovesUtils.handleStats2(mob, mob.getOwner(), boost, IMoveConstants.RAISE);
        }
    }
}
