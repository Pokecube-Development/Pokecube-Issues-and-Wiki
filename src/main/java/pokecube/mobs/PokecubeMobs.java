package pokecube.mobs;

import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.events.init.InitDatabase;
import pokecube.api.events.init.RegisterMiscItems;
import pokecube.api.events.init.RegisterPokecubes;
import pokecube.api.events.init.RegisterPokemobsEvent;
import pokecube.api.events.pokemobs.CaptureEvent.Post;
import pokecube.api.events.pokemobs.CaptureEvent.Pre;
import pokecube.api.items.IPokecube.DefaultPokecubeBehaviour;
import pokecube.api.items.IPokecube.NormalPokecubeBehaviour;
import pokecube.api.items.IPokecube.PokecubeBehaviour;
import pokecube.api.stats.CaptureStats;
import pokecube.api.stats.EggStats;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.berries.BerryGenManager;
import pokecube.core.database.Database;
import pokecube.core.entity.pokecubes.EntityPokecubeBase;
import pokecube.core.eventhandlers.EventsHandler;
import pokecube.core.init.ItemGenerator;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.megastuff.ItemMegawearable;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.mobs.abilities.AbilityRegister;
import pokecube.mobs.data.DataGenerator;
import pokecube.mobs.init.PokemobSounds;
import pokecube.mobs.moves.MoveRegister;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

@Mod(value = PokecubeMobs.MODID)
public class PokecubeMobs
{
    public static final String MODID = "pokecube_mobs";

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,
            PokecubeMobs.MODID);;

    Map<PokedexEntry, Integer> genMap = Maps.newHashMap();

    public PokecubeMobs()
    {
        ThutCore.FORGE_BUS.register(this);
        PokecubeAPI.POKEMOB_BUS.register(this);

        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        PokecubeMobs.SOUNDS.register(bus);
        bus.addListener(this::loadComplete);

        new BerryGenManager(PokecubeMobs.MODID);
        MoveRegister.init();
        AbilityRegister.init();
    }

    private void loadComplete(final FMLLoadCompleteEvent event)
    {
        if (PokecubeCore.getConfig().debug_misc && !FMLLoader.isProduction())
        {
            DataGenerator.execute(false);
        }
    }

    @SubscribeEvent
    public void livingUpdate(final LivingUpdateEvent evt)
    {
        final IPokemob shuckle = PokemobCaps.getPokemobFor(evt.getEntity());
        if (shuckle != null && shuckle.getPokedexNb() == 213)
        {
            if (evt.getEntity().level.isClientSide) return;

            final ItemStack item = shuckle.getEntity().getMainHandItem();
            if (item.isEmpty()) return;
            final Item itemId = item.getItem();
            boolean berry = itemId == BerryManager.getBerryItem("oran");
            final Random r = ThutCore.newRandom();
            if (berry && r.nextGaussian() > EventsHandler.juiceChance)
            {
                if (shuckle.getOwner() instanceof Player player)
                {
                    final String message = "A sweet smell is coming from " + shuckle.getDisplayName().getString();
                    thut.lib.ChatHelper.sendSystemMessage(player, TComponent.literal(message));
                }
                shuckle.setHeldItem(new ItemStack(PokecubeItems.BERRYJUICE.get()));
                return;
            }
            berry = itemId == PokecubeItems.BERRYJUICE.get();
            if (berry && r.nextGaussian() > EventsHandler.candyChance)
            {
                final ItemStack candy = PokecubeItems.makeCandyStack();
                if (candy.isEmpty()) return;

                if (shuckle.getOwner() != null && shuckle.getOwner() instanceof Player player)
                {
                    final String message = "The smell coming from " + shuckle.getDisplayName().getString()
                            + " has changed";
                    thut.lib.ChatHelper.sendSystemMessage(player, TComponent.literal(message));
                }
                shuckle.setHeldItem(candy);
                return;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void registerDatabases(final InitDatabase.Pre evt)
    {
        evt.modIDs.add(PokecubeMobs.MODID);
    }

    @SubscribeEvent
    public void registerItems(final RegisterMiscItems event)
    {

        ItemGenerator.berryWoods.put("enigma", MaterialColor.COLOR_BLACK);
        ItemGenerator.berryWoods.put("leppa", MaterialColor.COLOR_RED);
        ItemGenerator.berryWoods.put("nanab", MaterialColor.TERRACOTTA_BROWN);
        ItemGenerator.berryWoods.put("oran", MaterialColor.COLOR_LIGHT_BLUE);
        ItemGenerator.berryWoods.put("pecha", MaterialColor.COLOR_MAGENTA);
        ItemGenerator.berryWoods.put("sitrus", MaterialColor.TERRACOTTA_YELLOW);

        ItemGenerator.berryLeaves.put("enigma", MaterialColor.TERRACOTTA_WHITE);
        ItemGenerator.berryLeaves.put("leppa", MaterialColor.SAND);
        ItemGenerator.berryLeaves.put("nanab", MaterialColor.COLOR_LIGHT_BLUE);
        ItemGenerator.berryLeaves.put("oran", MaterialColor.COLOR_ORANGE);
        ItemGenerator.berryLeaves.put("pecha", MaterialColor.TERRACOTTA_LIGHT_BLUE);
        ItemGenerator.berryLeaves.put("sitrus", MaterialColor.TERRACOTTA_LIGHT_BLUE);

        ItemGenerator.onlyBerryLeaves.put("grepa", MaterialColor.COLOR_RED);
        ItemGenerator.onlyBerryLeaves.put("hondew", MaterialColor.TERRACOTTA_PURPLE);
        ItemGenerator.onlyBerryLeaves.put("kelpsy", MaterialColor.COLOR_PINK);
        ItemGenerator.onlyBerryLeaves.put("pomeg", MaterialColor.COLOR_RED);
        ItemGenerator.onlyBerryLeaves.put("qualot", MaterialColor.PLANT);
        ItemGenerator.onlyBerryLeaves.put("tamato", MaterialColor.COLOR_LIGHT_BLUE);

        ItemGenerator.berryCrops.put("aspear", MaterialColor.COLOR_RED);
        ItemGenerator.berryCrops.put("cheri", MaterialColor.PLANT);
        ItemGenerator.berryCrops.put("chesto", MaterialColor.COLOR_PINK);
        ItemGenerator.berryCrops.put("cornn", MaterialColor.COLOR_PINK);
        ItemGenerator.berryCrops.put("enigma", MaterialColor.TERRACOTTA_WHITE);
        ItemGenerator.berryCrops.put("grepa", MaterialColor.COLOR_RED);
        ItemGenerator.berryCrops.put("hondew", MaterialColor.PLANT);
        ItemGenerator.berryCrops.put("jaboca", MaterialColor.COLOR_PURPLE);
        ItemGenerator.berryCrops.put("kelpsy", MaterialColor.COLOR_PINK);
        ItemGenerator.berryCrops.put("leppa", MaterialColor.TERRACOTTA_YELLOW);
        ItemGenerator.berryCrops.put("lum", MaterialColor.COLOR_PURPLE);
        ItemGenerator.berryCrops.put("nanab", MaterialColor.COLOR_LIGHT_BLUE);
        ItemGenerator.berryCrops.put("null", MaterialColor.PLANT);
        ItemGenerator.berryCrops.put("oran", MaterialColor.COLOR_ORANGE);
        ItemGenerator.berryCrops.put("pecha", MaterialColor.PLANT);
        ItemGenerator.berryCrops.put("persim", MaterialColor.COLOR_LIGHT_BLUE);
        ItemGenerator.berryCrops.put("pinap", MaterialColor.PLANT);
        ItemGenerator.berryCrops.put("pomeg", MaterialColor.COLOR_RED);
        ItemGenerator.berryCrops.put("qualot", MaterialColor.PLANT);
        ItemGenerator.berryCrops.put("rawst", MaterialColor.PLANT);
        ItemGenerator.berryCrops.put("rowap", MaterialColor.COLOR_LIGHT_BLUE);
        ItemGenerator.berryCrops.put("sitrus", MaterialColor.TERRACOTTA_LIGHT_BLUE);
        ItemGenerator.berryCrops.put("tamato", MaterialColor.COLOR_LIGHT_BLUE);

        ItemGenerator.berryFruits.put("aspear", MaterialColor.COLOR_RED);
        ItemGenerator.berryFruits.put("cheri", MaterialColor.COLOR_LIGHT_BLUE);
        ItemGenerator.berryFruits.put("chesto", MaterialColor.COLOR_PINK);
        ItemGenerator.berryFruits.put("cornn", MaterialColor.COLOR_PINK);
        ItemGenerator.berryFruits.put("enigma", MaterialColor.COLOR_BLACK);
        ItemGenerator.berryFruits.put("grepa", MaterialColor.COLOR_YELLOW);
        ItemGenerator.berryFruits.put("hondew", MaterialColor.COLOR_LIGHT_GREEN);
        ItemGenerator.berryFruits.put("jaboca", MaterialColor.COLOR_PURPLE);
        ItemGenerator.berryFruits.put("kelpsy", MaterialColor.COLOR_LIGHT_BLUE);
        ItemGenerator.berryFruits.put("leppa", MaterialColor.COLOR_RED);
        ItemGenerator.berryFruits.put("lum", MaterialColor.COLOR_PURPLE);
        ItemGenerator.berryFruits.put("nanab", MaterialColor.COLOR_PINK);
        ItemGenerator.berryFruits.put("null", MaterialColor.PLANT);
        ItemGenerator.berryFruits.put("oran", MaterialColor.COLOR_LIGHT_BLUE);
        ItemGenerator.berryFruits.put("pecha", MaterialColor.COLOR_PINK);
        ItemGenerator.berryFruits.put("persim", MaterialColor.COLOR_LIGHT_BLUE);
        ItemGenerator.berryFruits.put("pinap", MaterialColor.COLOR_YELLOW);
        ItemGenerator.berryFruits.put("pomeg", MaterialColor.TERRACOTTA_ORANGE);
        ItemGenerator.berryFruits.put("qualot", MaterialColor.COLOR_YELLOW);
        ItemGenerator.berryFruits.put("rawst", MaterialColor.PLANT);
        ItemGenerator.berryFruits.put("rowap", MaterialColor.COLOR_LIGHT_BLUE);
        ItemGenerator.berryFruits.put("sitrus", MaterialColor.COLOR_YELLOW);
        ItemGenerator.berryFruits.put("tamato", MaterialColor.TERRACOTTA_ORANGE);

        ItemGenerator.variants.add("dawnstone");
        ItemGenerator.variants.add("dubiousdisc");
        ItemGenerator.variants.add("duskstone");
        ItemGenerator.variants.add("electirizer");
        ItemGenerator.variants.add("everstone");
        ItemGenerator.variants.add("firestone");
        ItemGenerator.variants.add("kingsrock");
        ItemGenerator.variants.add("leafstone");
        ItemGenerator.variants.add("magmarizer");
        ItemGenerator.variants.add("metalcoat");
        ItemGenerator.variants.add("moonstone");
        ItemGenerator.variants.add("ovalstone");
        ItemGenerator.variants.add("prismscale");
        ItemGenerator.variants.add("protector");
        ItemGenerator.variants.add("reapercloth");
        ItemGenerator.variants.add("shinystone");
        ItemGenerator.variants.add("sunstone");
        ItemGenerator.variants.add("thunderstone");
        ItemGenerator.variants.add("upgrade");
        ItemGenerator.variants.add("waterstone");

        ItemGenerator.variants.add("abomasnowmega");
        ItemGenerator.variants.add("absolmega");
        ItemGenerator.variants.add("aerodactylmega");
        ItemGenerator.variants.add("aggronmega");
        ItemGenerator.variants.add("alakazammega");
        ItemGenerator.variants.add("alphaorb");
        ItemGenerator.variants.add("altariamega");
        ItemGenerator.variants.add("ampharosmega");
        ItemGenerator.variants.add("audinomega");
        ItemGenerator.variants.add("banettemega");
        ItemGenerator.variants.add("beedrillmega");
        ItemGenerator.variants.add("blastoisemega");
        ItemGenerator.variants.add("blazikenmega");
        ItemGenerator.variants.add("cameruptmega");
        ItemGenerator.variants.add("charizardmega-x");
        ItemGenerator.variants.add("charizardmega-y");
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
        ItemGenerator.variants.add("megastone");
        ItemGenerator.variants.add("metagrossmega");
        ItemGenerator.variants.add("mewtwomega-x");
        ItemGenerator.variants.add("mewtwomega-y");
        ItemGenerator.variants.add("omegaorb");
        ItemGenerator.variants.add("pidgeotmega");
        ItemGenerator.variants.add("pinsirmega");
        ItemGenerator.variants.add("sableyemega");
        ItemGenerator.variants.add("salamencemega");
        ItemGenerator.variants.add("sceptilemega");
        ItemGenerator.variants.add("scizormega");
        ItemGenerator.variants.add("sharpedomega");
        ItemGenerator.variants.add("shiny_charm");
        ItemGenerator.variants.add("slowbromega");
        ItemGenerator.variants.add("steelixmega");
        ItemGenerator.variants.add("swampertmega");
        ItemGenerator.variants.add("tyranitarmega");
        ItemGenerator.variants.add("venusaurmega");

        ItemGenerator.other.add("mewhair");

        ItemGenerator.fossilVariants.add("aerodactyl");
        ItemGenerator.fossilVariants.add("amaura");
        ItemGenerator.fossilVariants.add("anorith");
        ItemGenerator.fossilVariants.add("archen");
        ItemGenerator.fossilVariants.add("arctovish");
        ItemGenerator.fossilVariants.add("arctozolt");
        ItemGenerator.fossilVariants.add("cranidos");
        ItemGenerator.fossilVariants.add("dracovish");
        ItemGenerator.fossilVariants.add("dracozolt");
        ItemGenerator.fossilVariants.add("kabuto");
        ItemGenerator.fossilVariants.add("lileep");
        ItemGenerator.fossilVariants.add("omanyte");
        ItemGenerator.fossilVariants.add("shieldon");
        ItemGenerator.fossilVariants.add("tirtouga");
        ItemGenerator.fossilVariants.add("tyrunt");
        BerryHelper.initBerries();

        ItemMegawearable.registerWearable("tiara", "HAT");
        ItemMegawearable.registerWearable("ankletzinnia", "ANKLE");
        ItemMegawearable.registerWearable("pendant", "NECK");
        ItemMegawearable.registerWearable("earring", "EAR");
        ItemMegawearable.registerWearable("glasses", "EYE");
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void registerPokecubes(final RegisterPokecubes event)
    {
        MiscItemHelper.init();
        // Charcoal
        MiscItemHelper.CHARCOALSTACK = new ItemStack(Items.CHARCOAL);

        final PokecubeHelper helper = new PokecubeHelper();
        PokecubeBehaviour.DEFAULTCUBE = new ResourceLocation("pokecube", "pokecube");

        event.register(new NormalPokecubeBehaviour(1).setName("poke"));
        event.register(new NormalPokecubeBehaviour(1.5).setName("great"));
        event.register(new NormalPokecubeBehaviour(2).setName("ultra"));
        event.register(new NormalPokecubeBehaviour(255).setName("master"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.dusk(mob);
            }
        }.setName("dusk"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.quick(mob);
            }
        }.setName("quick"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.timer(mob);
            }
        }.setName("timer"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.net(mob);
            }
        }.setName("net"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.nest(mob);
            }
        }.setName("nest"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.dive(mob);
            }
        }.setName("dive"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.premier(mob);
            }
        }.setName("premier"));
        event.register(new NormalPokecubeBehaviour(1).setName("cherish"));
        event.register(new NormalPokecubeBehaviour(1.5).setName("safari"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.level(mob);
            }
        }.setName("level"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.lure(mob);
            }
        }.setName("lure"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.moon(mob);
            }
        }.setName("moon"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return 1;
            }

            @Override
            public void onPostCapture(final Post evt)
            {
                final IPokemob mob = evt.getCaught();
                mob.addHappiness(200 - mob.getHappiness());
            }
        }.setName("friend"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.love(mob);
            }
        }.setName("love"));
        event.register(new NormalPokecubeBehaviour(1)
        {
            @Override
            public int getAdditionalBonus(final IPokemob mob)
            {
                return helper.heavy(mob);
            }
        }.setName("heavy"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.fast(mob);
            }
        }.setName("fast"));
        event.register(new NormalPokecubeBehaviour(1.5).setName("sport"));
        event.register(new NormalPokecubeBehaviour(1)
        {
            @Override
            public void onUpdate(final IPokemob mob)
            {
                helper.luxury(mob);
            }
        }.setName("luxury"));
        event.register(new NormalPokecubeBehaviour(1)
        {
            @Override
            public void onPostCapture(final Post evt)
            {
                final IPokemob mob = evt.getCaught();
                mob.getEntity().setHealth(mob.getEntity().getMaxHealth());
                mob.healStatus();
            }
        }.setName("heal"));
        event.register(new NormalPokecubeBehaviour(255).setName("park"));
        event.register(new NormalPokecubeBehaviour(255).setName("dream"));

        final PokecubeBehaviour snag = new PokecubeBehaviour()
        {

            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return 0;
            }

            @Override
            public void onPostCapture(final Post evt)
            {
                final IPokemob mob = evt.getCaught();
                if (mob != null) evt.pokecube.spawnAtLocation(PokecubeManager.pokemobToItem(mob), 1.0F);
                evt.setCanceled(true);
            }

            @Override
            public void onPreCapture(final Pre evt)
            {
                // The below processing is for pokemobs only
                if (evt.getCaught() == null) return;

                if (evt.getCaught().isShadow())
                {
                    final EntityPokecubeBase cube = evt.pokecube;
                    final IPokemob mob = PokemobCaps.getPokemobFor(
                            PokecubeCore.createPokemob(evt.getCaught().getPokedexEntry(), cube.getLevel()));
                    cube.setTilt(Tools.computeCatchRate(mob, 1));
                    evt.setCanceled(true);
                }
            }
        };

        final PokecubeBehaviour repeat = new PokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return 1;
            }

            @Override
            public void onPostCapture(final Post evt)
            {

            }

            @Override
            public void onPreCapture(final Pre evt)
            {
                if (evt.getResult() == Result.DENY || evt.getCaught() == null) return;
                final EntityPokecubeBase cube = evt.pokecube;
                final Entity thrower = cube.shootingEntity;
                int has = CaptureStats.getTotalNumberOfPokemobCaughtBy(thrower.getUUID(),
                        evt.getCaught().getPokedexEntry());
                has = has + EggStats.getTotalNumberOfPokemobHatchedBy(thrower.getUUID(),
                        evt.getCaught().getPokedexEntry());
                final double rate = has > 0 ? 3 : 1;
                cube.setTilt(Tools.computeCatchRate(evt.getCaught(), rate));
                evt.setCanceled(true);
            }

        };
        event.register(snag.setName("snag"));
        event.register(repeat.setName("repeat"));
    }

    @SubscribeEvent
    public void RegisterPokemobsEvent(final RegisterPokemobsEvent.Register event)
    {
        final String modid = PokecubeMobs.MODID;
        Database.getSortedFormes().forEach(entry -> {
            if (!modid.equals(entry.getModId())) return;
            String tex = entry.texturePath;
            String model = entry.modelPath;
            entry.texturePath = tex;
            if (!tex.contains(":")) entry.texturePath = modid + ":" + tex;
            else tex = tex.split(":")[1];
            if (!model.contains(":")) entry.modelPath = modid + ":" + model;
            else model = model.split(":")[1];
            entry.model = new ResourceLocation(modid, model + entry.getTrimmedName() + entry.modelExt);
            entry.texture = new ResourceLocation(modid, tex + entry.getTrimmedName() + ".png");
            entry.animation = new ResourceLocation(modid, model + entry.getTrimmedName() + ".xml");
        });
        if (PokecubeCore.getConfig().debug_data) PokecubeAPI.logInfo("Finished adjusting model and texture locations");
    }

    @SubscribeEvent
    public void postRegisterPokemobs(final RegisterPokemobsEvent.Post event)
    {
        PokemobSounds.initMobSounds();
    }
}
