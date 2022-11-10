package pokecube.core.database.types;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.Gson;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import pokecube.api.PokecubeAPI;
import pokecube.api.utils.PokeType;
import pokecube.core.database.resources.PackFinder;
import thut.lib.ResourceHelper;

public class CombatTypeLoader
{
    public static class CombatTypes implements Comparable<CombatTypes>
    {
        public int priority = 100;
        public List<JsonType> types = new ArrayList<>();

        public void init()
        {
            Map<String, JsonType> typeMap = Maps.newHashMap();
            // First add them all in as enums.
            for (final JsonType type2 : this.types)
            {
                final JsonType type = type2;
                type.init();
                if (PokeType.getType(type.name) == PokeType.unknown && !type.name.equals("???"))
                    PokeType.create(type.name, type.colour, type.name);
                typeMap.put(type.name, type);
            }
            int n = PokeType.values().length;
            PokeType.typeTable = new float[n][n];
            for (int i = 0; i < n; i++)
            {
                final float[] arr = new float[n];
                PokeType.typeTable[i] = arr;
                Arrays.fill(arr, 1.0f);
                PokeType type = PokeType.values()[i];
                final JsonType current = typeMap.get(type.name);
                for (int j = 0; j < n; j++) arr[j] = current.effect(PokeType.values()[j].name);
            }
        }

        @Override
        public int compareTo(CombatTypes o)
        {
            return priority - o.priority;
        }
    }

    public static class JsonType
    {
        public String name;
        public int colour;
        public TypeEffect[] outgoing;
        Map<String, Float> effects = Maps.newHashMap();

        float effect(final String type)
        {
            return this.effects.getOrDefault(type, 1.0f);
        }

        void init()
        {
            for (final TypeEffect e : this.outgoing) this.effects.put(e.type, e.amount);
        }
    }

    public static class TypeEffect
    {
        public String type;
        public float amount;
    }

    private static final String DATABASES = "database/types/";
    private static Gson gson = new Gson();

    public static void loadTypes()
    {
        final Map<ResourceLocation, Resource> resources = PackFinder.getResources(CombatTypeLoader.DATABASES,
                s -> s.endsWith(".json"));
        List<CombatTypes> loaded = new ArrayList<>();
        resources.forEach((s, r) -> {
            try
            {
                BufferedReader reader = ResourceHelper.getReader(r);
                CombatTypes types = CombatTypeLoader.gson.fromJson(reader, CombatTypes.class);
                if (!types.types.isEmpty()) loaded.add(types);
                reader.close();
            }
            catch (final Exception e1)
            {
                PokecubeAPI.LOGGER.error("Error with types database " + s, e1);
            }
        });
        loaded.sort(null);
        CombatTypes compound = new CombatTypes();
        Map<String, JsonType> typesMap = new HashMap<>();
        loaded.forEach(l -> l.types.forEach(t -> typesMap.putIfAbsent(t.name, t)));
        compound.types.addAll(typesMap.values());
        compound.init();
    }
}
