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
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.utils.PokeType;
import thut.api.IOwnable;
import thut.api.OwnableCaps;

/**
 * This class extends {@link EntityDamageSource} and only modifies the death
 * message.
 *
 * @author Manchou
 */
public class PokemobDamageSource extends DamageSource implements IPokedamage
{

    private final LivingEntity damageSourceEntity;
    // TODO use this for damage stuff
    public Move_Base move;
    /**
     * This is the type of the used move, can be different from
     * move.getType()
     */
    private PokeType moveType = null;
    public IPokemob  user;

    /**
     * @param par1Str
     * @param par2Entity
     */
    public PokemobDamageSource(final LivingEntity par2Entity, final Move_Base type)
    {
        super("mob");
        this.damageSourceEntity = par2Entity;
        this.user = CapabilityPokemob.getPokemobFor(par2Entity);
        this.move = type;
    }

    @Override
    public ITextComponent getLocalizedDeathMessage(final LivingEntity par1PlayerEntity)
    {
        final ItemStack localObject = this.damageSourceEntity != null ? this.damageSourceEntity.getMainHandItem()
                : ItemStack.EMPTY;
        if (!localObject.isEmpty() && localObject.hasCustomHoverName()) return new TranslationTextComponent("death.attack."
                + this.msgId, new Object[] { par1PlayerEntity.getDisplayName(), this.damageSourceEntity
                        .getDisplayName(), localObject.getDisplayName() });
        final IPokemob sourceMob = CapabilityPokemob.getPokemobFor(this.damageSourceEntity);
        if (sourceMob != null && sourceMob.getOwner() != null)
        {
            final TranslationTextComponent message = new TranslationTextComponent("pokemob.killed.tame",
                    par1PlayerEntity.getDisplayName(), sourceMob.getOwner().getDisplayName(), this.damageSourceEntity
                            .getDisplayName());
            return message;
        }
        else if (sourceMob != null && sourceMob.getOwner() == null && !sourceMob.getGeneralState(GeneralStates.TAMED))
        {
            final TranslationTextComponent message = new TranslationTextComponent("pokemob.killed.wild",
                    par1PlayerEntity.getDisplayName(), this.damageSourceEntity.getDisplayName());
            return message;
        }
        return new TranslationTextComponent("death.attack." + this.msgId, new Object[] { par1PlayerEntity
                .getDisplayName(), this.damageSourceEntity.getDisplayName() });
    }

    public float getEffectiveness(final IPokemob pokemobCap)
    {
        return PokeType.getAttackEfficiency(this.getType(), pokemobCap.getType1(), pokemobCap.getType2());
    }

    @Nullable
    @Override
    public Entity getDirectEntity()
    {
        return this.damageSourceEntity;
    }

    @Override
    public Entity getEntity()
    {
        final IPokemob sourceMob = CapabilityPokemob.getPokemobFor(this.damageSourceEntity);
        if (sourceMob != null && sourceMob.getOwner() != null) return sourceMob.getOwner();
        final IOwnable ownable = OwnableCaps.getOwnable(this.damageSourceEntity);
        if (ownable != null)
        {
            final Entity owner = ownable.getOwner();
            return owner != null ? owner : this.damageSourceEntity;
        }
        return this.damageSourceEntity;
    }

    public PokeType getType()
    {
        return this.moveType == null ? this.move.getType(this.user) : this.moveType;
    }

    @Override
    /** Returns true if the damage is projectile based. */
    public boolean isProjectile()
    {
        return (this.move.getAttackCategory() & IMoveConstants.CATEGORY_DISTANCE) != 0;
    }

    public PokemobDamageSource setType(final PokeType type)
    {
        this.moveType = type;
        return this;
    }
}
