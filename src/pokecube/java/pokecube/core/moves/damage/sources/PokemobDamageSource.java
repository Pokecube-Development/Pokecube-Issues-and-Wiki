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
import net.minecraft.world.phys.Vec3;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.moves.MoveEntry;
import pokecube.api.utils.PokeType;
import pokecube.api.utils.Tools;
import thut.api.ThutCaps;
import thut.api.attachments.IOwnable;
import thut.lib.TComponent;

import javax.annotation.Nullable;

/**
 * This class extends {@link DamageSource} and only modifies the death message.
 *
 * @author Manchou
 */
public class PokemobDamageSource extends DamageSource implements IPokedamage
{

    private final LivingEntity damageSourceEntity;
    public MoveEntry move;
    /**
     * This is the type of the used move, can be different from move.getType()
     */
    private PokeType moveType = null;
    public IPokemob user;

    /**
     *
     */
    public PokemobDamageSource(final LivingEntity source, final MoveEntry move)
    {
        super(move.isRanged(PokemobCaps.getPokemobFor(source))
                ? PokecubeDamageSources.pokemobAttackRanged()
                : PokecubeDamageSources.pokemobAttackContact(), source);
        this.damageSourceEntity = source;
        this.user = PokemobCaps.getPokemobFor(source);
        this.move = move;
    }

    @Override
    public Component getLocalizedDeathMessage(final LivingEntity died)
    {
        final ItemStack usedItem =
                this.damageSourceEntity != null ? this.damageSourceEntity.getMainHandItem() : ItemStack.EMPTY;
        if (!usedItem.isEmpty() && usedItem.has(DataComponents.CUSTOM_NAME))
            return TComponent.translatable("death.attack." + this.type().msgId(), died.getDisplayName(),
                    this.damageSourceEntity.getDisplayName(), usedItem.getDisplayName());
        final IPokemob sourceMob = PokemobCaps.getPokemobFor(this.damageSourceEntity);
        if (sourceMob != null && sourceMob.getOwner() != null)
        {
            return TComponent.translatable("pokemob.killed.tame", died.getDisplayName(),
                    sourceMob.getOwner().getDisplayName(), this.damageSourceEntity.getDisplayName());
        }
        else if (sourceMob != null && sourceMob.getOwner() == null && !sourceMob.getGeneralState(GeneralStates.TAMED))
        {
            return TComponent.translatable("pokemob.killed.wild", died.getDisplayName(),
                    this.damageSourceEntity.getDisplayName());
        }
        return TComponent.translatable("death.attack." + this.type().msgId(), died.getDisplayName(),
                this.damageSourceEntity.getDisplayName());
    }

    public float getEffectiveness(final IPokemob pokemobCap)
    {
        return Tools.getAttackEfficiency(this.getType(), pokemobCap.getType1(), pokemobCap.getType2());
    }

    @Nullable
    @Override
    public Entity getDirectEntity()
    {
        return this.damageSourceEntity;
    }

    @Override
    public Vec3 getSourcePosition()
    {
        return this.getDirectEntity().position();
    }

    @Override
    public Entity getEntity()
    {
        final IPokemob sourceMob = PokemobCaps.getPokemobFor(this.damageSourceEntity);
        if (sourceMob != null && sourceMob.getOwner() != null) return sourceMob.getOwner();
        final IOwnable ownable = ThutCaps.getOwnable(this.damageSourceEntity);
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

    public PokemobDamageSource setType(final PokeType type)
    {
        this.moveType = type;
        return this;
    }
}
