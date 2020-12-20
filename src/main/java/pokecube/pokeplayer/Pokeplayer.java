package pokecube.pokeplayer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.PokecubeCore;
import pokecube.pokeplayer.init.BlockInit;
import pokecube.pokeplayer.init.ContainerInit;
import pokecube.pokeplayer.init.TileEntityInit;
import pokecube.pokeplayer.proxy.ClientSetupHandler;
import thut.core.common.handlers.PlayerDataHandler;

@Mod(value = Reference.ID)
public class Pokeplayer
{
	public static final Logger LOGGER = LogManager.getLogger();
	
    public static final DeferredRegister<Block> BLOCKS     = DeferredRegister.create(ForgeRegistries.BLOCKS,
            Reference.ID);
    public static final DeferredRegister<Item>  ITEMS      = DeferredRegister.create(ForgeRegistries.ITEMS,
            Reference.ID);
    public static final DeferredRegister<TileEntityType<?>>  TILES      = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES,
            Reference.ID);
    public static final DeferredRegister<ContainerType<?>>  CONTAINER      = DeferredRegister.create(ForgeRegistries.CONTAINERS,
            Reference.ID);
    
    public static ClientSetupHandler                 proxyProxy;
    
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.ID)
    public static class RegistryHandler
    {
    	 @SubscribeEvent
	    public void registerTiles(final RegistryEvent.Register<TileEntityType<?>> event)
	    {}
    }
 
    public Pokeplayer()
    {
        MinecraftForge.EVENT_BUS.register(this);
        PokecubeCore.POKEMOB_BUS.register(this);

        MinecraftForge.EVENT_BUS.register(new EventsHandler());
        
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        PlayerDataHandler.register(PokeInfo.class);
        
        Pokeplayer.BLOCKS.register(modEventBus);
        Pokeplayer.ITEMS.register(modEventBus);
        Pokeplayer.TILES.register(modEventBus);
        Pokeplayer.CONTAINER.register(modEventBus);

        BlockInit.init();
        TileEntityInit.init();
        ContainerInit.init();
    }
}
