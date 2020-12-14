package pokecube.pokeplayer;

import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.PokecubeCore;
import pokecube.pokeplayer.events.Render;
import pokecube.pokeplayer.init.BlockInit;
import pokecube.pokeplayer.init.Config;
import pokecube.pokeplayer.init.TileEntityInit;

@Mod(value = Reference.ID)
public class Pokeplayer
{
	public static final DeferredRegister<Block> BLOCKS     = DeferredRegister.create(ForgeRegistries.BLOCKS,
            Reference.ID);
    public static final DeferredRegister<Item>  ITEMS      = DeferredRegister.create(ForgeRegistries.ITEMS,
            Reference.ID);
    public static final DeferredRegister<TileEntityType<?>>  TILES      = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES,
            Reference.ID);
    
    public static float sizePercentage = 1.0f;
    
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.ID)
    public static class RegistryHandler
    {
    	@SubscribeEvent
        public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event)
        {}
    	
    	@SubscribeEvent
        public static void registerTiles(final RegistryEvent.Register<TileEntityType<?>> event)
        {}
    }
    
    public static final Config config = new Config();

    public Pokeplayer()
    {
        thut.core.common.config.Config.setupConfigs(Pokeplayer.config, PokecubeCore.MODID, Reference.ID);
        MinecraftForge.EVENT_BUS.register(this);
        PokecubeCore.POKEMOB_BUS.register(this);

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        Pokeplayer.BLOCKS.register(modEventBus);
        Pokeplayer.ITEMS.register(modEventBus);
        Pokeplayer.TILES.register(modEventBus);
        
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> Render::new);
        
        BlockInit.init();
        TileEntityInit.init();
    }
    
    @SubscribeEvent
    public void serverStarting(final FMLServerStartingEvent event)
    {
    	Pokeplayer.config.loaded = true;
    	Pokeplayer.config.onUpdated();
    }
}
