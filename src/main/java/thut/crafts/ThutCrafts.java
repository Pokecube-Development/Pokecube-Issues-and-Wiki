package thut.crafts;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import thut.api.entity.blockentity.BlockEntityBase;
import thut.api.entity.blockentity.block.TempBlock;
import thut.api.entity.blockentity.block.TempTile;
import thut.core.common.ThutCore;
import thut.core.common.config.Config;
import thut.core.common.config.Config.ConfigData;
import thut.core.common.config.Configure;
import thut.core.common.network.PacketHandler;
import thut.crafts.entity.EntityCraft;
import thut.crafts.network.PacketCraftControl;

@Mod(Reference.MODID)
public class ThutCrafts
{
    // This is our config storing object.
    public static class CraftsConfig extends ConfigData
    {
        @Configure(category = "rotates", type = Type.SERVER, comment = "Enables rotation for crafts. [Default: false]")
        public boolean canRotate = false;

        public CraftsConfig()
        {
            super(Reference.MODID);
        }

        @Override
        public void onUpdated()
        {
        }
    }

    // You can use EventBusSubscriber to automatically subscribe events on the
    // contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.MODID)
    public static class RegistryEvents
    {
        @SubscribeEvent
        public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event)
        {
            // register a new mob here
            EntityCraft.CRAFTTYPE.setRegistryName(Reference.MODID, "craft");
            event.getRegistry().register(EntityCraft.CRAFTTYPE);
//            EntityTest.TYPE.setRegistryName(Reference.MODID, "testmob");
//            event.getRegistry().register(EntityTest.TYPE);
        }

        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event)
        {
            // register items
            event.getRegistry().register(ThutCrafts.CRAFTMAKER);
        }

        @SubscribeEvent
        public static void registerBlocks(final RegistryEvent.Register<Block> event)
        {
            // register blocks
            event.getRegistry().register(ThutCrafts.CRAFTBLOCK);
        }

        @SubscribeEvent
        public static void registerTileEntity(final RegistryEvent.Register<BlockEntityType<?>> event)
        {
            // register tile entities
            event.getRegistry().register(ThutCrafts.CRAFTTE);
        }
    }

    public final static PacketHandler packets = new PacketHandler(new ResourceLocation(Reference.MODID, "comms"),
            Reference.NETVERSION);

    public static Item CRAFTMAKER;

    public static Block CRAFTBLOCK;

    public static BlockEntityType<TempTile> CRAFTTE;

    public static CraftsConfig conf = new CraftsConfig();

    public ThutCrafts()
    {
        ThutCrafts.CRAFTMAKER = new Item(new Item.Properties()).setRegistryName(Reference.MODID, "craftmaker");
        ThutCrafts.CRAFTBLOCK = TempBlock.make().setRegistryName(Reference.MODID, "craft");
        ThutCrafts.CRAFTTE = BlockEntityType.Builder.of(TempTile::new, ThutCrafts.CRAFTBLOCK).build(null);
        ThutCrafts.CRAFTTE.setRegistryName(Reference.MODID, "craft");
        TempTile.TYPE = ThutCrafts.CRAFTTE;
        BlockEntityBase.FAKEBLOCK = ThutCrafts.CRAFTBLOCK;

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        // Register Config stuff
        Config.setupConfigs(ThutCrafts.conf, ThutCore.MODID, Reference.MODID);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // Register the packets
        ThutCrafts.packets.registerMessage(PacketCraftControl.class, PacketCraftControl::new);

        // SEtup proxy
        MinecraftForge.EVENT_BUS.register(ThutCrafts.class);
    }
}
