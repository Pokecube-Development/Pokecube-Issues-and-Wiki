package thut.tech.common;

import java.util.function.Supplier;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import thut.core.common.ThutCore;
import thut.core.common.config.Config;
import thut.core.common.network.PacketHandler;
import thut.core.init.ThutCreativeTabs;
import thut.tech.Reference;
import thut.tech.common.blocks.lift.ControllerBlock;
import thut.tech.common.blocks.lift.ControllerTile;
import thut.tech.common.entity.EntityLift;
import thut.tech.common.handlers.ConfigHandler;
import thut.tech.common.items.ItemLinker;
import thut.tech.common.util.RecipeSerializers;

@Mod(value = Reference.MOD_ID)
public class TechCore
{
    public final static PacketHandler packets = new PacketHandler(Reference.NETVERSION);

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Reference.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Reference.MOD_ID);
    
    public static final DeferredRegister<EntityType<?>> ENTITY;
    public static final DeferredRegister<BlockEntityType<?>> TILEENTITY;

    public static final DeferredBlock<Block> LIFTCONTROLLER;

    public static final DeferredItem<Item> LIFT;
    public static final DeferredItem<Item> LINKER;

    public static final Supplier<EntityType<EntityLift>> LIFTTYPE;

    public static final Supplier<BlockEntityType<ControllerTile>> CONTROLTYPE;

    public static final ConfigHandler config = new ConfigHandler(Reference.MOD_ID);

    static
    {
        TILEENTITY = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Reference.MOD_ID);
        ENTITY = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Reference.MOD_ID);

        LIFTTYPE = TechCore.ENTITY.register("lift", () -> new EntityLift.BlockEntityType<>(EntityLift::new));

        CONTROLTYPE = TechCore.TILEENTITY.register("controller",
                () -> BlockEntityType.Builder.of(ControllerTile::new, TechCore.LIFTCONTROLLER.get()).build(null));
        LIFTCONTROLLER = TechCore.BLOCKS.register("controller",
                () -> new ControllerBlock(Block.Properties.of().strength(3.5f).dynamicShape().noOcclusion()));

        LIFT = TechCore.ITEMS.register("lift", () -> new Item(new Item.Properties().stacksTo(1)));
        LINKER = TechCore.ITEMS.register("linker", () -> new ItemLinker(new Item.Properties().stacksTo(1)));

        for (final DeferredHolder<Block, ? extends Block> reg : TechCore.BLOCKS.getEntries())
            TechCore.ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), new Item.Properties()));
    }

    public TechCore(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register recipe serializers
        RecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        TechCore.ITEMS.register(modEventBus);
        TechCore.BLOCKS.register(modEventBus);
        TechCore.TILEENTITY.register(modEventBus);
        TechCore.ENTITY.register(modEventBus);
        modEventBus.addListener(this::addCreative);

        // Register Config stuff
        Config.setupConfigs(modContainer, TechCore.config, Reference.MOD_ID, Reference.MOD_ID);
    }

    void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTab().equals(ThutCreativeTabs.UTILITIES_TAB.get()))
        {
            event.accept(LINKER);
            event.accept(LIFT);
            event.accept(LIFTCONTROLLER);
        }

        if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES) && ThutCore.getConfig().itemsInCreativeTabs)
        {
            add(event, Items.WARPED_FUNGUS_ON_A_STICK, LINKER.get());
            add(event, LINKER.get(), LIFT.get());
        }

        if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS) && ThutCore.getConfig().itemsInCreativeTabs)
        {
            add(event, Items.LODESTONE, LIFT.get());
            add(event, LIFT.get(), LIFTCONTROLLER.get());
        }
    }

    public static void add(BuildCreativeModeTabContentsEvent event, ItemLike afterItem, ItemLike item) {
        event.insertAfter(new ItemStack(afterItem), new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }
}
