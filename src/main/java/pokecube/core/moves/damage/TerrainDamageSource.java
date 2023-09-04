package pokecube.core.moves.damage;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import thut.lib.TComponent;

public class TerrainDamageSource extends DamageSource implements IPokedamage
{
    public static enum TerrainType
    {
        MATERIAL, TERRAIN;
    }

    public final TerrainType type;

    public final IPokemob user;

    public TerrainDamageSource(Holder<DamageType> damageType, final TerrainType type, final IPokemob user)
    {
        super(damageType);
        this.type = type;
        this.user = user;
    }

    @Override
    /** Gets the death message that is displayed when the player dies */
    public Component getLocalizedDeathMessage(final LivingEntity LivingEntityIn)
    {
        final String s = "death.attack." + this.getMsgId();
        return TComponent.translatable(s, LivingEntityIn.getDisplayName());
    }

    @Override
    public Entity getDirectEntity()
    {
        if (this.user != null) return this.user.getEntity();
        return super.getDirectEntity();
    }

    @Override
    public Entity getEntity()
    {
        if (this.user != null)
        {
            Entity source = this.user.getEntity();
            if (this.user.getOwner() != null) source = this.user.getOwner();
            return source;
        }
        return super.getEntity();
    }
}
