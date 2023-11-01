package thut.crafts;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
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
        {}
    }

    public final static PacketHandler packets = new PacketHandler(new ResourceLocation(Reference.MODID, "comms"),
            Reference.NETVERSION);

    public static final RegistryObject<EntityType<EntityCraft>> CRAFTTYPE;
    public static final RegistryObject<Item> CRAFTMAKER;
    public static final RegistryObject<TempBlock> CRAFTBLOCK;
    public static final RegistryObject<BlockEntityType<TempTile>> CRAFTTE;

    public static CraftsConfig conf = new CraftsConfig();

    public static final DeferredRegister<Block> BLOCKS;
    public static final DeferredRegister<Item> ITEMS;
    public static final DeferredRegister<BlockEntityType<?>> TILES;
    public static final DeferredRegister<EntityType<?>> ENTITIES;

    static
    {
        BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Reference.MODID);
        ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Reference.MODID);
        TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Reference.MODID);
        ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, Reference.MODID);

        CRAFTTYPE = ENTITIES.register("craft", () -> new BlockEntityBase.BlockEntityType<>(EntityCraft::new));
        CRAFTMAKER = ITEMS.register("craftmaker", () -> new Item(new Item.Properties()));
        CRAFTBLOCK = BLOCKS.register("craft", () -> TempBlock.make());
        CRAFTTE = TILES.register("craft",
                () -> BlockEntityType.Builder.of(TempTile::new, ThutCrafts.CRAFTBLOCK.get()).build(null));
    }

    public ThutCrafts()
    {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register the setup method for modloading
        bus.addListener(this::setup);

        ThutCrafts.ITEMS.register(bus);
        ThutCrafts.BLOCKS.register(bus);
        ThutCrafts.TILES.register(bus);
        ThutCrafts.ENTITIES.register(bus);

        // Register Config stuff
        Config.setupConfigs(ThutCrafts.conf, ThutCore.MODID, Reference.MODID);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // Register the packets
        ThutCrafts.packets.registerMessage(PacketCraftControl.class, PacketCraftControl::new);

        // SEtup proxy
        ThutCore.FORGE_BUS.register(ThutCrafts.class);
    }
}
