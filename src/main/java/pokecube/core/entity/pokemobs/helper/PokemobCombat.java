package pokecube.core.entity.pokemobs.helper;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.passive.ShoulderRidingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.moves.damage.PokemobDamageSource;

public abstract class PokemobCombat extends PokemobBase
{

    public PokemobCombat(final EntityType<? extends ShoulderRidingEntity> type, final World worldIn)
    {
        super(type, worldIn);
    }

    @Override
    public boolean isInvulnerableTo(final DamageSource source)
    {
        // Check type effectiveness for damage sources.
        if (source instanceof PokemobDamageSource) return ((PokemobDamageSource) source).getEffectiveness(
                this.pokemobCap) <= 0;
        return super.isInvulnerableTo(source);
    }

    @Override
    public int getArmorValue()
    {
        return (int) (this.pokemobCap.getStat(Stats.DEFENSE, true) / 12.5);
    }

    @Override
    protected float getDamageAfterArmorAbsorb(final DamageSource source, float damage)
    {
        if (!(source instanceof PokemobDamageSource))
        {
            int armour = 0;
            if (source.isMagic()) armour = (int) (this.pokemobCap.getStat(Stats.SPDEFENSE, true) / 12.5);
            else armour = this.getArmorValue();
            damage = CombatRules.getDamageAfterAbsorb(damage, armour, (float) this.getAttribute(
                    Attributes.ARMOR_TOUGHNESS).getValue());
        }
        return damage;
    }

    @Override
    protected void dropExperience()
    {
        if (!this.level.isClientSide && (this.isAlwaysExperienceDropper() || this.lastHurtByPlayerTime > 0 && this.shouldDropExperience() && this.level
                .getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) && this.pokemobCap.getOwnerId() == null)
        {
            int i = this.getExperienceReward(this.lastHurtByPlayer);
            i = net.minecraftforge.event.ForgeEventFactory.getExperienceDrop(this, this.lastHurtByPlayer, i);
            while (i > 0)
            {
                final int j = ExperienceOrbEntity.getExperienceValue(i);
                i -= j;
                this.level.addFreshEntity(new ExperienceOrbEntity(this.level, this.getX(), this.getY(), this.getZ(),
                        j));
            }
        }
    }

    @Override
    /** Get the experience points the entity currently has. */
    protected int getExperienceReward(final PlayerEntity player)
    {
        final float scale = (float) PokecubeCore.getConfig().expFromDeathDropScale;
        final int exp = (int) Math.max(1, this.pokemobCap.getBaseXP() * scale * 0.01 * Math.sqrt(this.pokemobCap
                .getLevel()));
        return exp;
    }
}
