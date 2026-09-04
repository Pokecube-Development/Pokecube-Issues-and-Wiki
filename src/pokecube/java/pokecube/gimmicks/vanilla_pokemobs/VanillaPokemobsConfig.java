package pokecube.gimmicks.vanilla_pokemobs;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.loading.FMLPaths;
import pokecube.api.PokecubeAPI;
import thut.api.util.JsonUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class VanillaPokemobsConfig
{
    public boolean vanilla_pokemobs = false;
    public boolean non_vanilla_pokemobs = false;
    public List<String> not_pokemobs = new ArrayList<>();
    public boolean _registered = false;
    public List<TagKey<EntityType<?>>> _tags_not_pokemob = new ArrayList<>();

    public VanillaPokemobsConfig(){
        not_pokemobs.add("minecraft:player");
        not_pokemobs.add("minecraft:villager");
        not_pokemobs.add("minecraft:wandering_trader");
        not_pokemobs.add("pokecube:npc");
        not_pokemobs.add("pokecube_adventures:trainer");
        not_pokemobs.add("pokecube_adventures:leader");
        not_pokemobs.add("#c:villagers");
        not_pokemobs.add("#minecraft:raiders");
        not_pokemobs.add("#c:capturing_not_supported");
        not_pokemobs.add("#c:teleporting_not_supported");
        not_pokemobs.add("#pokecube:never_pokemob");
    }

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
        // Now process tags out
        config._tags_not_pokemob.clear();
        for (String s : config.not_pokemobs)
        {
            try
            {
                if (s.startsWith("#"))
                {
                    s = s.substring(1);
                    config._tags_not_pokemob.add(TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse(s)));
                }
            }
            catch (Exception ignored)
            {
            }
        }
        // Re-save the config file to ensure standard format, etc
        saveConfig(config);
        return config;
    }

    public static void saveConfig(VanillaPokemobsConfig config)
    {
        try
        {
            // Sort the out pokemobs
            config.not_pokemobs.sort(null);
            final String json = JsonUtil.gson.toJson(config);
            // We put the config option in config/pokecube/gimmicks/
            Path folder = FMLPaths.CONFIGDIR.get().resolve("pokecube").resolve("gimmicks");
            // Ensure the folder exists for it
            if (folder.toFile().mkdirs()) PokecubeAPI.logInfo("Created config dir for gimmicks");
            Path config_path = folder.resolve("vanilla_pokemobs.json");
            final File dir = config_path.toFile();
            FileOutputStream outS = new FileOutputStream(dir);
            outS.write(json.getBytes());
            outS.close();
        }
        catch (Exception e)
        {
            PokecubeAPI.LOGGER.error("Error saving config for vanilla_pokemobs", e);
        }
    }
}
