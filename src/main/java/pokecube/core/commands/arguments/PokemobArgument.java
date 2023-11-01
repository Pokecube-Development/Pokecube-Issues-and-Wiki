package pokecube.core.commands.arguments;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import pokecube.api.data.PokedexEntry;
import pokecube.core.commands.arguments.PokemobArgument.PokemobInput;
import pokecube.core.database.Database;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

public class PokemobArgument implements ArgumentType<PokemobInput>
{
    public static class PokemobInput
    {
        public PokedexEntry entry;
        public Tag nbt;
    }

    private static final Collection<String> EXAMPLES = Arrays.asList("missingno", "rattata");

    public static final SuggestionProvider<CommandSourceStack> SUMMONABLE_ENTITIES = SuggestionProviders
            .register(new ResourceLocation("pokedex_entries"), (provider, builder) ->
            {
                return SharedSuggestionProvider.suggest(Database.getSortedFormes(), builder, e -> e.getTrimmedName(),
                        e -> TComponent.translatable(e.getName()));
            });

    public static final DynamicCommandExceptionType ERROR_UNKNOWN_ENTITY = new DynamicCommandExceptionType((thing) -> {
        return TComponent.translatable("entity.notFound", thing);
    });

    public static PokemobInput getEntry(CommandContext<CommandSourceStack> stack, String key)
            throws CommandSyntaxException
    {
        return stack.getArgument(key, PokemobInput.class);
    }

    public static List<PokedexEntry> getMatching(String name)
    {
        if (name.endsWith("*"))
        {
            var key = name.length() > 1 ? name.substring(0, name.length() - 1) : "";
            var entries = Database.getSortedFormes().stream().filter(e -> key.isBlank() || e.getName().startsWith(key))
                    .toList();
            return entries;
        }
        var entry = Database.getEntry(name);
        if (entry == null) return Collections.emptyList();
        return Lists.newArrayList(entry);
    }

    public static PokemobArgument pokemob()
    {
        return new PokemobArgument();
    }

    @Override
    public PokemobInput parse(StringReader reader) throws CommandSyntaxException
    {
        int i = reader.getCursor();
        char c;
        while (reader.canRead() && (ResourceLocation.isAllowedInResourceLocation(c = reader.peek()) || c == '*'))
        {
            reader.skip();
        }
        String s = reader.getString().substring(i, reader.getCursor());
        var entries = getMatching(s);
        if (entries.isEmpty()) throw ERROR_UNKNOWN_ENTITY.create(reader);
        PokedexEntry entry = entries.size() == 1 ? entries.get(0)
                : entries.get(ThutCore.newRandom().nextInt(entries.size()));
        if (entry == null) throw ERROR_UNKNOWN_ENTITY.create(reader);
        PokemobInput resp = new PokemobInput();
        resp.entry = entry;
        if (reader.canRead() && reader.peek() != ' ') resp.nbt = (new TagParser(reader)).readValue();
        return resp;
    }

    public Collection<String> getExamples()
    {
        return EXAMPLES;
    }
}
