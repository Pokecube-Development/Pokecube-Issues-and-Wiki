package pokecube.core.utils;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IExtensibleEnum;
import thut.core.common.ThutCore;

public enum PokeType implements IExtensibleEnum
{
    unknown(0, "???");

    public static float[][] typeTable;

    public static PokeType create(final String dummy, final int colour, final String name)
    {
        throw new IllegalStateException("Enum not extended");
    }

    public static float getAttackEfficiency(final PokeType type, final PokeType defenseType1,
            final PokeType defenseType2)
    {
        float multiplier = 1;
        if (type == null) return multiplier;
        if (defenseType1 != unknown && defenseType1 != null) multiplier *= PokeType.typeTable[type
                .ordinal()][defenseType1.ordinal()];
        if (defenseType2 != unknown && defenseType2 != null) multiplier *= PokeType.typeTable[type
                .ordinal()][defenseType2.ordinal()];
        return multiplier;
    }

    public static String getName(final PokeType type)
    {
        return type.name;
    }

    public static String getUnlocalizedName(final PokeType type)
    {
        return "type." + type.name;
    }

    @OnlyIn(Dist.CLIENT)
    public static IFormattableTextComponent getTranslatedName(final PokeType type)
    {
        IFormattableTextComponent ret = new StringTextComponent(type.name);
        final String translated = I18n.format(PokeType.getUnlocalizedName(type));
        if (translated != null && !translated.startsWith("type.")) ret = new TranslationTextComponent(PokeType
                .getUnlocalizedName(type));
        ret.setStyle(ret.getStyle().setColor(Color.fromInt(type.colour)));
        return ret;
    }

    public static PokeType getType(String name)
    {
        name = ThutCore.trim(name);
        for (final PokeType type : PokeType.values())
            if (name.equalsIgnoreCase(type.name)) return type;
        return unknown;
    }

    public final int colour;

    public final String name;

    private PokeType(final int colour, final String name)
    {
        this.colour = colour;
        this.name = name;
    }
}
