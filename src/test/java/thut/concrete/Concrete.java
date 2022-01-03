package thut.concrete;

import java.lang.reflect.Array;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import thut.api.block.flowing.FlowingBlock;
import thut.api.block.flowing.MoltenBlock;
import thut.api.block.flowing.SolidBlock;
import thut.concrete.block.ConcreteBlock;
import thut.concrete.block.RebarBlock;
import thut.concrete.block.ReinforcedConcreteBlock;
import thut.concrete.block.VolcanoBlock;
import thut.concrete.block.WetConcreteBlock;
import thut.concrete.block.entity.VolcanoEntity;
import thut.concrete.item.ConcreteBucket;
import thut.concrete.item.ConcreteDispenseBehaviour;
import thut.concrete.item.PaintBrush;
import thut.concrete.item.Smoother;
import thut.core.common.ThutCore;

@Mod(value = Concrete.MODID)
public class Concrete
{
    public static final String MODID = "concrete";

    public static final ResourceLocation FLUID_STILL = new ResourceLocation("minecraft:block/brown_mushroom_block");
    public static final ResourceLocation FLUID_FLOWING = new ResourceLocation("minecraft:block/mushroom_stem");
    public static final ResourceLocation FLUID_OVERLAY = new ResourceLocation("minecraft:block/obsidian");

    public static final DeferredRegister<Fluid> FLUIDS;
    public static final DeferredRegister<Block> BLOCKS;
    public static final DeferredRegister<Item> ITEMS;

    public static final DeferredRegister<BlockEntityType<?>> TILES;

    public static final RegistryObject<BlockEntityType<VolcanoEntity>> VOLCANO_TYPE;
    public static final RegistryObject<VolcanoBlock> VOLCANO;

    public static final RegistryObject<FlowingBlock> DUST_LAYER;
    public static final RegistryObject<FlowingBlock> DUST_BLOCK;

    public static final RegistryObject<FlowingBlock> MOLTEN_LAYER;
    public static final RegistryObject<FlowingBlock> MOLTEN_BLOCK;

    public static final RegistryObject<FlowingBlock> SOLID_LAYER;
    public static final RegistryObject<FlowingBlock> SOLID_BLOCK;

    public static final RegistryObject<FlowingBlock> WET_LAYER;
    public static final RegistryObject<FlowingBlock> WET_BLOCK;

    public static final RegistryObject<FlowingBlock>[] DRY_LAYER = makeBlockArr(DyeColor.values().length);
    public static final RegistryObject<FlowingBlock>[] DRY_BLOCK = makeBlockArr(DyeColor.values().length);

    public static final RegistryObject<Block>[] REF_LAYER = makeBlockArr(DyeColor.values().length);
    public static final RegistryObject<Block>[] REF_BLOCK = makeBlockArr(DyeColor.values().length);

    public static final RegistryObject<RebarBlock> REBAR_BLOCK;

    public static final RegistryObject<PaintBrush>[] BRUSHES = makeItemArr(DyeColor.values().length + 1);

    public static final RegistryObject<ConcreteBucket> BUCKET;

    public static final RegistryObject<Smoother> SMOOTHER;

    public static final RegistryObject<Item> DUST_ITEM;
    public static final RegistryObject<Item> CEMENT_ITEM;

    public static final RegistryObject<Item> CAO_ITEM;
    public static final RegistryObject<Item> CACO3_ITEM;

    private static final Set<RegistryObject<?>> NOTAB = Sets.newHashSet();

    private static <T extends Block> RegistryObject<T>[] makeBlockArr(int i)
    {
        @SuppressWarnings("unchecked")
        RegistryObject<T>[] arr = (RegistryObject<T>[]) Array.newInstance(RegistryObject.class, i);
        return arr;
    }

    private static <T extends Item> RegistryObject<T>[] makeItemArr(int i)
    {
        @SuppressWarnings("unchecked")
        RegistryObject<T>[] arr = (RegistryObject<T>[]) Array.newInstance(RegistryObject.class, i);
        return arr;
    }

    static
    {
        FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, MODID);

        BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
        ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
        TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MODID);

        BlockBehaviour.Properties layer_props = BlockBehaviour.Properties.of(Material.STONE).noOcclusion().randomTicks()
                .requiresCorrectToolForDrops();
        BlockBehaviour.Properties block_props = BlockBehaviour.Properties.of(Material.STONE).randomTicks()
                .requiresCorrectToolForDrops();

        RegistryObject<FlowingBlock>[] regs = FlowingBlock.makeDust(BLOCKS, MODID, "dust_layer", "dust_block",
                layer_props, block_props);

        DUST_LAYER = regs[0];
        DUST_BLOCK = regs[1];

        layer_props = BlockBehaviour.Properties.of(Material.STONE).strength(30.0F).noOcclusion()
                .requiresCorrectToolForDrops();
        block_props = BlockBehaviour.Properties.of(Material.STONE).strength(30.0F).requiresCorrectToolForDrops();

        regs = SolidBlock.makeSolid(BLOCKS, MODID, "solid_layer", "solid_block", layer_props, block_props);

        SOLID_LAYER = regs[0];
        SOLID_BLOCK = regs[1];

        layer_props = BlockBehaviour.Properties.of(Material.LAVA).strength(100.0F).noOcclusion().randomTicks()
                .requiresCorrectToolForDrops().lightLevel(s -> s.getValue(FlowingBlock.LAYERS) - 1);
        block_props = BlockBehaviour.Properties.of(Material.LAVA).strength(100.0F).randomTicks()
                .requiresCorrectToolForDrops().lightLevel(s -> 15);

        ResourceLocation solid_layer = new ResourceLocation(MODID, "solid_layer");
        ResourceLocation solid_block = new ResourceLocation(MODID, "solid_block");

        regs = MoltenBlock.makeMolten(BLOCKS, MODID, "molten_layer", "molten_block", layer_props, block_props,
                solid_layer, solid_block);

        MOLTEN_LAYER = regs[0];
        MOLTEN_BLOCK = regs[1];

        BlockBehaviour.Properties volc_props = BlockBehaviour.Properties.of(Material.BARRIER).noDrops();
        VOLCANO = BLOCKS.register("volcano", () -> new VolcanoBlock(volc_props));

        VOLCANO_TYPE = TILES.register("volcano",
                () -> BlockEntityType.Builder.of(VolcanoEntity::new, VOLCANO.get()).build(null));

        layer_props = BlockBehaviour.Properties.of(Material.STONE).strength(100.0F).noOcclusion().randomTicks()
                .requiresCorrectToolForDrops();
        block_props = BlockBehaviour.Properties.of(Material.STONE).strength(100.0F).randomTicks()
                .requiresCorrectToolForDrops();

        solid_layer = new ResourceLocation(MODID, "concrete_layer_" + DyeColor.LIGHT_GRAY.getName());
        solid_block = new ResourceLocation(MODID, "concrete_block_" + DyeColor.LIGHT_GRAY.getName());

        regs = WetConcreteBlock.makeWet(BLOCKS, MODID, "wet_concrete_layer", "wet_concrete_block", layer_props,
                block_props, solid_layer, solid_block);

        WET_LAYER = regs[0];
        WET_BLOCK = regs[1];

        REBAR_BLOCK = BLOCKS.register("rebar", () -> new RebarBlock(
                Properties.of(Material.METAL).noCollission().randomTicks().strength(100.0F).noDrops()));

        // The three loops below are separate so that the items get grouped
        // together, otherwise they are interlaced
        for (DyeColor colour : DyeColor.values())
        {
            int i = colour.ordinal();

            layer_props = BlockBehaviour.Properties.of(Material.STONE).strength(30.0F).noOcclusion()
                    .requiresCorrectToolForDrops();
            block_props = BlockBehaviour.Properties.of(Material.STONE).strength(30.0F).requiresCorrectToolForDrops();

            regs = ConcreteBlock.makeDry(BLOCKS, MODID, "concrete_layer_" + colour.getName(),
                    "concrete_block_" + colour.getName(), layer_props, block_props, colour);

            DRY_LAYER[i] = regs[0];
            DRY_BLOCK[i] = regs[1];

        }

        for (DyeColor colour : DyeColor.values())
        {
            int i = colour.ordinal();

            layer_props = BlockBehaviour.Properties.of(Material.STONE).strength(100.0F).noOcclusion()
                    .requiresCorrectToolForDrops();
            block_props = BlockBehaviour.Properties.of(Material.STONE).strength(100.0F).requiresCorrectToolForDrops();

            RegistryObject<Block>[] refs = ReinforcedConcreteBlock.makeDry(BLOCKS, MODID,
                    "reinforced_concrete_layer_" + colour.getName(), "reinforced_concrete_block_" + colour.getName(),
                    layer_props, block_props, colour);

            REF_LAYER[i] = refs[0];
            REF_BLOCK[i] = refs[1];

            NOTAB.add(REF_LAYER[i]);
        }

        for (DyeColor colour : DyeColor.values())
        {
            int i = colour.ordinal();
            Item.Properties props = new Item.Properties().tab(ThutCore.THUTITEMS);
            BRUSHES[i] = ITEMS.register("paint_brush_" + colour.getName(), () -> new PaintBrush(props, colour));
        }

        Item.Properties props = new Item.Properties().tab(ThutCore.THUTITEMS).durability(256);
        Item.Properties no_paint = props;
        BRUSHES[DyeColor.values().length] = ITEMS.register("paint_brush", () -> new PaintBrush(no_paint, null));

        Item.Properties bucket = new Item.Properties().tab(ThutCore.THUTITEMS);
        BUCKET = ITEMS.register("concrete_bucket", () -> new ConcreteBucket(bucket));

        Item.Properties smoother = new Item.Properties().tab(ThutCore.THUTITEMS).durability(256);
        SMOOTHER = ITEMS.register("smoother", () -> new Smoother(smoother));

        DUST_ITEM = ITEMS.register("dust", () -> new Item(new Item.Properties().tab(ThutCore.THUTITEMS)));
        CEMENT_ITEM = ITEMS.register("cement", () -> new Item(new Item.Properties().tab(ThutCore.THUTITEMS)));

        CAO_ITEM = ITEMS.register("dust_cao", () -> new Item(new Item.Properties().tab(ThutCore.THUTITEMS)));
        CACO3_ITEM = ITEMS.register("dust_caco3", () -> new Item(new Item.Properties().tab(ThutCore.THUTITEMS)));

        // Register the item blocks.
        for (final RegistryObject<Block> reg : BLOCKS.getEntries())
        {
            props = new Item.Properties();
            if (!NOTAB.contains(reg)) props = props.tab(ThutCore.THUTITEMS);
            Item.Properties use = props;
            ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), use));
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MODID, value = Dist.CLIENT)
    public static class ClientEvents
    {
        @SubscribeEvent
        public static void setupClient(final FMLClientSetupEvent event)
        {
            ItemBlockRenderTypes.setRenderLayer(DUST_LAYER.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(MOLTEN_LAYER.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(WET_LAYER.get(), RenderType.cutoutMipped());
        }
    }

    public Concrete()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::loadComplete);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        FLUIDS.register(modEventBus);
        TILES.register(modEventBus);
    }

    public void loadComplete(FMLLoadCompleteEvent event)
    {
        event.enqueueWork(() -> {
            DispenserBlock.registerBehavior(BUCKET.get(), new ConcreteDispenseBehaviour());

        });
    }
}
