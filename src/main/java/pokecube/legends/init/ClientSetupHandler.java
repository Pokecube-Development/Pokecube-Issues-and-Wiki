package pokecube.legends.init;

import java.util.function.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.particle.SoulParticle;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.CampfireRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.handlers.ItemGenerator;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.Reference;
import pokecube.legends.blocks.FlowerBase;
import pokecube.legends.blocks.MushroomBase;
import pokecube.legends.blocks.containers.GenericBookshelfEmpty;
import pokecube.legends.blocks.normalblocks.InfectedFireBlock;
import pokecube.legends.blocks.plants.TaintedKelpPlantBlock;
import pokecube.legends.client.render.block.Raid;
import pokecube.legends.client.render.entity.Wormhole;
import pokecube.legends.tileentity.RaidSpawn;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.ID, value = Dist.CLIENT)
public class ClientSetupHandler
{
    static final Predicate<Material> notSolid = m -> m == Material.ICE || m == Material.ICE_SOLID
            || m == Material.HEAVY_METAL || m == Material.LEAVES || m == Material.REPLACEABLE_PLANT;

    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            for (final RegistryObject<Block> reg : PokecubeLegends.NO_TAB.getEntries())
            {
                final Block b = reg.get();
                if (b instanceof ItemGenerator.GenericPottedPlant || b instanceof InfectedFireBlock
                        || b instanceof TaintedKelpPlantBlock)
                    ItemBlockRenderTypes.setRenderLayer(b, RenderType.cutout());
            }
            for (final RegistryObject<Block> reg : PokecubeLegends.DIMENSIONS_TAB.getEntries())
            {
                final Block b = reg.get();
                if (b instanceof FlowerBase || b instanceof MushroomBase)
                    ItemBlockRenderTypes.setRenderLayer(b, RenderType.cutout());
                boolean fullCube = true;
                for (final BlockState state : b.getStateDefinition().getPossibleStates())
                {
                    final Material m = state.getMaterial();
                    if (ClientSetupHandler.notSolid.test(m))
                    {
                        fullCube = false;
                        break;
                    }
                    try
                    {
                        final VoxelShape s = state.getShape(null, BlockPos.ZERO);
                        if (s != Shapes.block())
                        {
                            fullCube = false;
                            break;
                        }
                    }
                    catch (final Exception e)
                    {
                        fullCube = false;
                        break;
                    }
                }
                if (!fullCube) ItemBlockRenderTypes.setRenderLayer(b, RenderType.cutout());

                ItemBlockRenderTypes.setRenderLayer(BlockInit.INFECTED_CAMPFIRE.get(), RenderType.cutoutMipped());
                ItemBlockRenderTypes.setRenderLayer(BlockInit.INFECTED_LANTERN.get(), RenderType.cutoutMipped());
                ItemBlockRenderTypes.setRenderLayer(BlockInit.MIRAGE_GLASS.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(BlockInit.SPECTRUM_GLASS.get(), RenderType.translucent());
                ItemBlockRenderTypes.setRenderLayer(BlockInit.TALL_CRYSTALLIZED_BUSH.get(), RenderType.cutoutMipped());
                ItemBlockRenderTypes.setRenderLayer(BlockInit.YVELTAL_CORE.get(), RenderType.cutoutMipped());
                ItemBlockRenderTypes.setRenderLayer(PlantsInit.LARGE_GOLDEN_FERN.get(), RenderType.cutoutMipped());
                ItemBlockRenderTypes.setRenderLayer(PlantsInit.TALL_GOLDEN_GRASS.get(), RenderType.cutoutMipped());
                if (b instanceof GenericBookshelfEmpty)
                    ItemBlockRenderTypes.setRenderLayer(b, RenderType.cutoutMipped());
            }

            for (final RegistryObject<Block> reg : PokecubeLegends.DECORATION_TAB.getEntries())
            {
                final Block b = reg.get();
                if (b instanceof FlowerBase || b instanceof MushroomBase)
                    ItemBlockRenderTypes.setRenderLayer(b, RenderType.cutout());
                boolean fullCube = true;
                for (final BlockState state : b.getStateDefinition().getPossibleStates())
                {
                    final Material m = state.getMaterial();
                    if (ClientSetupHandler.notSolid.test(m))
                    {
                        fullCube = false;
                        break;
                    }
                    try
                    {
                        final VoxelShape s = state.getShape(null, BlockPos.ZERO);
                        if (s != Shapes.block())
                        {
                            fullCube = false;
                            break;
                        }
                        if (m == Material.GLASS) ItemBlockRenderTypes.setRenderLayer(b, RenderType.translucent());
                    }
                    catch (final Exception e)
                    {
                        fullCube = false;
                        break;
                    }
                }
                if (!fullCube) ItemBlockRenderTypes.setRenderLayer(b, RenderType.cutout());
                ItemBlockRenderTypes.setRenderLayer(BlockInit.ONE_WAY_GLASS.get(), RenderType.cutoutMipped());
                ItemBlockRenderTypes.setRenderLayer(BlockInit.FRAMED_DISTORTIC_MIRROR.get(), RenderType.translucent());
            }

            for (final RegistryObject<Block> reg : PokecubeLegends.POKECUBE_BLOCKS_TAB.getEntries())
            {
                final Block b = reg.get();
                boolean fullCube = true;
                for (final BlockState state : b.getStateDefinition().getPossibleStates())
                {
                    final Material m = state.getMaterial();
                    if (ClientSetupHandler.notSolid.test(m))
                    {
                        fullCube = false;
                        break;
                    }
                    try
                    {
                        final VoxelShape s = state.getShape(null, BlockPos.ZERO);
                        if (s != Shapes.block())
                        {
                            fullCube = false;
                            break;
                        }
                    }
                    catch (final Exception e)
                    {
                        fullCube = false;
                        break;
                    }
                }
                if (!fullCube) ItemBlockRenderTypes.setRenderLayer(b, RenderType.cutout());
            }

            // Register config gui
            // FIXME config gui
            // ModList.get().getModContainerById(Reference.ID).ifPresent(c ->
            // c.registerExtensionPoint(
            // ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, parent) -> new
            // ConfigGui(PokecubeLegends.config, parent)));

            // Shields
            ItemInit.addItemModelProperties();
        });
    }

    @SubscribeEvent
    public static void registerRenderers(final RegisterRenderers event)
    {
        // Renderer for blocks
        event.registerBlockEntityRenderer(RaidSpawn.TYPE, Raid::new);
        event.registerBlockEntityRenderer(TileEntityInit.CAMPFIRE_ENTITY.get(), CampfireRenderer::new);

        // Register entity renderer for the wormhole
        event.registerEntityRenderer(EntityInit.WORMHOLE.get(), Wormhole::new);
    }

    @SubscribeEvent
    public static void registerParticleFactories(final ParticleFactoryRegisterEvent event)
    {
        Minecraft.getInstance().particleEngine.register(ParticleInit.INFECTED_FIRE_FLAME.get(),
                FlameParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(ParticleInit.INFECTED_SMOKE.get(), SmokeParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(ParticleInit.INFECTED_SOUL.get(), SoulParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(ParticleInit.MUSHROOM.get(),
                SuspendedTownParticle.Provider::new);
    }
}
