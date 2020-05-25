package pokecube.core.moves.damage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.interfaces.IPokemob;

public class TerrainDamageSource extends DamageSource implements IPokedamage
{
    public static enum TerrainType
    {
        MATERIAL, TERRAIN;
    }

    public final TerrainType type;

    public final IPokemob user;

    public TerrainDamageSource(final String damageTypeIn, final TerrainType type, final IPokemob user)
    {
        super(damageTypeIn);
        this.type = type;
        this.user = user;
    }

    @Override
    /** Gets the death message that is displayed when the player dies */
    public ITextComponent getDeathMessage(final LivingEntity LivingEntityIn)
    {
        final String s = "death.attack." + this.damageType;
        return new TranslationTextComponent(s, LivingEntityIn.getDisplayName());
    }

    @Override
    public Entity getImmediateSource()
    {
        if (this.user != null) return this.user.getEntity();
        return super.getImmediateSource();
    }

    @Override
    public Entity getTrueSource()
    {
        if (this.user != null)
        {
            Entity source = this.user.getEntity();
            if (this.user.getOwner() != null) source = this.user.getOwner();
            return source;
        }
        return super.getTrueSource();
    }
}
