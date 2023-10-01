package thut.concrete;

import java.lang.reflect.Array;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.ItemLike;
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
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistries.Keys;
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
import thut.core.common.config.Config;
import thut.core.init.ThutCreativeTabs;
import thut.lib.RegHelper;

@Mod(value = Concrete.MODID)
public class Concrete
{
    public static final String MODID = "concrete";

    public static final ResourceLocation FLUID_STILL = new ResourceLocation("concrete:block/wet_concrete_white");
    public static final ResourceLocation FLUID_FLOWING = new ResourceLocation("concrete:block/wet_concrete_white");
    public static final ResourceLocation FLUID_OVERLAY = new ResourceLocation("concrete:block/wet_concrete_white");

    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(Keys.FLUID_TYPES, MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS;

    public static final DeferredRegister<BlockEntityType<?>> TILES;

    public static final RegistryObject<SimpleCraftingRecipeSerializer<PaintBrushRecipe>> BRUSH_DYE_RECIPE;

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
        return new ForgeFlowingFluid.Properties(CONCRETE_FLUID_TYPE, CONCRETE_FLUID, CONCRETE_FLUID_FLOWING)
                .bucket(BUCKET).block(CONCRETE_FLUID_BLOCK);
    }

    private static Supplier<Block> getWetBlock()
    {
        return () -> WET_BLOCK.get();
    }

    public static RegistryObject<FluidType> CONCRETE_FLUID_TYPE = FLUID_TYPES.register("liquid_concrete",
            () -> new FluidType(FluidType.Properties.create())
            {
                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer)
                {
                    consumer.accept(new IClientFluidTypeExtensions()
                    {
                        @Override
                        public ResourceLocation getStillTexture()
                        {
                            return FLUID_STILL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture()
                        {
                            return FLUID_FLOWING;
                        }

                        @Nullable
                        @Override
                        public ResourceLocation getOverlayTexture()
                        {
                            return FLUID_OVERLAY;
                        }

                        @Override
                        public int getTintColor()
                        {
                            return 0xFFAAAAAA;
                        }
                    });
                }
            });

    public static RegistryObject<FlowingFluid> CONCRETE_FLUID = FLUIDS.register("concrete",
            () -> new ForgeFlowingFluid.Source(makeProperties()));
    public static RegistryObject<FlowingFluid> CONCRETE_FLUID_FLOWING = FLUIDS.register("concrete_flowing",
            () -> new ForgeFlowingFluid.Flowing(makeProperties()));
    public static final RegistryObject<DummyLiquidBlock> CONCRETE_FLUID_BLOCK = BLOCKS.register("concrete_fluid_block",
            () -> new DummyLiquidBlock(CONCRETE_FLUID, getWetBlock(), Properties.of().noCollission().strength(100.0F)));

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
        TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
        RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Concrete.MODID);

        BRUSH_DYE_RECIPE = Concrete.RECIPE_SERIALIZERS.register("paint_brush_dye",
                PaintBrushRecipe.brushDye(PaintBrushRecipe::new));

        VOLCANO_BIOME = ResourceKey.create(RegHelper.BIOME_REGISTRY, new ResourceLocation(MODID, "volcano"));

        BlockBehaviour.Properties layer_props = BlockBehaviour.Properties.of().noOcclusion().randomTicks()
                .requiresCorrectToolForDrops();
        BlockBehaviour.Properties block_props = BlockBehaviour.Properties.of().randomTicks()
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

        layer_props = BlockBehaviour.Properties.of().strength(2.0F).noOcclusion().randomTicks()
                .requiresCorrectToolForDrops().lightLevel(s -> 15);
        block_props = BlockBehaviour.Properties.of().strength(2.0F).noOcclusion().randomTicks()
                .requiresCorrectToolForDrops().lightLevel(s -> 15);

        ResourceLocation solid_layer = new ResourceLocation(MODID, "solid_layer");
        ResourceLocation solid_block = new ResourceLocation(MODID, "solid_block");

        regs = LavaBlock.makeLava(BLOCKS, MODID, "molten_layer", "molten_block", layer_props, block_props, solid_layer,
                solid_block);

        MOLTEN_LAYER = regs[0];
        MOLTEN_BLOCK = regs[1];

        NOITEM.add(MOLTEN_LAYER);

        BlockBehaviour.Properties volc_props = BlockBehaviour.Properties.of();
        VOLCANO = BLOCKS.register("volcano", () -> new VolcanoBlock(volc_props));

        VOLCANO_TYPE = TILES.register("volcano",
                () -> BlockEntityType.Builder.of(VolcanoEntity::new, VOLCANO.get()).build(null));

        layer_props = BlockBehaviour.Properties.of().strength(100.0F).noOcclusion().randomTicks()
                .requiresCorrectToolForDrops();
        block_props = BlockBehaviour.Properties.of().strength(100.0F).randomTicks().requiresCorrectToolForDrops();

        solid_layer = new ResourceLocation(MODID, "concrete_layer_" + DyeColor.LIGHT_GRAY.getName());
        solid_block = new ResourceLocation(MODID, "concrete_block_" + DyeColor.LIGHT_GRAY.getName());

        regs = WetConcreteBlock.makeWet(BLOCKS, MODID, "wet_concrete_layer", "wet_concrete_block", layer_props,
                block_props, solid_layer, solid_block);

        WET_LAYER = regs[0];
        WET_BLOCK = regs[1];

        NOITEM.add(WET_BLOCK);
        NOITEM.add(WET_LAYER);
        NOITEM.add(CONCRETE_FLUID_BLOCK);

        REBAR_BLOCK = BLOCKS.register("rebar",
                () -> new RebarBlock(Properties.of().dynamicShape().randomTicks().strength(1.0F, 1200.0F)));
        FORMWORK_BLOCK = BLOCKS.register("formwork",
                () -> new FormworkBlock(Properties.of().sound(SoundType.SCAFFOLDING)));
        // This gets a custom item instead
        NOITEM.add(REBAR_BLOCK);
        NOITEM.add(FORMWORK_BLOCK);

        // The three loops below are separate so that the items get grouped
        // together, otherwise they are interlaced
        for (DyeColor colour : DyeColor.values())
        {
            int i = colour.ordinal();

            layer_props = BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.8F).noOcclusion();
            block_props = BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(1.8F);

            regs = ConcreteBlock.makeDry(BLOCKS, MODID, "concrete_layer_" + colour.getName(),
                    "concrete_block_" + colour.getName(), layer_props, block_props, colour);

            DRY_LAYER[i] = regs[0];
        }

        for (DyeColor colour : DyeColor.values())
        {
            int i = colour.ordinal();

            layer_props = BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(50.0F, 1200.0F)
                    .noOcclusion();
            block_props = BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(50.0F, 1200.0F);

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
            Item.Properties props = new Item.Properties().durability(64);
            BRUSHES[i] = ITEMS.register("paint_brush_" + colour.getName(), () -> new PaintBrush(props, colour));
        }

        Item.Properties props = new Item.Properties();
        Item.Properties no_paint = props;
        BRUSHES[DyeColor.values().length] = ITEMS.register("paint_brush", () -> new PaintBrush(no_paint, null));

        BUCKET = ITEMS.register("concrete_bucket",
                () -> new BucketItem(CONCRETE_FLUID, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

        Item.Properties smoother = new Item.Properties().durability(256);
        SMOOTHER = ITEMS.register("smoother", () -> new SmootherItem(smoother));

        DUST_ITEM = ITEMS.register("dust", () -> new Item(new Item.Properties()));
        CEMENT_ITEM = ITEMS.register("cement", () -> new Item(new Item.Properties()));

        CAO_ITEM = ITEMS.register("dust_cao", () -> new Item(new Item.Properties()));
        CACO3_ITEM = ITEMS.register("dust_caco3", () -> new Item(new Item.Properties()));

        // Register the item blocks.

        // First the custom item blocks
        ITEMS.register(REBAR_BLOCK.getId().getPath(),
                () -> new RebarBlockItem(REBAR_BLOCK.get(), new Item.Properties()));
        ITEMS.register(FORMWORK_BLOCK.getId().getPath(),
                () -> new FormworkBlockItem(FORMWORK_BLOCK.get(), new Item.Properties()));

        // Wet block gets an item, for intermediate crafting, not placement.
        WET_BLOCK_ITEM = ITEMS.register(WET_BLOCK.getId().getPath(), () -> new Item(new Item.Properties()));

        // Then the rest
        for (final RegistryObject<Block> reg : BLOCKS.getEntries())
        {
            if (NOITEM.contains(reg)) continue;
            props = new Item.Properties();
            Item.Properties use = props;
            ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), use));
        }
    }
    
    public static final ConcreteConfig config = new ConcreteConfig(Concrete.MODID);

    public Concrete()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::loadComplete);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        FLUIDS.register(modEventBus);
        FLUID_TYPES.register(modEventBus);
        TILES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        
        // Register Config stuff
        Config.setupConfigs(Concrete.config, Concrete.MODID, Concrete.MODID);
    }

    public ItemStack getItem(String modID, String name)
    {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(modID, name)).getDefaultInstance();
    }

    void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey().equals(ThutCreativeTabs.UTILITIES_TAB.getKey()))
        {
            event.accept(BUCKET);
            event.accept(SMOOTHER);

            event.accept(getItem(Concrete.MODID, "paint_brush"));
            event.accept(getItem(Concrete.MODID, "paint_brush_white"));
            event.accept(getItem(Concrete.MODID, "paint_brush_light_gray"));
            event.accept(getItem(Concrete.MODID, "paint_brush_gray"));
            event.accept(getItem(Concrete.MODID, "paint_brush_black"));
            event.accept(getItem(Concrete.MODID, "paint_brush_brown"));
            event.accept(getItem(Concrete.MODID, "paint_brush_red"));
            event.accept(getItem(Concrete.MODID, "paint_brush_orange"));
            event.accept(getItem(Concrete.MODID, "paint_brush_yellow"));
            event.accept(getItem(Concrete.MODID, "paint_brush_lime"));
            event.accept(getItem(Concrete.MODID, "paint_brush_green"));
            event.accept(getItem(Concrete.MODID, "paint_brush_cyan"));
            event.accept(getItem(Concrete.MODID, "paint_brush_light_blue"));
            event.accept(getItem(Concrete.MODID, "paint_brush_blue"));
            event.accept(getItem(Concrete.MODID, "paint_brush_purple"));
            event.accept(getItem(Concrete.MODID, "paint_brush_magenta"));
            event.accept(getItem(Concrete.MODID, "paint_brush_pink"));

            for (DyeColor colour : DyeColor.values())
            {
                int i = colour.ordinal();
                event.accept(BRUSHES[i]);
            }

            event.accept(CACO3_ITEM);
            event.accept(CAO_ITEM);
            event.accept(DUST_ITEM);
            event.accept(CEMENT_ITEM);
            
            event.accept(VOLCANO);
            event.accept(getItem(Concrete.MODID, "molten_block"));
            event.accept(getItem(Concrete.MODID, "solid_block"));
            event.accept(getItem(Concrete.MODID, "dust_block"));

            event.accept(REBAR_BLOCK);
            event.accept(FORMWORK_BLOCK);
            event.accept(WET_BLOCK_ITEM);

            // Items listed in order of rainbow like vanilla. This is why the for statement for DyeColor isn't used
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_block_white"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_block_light_gray"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_block_gray"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_block_black"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_block_brown"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_block_red"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_block_orange"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_block_yellow"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_block_lime"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_block_green"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_block_cyan"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_block_light_blue"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_block_blue"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_block_purple"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_block_magenta"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_block_pink"));

            event.accept(getItem(Concrete.MODID, "reinforced_concrete_layer_white"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_layer_light_gray"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_layer_gray"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_layer_black"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_layer_brown"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_layer_red"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_layer_orange"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_layer_yellow"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_layer_lime"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_layer_green"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_layer_cyan"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_layer_light_blue"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_layer_blue"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_layer_purple"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_layer_magenta"));
            event.accept(getItem(Concrete.MODID, "reinforced_concrete_layer_pink"));

            event.accept(getItem(Concrete.MODID, "concrete_layer_white"));
            event.accept(getItem(Concrete.MODID, "concrete_layer_light_gray"));
            event.accept(getItem(Concrete.MODID, "concrete_layer_gray"));
            event.accept(getItem(Concrete.MODID, "concrete_layer_black"));
            event.accept(getItem(Concrete.MODID, "concrete_layer_brown"));
            event.accept(getItem(Concrete.MODID, "concrete_layer_red"));
            event.accept(getItem(Concrete.MODID, "concrete_layer_orange"));
            event.accept(getItem(Concrete.MODID, "concrete_layer_yellow"));
            event.accept(getItem(Concrete.MODID, "concrete_layer_lime"));
            event.accept(getItem(Concrete.MODID, "concrete_layer_green"));
            event.accept(getItem(Concrete.MODID, "concrete_layer_cyan"));
            event.accept(getItem(Concrete.MODID, "concrete_layer_light_blue"));
            event.accept(getItem(Concrete.MODID, "concrete_layer_blue"));
            event.accept(getItem(Concrete.MODID, "concrete_layer_purple"));
            event.accept(getItem(Concrete.MODID, "concrete_layer_magenta"));
            event.accept(getItem(Concrete.MODID, "concrete_layer_pink"));
        }

        if (event.getTabKey().equals(CreativeModeTabs.BUILDING_BLOCKS) && ThutCore.getConfig().itemsInCreativeTabs)
        {
            addAfter(event, Items.CHAIN, BUCKET.get());
            addAfter(event, BUCKET.get(), REBAR_BLOCK.get());
            addAfter(event, REBAR_BLOCK.get(), FORMWORK_BLOCK.get());
            addAfter(event, FORMWORK_BLOCK.get(), WET_BLOCK_ITEM.get());
            addAfter(event, Items.SMOOTH_BASALT, getItem(Concrete.MODID, "molten_block").getItem());
            addAfter(event, getItem(Concrete.MODID, "molten_block").getItem(), getItem(Concrete.MODID, "solid_block").getItem());
            addAfter(event, getItem(Concrete.MODID, "solid_block").getItem(), getItem(Concrete.MODID, "dust_block").getItem());
        }

        if (event.getTabKey().equals(CreativeModeTabs.NATURAL_BLOCKS) && ThutCore.getConfig().itemsInCreativeTabs)
        {
            addAfter(event, Items.SMOOTH_BASALT, getItem(Concrete.MODID, "molten_block").getItem());
            addAfter(event, getItem(Concrete.MODID, "molten_block").getItem(), getItem(Concrete.MODID, "solid_block").getItem());
            addAfter(event, getItem(Concrete.MODID, "solid_block").getItem(), getItem(Concrete.MODID, "dust_block").getItem());
        }

        if (event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES) && ThutCore.getConfig().itemsInCreativeTabs)
        {
            addAfter(event, Items.LAVA_BUCKET, BUCKET.get());

            addAfter(event, Items.BRUSH, getItem(Concrete.MODID, "paint_brush").getItem());
            addAfter(event, getItem(Concrete.MODID, "paint_brush").getItem(), getItem(Concrete.MODID, "paint_brush_white").getItem());
            addAfter(event, getItem(Concrete.MODID, "paint_brush_white").getItem(), getItem(Concrete.MODID, "paint_brush_light_gray").getItem());
            addAfter(event, getItem(Concrete.MODID, "paint_brush_light_gray").getItem(), getItem(Concrete.MODID, "paint_brush_gray").getItem());
            addAfter(event, getItem(Concrete.MODID, "paint_brush_gray").getItem(), getItem(Concrete.MODID, "paint_brush_black").getItem());
            addAfter(event, getItem(Concrete.MODID, "paint_brush_black").getItem(), getItem(Concrete.MODID, "paint_brush_brown").getItem());
            addAfter(event, getItem(Concrete.MODID, "paint_brush_brown").getItem(), getItem(Concrete.MODID, "paint_brush_red").getItem());
            addAfter(event, getItem(Concrete.MODID, "paint_brush_red").getItem(), getItem(Concrete.MODID, "paint_brush_orange").getItem());
            addAfter(event, getItem(Concrete.MODID, "paint_brush_orange").getItem(), getItem(Concrete.MODID, "paint_brush_yellow").getItem());
            addAfter(event, getItem(Concrete.MODID, "paint_brush_yellow").getItem(), getItem(Concrete.MODID, "paint_brush_lime").getItem());
            addAfter(event, getItem(Concrete.MODID, "paint_brush_lime").getItem(), getItem(Concrete.MODID, "paint_brush_green").getItem());
            addAfter(event, getItem(Concrete.MODID, "paint_brush_green").getItem(), getItem(Concrete.MODID, "paint_brush_cyan").getItem());
            addAfter(event, getItem(Concrete.MODID, "paint_brush_cyan").getItem(), getItem(Concrete.MODID, "paint_brush_light_blue").getItem());
            addAfter(event, getItem(Concrete.MODID, "paint_brush_light_blue").getItem(), getItem(Concrete.MODID, "paint_brush_blue").getItem());
            addAfter(event, getItem(Concrete.MODID, "paint_brush_blue").getItem(), getItem(Concrete.MODID, "paint_brush_purple").getItem());
            addAfter(event, getItem(Concrete.MODID, "paint_brush_purple").getItem(), getItem(Concrete.MODID, "paint_brush_magenta").getItem());
            addAfter(event, getItem(Concrete.MODID, "paint_brush_magenta").getItem(), getItem(Concrete.MODID, "paint_brush_pink").getItem());
            addAfter(event, getItem(Concrete.MODID, "paint_brush_pink").getItem(), SMOOTHER.get());
        }

        if (event.getTabKey().equals(CreativeModeTabs.COLORED_BLOCKS) && ThutCore.getConfig().itemsInCreativeTabs)
        {
            // Items listed in order of rainbow like vanilla. This is why the for statement for DyeColor isn't used
            addAfter(event, Items.PINK_CONCRETE_POWDER, getItem(Concrete.MODID, "reinforced_concrete_block_white").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_block_white").getItem(), getItem(Concrete.MODID, "reinforced_concrete_block_light_gray").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_block_light_gray").getItem(), getItem(Concrete.MODID, "reinforced_concrete_block_gray").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_block_gray").getItem(), getItem(Concrete.MODID, "reinforced_concrete_block_black").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_block_black").getItem(), getItem(Concrete.MODID, "reinforced_concrete_block_brown").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_block_brown").getItem(), getItem(Concrete.MODID, "reinforced_concrete_block_red").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_block_red").getItem(), getItem(Concrete.MODID, "reinforced_concrete_block_orange").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_block_orange").getItem(), getItem(Concrete.MODID, "reinforced_concrete_block_yellow").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_block_yellow").getItem(), getItem(Concrete.MODID, "reinforced_concrete_block_lime").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_block_lime").getItem(), getItem(Concrete.MODID, "reinforced_concrete_block_green").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_block_green").getItem(), getItem(Concrete.MODID, "reinforced_concrete_block_cyan").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_block_cyan").getItem(), getItem(Concrete.MODID, "reinforced_concrete_block_light_blue").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_block_light_blue").getItem(), getItem(Concrete.MODID, "reinforced_concrete_block_blue").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_block_blue").getItem(), getItem(Concrete.MODID, "reinforced_concrete_block_purple").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_block_purple").getItem(), getItem(Concrete.MODID, "reinforced_concrete_block_magenta").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_block_magenta").getItem(), getItem(Concrete.MODID, "reinforced_concrete_block_pink").getItem());

            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_block_pink").getItem(), getItem(Concrete.MODID, "reinforced_concrete_layer_white").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_layer_white").getItem(), getItem(Concrete.MODID, "reinforced_concrete_layer_light_gray").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_layer_light_gray").getItem(), getItem(Concrete.MODID, "reinforced_concrete_layer_gray").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_layer_gray").getItem(), getItem(Concrete.MODID, "reinforced_concrete_layer_black").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_layer_black").getItem(), getItem(Concrete.MODID, "reinforced_concrete_layer_brown").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_layer_brown").getItem(), getItem(Concrete.MODID, "reinforced_concrete_layer_red").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_layer_red").getItem(), getItem(Concrete.MODID, "reinforced_concrete_layer_orange").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_layer_orange").getItem(), getItem(Concrete.MODID, "reinforced_concrete_layer_yellow").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_layer_yellow").getItem(), getItem(Concrete.MODID, "reinforced_concrete_layer_lime").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_layer_lime").getItem(), getItem(Concrete.MODID, "reinforced_concrete_layer_green").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_layer_green").getItem(), getItem(Concrete.MODID, "reinforced_concrete_layer_cyan").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_layer_cyan").getItem(), getItem(Concrete.MODID, "reinforced_concrete_layer_light_blue").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_layer_light_blue").getItem(), getItem(Concrete.MODID, "reinforced_concrete_layer_blue").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_layer_blue").getItem(), getItem(Concrete.MODID, "reinforced_concrete_layer_purple").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_layer_purple").getItem(), getItem(Concrete.MODID, "reinforced_concrete_layer_magenta").getItem());
            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_layer_magenta").getItem(), getItem(Concrete.MODID, "reinforced_concrete_layer_pink").getItem());

            addAfter(event, getItem(Concrete.MODID, "reinforced_concrete_layer_pink").getItem(), getItem(Concrete.MODID, "concrete_layer_white").getItem());
            addAfter(event, getItem(Concrete.MODID, "concrete_layer_white").getItem(), getItem(Concrete.MODID, "concrete_layer_light_gray").getItem());
            addAfter(event, getItem(Concrete.MODID, "concrete_layer_light_gray").getItem(), getItem(Concrete.MODID, "concrete_layer_gray").getItem());
            addAfter(event, getItem(Concrete.MODID, "concrete_layer_gray").getItem(), getItem(Concrete.MODID, "concrete_layer_black").getItem());
            addAfter(event, getItem(Concrete.MODID, "concrete_layer_black").getItem(), getItem(Concrete.MODID, "concrete_layer_brown").getItem());
            addAfter(event, getItem(Concrete.MODID, "concrete_layer_brown").getItem(), getItem(Concrete.MODID, "concrete_layer_red").getItem());
            addAfter(event, getItem(Concrete.MODID, "concrete_layer_red").getItem(), getItem(Concrete.MODID, "concrete_layer_orange").getItem());
            addAfter(event, getItem(Concrete.MODID, "concrete_layer_orange").getItem(), getItem(Concrete.MODID, "concrete_layer_yellow").getItem());
            addAfter(event, getItem(Concrete.MODID, "concrete_layer_yellow").getItem(), getItem(Concrete.MODID, "concrete_layer_lime").getItem());
            addAfter(event, getItem(Concrete.MODID, "concrete_layer_lime").getItem(), getItem(Concrete.MODID, "concrete_layer_green").getItem());
            addAfter(event, getItem(Concrete.MODID, "concrete_layer_green").getItem(), getItem(Concrete.MODID, "concrete_layer_cyan").getItem());
            addAfter(event, getItem(Concrete.MODID, "concrete_layer_cyan").getItem(), getItem(Concrete.MODID, "concrete_layer_light_blue").getItem());
            addAfter(event, getItem(Concrete.MODID, "concrete_layer_light_blue").getItem(), getItem(Concrete.MODID, "concrete_layer_blue").getItem());
            addAfter(event, getItem(Concrete.MODID, "concrete_layer_blue").getItem(), getItem(Concrete.MODID, "concrete_layer_purple").getItem());
            addAfter(event, getItem(Concrete.MODID, "concrete_layer_purple").getItem(), getItem(Concrete.MODID, "concrete_layer_magenta").getItem());
            addAfter(event, getItem(Concrete.MODID, "concrete_layer_magenta").getItem(), getItem(Concrete.MODID, "concrete_layer_pink").getItem());
        }

        if (event.getTabKey().equals(CreativeModeTabs.OP_BLOCKS))
        {
            if (event.hasPermissions())
            {
                addAfter(event, Items.DEBUG_STICK, VOLCANO.get());
            }
        }
    }

    public static void addAfter(BuildCreativeModeTabContentsEvent event, ItemLike afterItem, ItemLike item) {
        event.getEntries().putAfter(new ItemStack(afterItem), new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    public void loadComplete(FMLLoadCompleteEvent event)
    {
        event.enqueueWork(() -> {
            DispenserBlock.registerBehavior(BUCKET.get(), ConcreteDispenseBehaviour.INSTANCE);
            DispenserBlock.registerBehavior(WET_BLOCK_ITEM.get(), ConcreteDispenseBehaviour.INSTANCE);
        });
    }
}
