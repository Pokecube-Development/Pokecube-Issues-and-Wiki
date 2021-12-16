package thut.api.terrain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import thut.core.common.ThutCore;

public class BiomeType
{
    private static final Map<Integer, BiomeType> typeMap       = Maps.newHashMap();
    private static final Map<Integer, BiomeType> typeMapClient = Maps.newHashMap();
    private static int                           MAXID         = 256;
    public static final BiomeType
    //@formatter:off
    NONE  = new BiomeType("none").setNoSave(),
    SKY = new BiomeType("sky").setNoSave(),
    FLOWER = new BiomeType("flower").setNoSave(),
    LAKE = new BiomeType("lake"),
    INDUSTRIAL = new BiomeType("industrial"),
    METEOR = new BiomeType("meteor"),
    RUIN = new BiomeType("ruin"),
    CAVE = new BiomeType("cave").setNoSave(),
    CAVE_WATER = new BiomeType("cavewater").setNoSave(),
    VILLAGE = new BiomeType("village"),
    ALL = new BiomeType("all").setNoSave();
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

    public static BiomeType getType(final int id)
    {
        if (ThutCore.proxy.isClientSide()) return BiomeType.typeMapClient.containsKey(id) ? BiomeType.typeMapClient.get(
                id) : BiomeType.NONE;
        return BiomeType.typeMap.containsKey(id) ? BiomeType.typeMap.get(id) : BiomeType.NONE;
    }

    public static BiomeType merge(final BiomeType typeA, final BiomeType typeB)
    {
        final Set<String> noDupes = Sets.newHashSet();
        if (typeA.subTypes.isEmpty()) noDupes.add(typeA.name);
        else noDupes.addAll(typeA.subTypes);
        if (typeB.subTypes.isEmpty()) noDupes.add(typeB.name);
        else noDupes.addAll(typeB.subTypes);
        final List<String> names = Lists.newArrayList(noDupes);
        Collections.sort(names);
        final BiomeType type = BiomeType.getBiome(names.toString(), true);
        if (type.subTypes.isEmpty()) type.setSubTypes(names);
        return type;
    }

    public static BiomeType remove(final BiomeType container, final BiomeType toRemove)
    {
        final List<String> names = Lists.newArrayList(container.subTypes);
        names.remove(toRemove.name);
        final BiomeType type = BiomeType.getBiome(names.toString(), true);
        if (type.subTypes.isEmpty()) type.setSubTypes(names);
        return type;
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

    private int id;

    public final String readableName;

    private boolean save = true;

    private List<String> subTypes = Lists.newArrayList();

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

        // TODO validation test for if name is a list. In this case, we should
        // populate subTypes from there!

    }

    public BiomeType setNoSave()
    {
        this.save = false;
        return this;
    }

    private BiomeType setSubTypes(final List<String> names)
    {
        this.subTypes = names;
        return this;
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

    public boolean contains(final BiomeType other)
    {
        if (this == BiomeType.ALL) return true;
        return other == this || this.subTypes.contains(other.name);
    }

    public boolean anyMatch(final Set<BiomeType> biomes)
    {
        for (final BiomeType b : biomes)
            if (this.contains(b)) return true;
        return this == BiomeType.ALL;
    }

    public boolean isNone()
    {
        return this == BiomeType.NONE;
    }

    public boolean shouldSave()
    {
        return this.save;
    }

    @Override
    public String toString()
    {
        return this.name;
    }
}
