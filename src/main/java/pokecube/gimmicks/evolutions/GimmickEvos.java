package pokecube.gimmicks.evolutions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.pokemobs.EvolveEvent;
import pokecube.api.items.IPokecube;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.eventhandlers.StatsCollector;
import pokecube.core.items.pokecubes.PokecubeManager;
import thut.api.util.JsonUtil;

/**
 * This class handles gimmicky evolutions, such as tyrogue, shedinja, etc, which
 * are not as easy to define via the data.
 *
 */
@Mod.EventBusSubscriber(bus = Bus.MOD, modid = PokecubeCore.MODID)
public class GimmickEvos
{
    /**
     * This class holds our config options, we will do this with a json file, as
     * the default forge version only supports up to 3 files per mod id
     *
     */
    public static class EvoConfig
    {
        public boolean doTyrogue = true;
        public boolean doShedinja = true;

        public static EvoConfig loadConfig()
        {
            // We put the config option in config/pokecube/gimmicks/
            Path folder = FMLPaths.CONFIGDIR.get().resolve("pokecube").resolve("gimmicks");
            // Ensure the folder exists for it
            folder.toFile().mkdirs();
            Path config_path = folder.resolve("gimmick_evos.json");
            final File dir = config_path.toFile();

            EvoConfig config = new EvoConfig();

            if (config_path.toFile().exists())
            {
                try
                {
                    FileInputStream inS = new FileInputStream(dir);
                    var inSR = new InputStreamReader(inS);
                    config = JsonUtil.gson.fromJson(inSR, EvoConfig.class);
                    inSR.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
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
                e.printStackTrace();
            }

            return config;
        }
    }

    static EvoConfig config = EvoConfig.loadConfig();

    /**
     * Setup and register the event listeners
     */
    @SubscribeEvent
    public static void init(FMLLoadCompleteEvent event)
    {
        PokecubeAPI.POKEMOB_BUS.addListener(GimmickEvos::evolveTyrogue);
        PokecubeAPI.POKEMOB_BUS.addListener(GimmickEvos::makeShedinja);
    }

    /**
     * Changes tyrogue's evolution based on stats.
     * 
     * @param evt
     */
    private static void evolveTyrogue(final EvolveEvent.Pre evt)
    {
        if (evt.mob.getPokedexEntry() == Database.getEntry("tyrogue"))
        {
            final int atk = evt.mob.getStat(Stats.ATTACK, false);
            final int def = evt.mob.getStat(Stats.DEFENSE, false);
            if (atk > def) evt.forme = Database.getEntry("hitmonlee");
            else if (def > atk) evt.forme = Database.getEntry("hitmonchan");
            else evt.forme = Database.getEntry("hitmontop");
        }
    }

    /**
     * Creates a shedinja in the player's inventory if their nincada evolves
     * into ninjask, and they have a spare pokecube.
     * 
     * @param evt
     */
    private static void makeShedinja(final EvolveEvent.Post evt)
    {
        if (evt.mob.getOwner() instanceof ServerPlayer player)
        {
            if (evt.mob.getPokedexEntry() == Database.getEntry("ninjask"))
            {
                final Inventory inv = player.getInventory();
                boolean hasCube = false;
                boolean hasSpace = false;
                ItemStack cube = ItemStack.EMPTY;
                int m = -1;
                for (int n = 0; n < inv.getContainerSize(); n++)
                {
                    final ItemStack item = inv.getItem(n);
                    if (item == ItemStack.EMPTY) hasSpace = true;
                    final ResourceLocation key = PokecubeItems.getCubeId(item);
                    if (!hasCube && key != null && IPokecube.PokecubeBehaviour.BEHAVIORS.containsKey(key)
                            && !PokecubeManager.isFilled(item))
                    {
                        hasCube = true;
                        cube = item;
                        m = n;
                    }
                    if (hasCube && hasSpace) break;

                }
                if (hasCube && hasSpace)
                {
                    final Entity pokemon = PokecubeCore.createPokemob(Database.getEntry("shedinja"), player.getLevel());
                    if (pokemon != null)
                    {
                        final ItemStack mobCube = cube.copy();
                        mobCube.setCount(1);
                        final IPokemob poke = PokemobCaps.getPokemobFor(pokemon);
                        poke.setPokecube(mobCube);
                        poke.setOwner(player);
                        poke.setExp(Tools.levelToXp(poke.getExperienceMode(), 20), true);
                        poke.getEntity().setHealth(poke.getEntity().getMaxHealth());
                        final ItemStack shedinja = PokecubeManager.pokemobToItem(poke);
                        StatsCollector.addCapture(poke);
                        cube.shrink(1);
                        if (cube.isEmpty()) inv.setItem(m, ItemStack.EMPTY);
                        inv.add(shedinja);
                    }
                }
            }
        }
    }
}
