package thut.crafts;

import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import thut.api.entity.blockentity.BlockEntityBase;
import thut.api.entity.blockentity.block.TempBlock;
import thut.api.entity.blockentity.block.TempTile;
import thut.core.common.ThutCore;
import thut.core.common.config.Config;
import thut.core.common.config.Config.ConfigData;
import thut.core.common.config.Configure;
import thut.core.common.network.PacketHandler;
import thut.core.init.CommonInit;
import thut.core.init.ThutCreativeTabs;
import thut.crafts.entity.CraftStickApplier;
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

    public final static PacketHandler packets = new PacketHandler(Reference.NETVERSION);

    public static final Supplier<EntityType<EntityCraft>> CRAFTTYPE;
    public static final Supplier<Item> CRAFTMAKER;
    public static final Supplier<TempBlock> CRAFTBLOCK;
    public static final Supplier<BlockEntityType<TempTile>> CRAFTTE;

    public static CraftsConfig conf = new CraftsConfig();

    public static final DeferredRegister<Block> BLOCKS;
    public static final DeferredRegister<Item> ITEMS;
    public static final DeferredRegister<BlockEntityType<?>> TILES;
    public static final DeferredRegister<EntityType<?>> ENTITIES;

    static
    {
        BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, Reference.MODID);
        ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Reference.MODID);
        TILES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Reference.MODID);
        ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Reference.MODID);

        CRAFTTYPE = ENTITIES.register("craft", () -> new BlockEntityBase.BlockEntityType<>(EntityCraft::new));
        CRAFTMAKER = ITEMS.register("craftmaker", () -> new Item(new Item.Properties()));
        CRAFTBLOCK = BLOCKS.register("craft", TempBlock::make);
        CRAFTTE = TILES.register("craft",
                () -> BlockEntityType.Builder.of(TempTile::new, ThutCrafts.CRAFTBLOCK.get()).build(null));
    }

    public ThutCrafts(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the setup method for modloading
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::addCreative);

        ThutCrafts.ITEMS.register(modEventBus);
        ThutCrafts.BLOCKS.register(modEventBus);
        ThutCrafts.TILES.register(modEventBus);
        ThutCrafts.ENTITIES.register(modEventBus);

        // Register Config stuff
        Config.setupConfigs(modContainer, ThutCrafts.conf, ThutCore.MODID, Reference.MODID);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // Register the packets
        ThutCrafts.packets.registerToServerMessage(PacketCraftControl.class);

        // Add stick applier
        CommonInit.HANDLERS.add(new CraftStickApplier());
    }

    public void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(!ThutCore.conf.craftMakerInTabs) return;
        if (event.getTabKey() == CreativeModeTabs.OP_BLOCKS)
        {
            ThutCreativeTabs.addFront(event, ThutCrafts.CRAFTMAKER.get());
        }
        if (event.getTab().equals(ThutCreativeTabs.UTILITIES_TAB.get()))
        {
            event.accept(ThutCrafts.CRAFTMAKER.get());
        }
    }
}
