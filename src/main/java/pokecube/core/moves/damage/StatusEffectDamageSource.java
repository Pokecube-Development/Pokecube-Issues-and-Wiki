/**
 *
 */
package pokecube.core.moves.damage;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import thut.api.IOwnable;
import thut.api.OwnableCaps;

/**
 * This class extends {@link EntityDamageSource} and only modifies the death
 * message.
 *
 * @author Manchou
 */
public class StatusEffectDamageSource extends DamageSource implements IPokedamage
{

    private final LivingEntity sourceMob;
    /**
     * This is the type of the used move, can be different from
     * move.getType()
     */
    public IPokemob            user;

    /**
     * @param par1Str
     * @param mob
     */
    public StatusEffectDamageSource(final LivingEntity mob)
    {
        super("mob");
        this.sourceMob = mob;
        this.user = CapabilityPokemob.getPokemobFor(mob);
    }

    @Override
    public ITextComponent getDeathMessage(final LivingEntity died)
    {
        final ItemStack localObject = this.sourceMob != null ? this.sourceMob.getHeldItemMainhand()
                : ItemStack.EMPTY;
        if (!localObject.isEmpty() && localObject.hasDisplayName()) return new TranslationTextComponent("death.attack."
                + this.damageType, new Object[] { died.getDisplayName(), this.sourceMob
                        .getDisplayName(), localObject.getTextComponent() });
        final IPokemob sourceMob = CapabilityPokemob.getPokemobFor(this.sourceMob);
        if (sourceMob != null && sourceMob.getOwner() != null)
        {
            final TranslationTextComponent message = new TranslationTextComponent("pokemob.killed.tame",
                    died.getDisplayName(), sourceMob.getOwner().getDisplayName(), this.sourceMob
                            .getDisplayName());
            return message;
        }
        else if (sourceMob != null && sourceMob.getOwner() == null && !sourceMob.getGeneralState(GeneralStates.TAMED))
        {
            final TranslationTextComponent message = new TranslationTextComponent("pokemob.killed.wild",
                    died.getDisplayName(), this.sourceMob.getDisplayName());
            return message;
        }
        return new TranslationTextComponent("death.attack." + this.damageType, new Object[] { died
                .getDisplayName(), this.sourceMob.getDisplayName() });
    }

    @Nullable
    @Override
    public Entity getImmediateSource()
    {
        return this.sourceMob;
    }

    @Override
    public Entity getTrueSource()
    {
        final IPokemob sourceMob = CapabilityPokemob.getPokemobFor(this.sourceMob);
        if (sourceMob != null && sourceMob.getOwner() != null) return sourceMob.getOwner();
        final IOwnable ownable = OwnableCaps.getOwnable(this.sourceMob);
        if (ownable != null)
        {
            final Entity owner = ownable.getOwner();
            return owner != null ? owner : this.sourceMob;
        }
        return this.sourceMob;
    }

    @Override
    /** Returns true if the damage is projectile based. */
    public boolean isProjectile()
    {
        return false;
    }
}
