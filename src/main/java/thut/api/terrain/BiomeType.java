package thut.api.terrain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.core.common.ThutCore;

public class BiomeType
{
    private static final Map<Integer, BiomeType> typeMap       = Maps.newHashMap();
    private static final Map<Integer, BiomeType> typeMapClient = Maps.newHashMap();
    private static int                           MAXID         = 256;
    public static final BiomeType
    //@formatter:off
    NONE  = new BiomeType("none"),
    SKY = new BiomeType("sky"),
    FLOWER = new BiomeType("flower"),
    LAKE = new BiomeType("lake"),
    INDUSTRIAL = new BiomeType("industrial"),
    METEOR = new BiomeType("meteor"),
    RUIN = new BiomeType("ruin"),
    CAVE = new BiomeType("cave"),
    CAVE_WATER = new BiomeType("cavewater"),
    VILLAGE = new BiomeType("village"),
    ALL = new BiomeType("all");
    //@formatter:on

    public static BiomeType getBiome(final String name)
    {
        return BiomeType.getBiome(name, true);
    }

    public static BiomeType getBiome(String name, final boolean generate)
    {
        name = ThutCore.trim(name);
        for (final BiomeType b : BiomeType.values())
            if (b.name.equalsIgnoreCase(name)) return b;
        if (generate)
        {
            final BiomeType ret = new BiomeType(name);
            return ret;
        }
        return BiomeType.NONE;
    }

    public static Map<Integer, String> getMap()
    {
        final Map<Integer, String> map = Maps.newHashMap();
        for (final BiomeType type : BiomeType.values())
            map.put(type.getType(), type.name);
        return map;
    }

    public static BiomeType getType(final int id)
    {
        if (ThutCore.proxy.isClientSide()) return BiomeType.typeMapClient.containsKey(id) ? BiomeType.typeMapClient.get(
                id) : BiomeType.NONE;
                return BiomeType.typeMap.containsKey(id) ? BiomeType.typeMap.get(id) : BiomeType.NONE;
    }

    @OnlyIn(Dist.CLIENT)
    public static void setMap(final Map<Integer, String> mapIn)
    {
        BiomeType.typeMapClient.clear();
        for (final Integer i : mapIn.keySet())
        {
            final String name = mapIn.get(i);
            final BiomeType type = BiomeType.getBiome(name, true);
            BiomeType.typeMapClient.put(i, type);
        }
    }

    public static ArrayList<BiomeType> values()
    {
        if (ThutCore.proxy.isClientSide())
        {
            final ArrayList<BiomeType> types = Lists.newArrayList();
            final Collection<BiomeType> values = BiomeType.typeMapClient.values();
            synchronized (values)
            {
                types.addAll(values);
            }
            return types;
        }
        final ArrayList<BiomeType> types = Lists.newArrayList();
        final Collection<BiomeType> values = BiomeType.typeMap.values();
        synchronized (values)
        {
            types.addAll(values);
        }
        return types;
    }

    public final String name;
    private int         id;
    public final String readableName;

    private BiomeType(final String name)
    {
        this.name = name;
        this.readableName = "thutcore.biometype." + name;
        this.id = -1;
        for (final BiomeType type : BiomeType.typeMap.values())
            if (type.name.equals(name)) this.id = type.id;
        if (this.id == -1) this.id = BiomeType.MAXID++;
        BiomeType.typeMap.put(this.id, this);
        BiomeType.typeMapClient.put(this.id, this);
    }

    @Override
    public boolean equals(final Object o)
    {
        if (o instanceof BiomeType) return ((BiomeType) o).id == this.id;
        return false;
    }

    public int getType()
    {
        return this.id;
    }

    @Override
    public int hashCode()
    {
        return this.id;
    }

    @Override
    public String toString()
    {
        return this.name;
    }
}
