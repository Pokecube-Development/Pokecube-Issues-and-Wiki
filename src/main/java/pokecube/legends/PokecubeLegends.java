package pokecube.legends;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import pokecube.core.PokecubeCore;
import pokecube.core.events.onload.RegisterPokecubes;
// import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.IPokecube.DefaultPokecubeBehavior;
import pokecube.core.interfaces.IPokemob;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.Config;
import pokecube.legends.init.ItemInit;
import pokecube.legends.init.PokecubeDim;
import pokecube.legends.init.function.UsableItemNatureEffects;
import pokecube.legends.init.function.UsableItemZMoveEffects;
import pokecube.legends.proxy.ClientProxy;
import pokecube.legends.proxy.CommonProxy;

@Mod(value = Reference.ID)
public class PokecubeLegends
{
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.ID)
    public static class RegistryHandler
    {

        @SubscribeEvent
        public static void onItemRegister(final RegistryEvent.Register<Item> event)
        {
            ItemInit.registerItems(event);
            event.getRegistry().registerAll(ItemInit.ITEMS.toArray(new Item[0]));
        }

        @SubscribeEvent
        public static void onBlockRegister(final RegistryEvent.Register<Block> event)
        {
            event.getRegistry().registerAll(BlockInit.BLOCKS.toArray(new Block[0]));
        }

        @SubscribeEvent
        public static void onItemCapabilityAttach(final AttachCapabilitiesEvent<ItemStack> event)
        {
            UsableItemNatureEffects.registerCapabilities(event);
            UsableItemZMoveEffects.registerCapabilities(event);
        }

        @SubscribeEvent
        public static void registerFeatures(final RegistryEvent.Register<Feature<?>> event)
        {

        }

        @SubscribeEvent
        public static void registerBiomes(final RegistryEvent.Register<Biome> event)
        {
            // BiomeInit.registerBiomes(event);
        }
    }

    public static CommonProxy proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(),
            () -> () -> new CommonProxy());

    public static final Config config = new Config();

    public PokecubeLegends()
    {
        thut.core.common.config.Config.setupConfigs(PokecubeLegends.config, PokecubeCore.MODID, Reference.ID);
        MinecraftForge.EVENT_BUS.register(this);
        // DimensionInit.initDimension();
        // Register setup for proxy
        FMLJavaModLoadingContext.get().getModEventBus().addListener(PokecubeLegends.proxy::setup);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(PokecubeLegends.proxy::setupClient);
        // Register the loaded method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(PokecubeLegends.proxy::loaded);
        // Just generally register it to event bus.
        FMLJavaModLoadingContext.get().getModEventBus().register(PokecubeLegends.proxy);
    }

    @SubscribeEvent
    public void registerPokecubes(final RegisterPokecubes event)
    {
        final PokecubeDim helper = new PokecubeDim();

        event.behaviors.add(new DefaultPokecubeBehavior()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.beast(mob);
            }
        }.setRegistryName("pokecube_legends", "beast"));

        // Pokecube Capture example dynamax resize
        /*
         * event.behaviors.add(new DefaultPokecubeBehavior() {
         * @Override public double getCaptureModifier(IPokemob mob) { return
         * helper.dynamax(mob); } }.setRegistryName("pokecube_legends",
         * "dynamax"));
         */
    }
}
