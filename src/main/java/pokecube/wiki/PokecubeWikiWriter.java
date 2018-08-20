package pokecube.wiki;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import pokecube.compat.Compat;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class PokecubeWikiWriter
{
    public static PrintWriter out;
    public static FileWriter  fwriter;

    public static String      pokemobDir = "https://github.com/Thutmose/Pokecube/wiki/";
    public static String      gifDir     = "https://raw.githubusercontent.com/wiki/Thutmose/Pokecube/pokemobs/img/";
    public static String      pagePrefix = "";

    static String formatLink(String link, String name)
    {
        return "[" + name + "](" + link + ")";
    }

    static String referenceLink(String ref, String name)
    {
        return "[[" + name + "|" + ref + "]]";
    }

    static String referenceImg(String ref)
    {
        return "![][" + ref + "]";
    }

    static String formatPokemobLink(PokedexEntry entry, List<String> refs, String reference)
    {
        String link = referenceLink(reference, entry.getTranslatedName());
        refs.add(pokemobDir + pagePrefix + entry.getName());
        return link;
    }

    static void writeWiki()
    {
        pokemobDir = "https://github.com/Thutmose/Pokecube/wiki/";

        String code = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
        if (code.equalsIgnoreCase("en_US"))
        {
            pagePrefix = "";
        }
        else
        {
            pagePrefix = code + "-";
        }

        for (PokedexEntry entry : Database.baseFormes.values())
        {
            PokemobPageWriter.outputPokemonWikiInfo(entry);
        }
        writeWikiPokemobList();
    }

    static void writeWikiPokemobList()
    {
        try
        {
            String fileName = Compat.CUSTOMSPAWNSFILE.replace("spawns.xml", pagePrefix + "pokemobList.md");
            fwriter = new FileWriter(fileName);
            out = new PrintWriter(fwriter);
            out.println("# " + I18n.format("list.pokemobs.title"));
            out.println("|  |  |  |  |");
            out.println("| --- | --- | --- | --- |");
            int n = 0;
            boolean ended = false;
            List<PokedexEntry> entries = Lists.newArrayList(Database.baseFormes.values());
            Collections.sort(entries, Database.COMPARATOR);
            for (PokedexEntry e : entries)
            {
                if (e == null) continue;
                ended = false;
                out.print("|" + referenceLink(e.getTranslatedName(), e.getTranslatedName()));
                if (n % 4 == 3)
                {
                    out.print("| \n");
                    ended = true;
                }
                n++;
            }
            if (!ended)
            {
                out.print("| \n");
            }
            out.close();
            fwriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}