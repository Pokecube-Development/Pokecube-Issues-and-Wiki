/**
 *
 */
package pokecube.core.moves.damage.sources;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import thut.api.ThutCaps;
import thut.api.attachments.IOwnable;

import javax.annotation.Nullable;

/**
 * This class extends {@link DamageSource} and only modifies the death message.
 *
 * @author Manchou
 */
public class StatusEffectDamageSource extends DamageSource
{

    private final LivingEntity sourceMob;
    /**
     * This is the type of the used move, can be different from move.getType()
     */
    public IPokemob user;

    /**
     *
     */
    public StatusEffectDamageSource(final LivingEntity mob)
    {
        super(PokecubeDamageSources.pokemobStatus(), mob);
        this.sourceMob = mob;
        this.user = PokemobCaps.getPokemobFor(mob);
    }

    @Override
    public Component getLocalizedDeathMessage(final LivingEntity died)
    {
        final ItemStack usedItem = this.sourceMob != null ? this.sourceMob.getMainHandItem() : ItemStack.EMPTY;

        if (!usedItem.isEmpty() && usedItem.has(DataComponents.CUSTOM_NAME))
            return Component.translatableEscape("death.attack." + this.type().msgId(), died.getDisplayName(),
                    this.sourceMob.getDisplayName(), usedItem.getDisplayName());
        final IPokemob sourceMob = PokemobCaps.getPokemobFor(this.sourceMob);
        if (sourceMob != null && sourceMob.getOwner() != null)
        {
            return Component.translatableEscape("pokemob.killed.tame", died.getDisplayName(),
                    sourceMob.getOwner().getDisplayName(), this.sourceMob.getDisplayName());
        }
        else if (sourceMob != null && sourceMob.getOwner() == null && !sourceMob.getGeneralState(GeneralStates.TAMED))
        {
            return Component.translatableEscape("pokemob.killed.wild", died.getDisplayName(),
                    this.sourceMob.getDisplayName());
        }
        return Component.translatableEscape("death.attack." + this.type().msgId(), died.getDisplayName(),
                this.sourceMob.getDisplayName());
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
        final IOwnable ownable = ThutCaps.getOwnable(this.sourceMob);
        if (ownable != null)
        {
            final Entity owner = ownable.getOwner();
            return owner != null ? owner : this.sourceMob;
        }
        return this.sourceMob;
    }
}
