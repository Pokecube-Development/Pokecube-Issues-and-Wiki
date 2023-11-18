package pokecube.gimmicks.builders;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import thut.api.util.JsonUtil;
import thut.lib.RegHelper;

public class BuilderConfig
{
    public static BuilderConfig loadConfig()
    {
        // We put the config option in config/pokecube/gimmicks/
        Path folder = FMLPaths.CONFIGDIR.get().resolve("pokecube").resolve("gimmicks");
        // Ensure the folder exists for it
        folder.toFile().mkdirs();
        Path config_path = folder.resolve("builders.json");
        final File dir = config_path.toFile();

        BuilderConfig config = new BuilderConfig();

        if (config_path.toFile().exists())
        {
            try
            {
                FileInputStream inS = new FileInputStream(dir);
                var inSR = new InputStreamReader(inS);
                config = JsonUtil.gson.fromJson(inSR, BuilderConfig.class);
                config.init();
                inSR.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        saveConfig(config);
        return config;
    }

    public static void saveConfig(BuilderConfig config)
    {
        // We put the config option in config/pokecube/gimmicks/
        Path folder = FMLPaths.CONFIGDIR.get().resolve("pokecube").resolve("gimmicks");
        // Ensure the folder exists for it
        folder.toFile().mkdirs();
        Path config_path = folder.resolve("builders.json");
        final File dir = config_path.toFile();

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
            e.printStackTrace();
        }
    }

    List<String> nbt_allowed_whitelist = new ArrayList<>();
    List<String> nbt_allowed_blacklist = new ArrayList<>();
    public boolean use_whitelist = true;

    List<String> known_ids = new ArrayList<>();

    private List<ResourceLocation> _whitelist = new ArrayList<>();
    private List<ResourceLocation> _blacklist = new ArrayList<>();

    public BuilderConfig()
    {
        nbt_allowed_whitelist.add("minecraft:sign");
        nbt_allowed_whitelist.add("minecraft:banner");
        nbt_allowed_whitelist.add("pokecube:repel");
        nbt_allowed_whitelist.add("pokecube:dynamax");
        nbt_allowed_whitelist.add("pokecube_adventures:statue");
    }

    private void init()
    {
        _whitelist.clear();
        _blacklist.clear();

        nbt_allowed_whitelist.forEach(name -> {
            try
            {
                _whitelist.add(new ResourceLocation(name));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });

        nbt_allowed_blacklist.forEach(name -> {
            try
            {
                _blacklist.add(new ResourceLocation(name));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    public boolean canHaveTag(ItemStack item)
    {
        if (use_whitelist) return _whitelist.contains(RegHelper.getKey(item));
        else return !_blacklist.contains(RegHelper.getKey(item));
    }
}
