package pokecube.mobs.moves.attacks;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.IOngoingAffected;
import pokecube.api.entity.IOngoingAffected.IOngoingEffect;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.damage.sources.PokecubeDamageSources;
import pokecube.core.moves.templates.Move_Ongoing;

public class Perishsong extends Move_Ongoing
{
    public static final ResourceKey<DamageType> PERISH_SONG;
    static
    {
        PERISH_SONG = ResourceKey.create(Registries.DAMAGE_TYPE,
                ResourceLocation.parse("pokecube_mobs:perish_song"));
    }
    @Override
    public void doOngoingEffect(final LivingEntity user, final IOngoingAffected mob, final IOngoingEffect effect)
    {
        if (effect.getDuration() == 0) this.damageTarget(mob.getEntity(), user, Integer.MAX_VALUE);
        else
        {
            // TODO perish counter here.
        }
    }

    @Override
    protected DamageSource getOngoingDamage(final LivingEntity user)
    {
        return new DamageSource(PokecubeCore.proxy.getRegistries().holderOrThrow(PERISH_SONG), user);
    }

    @Override
    public int getDuration()
    {
        return 3;
    }
}
