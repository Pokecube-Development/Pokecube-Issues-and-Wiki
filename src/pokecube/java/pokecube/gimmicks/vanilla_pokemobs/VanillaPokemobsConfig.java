package pokecube.gimmicks.vanilla_pokemobs;

import net.neoforged.fml.loading.FMLPaths;
import pokecube.api.PokecubeAPI;
import thut.api.util.JsonUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class VanillaPokemobsConfig
{
    public boolean vanilla_pokemobs = false;
    public boolean non_vanilla_pokemobs = false;
    public boolean _registered = false;

    public static VanillaPokemobsConfig loadConfig()
    {
        // We put the config option in config/pokecube/gimmicks/
        Path folder = FMLPaths.CONFIGDIR.get().resolve("pokecube").resolve("gimmicks");
        // Ensure the folder exists for it
        if(folder.toFile().mkdirs()) PokecubeAPI.logInfo("Created config dir for gimmicks");
        Path config_path = folder.resolve("vanilla_pokemobs.json");
        final File dir = config_path.toFile();

        VanillaPokemobsConfig config = new VanillaPokemobsConfig();

        if (config_path.toFile().exists())
        {
            try
            {
                FileInputStream inS = new FileInputStream(dir);
                var inSR = new InputStreamReader(inS);
                config = JsonUtil.gson.fromJson(inSR, VanillaPokemobsConfig.class);
                inSR.close();
            }
            catch (Exception e)
            {
                PokecubeAPI.LOGGER.error("Error loading config for vanilla_pokemobs", e);
            }
        }

        // Re-save the config file to ensure standard format, etc
        final String json = JsonUtil.gson.toJson(config);
        try
        {
            FileOutputStream outS = new FileOutputStream(dir);
            outS.write(json.getBytes());
            outS.close();
        }
        catch (Exception e)
        {
            PokecubeAPI.LOGGER.error("Error saving config for vanilla_pokemobs", e);
        }

        return config;
    }
}
