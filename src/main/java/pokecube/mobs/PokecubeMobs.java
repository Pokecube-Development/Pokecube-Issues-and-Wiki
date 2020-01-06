package pokecube.mobs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import pokecube.adventures.utils.DBLoader;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.database.CombatTypeLoader;
import pokecube.core.database.Database;
import pokecube.core.database.Database.EnumDatabase;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.database.stats.EggStats;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.events.onload.InitDatabase;
import pokecube.core.events.onload.RegisterMiscItems;
import pokecube.core.events.onload.RegisterPokecubes;
import pokecube.core.events.onload.RegisterPokemobsEvent;
import pokecube.core.events.pokemob.CaptureEvent.Post;
import pokecube.core.events.pokemob.CaptureEvent.Pre;
import pokecube.core.events.pokemob.EvolveEvent;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokecube.DefaultPokecubeBehavior;
import pokecube.core.interfaces.IPokecube.NormalPokecubeBehavoir;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.Stats;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.Tools;
import thut.api.maths.Vector3;

@Mod(value = PokecubeMobs.MODID)
public class PokecubeMobs
{

    public static final String MODID = "pokecube_mobs";

    public static ArrayList<String> getFile(final String file)
    {
        final InputStream res = PokecubeMobs.class.getResourceAsStream(file);

        final ArrayList<String> rows = new ArrayList<>();
        BufferedReader br = null;
        String line = "";
        try
        {
            br = new BufferedReader(new InputStreamReader(res));
            while ((line = br.readLine()) != null)
                rows.add(line);

        }
        catch (final FileNotFoundException e)
        {
            PokecubeCore.LOGGER.error("Missing a Database file " + file, e);
        }
        catch (final NullPointerException e)
        {
            try
            {
                final FileReader temp = new FileReader(new File(file));
                br = new BufferedReader(temp);
                while ((line = br.readLine()) != null)
                    rows.add(line);
            }
            catch (final Exception e1)
            {
                PokecubeCore.LOGGER.error("Error with " + file, e1);
            }

        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error with " + file, e);
        }
        finally
        {
            if (br != null) try
            {
                br.close();
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error("Error with " + file, e);
            }
        }

        return rows;
    }

    Map<PokedexEntry, Integer> genMap = Maps.newHashMap();

    public PokecubeMobs()
    {
        MinecraftForge.EVENT_BUS.register(this);
        PokecubeCore.POKEMOB_BUS.register(this);
        CombatTypeLoader.TYPES = new ResourceLocation(PokecubeMobs.MODID, "database/types.json");
        Database.STARTERPACK = new ResourceLocation(PokecubeMobs.MODID, "database/pack.xml");

        DBLoader.trainerDatabases.add(new ResourceLocation(PokecubeMobs.MODID, "database/trainers.json"));
        DBLoader.tradeDatabases.add(new ResourceLocation(PokecubeMobs.MODID, "database/trades.xml"));

        // MiscItemHelper.init();

        // // Register smd format for models
        // ModelFactory.registerIModel("smd", SMDModel.class);
        // ModelFactory.registerIModel("SMD", SMDModel.class);
    }

    @SubscribeEvent
    public void evolveTyrogue(final EvolveEvent.Pre evt)
    {
        if (evt.mob.getPokedexEntry() == Database.getEntry("Tyrogue"))
        {
            final int atk = evt.mob.getStat(Stats.ATTACK, false);
            final int def = evt.mob.getStat(Stats.DEFENSE, false);
            if (atk > def) evt.forme = Database.getEntry("Hitmonlee");
            else if (def > atk) evt.forme = Database.getEntry("Hitmonchan");
            else evt.forme = Database.getEntry("Hitmontop");
        }
    }

    private int getGen(PokedexEntry entry)
    {
        int gen;
        if (this.genMap.containsKey(entry)) gen = this.genMap.get(entry);
        else
        {
            gen = entry.getGen();
            final PokedexEntry real = entry;
            if (entry.getBaseForme() != null) entry = entry.getBaseForme();
            for (final EvolutionData d : entry.getEvolutions())
            {
                int gen1 = d.evolution.getGen();
                if (this.genMap.containsKey(d.evolution)) gen1 = this.genMap.get(d.evolution);
                if (gen1 < gen) gen = gen1;
                for (final EvolutionData d1 : d.evolution.getEvolutions())
                {
                    gen1 = d1.evolution.getGen();
                    if (this.genMap.containsKey(d1.evolution)) gen1 = this.genMap.get(d1.evolution);
                    if (d.evolution == entry && gen1 < gen) gen = gen1;
                }
            }
            for (final PokedexEntry e : Database.getSortedFormes())
            {
                int gen1 = e.getGen();
                if (this.genMap.containsKey(e)) gen1 = this.genMap.get(e);
                for (final EvolutionData d : e.getEvolutions())
                    if (d.evolution == entry && gen1 < gen) gen = gen1;
            }
            this.genMap.put(real, gen);
        }
        return gen;
    }

    public String getModelDirectory(final PokedexEntry entry)
    {
        final int gen = this.getGen(entry);
        switch (gen)
        {
        case 1:
            return "gen_1/entity/models/";
        case 2:
            return "gen_2/entity/models/";
        case 3:
            return "gen_3/entity/models/";
        case 4:
            return "gen_4/entity/models/";
        case 5:
            return "gen_5/entity/models/";
        case 6:
            return "gen_6/entity/models/";
        case 7:
            return "gen_7/entity/models/";
        }
        return "entity/models/";
    }

    public String getTextureDirectory(final PokedexEntry entry)
    {
        final int gen = this.getGen(entry);
        switch (gen)
        {
        case 1:
            return "gen_1/entity/textures/";
        case 2:
            return "gen_2/entity/textures/";
        case 3:
            return "gen_3/entity/textures/";
        case 4:
            return "gen_4/entity/textures/";
        case 5:
            return "gen_5/entity/textures/";
        case 6:
            return "gen_6/entity/textures/";
        case 7:
            return "gen_7/entity/textures/";
        }
        return "entity/textures/";
    }

    @SubscribeEvent
    public void livingUpdate(final LivingUpdateEvent evt)
    {
        final IPokemob shuckle = CapabilityPokemob.getPokemobFor(evt.getEntity());
        if (shuckle != null && shuckle.getPokedexNb() == 213)
        {
            if (evt.getEntity().getEntityWorld().isRemote) return;

            final ItemStack item = shuckle.getEntity().getHeldItemMainhand();
            if (item.isEmpty()) return;
            final Item itemId = item.getItem();
            boolean berry = itemId == BerryManager.getBerryItem("oran");
            final Random r = new Random();
            if (berry && r.nextGaussian() > EventsHandler.juiceChance)
            {
                if (shuckle.getOwner() != null)
                {
                    final String message = "A sweet smell is coming from " + shuckle.getDisplayName()
                            .getFormattedText();
                    ((PlayerEntity) shuckle.getOwner()).sendMessage(new StringTextComponent(message));
                }
                shuckle.setHeldItem(new ItemStack(PokecubeItems.BERRYJUICE));
                return;
            }
            berry = itemId == PokecubeItems.BERRYJUICE;
            if (berry && r.nextGaussian() > EventsHandler.candyChance)
            {
                final ItemStack candy = PokecubeItems.makeCandyStack();
                if (candy.isEmpty()) return;

                if (shuckle.getOwner() != null && shuckle.getOwner() instanceof PlayerEntity)
                {
                    final String message = "The smell coming from " + shuckle.getDisplayName().getFormattedText()
                            + " has changed";
                    ((PlayerEntity) shuckle.getOwner()).sendMessage(new StringTextComponent(message));
                }
                shuckle.setHeldItem(candy);
                return;
            }
        }
    }

    @SubscribeEvent
    public void makeShedinja(final EvolveEvent.Post evt)
    {
        Entity owner;
        if ((owner = evt.mob.getOwner()) instanceof PlayerEntity) this.makeShedinja(evt.mob, (PlayerEntity) owner);
    }

    void makeShedinja(final IPokemob evo, final PlayerEntity player)
    {
        if (evo.getPokedexEntry() == Database.getEntry("ninjask"))
        {
            final PlayerInventory inv = player.inventory;
            boolean hasCube = false;
            boolean hasSpace = false;
            ItemStack cube = ItemStack.EMPTY;
            int m = -1;
            for (int n = 0; n < inv.getSizeInventory(); n++)
            {
                final ItemStack item = inv.getStackInSlot(n);
                if (item == ItemStack.EMPTY) hasSpace = true;
                final ResourceLocation key = PokecubeItems.getCubeId(item);
                if (!hasCube && key != null && IPokecube.BEHAVIORS.containsKey(key) && !PokecubeManager.isFilled(item))
                {
                    hasCube = true;
                    cube = item;
                    m = n;
                }
                if (hasCube && hasSpace) break;

            }
            if (hasCube && hasSpace)
            {
                final Entity pokemon = PokecubeCore.createPokemob(Database.getEntry("shedinja"), player
                        .getEntityWorld());
                if (pokemon != null)
                {
                    final ItemStack mobCube = cube.copy();
                    mobCube.setCount(1);
                    final IPokemob poke = CapabilityPokemob.getPokemobFor(pokemon);
                    poke.setPokecube(mobCube);
                    poke.setOwner(player);
                    poke.setExp(Tools.levelToXp(poke.getExperienceMode(), 20), true);
                    poke.getEntity().setHealth(poke.getEntity().getMaxHealth());
                    final ItemStack shedinja = PokecubeManager.pokemobToItem(poke);
                    StatsCollector.addCapture(poke);
                    cube.shrink(1);
                    if (cube.isEmpty()) inv.setInventorySlotContents(m, ItemStack.EMPTY);
                    inv.addItemStackToInventory(shedinja);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void registerDatabases(final InitDatabase.Pre evt)
    {
        Database.addDatabase("pokecube_mobs:database/pokemobs/pokemobs_pokedex.json", EnumDatabase.POKEMON);
        Database.addDatabase("pokecube_mobs:database/pokemobs/pokemobs_spawns.json", EnumDatabase.POKEMON);
        Database.addDatabase("pokecube_mobs:database/pokemobs/pokemobs_drops.json", EnumDatabase.POKEMON);
        Database.addDatabase("pokecube_mobs:database/pokemobs/pokemobs_interacts.json", EnumDatabase.POKEMON);

        Database.addDatabase("pokecube_mobs:database/moves.json", EnumDatabase.MOVES);

        Database.addDatabase("pokecube_mobs:database/berries/spawns.json", EnumDatabase.BERRIES);

        evt.modIDs.add(PokecubeMobs.MODID);
    }

    @SubscribeEvent
    public void registerItems(final RegisterMiscItems event)
    {

        ItemGenerator.berryWoods.put("pecha", MaterialColor.PINK);
        ItemGenerator.berryWoods.put("oran", MaterialColor.BLUE);
        ItemGenerator.berryWoods.put("leppa", MaterialColor.RED);
        ItemGenerator.berryWoods.put("sitrus", MaterialColor.YELLOW);
        ItemGenerator.berryWoods.put("enigma", MaterialColor.BLACK);
        ItemGenerator.berryWoods.put("nanab", MaterialColor.WHITE_TERRACOTTA);

        ItemGenerator.onlyBerryLeaves.add("pomeg");
        ItemGenerator.onlyBerryLeaves.add("kelpsy");
        ItemGenerator.onlyBerryLeaves.add("qualot");
        ItemGenerator.onlyBerryLeaves.add("hondew");
        ItemGenerator.onlyBerryLeaves.add("grepa");
        ItemGenerator.onlyBerryLeaves.add("tamato");

        ItemGenerator.variants.add("waterstone");
        ItemGenerator.variants.add("firestone");
        ItemGenerator.variants.add("leafstone");
        ItemGenerator.variants.add("thunderstone");
        ItemGenerator.variants.add("moonstone");
        ItemGenerator.variants.add("sunstone");
        ItemGenerator.variants.add("shinystone");
        ItemGenerator.variants.add("ovalstone");
        ItemGenerator.variants.add("everstone");
        ItemGenerator.variants.add("duskstone");
        ItemGenerator.variants.add("dawnstone");
        ItemGenerator.variants.add("kingsrock");
        ItemGenerator.variants.add("dubiousdisc");
        ItemGenerator.variants.add("electirizer");
        ItemGenerator.variants.add("magmarizer");
        ItemGenerator.variants.add("reapercloth");
        ItemGenerator.variants.add("prismscale");
        ItemGenerator.variants.add("protector");
        ItemGenerator.variants.add("upgrade");
        ItemGenerator.variants.add("metalcoat");

        ItemGenerator.variants.add("megastone");
        ItemGenerator.variants.add("shiny_charm");
        ItemGenerator.variants.add("omegaorb");
        ItemGenerator.variants.add("alphaorb");
        ItemGenerator.variants.add("aerodactylmega");
        ItemGenerator.variants.add("abomasnowmega");
        ItemGenerator.variants.add("absolmega");
        ItemGenerator.variants.add("aggronmega");
        ItemGenerator.variants.add("alakazammega");
        ItemGenerator.variants.add("altariamega");
        ItemGenerator.variants.add("ampharosmega");
        ItemGenerator.variants.add("audinomega");
        ItemGenerator.variants.add("banettemega");
        ItemGenerator.variants.add("beedrillmega");
        ItemGenerator.variants.add("blastoisemega");
        ItemGenerator.variants.add("blazikenmega");
        ItemGenerator.variants.add("cameruptmega");
        ItemGenerator.variants.add("charizardmega-y");
        ItemGenerator.variants.add("charizardmega-x");
        ItemGenerator.variants.add("dianciemega");
        ItemGenerator.variants.add("gallademega");
        ItemGenerator.variants.add("garchompmega");
        ItemGenerator.variants.add("gardevoirmega");
        ItemGenerator.variants.add("gengarmega");
        ItemGenerator.variants.add("glaliemega");
        ItemGenerator.variants.add("gyaradosmega");
        ItemGenerator.variants.add("heracrossmega");
        ItemGenerator.variants.add("houndoommega");
        ItemGenerator.variants.add("kangaskhanmega");
        ItemGenerator.variants.add("latiasmega");
        ItemGenerator.variants.add("latiosmega");
        ItemGenerator.variants.add("lopunnymega");
        ItemGenerator.variants.add("lucariomega");
        ItemGenerator.variants.add("manectricmega");
        ItemGenerator.variants.add("mawilemega");
        ItemGenerator.variants.add("medichammega");
        ItemGenerator.variants.add("metagrossmega");
        ItemGenerator.variants.add("mewtwomega-y");
        ItemGenerator.variants.add("mewtwomega-x");
        ItemGenerator.variants.add("pidgeotmega");
        ItemGenerator.variants.add("pinsirmega");
        ItemGenerator.variants.add("sableyemega");
        ItemGenerator.variants.add("salamencemega");
        ItemGenerator.variants.add("sceptilemega");
        ItemGenerator.variants.add("scizormega");
        ItemGenerator.variants.add("sharpedomega");
        ItemGenerator.variants.add("slowbromega");
        ItemGenerator.variants.add("steelixmega");
        ItemGenerator.variants.add("swampertmega");
        ItemGenerator.variants.add("tyranitarmega");
        ItemGenerator.variants.add("venusaurmega");

        ItemGenerator.variants.add("mewhair");

        ItemGenerator.fossilVariants.add("omanyte");
        ItemGenerator.fossilVariants.add("kabuto");
        ItemGenerator.fossilVariants.add("aerodactyl");
        ItemGenerator.fossilVariants.add("lileep");
        ItemGenerator.fossilVariants.add("anorith");
        ItemGenerator.fossilVariants.add("cranidos");
        ItemGenerator.fossilVariants.add("shieldon");
        ItemGenerator.fossilVariants.add("archen");
        ItemGenerator.fossilVariants.add("tirtouga");
        ItemGenerator.fossilVariants.add("tyrunt");
        ItemGenerator.fossilVariants.add("amaura");
        BerryHelper.initBerries();
    }

    @SubscribeEvent
    public void registerPokecubes(final RegisterPokecubes event)
    {
        if (ModList.get().isLoaded("thut_wearables")) MegaWearablesHelper.initExtraWearables();

        MiscItemHelper.init();
        // Charcoal
        MiscItemHelper.CHARCOALSTACK = new ItemStack(Items.CHARCOAL);

        final PokecubeHelper helper = new PokecubeHelper();
        PokecubeBehavior.DEFAULTCUBE = new ResourceLocation("pokecube", "poke");

        event.register(new NormalPokecubeBehavoir(1).setRegistryName(PokecubeBehavior.DEFAULTCUBE));
        event.register(new NormalPokecubeBehavoir(1.5).setRegistryName("pokecube", "great"));
        event.register(new NormalPokecubeBehavoir(2).setRegistryName("pokecube", "ultra"));
        event.register(new NormalPokecubeBehavoir(255).setRegistryName("pokecube", "master"));
        event.register(new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.dusk(mob);
            }
        }.setRegistryName("pokecube", "dusk"));
        event.register(new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.quick(mob);
            }
        }.setRegistryName("pokecube", "quick"));
        event.register(new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.timer(mob);
            }
        }.setRegistryName("pokecube", "timer"));
        event.register(new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.net(mob);
            }
        }.setRegistryName("pokecube", "net"));
        event.register(new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.nest(mob);
            }
        }.setRegistryName("pokecube", "nest"));
        event.register(new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.dive(mob);
            }
        }.setRegistryName("pokecube", "dive"));
        event.register(new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.premier(mob);
            }
        }.setRegistryName("pokecube", "premier"));
        event.register(new NormalPokecubeBehavoir(1).setRegistryName("pokecube", "cherish"));
        event.register(new NormalPokecubeBehavoir(1.5).setRegistryName("pokecube", "safari"));
        event.register(new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.level(mob);
            }
        }.setRegistryName("pokecube", "level"));
        event.register(new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.lure(mob);
            }
        }.setRegistryName("pokecube", "lure"));
        event.register(new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.moon(mob);
            }
        }.setRegistryName("pokecube", "moon"));
        event.register(new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return 1;
            }

            @Override
            public void onPostCapture(final Post evt)
            {
                final IPokemob mob = evt.caught;
                mob.addHappiness(200 - mob.getHappiness());
            }
        }.setRegistryName("pokecube", "friend"));
        event.register(new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.love(mob);
            }
        }.setRegistryName("pokecube", "love"));
        event.register(new NormalPokecubeBehavoir(1)
        {
            @Override
            public int getAdditionalBonus(final IPokemob mob)
            {
                return helper.heavy(mob);
            }
        }.setRegistryName("pokecube", "heavy"));
        event.register(new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.fast(mob);
            }
        }.setRegistryName("pokecube", "fast"));
        event.register(new NormalPokecubeBehavoir(1.5).setRegistryName("pokecube", "sport"));
        event.register(new NormalPokecubeBehavoir(1)
        {
            @Override
            public void onUpdate(final IPokemob mob)
            {
                helper.luxury(mob);
            }
        }.setRegistryName("pokecube", "luxury"));
        event.register(new NormalPokecubeBehavoir(1)
        {
            @Override
            public void onPostCapture(final Post evt)
            {
                final IPokemob mob = evt.caught;
                mob.getEntity().setHealth(mob.getEntity().getMaxHealth());
                mob.healStatus();
            }
        }.setRegistryName("pokecube", "heal"));
        event.register(new NormalPokecubeBehavoir(255).setRegistryName("pokecube", "park"));
        event.register(new NormalPokecubeBehavoir(255).setRegistryName("pokecube", "dream"));

        final PokecubeBehavior snag = new PokecubeBehavior()
        {

            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return 0;
            }

            @Override
            public void onPostCapture(final Post evt)
            {
                final IPokemob mob = evt.caught;
                if (mob != null) evt.pokecube.entityDropItem(PokecubeManager.pokemobToItem(mob), 1.0F);
                evt.setCanceled(true);
            }

            @Override
            public void onPreCapture(final Pre evt)
            {
                final boolean tameSnag = !evt.caught.isPlayerOwned() && evt.caught.getGeneralState(GeneralStates.TAMED);

                if (evt.caught.isShadow())
                {
                    final EntityPokecube cube = (EntityPokecube) evt.pokecube;
                    final IPokemob mob = CapabilityPokemob.getPokemobFor(PokecubeCore.createPokemob(evt.caught
                            .getPokedexEntry(), cube.getEntityWorld()));
                    cube.tilt = Tools.computeCatchRate(mob, 1);
                    cube.setTime(cube.tilt * 20);
                    if (!tameSnag) evt.caught.setPokecube(evt.filledCube);
                    cube.setItemEntityStack(PokecubeManager.pokemobToItem(evt.caught));
                    PokecubeManager.setTilt(cube.getItemEntity(), cube.tilt);
                    Vector3.getNewVector().set(evt.pokecube).moveEntity(cube);
                    evt.caught.getEntity().remove();
                    cube.setMotion(0, 0.1, 0);
                    cube.getEntityWorld().addEntity(cube.copy());
                    evt.pokecube.remove();
                }
                evt.setCanceled(true);
            }
        };

        final PokecubeBehavior repeat = new PokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return 0;
            }

            @Override
            public void onPostCapture(final Post evt)
            {

            }

            @Override
            public void onPreCapture(final Pre evt)
            {
                if (evt.getResult() == Result.DENY) return;

                final EntityPokecube cube = (EntityPokecube) evt.pokecube;

                final IPokemob mob = CapabilityPokemob.getPokemobFor(PokecubeCore.createPokemob(evt.caught
                        .getPokedexEntry(), cube.getEntityWorld()));
                final Vector3 v = Vector3.getNewVector();
                final Entity thrower = cube.shootingEntity;
                int has = CaptureStats.getTotalNumberOfPokemobCaughtBy(thrower.getUniqueID(), mob.getPokedexEntry());
                has = has + EggStats.getTotalNumberOfPokemobHatchedBy(thrower.getUniqueID(), mob.getPokedexEntry());
                final double rate = has > 0 ? 3 : 1;
                cube.tilt = Tools.computeCatchRate(mob, rate);
                cube.setTime(cube.tilt * 20);
                evt.caught.setPokecube(evt.filledCube);
                cube.setItemEntityStack(PokecubeManager.pokemobToItem(evt.caught));
                PokecubeManager.setTilt(cube.getItemEntity(), cube.tilt);
                v.set(evt.pokecube).moveEntity(cube);
                v.moveEntity(mob.getEntity());
                evt.caught.getEntity().remove();
                cube.setMotion(0, 0.1, 0);
                cube.getEntityWorld().addEntity(cube.copy());
                evt.setCanceled(true);
                evt.pokecube.remove();
            }

        };

        event.register(snag.setRegistryName("pokecube", "snag"));
        event.register(repeat.setRegistryName("pokecube", "repeat"));
    }

    @SubscribeEvent
    public void RegisterPokemobsEvent(final RegisterPokemobsEvent.Register event)
    {
        for (final PokedexEntry entry : Database.getSortedFormes())
        {
            if (entry == Database.missingno) continue;
            if (entry.model != PokedexEntry.MODELNO) continue;
            final String tex = this.getTextureDirectory(entry);
            final String model = this.getModelDirectory(entry);
            entry.setModId(PokecubeMobs.MODID);
            entry.texturePath = tex;
            entry.model = new ResourceLocation(PokecubeMobs.MODID, model + entry.getTrimmedName());
            entry.texture = new ResourceLocation(PokecubeMobs.MODID, tex + entry.getTrimmedName() + ".png");
            entry.animation = new ResourceLocation(PokecubeMobs.MODID, model + entry.getTrimmedName() + ".xml");
        }
    }
}
