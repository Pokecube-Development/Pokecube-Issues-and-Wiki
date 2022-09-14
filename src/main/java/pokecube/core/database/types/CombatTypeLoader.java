package pokecube.core.database.types;

import java.io.BufferedReader;
import java.util.ArrayList;
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

public class CombatTypeLoader
{
    public static class CombatTypes implements Comparable<CombatTypes>
    {
        public int priority = 100;
        public List<JsonType> types = new ArrayList<>();

        public void init()
        {
            PokeType.typeTable = new float[this.types.size()][this.types.size()];

            // First add them all in as enums.
            for (final JsonType type2 : this.types)
            {
                final JsonType type = type2;
                type.init();
                if (PokeType.getType(type.name) == PokeType.unknown && !type.name.equals("???"))
                    PokeType.create(type.name, type.colour, type.name);
            }

            for (int i = 0; i < this.types.size(); i++)
            {
                final float[] arr = new float[this.types.size()];
                PokeType.typeTable[i] = arr;
                final JsonType current = this.types.get(i);
                for (int j = 0; j < this.types.size(); j++) arr[j] = current.effect(this.types.get(j).name);
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
            if (!this.effects.containsKey(type)) return 1;
            return this.effects.get(type);
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
                BufferedReader reader = PackFinder.getReader(r);
                CombatTypes types = CombatTypeLoader.gson.fromJson(reader, CombatTypes.class);
                if (!types.types.isEmpty()) loaded.add(types);
                reader.close();
            }
            catch (final Exception e1)
            {
                PokecubeAPI.LOGGER.error("Error with moves database " + s, e1);
            }
        });
        loaded.sort(null);

        loaded.forEach(t -> System.out.println(t.types));

        CombatTypes compound = new CombatTypes();
        Map<String, JsonType> typesMap = new HashMap<>();
        loaded.forEach(l -> l.types.forEach(t -> typesMap.putIfAbsent(t.name, t)));
        compound.types.addAll(typesMap.values());
        compound.init();
    }
}
