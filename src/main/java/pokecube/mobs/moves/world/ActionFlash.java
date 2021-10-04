package pokecube.mobs.moves.world;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import thut.api.entity.IHungrymob;
import thut.api.maths.Vector3;

public class ActionFlash implements IMoveAction
{
    public ActionFlash()
    {
    }

    @Override
    public boolean applyEffect(final IPokemob user, final Vector3 location)
    {
        if (user.inCombat()) return false;
        final LivingEntity owner = user.getOwner();
        if (owner == null) return false;
        final IHungrymob mob = user;
        int count = 1;
        final int level = user.getLevel();
        final int hungerValue = PokecubeCore.getConfig().pokemobLifeSpan / 16;
        count = (int) Math.max(1, Math.ceil(count * Math.pow((100 - level) / 100d, 3))) * hungerValue;
        final MobEffectInstance effect = new MobEffectInstance(MobEffects.NIGHT_VISION, 5000);
        owner.addEffect(effect);
        mob.applyHunger(count);
        return true;
    }

    @Override
    public String getMoveName()
    {
        return "flash";
    }
}
