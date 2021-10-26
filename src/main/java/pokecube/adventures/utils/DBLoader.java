package pokecube.adventures.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import net.minecraft.resources.ResourceLocation;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.core.PokecubeCore;
import pokecube.core.database.resources.PackFinder;

public class DBLoader
{
    public static ResourceLocation NAMESLOC = new ResourceLocation(PokecubeAdv.MODID, "database/trainer/names.csv");

    public static boolean loaded = false;

    private static ArrayList<ArrayList<String>> getRows(final ResourceLocation location) throws IOException
    {
        final InputStream res = PackFinder.getStream(location);

        final ArrayList<ArrayList<String>> rows = new ArrayList<>();
        BufferedReader br = null;
        String line = "";
        final String cvsSplitBy = ",";

        try
        {

            br = new BufferedReader(new InputStreamReader(res));
            int n = 0;
            while ((line = br.readLine()) != null)
            {

                final String[] row = line.split(cvsSplitBy);
                rows.add(new ArrayList<String>());
                for (final String element : row)
                    rows.get(n).add(element);
                n++;
            }

        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (br != null) try
            {
                br.close();
            }
            catch (final IOException e)
            {
                e.printStackTrace();
            }
        }

        return rows;
    }

    public static void load()
    {
        if (DBLoader.loaded) return;
        PokecubeCore.LOGGER.debug("Loading Trainer Databases");
        DBLoader.loaded = true;
        TrainerEntryLoader.makeEntries();
        DBLoader.loadNames();
    }

    public static void loadNames()
    {
        ArrayList<ArrayList<String>> rows;
        try
        {
            rows = DBLoader.getRows(DBLoader.NAMESLOC);
        }
        catch (final IOException e)
        {
            e.printStackTrace();
            TypeTrainer.femaleNames.add("Jane");
            TypeTrainer.maleNames.add("John");
            return;
        }

        for (final ArrayList<String> row : rows)
        {
            if (row.isEmpty()) continue;
            final String name = row.get(0);
            if (name.equalsIgnoreCase("female:"))
            {
                for (int i = 1; i < row.size(); i++)
                    TypeTrainer.femaleNames.add(row.get(i));
                continue;
            }
            if (name.equalsIgnoreCase("male:"))
            {
                for (int i = 1; i < row.size(); i++)
                    TypeTrainer.maleNames.add(row.get(i));
                continue;
            }
        }
    }
}
