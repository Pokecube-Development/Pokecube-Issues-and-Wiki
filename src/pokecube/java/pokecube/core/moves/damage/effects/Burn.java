package pokecube.core.moves.damage.effects;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.damage.StatusEffectDamageSource;

public class Burn extends StatusEffect
{
    public static ResourceLocation MODIFIER = ResourceLocation.parse("pokecube:effect.burn");

    public Burn(int color)
    {
        super(MobEffectCategory.HARMFUL, color, StatusEffects.BURN);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, MODIFIER, -0.5,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier)
    {
        if (!super.applyEffectTick(entity, amplifier)) return false;
        final IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        boolean toRemove = pokemob == null && Math.random() > 0.8;
        if (entity.level() instanceof ServerLevel level)
        {
            LivingEntity damageSource = StatusEffects.getSource(StatusEffects.BURN, entity, level);
            float scale = 1;
            final IPokemob user = PokemobCaps.getPokemobFor(damageSource);
            final DamageSource source = new StatusEffectDamageSource(damageSource);
            if (entity instanceof Player) scale = (float) (user != null && user.isPlayerOwned()
                    ? PokecubeCore.getConfig().ownedPlayerDamageRatio
                    : PokecubeCore.getConfig().wildPlayerDamageRatio);
            else if (pokemob == null) scale = (float) (entity instanceof Npc
                    ? PokecubeCore.getConfig().pokemobToNPCDamageRatio
                    : PokecubeCore.getConfig().pokemobToOtherMobDamageRatio);
            if (scale <= 0) toRemove = true;
            else entity.hurt(source, scale * entity.getMaxHealth() / 16f);
            return !toRemove;
        }
        return true;
    }
}
