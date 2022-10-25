package pokecube.api.utils;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IExtensibleEnum;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

public enum PokeType implements IExtensibleEnum
{
    unknown(0, "???");

    public static float[][] typeTable;

    public static PokeType create(final String dummy, final int colour, final String name)
    {
        throw new IllegalStateException("Enum not extended");
    }

    private static Map<String, PokeType> names = Maps.newHashMap();

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
        MutableComponent ret = TComponent.literal(type.name);
        final String translated = I18n.get(PokeType.getUnlocalizedName(type));
        if (translated != null && !translated.startsWith("type."))
            ret = TComponent.translatable(PokeType.getUnlocalizedName(type));
        ret.setStyle(ret.getStyle().withColor(TextColor.fromRgb(type.colour)));
        return ret;
    }

    public static PokeType getType(String name)
    {
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

    private PokeType(final int colour, final String name)
    {
        this.colour = colour;
        this.name = name;
    }
}
