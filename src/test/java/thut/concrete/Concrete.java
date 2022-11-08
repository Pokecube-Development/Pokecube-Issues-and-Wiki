package thut.concrete;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.commons.compress.utils.Lists;

import com.google.common.collect.Sets;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import thut.api.block.flowing.FlowingBlock;
import thut.api.block.flowing.SolidBlock;
import thut.concrete.block.ConcreteBlock;
import thut.concrete.block.FormworkBlock;
import thut.concrete.block.LavaBlock;
import thut.concrete.block.RebarBlock;
import thut.concrete.block.ReinforcedConcreteBlock;
import thut.concrete.block.VolcanoBlock;
import thut.concrete.block.WetConcreteBlock;
import thut.concrete.block.entity.VolcanoEntity;
import thut.concrete.fluid.DummyLiquidBlock;
import thut.concrete.item.ConcreteDispenseBehaviour;
import thut.concrete.item.FormworkBlockItem;
import thut.concrete.item.PaintBrush;
import thut.concrete.item.RebarBlockItem;
import thut.concrete.item.SmootherItem;
import thut.concrete.recipe.PaintBrushRecipe;
import thut.core.common.ThutCore;

@Mod(value = Concrete.MODID)
public class Concrete
{
    public static final String MODID = "concrete";

    public static final ResourceLocation FLUID_STILL = new ResourceLocation("concrete:block/wet_concrete_white");
    public static final ResourceLocation FLUID_FLOWING = new ResourceLocation("concrete:block/wet_concrete_white");
    public static final ResourceLocation FLUID_OVERLAY = new ResourceLocation("concrete:block/wet_concrete_white");

    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS;

    public static final DeferredRegister<BlockEntityType<?>> TILES;

    public static final RegistryObject<SimpleRecipeSerializer<PaintBrushRecipe>> BRUSH_DYE_RECIPE;

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

    public static final RegistryObject<Item> WET_BLOCK_ITEM;

    public static final RegistryObject<FlowingBlock>[] DRY_LAYER = makeBlockArr(DyeColor.values().length);

    public static final RegistryObject<Block>[] REF_LAYER = makeBlockArr(DyeColor.values().length);
    public static final RegistryObject<Block>[] REF_BLOCK = makeBlockArr(DyeColor.values().length);

    public static final RegistryObject<RebarBlock> REBAR_BLOCK;
    public static final RegistryObject<FormworkBlock> FORMWORK_BLOCK;

    public static final RegistryObject<Item>[] BRUSHES = makeItemArr(DyeColor.values().length + 1);

    public static final RegistryObject<Item> BUCKET;

    public static final RegistryObject<Item> SMOOTHER;

    public static final RegistryObject<Item> DUST_ITEM;
    public static final RegistryObject<Item> CEMENT_ITEM;

    public static final RegistryObject<Item> CAO_ITEM;
    public static final RegistryObject<Item> CACO3_ITEM;

    public static final ResourceKey<Biome> VOLCANO_BIOME;

    private static ForgeFlowingFluid.Properties makeProperties()
    {
        return new ForgeFlowingFluid.Properties(CONCRETE_FLUID, CONCRETE_FLUID_FLOWING,
                FluidAttributes.builder(FLUID_STILL, FLUID_FLOWING).overlay(FLUID_OVERLAY).color(0xFFAAAAAA))
                        .bucket(BUCKET).block(CONCRETE_FLUID_BLOCK);
    }

    private static Supplier<Block> getWetBlock()
    {
        return () -> WET_BLOCK.get();
    }

    public static RegistryObject<FlowingFluid> CONCRETE_FLUID = FLUIDS.register("concrete",
            () -> new ForgeFlowingFluid.Source(makeProperties()));
    public static RegistryObject<FlowingFluid> CONCRETE_FLUID_FLOWING = FLUIDS.register("concrete_flowing",
            () -> new ForgeFlowingFluid.Flowing(makeProperties()));
    public static final RegistryObject<DummyLiquidBlock> CONCRETE_FLUID_BLOCK = BLOCKS.register("concrete_fluid_block",
            () -> new DummyLiquidBlock(CONCRETE_FLUID, getWetBlock(),
                    Properties.of(Material.WATER).noCollission().strength(100.0F).noDrops()));

    private static final Set<RegistryObject<?>> NOTAB = Sets.newHashSet();
    private static final Set<RegistryObject<?>> NOITEM = Sets.newHashSet();

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
        TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MODID);
        RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Concrete.MODID);

        BRUSH_DYE_RECIPE = Concrete.RECIPE_SERIALIZERS.register("paint_brush_dye",
                PaintBrushRecipe.brushDye(PaintBrushRecipe::new));

        VOLCANO_BIOME = ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(MODID, "volcano"));

        BlockBehaviour.Properties layer_props = BlockBehaviour.Properties.of(Material.STONE).noOcclusion().randomTicks()
                .requiresCorrectToolForDrops();
        BlockBehaviour.Properties block_props = BlockBehaviour.Properties.of(Material.STONE).randomTicks()
                .requiresCorrectToolForDrops();

        RegistryObject<FlowingBlock>[] regs = FlowingBlock.makeDust(BLOCKS, MODID, "dust_layer", "dust_block",
                layer_props, block_props);

        DUST_LAYER = regs[0];
        DUST_BLOCK = regs[1];

        layer_props = BlockBehaviour.Properties.copy(Blocks.BASALT).explosionResistance(20).noOcclusion();
        block_props = BlockBehaviour.Properties.copy(Blocks.BASALT).explosionResistance(20);

        regs = SolidBlock.makeSolid(BLOCKS, MODID, "solid_layer", "solid_block", layer_props, block_props);

        SOLID_LAYER = regs[0];
        SOLID_BLOCK = regs[1];

        layer_props = BlockBehaviour.Properties.of(Material.LAVA).strength(2.0F).noOcclusion().randomTicks()
                .requiresCorrectToolForDrops().lightLevel(s -> 15);
        block_props = BlockBehaviour.Properties.of(Material.LAVA).strength(2.0F).noOcclusion().randomTicks()
                .requiresCorrectToolForDrops().lightLevel(s -> 15);

        ResourceLocation solid_layer = new ResourceLocation(MODID, "solid_layer");
        ResourceLocation solid_block = new ResourceLocation(MODID, "solid_block");

        regs = LavaBlock.makeLava(BLOCKS, MODID, "molten_layer", "molten_block", layer_props, block_props,
                solid_layer, solid_block);

        MOLTEN_LAYER = regs[0];
        MOLTEN_BLOCK = regs[1];

        NOITEM.add(MOLTEN_LAYER);

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

        NOITEM.add(WET_BLOCK);
        NOITEM.add(WET_LAYER);
        NOITEM.add(CONCRETE_FLUID_BLOCK);

        REBAR_BLOCK = BLOCKS.register("rebar", () -> new RebarBlock(
                Properties.of(Material.METAL).dynamicShape().randomTicks().strength(1.0F, 1200.0F)));
        FORMWORK_BLOCK = BLOCKS.register("formwork",
                () -> new FormworkBlock(Properties.of(Material.WOOD).sound(SoundType.SCAFFOLDING)));
        // This gets a custom item instead
        NOITEM.add(REBAR_BLOCK);
        NOITEM.add(FORMWORK_BLOCK);

        // The three loops below are separate so that the items get grouped
        // together, otherwise they are interlaced
        for (DyeColor colour : DyeColor.values())
        {
            int i = colour.ordinal();

            layer_props = BlockBehaviour.Properties.of(Material.STONE, colour).requiresCorrectToolForDrops()
                    .strength(1.8F).noOcclusion();
            block_props = BlockBehaviour.Properties.of(Material.STONE, colour).requiresCorrectToolForDrops()
                    .strength(1.8F);

            regs = ConcreteBlock.makeDry(BLOCKS, MODID, "concrete_layer_" + colour.getName(),
                    "concrete_block_" + colour.getName(), layer_props, block_props, colour);

            DRY_LAYER[i] = regs[0];
        }

        for (DyeColor colour : DyeColor.values())
        {
            int i = colour.ordinal();

            layer_props = BlockBehaviour.Properties.of(Material.STONE, colour).requiresCorrectToolForDrops()
                    .strength(50.0F, 1200.0F).noOcclusion();
            block_props = BlockBehaviour.Properties.of(Material.STONE, colour).requiresCorrectToolForDrops()
                    .strength(50.0F, 1200.0F);

            RegistryObject<Block>[] refs = ReinforcedConcreteBlock.makeDry(BLOCKS, MODID,
                    "reinforced_concrete_layer_" + colour.getName(), "reinforced_concrete_block_" + colour.getName(),
                    layer_props, block_props, colour);

            REF_LAYER[i] = refs[0];
            REF_BLOCK[i] = refs[1];

            NOTAB.add(REF_LAYER[i]);
        }

        // Now register the items

        for (DyeColor colour : DyeColor.values())
        {
            int i = colour.ordinal();
            Item.Properties props = new Item.Properties().durability(64).tab(ThutCore.THUTITEMS);
            BRUSHES[i] = ITEMS.register("paint_brush_" + colour.getName(), () -> new PaintBrush(props, colour));
        }

        Item.Properties props = new Item.Properties().tab(ThutCore.THUTITEMS);
        Item.Properties no_paint = props;
        BRUSHES[DyeColor.values().length] = ITEMS.register("paint_brush", () -> new PaintBrush(no_paint, null));

        BUCKET = ITEMS.register("concrete_bucket", () -> new BucketItem(CONCRETE_FLUID,
                new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).tab(ThutCore.THUTITEMS)));

        Item.Properties smoother = new Item.Properties().tab(ThutCore.THUTITEMS).durability(256);
        SMOOTHER = ITEMS.register("smoother", () -> new SmootherItem(smoother));

        DUST_ITEM = ITEMS.register("dust", () -> new Item(new Item.Properties().tab(ThutCore.THUTITEMS)));
        CEMENT_ITEM = ITEMS.register("cement", () -> new Item(new Item.Properties().tab(ThutCore.THUTITEMS)));

        CAO_ITEM = ITEMS.register("dust_cao", () -> new Item(new Item.Properties().tab(ThutCore.THUTITEMS)));
        CACO3_ITEM = ITEMS.register("dust_caco3", () -> new Item(new Item.Properties().tab(ThutCore.THUTITEMS)));

        // Register the item blocks.

        // First the custom item blocks
        ITEMS.register(REBAR_BLOCK.getId().getPath(),
                () -> new RebarBlockItem(REBAR_BLOCK.get(), new Item.Properties().tab(ThutCore.THUTITEMS)));
        ITEMS.register(FORMWORK_BLOCK.getId().getPath(),
                () -> new FormworkBlockItem(FORMWORK_BLOCK.get(), new Item.Properties().tab(ThutCore.THUTITEMS)));

        // Wet block gets an item, for intermediate crafting, not placement.
        WET_BLOCK_ITEM = ITEMS.register(WET_BLOCK.getId().getPath(),
                () -> new Item(new Item.Properties().tab(ThutCore.THUTITEMS)));

        // Then the rest
        for (final RegistryObject<Block> reg : BLOCKS.getEntries())
        {
            if (NOITEM.contains(reg)) continue;
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
        RECIPE_SERIALIZERS.register(modEventBus);
    }

    public void loadComplete(FMLLoadCompleteEvent event)
    {
        event.enqueueWork(() -> {
            DispenserBlock.registerBehavior(BUCKET.get(), ConcreteDispenseBehaviour.INSTANCE);
            DispenserBlock.registerBehavior(WET_BLOCK_ITEM.get(), ConcreteDispenseBehaviour.INSTANCE);
            initBiomeDict();
       });
    }

	@SuppressWarnings("deprecation")
    private void initBiomeDict() 
    {
		List<net.minecraftforge.common.BiomeDictionary.Type> types = Lists.newArrayList();
         types.add(net.minecraftforge.common.BiomeDictionary.Type.OVERWORLD);
         types.add(net.minecraftforge.common.BiomeDictionary.Type.HOT);
         types.add(net.minecraftforge.common.BiomeDictionary.Type.getType("volcano"));

         net.minecraftforge.common.BiomeDictionary.addTypes(VOLCANO_BIOME, types.toArray(new net.minecraftforge.common.BiomeDictionary.Type[0]));
     
    }
}
