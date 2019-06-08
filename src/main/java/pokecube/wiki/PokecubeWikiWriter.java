package pokecube.wiki;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import pokecube.compat.Compat;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;

public class PokecubeWikiWriter
{
    public static PrintWriter   out;
    public static FileWriter    fwriter;

    public static String        pokemobDir = "https://github.com/Pokecube-Development/Pokecube-Issues-and-Wiki/wiki/";
    public static String        gifDir     = "https://raw.githubusercontent.com/wiki/Pokecube-Development/Pokecube-Issues-and-Wiki/pokemobs/img/";
    public static String        pagePrefix = "";
    static Map<String, Integer> refs       = Maps.newHashMap();
    static int                  maxRef     = 0;

    static void clearRefs()
    {
        refs.clear();
        maxRef = 0;
    }

    static String formatLink(String link, String name)
    {
        return "[" + name + "](" + link.replace(" ", "%20") + ")";
    }

    static String referenceLink(String ref, String name)
    {
        int num = maxRef;
        if (refs.containsKey(ref)) num = refs.get(ref);
        else
        {
            maxRef++;
            num = maxRef;
            refs.put(ref, num);
        }
        return "[" + name + "][" + num + "]";
    }

    static String referenceImg(String ref)
    {
        int num = maxRef;
        if (refs.containsKey(ref)) num = refs.get(ref);
        else
        {
            maxRef++;
            num = maxRef;
            refs.put(ref, num);
        }
        return "![][" + num + "]";
    }

    static String formatPokemobLink(PokedexEntry entry)
    {
        String link = referenceLink(pokemobDir + pagePrefix + entry.getName(), entry.getTranslatedName());
        return link;
    }

    static String formatPokemobImage(PokedexEntry entry, boolean shiny)
    {
        String link = referenceImg(gifDir + entry.getName() + (shiny ? "S.png" : ".png"));
        return link;
    }

    static void writeWiki()
    {
        pokemobDir = "https://github.com/Pokecube-Development/Pokecube-Issues-and-Wiki/wiki/";

        String code = Minecraft.getInstance().getLanguageManager().getCurrentLanguage().getLanguageCode();
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

            int m = 5;
            String header1 = "|";
            String header2 = "|";
            for (int i = 0; i < m; i++)
            {
                header1 = header1 + "  |";
                header2 = header2 + " --- |";
            }
            out.println(header1);
            out.println(header2);
            int n = 0;
            boolean ended = false;
            List<PokedexEntry> entries = Lists.newArrayList(Database.baseFormes.values());
            Collections.sort(entries, Database.COMPARATOR);
            for (PokedexEntry e : entries)
            {
                if (e == null) continue;
                ended = false;
                out.print("|" + formatLink(pokemobDir + pagePrefix + e.getName(), e.getTranslatedName()));
                if (n % m == m - 1)
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