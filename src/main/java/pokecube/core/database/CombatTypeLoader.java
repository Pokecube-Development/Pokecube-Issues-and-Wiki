package pokecube.core.database;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gson.Gson;

import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;
import pokecube.core.database.resources.PackFinder;
import pokecube.core.utils.PokeType;

public class CombatTypeLoader
{
    public static class CombatTypes
    {
        public JsonType[] types;

        public void init()
        {
            PokeType.typeTable = new float[this.types.length][this.types.length];

            // First add them all in as enums.
            for (final JsonType type2 : this.types)
            {
                final JsonType type = type2;
                type.init();
                if (PokeType.getType(type.name) == PokeType.unknown && !type.name.equals("???")) PokeType.create(
                        type.name, type.colour, type.name);
            }

            for (int i = 0; i < this.types.length; i++)
            {
                final float[] arr = new float[this.types.length];
                PokeType.typeTable[i] = arr;
                final JsonType current = this.types[i];
                for (int j = 0; j < this.types.length; j++)
                    arr[j] = current.effect(this.types[j].name);
            }
        }
    }

    public static class JsonType
    {
        public String       name;
        public int          colour;
        public TypeEffect[] outgoing;
        Map<String, Float>  effects = Maps.newHashMap();

        float effect(final String type)
        {
            if (!this.effects.containsKey(type)) return 1;
            return this.effects.get(type);
        }

        void init()
        {
            for (final TypeEffect e : this.outgoing)
                this.effects.put(e.type, e.amount);
        }
    }

    public static class TypeEffect
    {
        public String type;
        public float  amount;
    }

    public static ResourceLocation TYPES = new ResourceLocation(PokecubeCore.MODID, "database/types.json");

    private static Gson gson = new Gson();

    public static void loadTypes()
    {
        try
        {
            final InputStream res = PackFinder.getStream(CombatTypeLoader.TYPES);
            final Reader reader = new InputStreamReader(res);
            final CombatTypes types = CombatTypeLoader.gson.fromJson(reader, CombatTypes.class);
            types.init();
            reader.close();
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error loading types.json", e);
            throw new RuntimeException(e);
        }
    }
}
