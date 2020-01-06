package pokecube.core.database;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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

    private static final Set<String> list = Sets.newHashSet();
    static
    {
        PokedexEntryJsonProcessor.list.add("abilities");
        PokedexEntryJsonProcessor.list.add("abilitiesHidden");
        PokedexEntryJsonProcessor.list.add("activeTimes");
        PokedexEntryJsonProcessor.list.add("base");
        PokedexEntryJsonProcessor.list.add("dummy");
        PokedexEntryJsonProcessor.list.add("customSound");
        PokedexEntryJsonProcessor.list.add("baseHappiness");
        PokedexEntryJsonProcessor.list.add("baseName");
        PokedexEntryJsonProcessor.list.add("baseXP");
        PokedexEntryJsonProcessor.list.add("breeds");
        PokedexEntryJsonProcessor.list.add("canSitShoulder");
        PokedexEntryJsonProcessor.list.add("catchRate");
        PokedexEntryJsonProcessor.list.add("colonyBuilder");
        PokedexEntryJsonProcessor.list.add("defaultSpecial");
        PokedexEntryJsonProcessor.list.add("drops");
        PokedexEntryJsonProcessor.list.add("lootTable");
        PokedexEntryJsonProcessor.list.add("dyeable");
        PokedexEntryJsonProcessor.list.add("event");
        PokedexEntryJsonProcessor.list.add("replacedEvent");
        PokedexEntryJsonProcessor.list.add("evolutionMode");
        PokedexEntryJsonProcessor.list.add("evs");
        PokedexEntryJsonProcessor.list.add("female");
        PokedexEntryJsonProcessor.list.add("food");
        PokedexEntryJsonProcessor.list.add("foods");
        PokedexEntryJsonProcessor.list.add("isGenderForme");
        PokedexEntryJsonProcessor.list.add("hasMegaForm");
        PokedexEntryJsonProcessor.list.add("hasShiny");
        PokedexEntryJsonProcessor.list.add("hatedMaterial");
        PokedexEntryJsonProcessor.list.add("height");
        PokedexEntryJsonProcessor.list.add("isMega");
        PokedexEntryJsonProcessor.list.add("ridable");
        PokedexEntryJsonProcessor.list.add("held");
        PokedexEntryJsonProcessor.list.add("heldTable");
        PokedexEntryJsonProcessor.list.add("interactionLogic");
        PokedexEntryJsonProcessor.list.add("isFemaleForme");
        PokedexEntryJsonProcessor.list.add("isMaleForme");
        PokedexEntryJsonProcessor.list.add("isShadowForme");
        PokedexEntryJsonProcessor.list.add("isSocial");
        PokedexEntryJsonProcessor.list.add("isStarter");
        PokedexEntryJsonProcessor.list.add("isStationary");
        PokedexEntryJsonProcessor.list.add("legendary");
        PokedexEntryJsonProcessor.list.add("length");
        PokedexEntryJsonProcessor.list.add("lvlUpMoves");
        PokedexEntryJsonProcessor.list.add("evolutionMoves");
        PokedexEntryJsonProcessor.list.add("mass");
        PokedexEntryJsonProcessor.list.add("megaRules");
        PokedexEntryJsonProcessor.list.add("mobType");
        PokedexEntryJsonProcessor.list.add("modId");
        PokedexEntryJsonProcessor.list.add("particleData");
        PokedexEntryJsonProcessor.list.add("passengerOffsets");
        PokedexEntryJsonProcessor.list.add("pokedexNb");
        PokedexEntryJsonProcessor.list.add("possibleMoves");
        PokedexEntryJsonProcessor.list.add("preferedHeight");
        PokedexEntryJsonProcessor.list.add("sexeRatio");
        PokedexEntryJsonProcessor.list.add("shadowForme");
        PokedexEntryJsonProcessor.list.add("shouldDive");
        PokedexEntryJsonProcessor.list.add("shouldFly");
        PokedexEntryJsonProcessor.list.add("shouldSurf");
        PokedexEntryJsonProcessor.list.add("sound");
        PokedexEntryJsonProcessor.list.add("spawns");
        PokedexEntryJsonProcessor.list.add("species");
        PokedexEntryJsonProcessor.list.add("stats");
        PokedexEntryJsonProcessor.list.add("textureDetails");
        PokedexEntryJsonProcessor.list.add("texturePath");
        PokedexEntryJsonProcessor.list.add("type1");
        PokedexEntryJsonProcessor.list.add("type2");
        PokedexEntryJsonProcessor.list.add("width");
        PokedexEntryJsonProcessor.list.add("modelSize");
        PokedexEntryJsonProcessor.list.add("name");
        PokedexEntryJsonProcessor.list.add("trimmedName");
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
            return !PokedexEntryJsonProcessor.list.contains(f.getName());
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
