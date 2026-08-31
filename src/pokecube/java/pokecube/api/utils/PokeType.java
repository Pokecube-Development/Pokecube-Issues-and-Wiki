package pokecube.api.utils;

import com.google.common.collect.Maps;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thut.core.common.ThutCore;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

public class PokeType
{
    private static final Map<String, PokeType> names = Maps.newHashMap();
    private static PokeType[] values = new PokeType[0];

    public static final PokeType unknown = new PokeType(0, "???");

    public static float[][] typeTable;

    public static PokeType[] values()
    {
        return values;
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
    public static MutableComponent getTranslatedName(final PokeType type)
    {
        MutableComponent ret = Component.literal(type.name);
        final String translated = I18n.get(PokeType.getUnlocalizedName(type));
        if (translated != null && !translated.startsWith("type."))
            ret = Component.translatable(PokeType.getUnlocalizedName(type));
        ret.setStyle(ret.getStyle().withColor(TextColor.fromRgb(type.colour)));
        return ret;
    }

    public static PokeType getType(String name)
    {
        name = name.toLowerCase(Locale.ROOT);
        if (name.equals(unknown.name)) return unknown;
        name = ThutCore.trim(name);
        if (PokeType.names.containsKey(name)) return PokeType.names.get(name);
        for (final PokeType type : PokeType.values()) if (name.equalsIgnoreCase(type.name))
        {
            PokeType.names.put(name, type);
            return type;
        }
        return unknown;
    }

    public final int colour;

    public final String name;

    private final int ordinal;

    public PokeType(int colour, String name)
    {
        name = name.toLowerCase(Locale.ROOT);
        this.colour = colour;
        this.name = name;
        this.ordinal = values.length;
        names.put(name, this);
        values = Arrays.copyOf(values, values.length + 1);
        values[values.length - 1] = this;
    }

    @Override
    public String toString()
    {
        return this.name;
    }

    public int ordinal()
    {
        return this.ordinal;
    }
}
