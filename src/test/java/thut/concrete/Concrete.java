package thut.concrete;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
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
import thut.api.block.flowing.DustBlock;
import thut.api.block.flowing.MoltenBlock;
import thut.api.block.flowing.SolidBlock;
import thut.concrete.block.ConcreteFluidBlock;
import thut.concrete.block.RebarBlock;
import thut.concrete.block.VolcanoBlock;
import thut.concrete.block.entity.VolcanoEntity;
import thut.concrete.fluid.ConcreteFluid;
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

    public static final RegistryObject<DustBlock> DUST;
    public static final RegistryObject<DustBlock> DUST_BLOCK;

    public static final RegistryObject<DustBlock> MOLTEN;
    public static final RegistryObject<DustBlock> MOLTEN_BLOCK;

    public static final RegistryObject<DustBlock> SOLID;
    public static final RegistryObject<DustBlock> SOLID_BLOCK;

    public static final RegistryObject<FlowingFluid> CONCRETE_FLUID;

    public static final RegistryObject<LiquidBlock> CONCRETE_FLUID_BLOCK;

    public static final RegistryObject<RebarBlock> REBAR_BLOCK;

    public static RegistryObject<Item> CONCRETE_BUCKET;

    private static ForgeFlowingFluid.Properties makeProperties()
    {
        return new ForgeFlowingFluid.Properties(CONCRETE_FLUID, CONCRETE_FLUID,
                FluidAttributes.builder(FLUID_STILL, FLUID_FLOWING).overlay(FLUID_OVERLAY).color(0x3F1080FF))
                        .bucket(CONCRETE_BUCKET).block(CONCRETE_FLUID_BLOCK);
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

        RegistryObject<DustBlock>[] regs = DustBlock.makeDust(BLOCKS, MODID, "dust_layer", "dust_block", layer_props,
                block_props);

        DUST = regs[0];
        DUST_BLOCK = regs[1];

        layer_props = BlockBehaviour.Properties.of(Material.STONE).strength(30.0F).noOcclusion()
                .requiresCorrectToolForDrops();
        block_props = BlockBehaviour.Properties.of(Material.STONE).strength(30.0F).requiresCorrectToolForDrops();

        regs = SolidBlock.makeSolid(BLOCKS, MODID, "solid_layer", "solid_block", layer_props, block_props);

        SOLID = regs[0];
        SOLID_BLOCK = regs[1];

        layer_props = BlockBehaviour.Properties.of(Material.LAVA).strength(100.0F).noOcclusion().randomTicks()
                .requiresCorrectToolForDrops().lightLevel(s -> s.getValue(DustBlock.LAYERS) - 1);
        block_props = BlockBehaviour.Properties.of(Material.LAVA).strength(100.0F).randomTicks()
                .requiresCorrectToolForDrops().lightLevel(s -> 15);

        ResourceLocation solid_layer = new ResourceLocation(MODID, "solid_layer");
        ResourceLocation solid_block = new ResourceLocation(MODID, "solid_block");

        regs = MoltenBlock.makeMolten(BLOCKS, MODID, "molten_layer", "molten_block", layer_props, block_props,
                solid_layer, solid_block);

        MOLTEN = regs[0];
        MOLTEN_BLOCK = regs[1];

        BlockBehaviour.Properties volc_props = BlockBehaviour.Properties.of(Material.BARRIER).noDrops();
        VOLCANO = BLOCKS.register("volcano", () -> new VolcanoBlock(volc_props));

        VOLCANO_TYPE = TILES.register("volcano",
                () -> BlockEntityType.Builder.of(VolcanoEntity::new, VOLCANO.get()).build(null));

        CONCRETE_FLUID = FLUIDS.register("concrete_fluid", () -> new ConcreteFluid(makeProperties()));

        CONCRETE_FLUID_BLOCK = BLOCKS.register("concrete_fluid_block",
                () -> new ConcreteFluidBlock(Properties.of(Material.WATER).noCollission().strength(100.0F).noDrops(),
                        CONCRETE_FLUID));

        CONCRETE_BUCKET = ITEMS.register("concrete_bucket", () -> new BucketItem(CONCRETE_FLUID,
                new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).tab(CreativeModeTab.TAB_MISC)));

        REBAR_BLOCK = BLOCKS.register("rebar",
                () -> new RebarBlock(Properties.of(Material.METAL).noCollission().strength(100.0F).noDrops()));

        // Register the item blocks.
        for (final RegistryObject<Block> reg : BLOCKS.getEntries())
        {
            final Item.Properties props = new Item.Properties().tab(ThutCore.THUTITEMS);
            ITEMS.register(reg.getId().getPath(), () -> new BlockItem(reg.get(), props));
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MODID, value = Dist.CLIENT)
    public static class ClientEvents
    {
        @SubscribeEvent
        public static void setupClient(final FMLClientSetupEvent event)
        {
            ItemBlockRenderTypes.setRenderLayer(DUST.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(MOLTEN.get(), RenderType.cutoutMipped());
            ItemBlockRenderTypes.setRenderLayer(SOLID.get(), RenderType.cutoutMipped());
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

    }
}
