package pokecube.mobs.moves.attacks;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
        var target = mob.getEntity();
        if (effect.getDuration() == 0) this.damageTarget(target, user, Integer.MAX_VALUE);
        else
        {
            target.level().playSound(null, target, SoundEvents.APPLY_EFFECT_BAD_OMEN, SoundSource.HOSTILE,0.5f, 1);
            if(target instanceof ServerPlayer player){
                player.sendSystemMessage(Component.translatable("pokecube.perish.song.counter", effect.getDuration()));
            }
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
