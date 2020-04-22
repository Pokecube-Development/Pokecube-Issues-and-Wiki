package pokecube.core.database;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.minecraft.entity.player.PlayerEntity;
import pokecube.core.utils.PokeType;

public class PokedexEntryJsonProcessor
{
    public static class PokedexEntries
    {
        protected List<PokedexEntry> entries = Lists.newArrayList();
    }

    private static TypeAdapter<PokeType> typeAdaptor = new TypeAdapter<PokeType>()
    {
        @Override
        public PokeType read(final JsonReader in) throws IOException
        {
            return PokeType.getType(in.nextString());
        }

        @Override
        public void write(final JsonWriter out, final PokeType value) throws IOException
        {
            out.value(value.name);
        }
    };

    private static ExclusionStrategy strat = new ExclusionStrategy()
    {
        @Override
        public boolean shouldSkipClass(final Class<?> clazz)
        {
            return clazz == PlayerEntity.class;
        }

        @Override
        public boolean shouldSkipField(final FieldAttributes f)
        {
            return f.getName().startsWith("_");
        }
    };

    public static Gson gson = new GsonBuilder().registerTypeAdapter(PokeType.class,
            PokedexEntryJsonProcessor.typeAdaptor).addSerializationExclusionStrategy(PokedexEntryJsonProcessor.strat)
            .addDeserializationExclusionStrategy(PokedexEntryJsonProcessor.strat).setPrettyPrinting().create();

    static PokedexEntries entries;

    public static void loadEntries(final InputStream stream)
    {
        PokedexEntryJsonProcessor.entries = PokedexEntryJsonProcessor.gson.fromJson(new InputStreamReader(stream),
                PokedexEntries.class);
    }
}
