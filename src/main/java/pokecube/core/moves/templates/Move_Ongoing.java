package pokecube.core.moves.templates;

import java.util.Random;

import net.minecraft.entity.INPC;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.entity.IOngoingAffected;
import pokecube.core.interfaces.entity.IOngoingAffected.IOngoingEffect;
import pokecube.core.interfaces.entity.impl.OngoingMoveEffect;

public class Move_Ongoing extends Move_Basic
{

    public Move_Ongoing(String name)
    {
        super(name);
    }

    protected float damageTarget(LivingEntity mob, DamageSource source, float damage)
    {
        LivingEntity target = mob.getAttackingEntity();
        if (target == null) target = mob.getRevengeTarget();
        if (target == null) target = mob.getLastAttackedEntity();
        if (target == null) target = mob;
        final IPokemob user = CapabilityPokemob.getPokemobFor(target);
        float scale = 1;
        if (source == null) source = user != null && user.getOwner() != null ? DamageSource.causeIndirectDamage(
                target, user.getOwner())
                : target != null ? DamageSource.causeMobDamage(target) : new DamageSource("generic");
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob != null)
        {
            source.setDamageIsAbsolute();
            source.setDamageBypassesArmor();
        }
        else if (mob instanceof PlayerEntity) scale = (float) (user != null && user.isPlayerOwned() ? PokecubeCore
                .getConfig().ownedPlayerDamageRatio : PokecubeCore.getConfig().wildPlayerDamageRatio);
        else scale = (float) (mob instanceof INPC ? PokecubeCore.getConfig().pokemobToNPCDamageRatio
                : PokecubeCore.getConfig().pokemobToOtherMobDamageRatio);
        damage *= scale;
        mob.attackEntityFrom(source, damage);
        return damage;
    }

    public void doOngoingEffect(IOngoingAffected mob, IOngoingEffect effect)
    {
        final float thisMaxHP = mob.getEntity().getMaxHealth();
        final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
        mob.getEntity().attackEntityFrom(this.getOngoingDamage(mob.getEntity()), damage);
    }

    /**
     * I have these attacks affecting the target roughly once per 40 ticks,
     * this duration is how many times it occurs -1 can be used for a move that
     * occurs until the mob dies or returns to cube.
     *
     * @return the number of times this can affect the target
     */

    public int getDuration()
    {
        final Random r = new Random();
        return 4 + r.nextInt(2);
    }

    protected DamageSource getOngoingDamage(LivingEntity mob)
    {
        LivingEntity target = mob.getAttackingEntity();
        if (target == null) target = mob.getRevengeTarget();
        if (target == null) target = mob.getLastAttackedEntity();
        if (target == null) target = mob;
        final DamageSource source = DamageSource.causeMobDamage(target);
        if (CapabilityPokemob.getPokemobFor(mob) != null)
        {
            source.setDamageIsAbsolute();
            source.setDamageBypassesArmor();
        }
        return source;
    }

    public OngoingMoveEffect makeEffect()
    {
        final OngoingMoveEffect effect = new OngoingMoveEffect();
        effect.setDuration(this.getDuration());
        effect.move = this;
        return effect;
    }

    /**
     * Does this apply an ongoing move to the attacker
     *
     * @return
     */
    public boolean onSource()
    {
        return false;
    }

    /**
     * Is and ongoing move applied to the source
     *
     * @return
     */
    public boolean onTarget()
    {
        return true;
    }
}
