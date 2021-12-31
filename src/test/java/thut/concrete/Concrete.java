package thut.concrete;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
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
import thut.api.block.flowing.DustBlock;
import thut.api.block.flowing.MoltenBlock;
import thut.api.block.flowing.SolidBlock;
import thut.concrete.block.VolcanoBlock;
import thut.concrete.block.entity.VolcanoEntity;
import thut.core.common.ThutCore;

@Mod(value = Concrete.MODID)
public class Concrete
{
    public static final String MODID = "concrete";

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

//    public static final RegistryObject<LiquidBlock> WETCONCRETEB;
//    public static final RegistryObject<LiquidBlock> DRYCONCRETEB;
//
//    public static final RegistryObject<FlowingFluid> WETCONCRETEF;
//    public static final RegistryObject<FlowingFluid> DRYCONCRETEF;
//
//    public static final RegistryObject<FlowingFluid> WETCONCRETES;
//    public static final RegistryObject<FlowingFluid> DRYCONCRETES;
//
//    private static ForgeFlowingFluid.Properties makeDryProperties()
//    {
//        return new ForgeFlowingFluid.Properties(DRYCONCRETES, DRYCONCRETEF, FluidAttributes
//                .builder(new ResourceLocation(MODID, "dry_s"), new ResourceLocation(MODID, "dry_f")).color(0x3F1080FF))
//                        .block(DRYCONCRETEB).levelDecreasePerBlock(1);
//    }
//
//    private static ForgeFlowingFluid.Properties makeWetProperties()
//    {
//        return new ForgeFlowingFluid.Properties(WETCONCRETES, WETCONCRETEF, FluidAttributes
//                .builder(new ResourceLocation(MODID, "wet_s"), new ResourceLocation(MODID, "wet_f")).color(0x3F1080FF))
//                        .block(WETCONCRETEB).levelDecreasePerBlock(1);
//    }
//  WETCONCRETEF = FLUIDS.register("concete_wet_flow", () -> new FluidBase(makeWetProperties()));
//  DRYCONCRETEF = FLUIDS.register("concete_dry_flow", () -> new FluidBase(makeDryProperties()));
//
//  WETCONCRETES = FLUIDS.register("concete_wet_still", () -> new FluidBase(makeWetProperties()));
//  DRYCONCRETES = FLUIDS.register("concete_dry_still", () -> new FluidBase(makeDryProperties()));
//
//  WETCONCRETEB = BLOCKS.register("concete_wet",
//          () -> new LiquidBase(BlockBehaviour.Properties.of(Material.CLAY), WETCONCRETES));
//  DRYCONCRETEB = BLOCKS.register("concete_dry",
//          () -> new LiquidBase(BlockBehaviour.Properties.of(Material.STONE), DRYCONCRETES));

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

        layer_props = BlockBehaviour.Properties.of(Material.STONE).noOcclusion().requiresCorrectToolForDrops();
        block_props = BlockBehaviour.Properties.of(Material.STONE).requiresCorrectToolForDrops();

        regs = SolidBlock.makeSolid(BLOCKS, MODID, "solid_layer", "solid_block", layer_props, block_props);

        SOLID = regs[0];
        SOLID_BLOCK = regs[1];

        layer_props = BlockBehaviour.Properties.of(Material.LAVA).noOcclusion().randomTicks()
                .requiresCorrectToolForDrops();
        block_props = BlockBehaviour.Properties.of(Material.LAVA).randomTicks().requiresCorrectToolForDrops();

        ResourceLocation solid_layer = new ResourceLocation(MODID, "solid_layer");
        ResourceLocation solid_block = new ResourceLocation(MODID, "solid_block");

        regs = MoltenBlock.makeMolten(BLOCKS, MODID, "molten_layer", "molten_block", layer_props, block_props,
                solid_layer, solid_block);

        MOLTEN = regs[0];
        MOLTEN_BLOCK = regs[1];

        BlockBehaviour.Properties volc_props = BlockBehaviour.Properties.of(Material.BARRIER).noDrops();
        VOLCANO = BLOCKS.register("volcano", () -> new VolcanoBlock(volc_props));
        
        VOLCANO_TYPE = TILES.register("volcano", () -> BlockEntityType.Builder.of(VolcanoEntity::new, VOLCANO.get()).build(null));

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
