package pokecube.core.moves.damage;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TerrainDamageSource extends DamageSource implements IPokedamage
{
    public static enum TerrainType
    {
        MATERIAL, TERRAIN;
    }

    public final TerrainType type;

    public TerrainDamageSource(final String damageTypeIn, final TerrainType type)
    {
        super(damageTypeIn);
        this.type = type;
    }

    @Override
    /** Gets the death message that is displayed when the player dies */
    public ITextComponent getDeathMessage(final LivingEntity LivingEntityIn)
    {
        final String s = "death.attack." + this.damageType;
        return new TranslationTextComponent(s, LivingEntityIn.getDisplayName());
    }
}
