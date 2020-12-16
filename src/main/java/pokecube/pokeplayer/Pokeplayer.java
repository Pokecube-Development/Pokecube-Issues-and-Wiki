package pokecube.pokeplayer;

import net.minecraft.block.Block;
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
import pokecube.pokeplayer.init.TileEntityInit;
import pokecube.pokeplayer.proxy.ClientSetupHandler;
import thut.core.common.handlers.PlayerDataHandler;

@Mod(value = Reference.ID)
public class PokePlayer
{
    public static final DeferredRegister<Block> BLOCKS     = DeferredRegister.create(ForgeRegistries.BLOCKS,
            Reference.ID);
    public static final DeferredRegister<Item>  ITEMS      = DeferredRegister.create(ForgeRegistries.ITEMS,
            Reference.ID);
    public static final DeferredRegister<TileEntityType<?>>  TILES      = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES,
            Reference.ID);
    
    public static ClientSetupHandler                 proxyProxy;
    
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.ID)
    public static class RegistryHandler
    {
    	 @SubscribeEvent
	    public void registerTiles(final RegistryEvent.Register<TileEntityType<?>> event)
	    {}
    }
 
    public PokePlayer()
    {
        MinecraftForge.EVENT_BUS.register(this);
        PokecubeCore.POKEMOB_BUS.register(this);

        MinecraftForge.EVENT_BUS.register(new EventsHandler());
        
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        PlayerDataHandler.register(PokeInfo.class);
        
        PokePlayer.BLOCKS.register(modEventBus);
        PokePlayer.ITEMS.register(modEventBus);
        PokePlayer.TILES.register(modEventBus);

        BlockInit.init();
        TileEntityInit.init();
    }
}
