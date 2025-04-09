package pokecube.core.entity.pokemobs.helper;

import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.core.PokecubeCore;
import pokecube.core.moves.damage.PokemobDamageSource;

public abstract class PokemobCombat extends PokemobBase
{

    public PokemobCombat(final EntityType<? extends TamableAnimal> type, final Level worldIn)
    {
        super(type, worldIn);
    }

    @Override
    public boolean isInvulnerableTo(final DamageSource source)
    {
        // Check type effectiveness for damage sources.
        if (source instanceof PokemobDamageSource psource) return psource.getEffectiveness(this.getPokemob()) <= 0;
        return super.isInvulnerableTo(source);
    }

    @Override
    public int getArmorValue()
    {
        return (int) (this.getPokemob().getStat(Stats.DEFENSE, true) / 12.5);
    }

    @Override
    protected float getDamageAfterArmorAbsorb(final DamageSource source, float damage)
    {
        if (!(source instanceof PokemobDamageSource))
        {
            int armour = 0;
            if (source.is(DamageTypes.MAGIC)) armour = (int) (this.getPokemob().getStat(Stats.SPDEFENSE, true) / 12.5);
            else armour = this.getArmorValue();
            damage = CombatRules.getDamageAfterAbsorb(this, damage, source, armour,
                    (float) this.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue());
        }
        return damage;
    }

    @Override
    protected void dropExperience(Entity harmer)
    {
        if (!this.level.isClientSide
                && (this.isAlwaysExperienceDropper() || this.lastHurtByPlayerTime > 0 && this.shouldDropExperience()
                        && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT))
                && this.getPokemob().getOwnerId() == null)
        {
            int i = this.getBaseExperienceReward();
            i = EventHooks.getExperienceDrop(this, this.lastHurtByPlayer, i);
            while (i > 0)
            {
                final int j = ExperienceOrb.getExperienceValue(i);
                i -= j;
                this.level.addFreshEntity(new ExperienceOrb(this.level, this.getX(), this.getY(), this.getZ(), j));
            }
        }
    }

    @Override
    /** Get the experience points the entity currently has. */
    public int getBaseExperienceReward()
    {
        final float scale = (float) PokecubeCore.getConfig().expFromDeathDropScale;
        final int exp = (int) Math.max(1,
                this.getPokemob().getBaseXP() * scale * 0.01 * Math.sqrt(this.getPokemob().getLevel()));
        return exp;
    }
}
