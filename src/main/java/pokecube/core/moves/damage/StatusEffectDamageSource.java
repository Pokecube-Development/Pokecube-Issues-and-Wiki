/**
 *
 */
package pokecube.core.moves.damage;

import javax.annotation.Nullable;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import thut.api.IOwnable;
import thut.api.OwnableCaps;
import thut.lib.TComponent;

/**
 * This class extends {@link DamageSource} and only modifies the death message.
 *
 * @author Manchou
 */
public class StatusEffectDamageSource extends DamageSource implements IPokedamage
{

    private final LivingEntity sourceMob;
    /**
     * This is the type of the used move, can be different from move.getType()
     */
    public IPokemob user;

    /**
     * @param par1Str
     * @param mob
     */
    public StatusEffectDamageSource(final LivingEntity mob)
    {
        super(mob.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.MOB_ATTACK));
        this.sourceMob = mob;
        this.user = PokemobCaps.getPokemobFor(mob);
    }

    // TODO: Check this
    @Override
    public Component getLocalizedDeathMessage(final LivingEntity died)
    {
        final ItemStack localObject = this.sourceMob != null ? this.sourceMob.getMainHandItem() : ItemStack.EMPTY;
        if (!localObject.isEmpty() && localObject.hasCustomHoverName())
            return TComponent.translatable("death.attack." + this.type().msgId(), new Object[]
            { died.getDisplayName(), this.sourceMob.getDisplayName(), localObject.getDisplayName() });
        final IPokemob sourceMob = PokemobCaps.getPokemobFor(this.sourceMob);
        if (sourceMob != null && sourceMob.getOwner() != null)
        {
            final MutableComponent message = TComponent.translatable("pokemob.killed.tame", died.getDisplayName(),
                    sourceMob.getOwner().getDisplayName(), this.sourceMob.getDisplayName());
            return message;
        }
        else if (sourceMob != null && sourceMob.getOwner() == null && !sourceMob.getGeneralState(GeneralStates.TAMED))
        {
            final MutableComponent message = TComponent.translatable("pokemob.killed.wild", died.getDisplayName(),
                    this.sourceMob.getDisplayName());
            return message;
        }
        return TComponent.translatable("death.attack." + this.type().msgId(), new Object[]
        { died.getDisplayName(), this.sourceMob.getDisplayName() });
    }

    @Nullable
    @Override
    public Entity getDirectEntity()
    {
        return this.sourceMob;
    }

    @Override
    public Entity getEntity()
    {
        final IPokemob sourceMob = PokemobCaps.getPokemobFor(this.sourceMob);
        if (sourceMob != null && sourceMob.getOwner() != null) return sourceMob.getOwner();
        final IOwnable ownable = OwnableCaps.getOwnable(this.sourceMob);
        if (ownable != null)
        {
            final Entity owner = ownable.getOwner();
            return owner != null ? owner : this.sourceMob;
        }
        return this.sourceMob;
    }

    // TODO: Check for replacement
    // @Override
    /** Returns true if the damage is projectile based. */
    public boolean isProjectile()
    {
        return false;
    }
}
