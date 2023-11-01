package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.core.moves.MovesUtils;
@AbilityProvider(name = "swift-swim")
public class SwiftSwim extends Ability {
    @Override
    public void onAgress(IPokemob mob, LivingEntity target) {
        final Level world = mob.getEntity().getLevel();
        final boolean rain = world.isRaining();
        if (rain) {
            byte boost = IMoveConstants.VIT;
            MovesUtils.handleStats2(mob, mob.getOwner(), boost, IMoveConstants.RAISE);
        }
    }
}
