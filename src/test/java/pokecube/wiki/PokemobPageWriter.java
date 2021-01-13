package pokecube.wiki;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.loading.FMLPaths;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class PokemobPageWriter
{
    public static PrintWriter out;
    public static FileWriter  fwriter;

    public static String        pokemobDir = "https://github.com/Pokecube-Development/Pokecube-Issues-and-Wiki/wiki/";
    public static String        gifDir     = "https://raw.githubusercontent.com/wiki/Pokecube-Development/Pokecube-Issues-and-Wiki/pokemobs/img/";
    public static String        pagePrefix = "";
    static Map<String, Integer> refs       = Maps.newHashMap();
    static int                  maxRef     = 0;

    static void clearRefs()
    {
        PokemobPageWriter.refs.clear();
        PokemobPageWriter.maxRef = 0;
    }

    static String formatLink(final String link, final String name)
    {
        return "[" + name + "](" + link.replace(" ", "%20") + ")";
    }

    static String referenceLink(final String ref, final String name)
    {
        int num = PokemobPageWriter.maxRef;
        if (PokemobPageWriter.refs.containsKey(ref)) num = PokemobPageWriter.refs.get(ref);
        else
        {
            PokemobPageWriter.maxRef++;
            num = PokemobPageWriter.maxRef;
            PokemobPageWriter.refs.put(ref, num);
        }
        return "[" + name + "][" + num + "]";
    }

    static String referenceImg(final String ref)
    {
        int num = PokemobPageWriter.maxRef;
        if (PokemobPageWriter.refs.containsKey(ref)) num = PokemobPageWriter.refs.get(ref);
        else
        {
            PokemobPageWriter.maxRef++;
            num = PokemobPageWriter.maxRef;
            PokemobPageWriter.refs.put(ref, num);
        }
        return "![][" + num + "]";
    }

    static String getEntryName(final PokedexEntry entry)
    {
        return entry.getTranslatedName().getString();
    }

    static String getEntryRef(final PokedexEntry entry)
    {
        return ":doc:`" + entry.getTrimmedName() + "`";
    }

    static String getEntryLabel(final PokedexEntry entry)
    {
        return ".. " + entry.getTrimmedName() + ":";
    }

    static String formatPokemobImage(final PokedexEntry entry, final boolean male, final boolean shiny)
    {
        final String link = ".. image:: " + "../../_images/pokemobs/" + entry.getIcon(male, shiny).getPath()
                + "\n    :width: 400" + "\n    :alt: " + entry.getTranslatedName().getString() + "\n";
        return link;
    }

    static String replaceWithRefs(String in, final PokedexEntry ignore)
    {
        final String ignoreName = PokemobPageWriter.getEntryName(ignore);
        in = in.replace(ignoreName, "___" + ignoreName + "___");

        for (final PokedexEntry entry : Database.baseFormes.values())
        {
            final String entryName = PokemobPageWriter.getEntryName(entry);
            if (entry == ignore) continue;
            if (entryName.contains(ignoreName) || ignoreName.contains(entryName))
            {
                if (in.contains(entryName + " ")) in = in.replace(PokemobPageWriter.getEntryName(entry) + " ",
                        PokemobPageWriter.getEntryRef(entry) + " ");
                else if (in.contains(PokemobPageWriter.getEntryName(entry) + ":")) in = in.replace(PokemobPageWriter
                        .getEntryName(entry) + ":", PokemobPageWriter.getEntryRef(entry) + ":");
                else if (in.contains(PokemobPageWriter.getEntryName(entry) + ".")) in = in.replace(PokemobPageWriter
                        .getEntryName(entry) + ".", PokemobPageWriter.getEntryRef(entry) + ".");
            }
            else in = in.replace(PokemobPageWriter.getEntryName(entry), PokemobPageWriter.getEntryRef(entry));
        }
        in = in.replace("___" + ignoreName + "___", ignoreName);
        return in;
    }

    static void outputPokemonWikiInfo(final PokedexEntry entry)
    {
        try
        {
            final StringBuilder builder = new StringBuilder();
            builder.append(PokemobPageWriter.getEntryLabel(entry)).append("\n\n");
            final String name = PokemobPageWriter.getEntryName(entry);
            builder.append(name).append("\n");
            for (int i = 0; i < name.length() + 1; i++)
                builder.append("-");
            builder.append("\n\n");
            builder.append(PokemobPageWriter.formatPokemobImage(entry, true, false));
            builder.append(PokemobPageWriter.formatPokemobImage(entry, true, true));
            builder.append("\n\n");
            final String header = I18n.format("pokemob.description.header");
            builder.append(header);
            builder.append("\n");
            for (int i = 0; i < header.length() + 1; i++)
                builder.append("=");
            builder.append("\n");

            final ITextComponent var = entry.getDescription();
            String desc = PokemobPageWriter.replaceWithRefs(var.getString(), entry);
            desc = desc.replace("\n", "\n| ");
            desc = desc.replace("| - ", "|  - ");
            desc = "| " + desc;

            builder.append(desc);

            final String json = builder.toString();
            final Path path = FMLPaths.CONFIGDIR.get().resolve("pokecube").resolve("pokemobs").resolve("subpages");
            path.toFile().mkdirs();
            final File dir = path.resolve(entry.getTrimmedName() + ".rst").toFile();
            final OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dir), Charset.forName("UTF-8")
                    .newEncoder());
            out.write(json);
            out.close();
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void outputAll()
    {
        for (final PokedexEntry entry : Database.baseFormes.values())
            PokemobPageWriter.outputPokemonWikiInfo(entry);
    }
}
