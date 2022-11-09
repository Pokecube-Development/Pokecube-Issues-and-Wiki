package pokecube.mobs.moves.world;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveWorldEffect;
import pokecube.core.PokecubeCore;
import thut.api.entity.IHungrymob;
import thut.api.maths.Vector3;

public class ActionFlash implements IMoveWorldEffect
{
    public ActionFlash()
    {
    }

    @Override
    public boolean applyOutOfCombat(final IPokemob user, final Vector3 location)
    {
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
