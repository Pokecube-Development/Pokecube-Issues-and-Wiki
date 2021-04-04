package pokecube.legends.init;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.Reference;
import pokecube.legends.blocks.PlantBase;
import pokecube.legends.client.render.block.Raid;
import pokecube.legends.client.render.entity.Wormhole;
import pokecube.legends.tileentity.RaidSpawn;
import thut.core.client.gui.ConfigGui;

import java.util.function.Predicate;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = Reference.ID, value = Dist.CLIENT)
public class ClientSetupHandler
{
    static final Predicate<Material> notSolid = m -> m == Material.ICE ||
    		m == Material.ICE_SOLID || m == Material.LEAVES || m == Material.HEAVY_METAL;

    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
        for (final RegistryObject<Block> reg : PokecubeLegends.BLOCKS_TAB.getEntries())
        {
            final Block b = reg.get();
            if (b instanceof PlantBase) RenderTypeLookup.setRenderLayer(b, RenderType.cutout());
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
                    if (s != VoxelShapes.block())
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
            if (!fullCube) RenderTypeLookup.setRenderLayer(b, RenderType.cutout());

            RenderTypeLookup.setRenderLayer(BlockInit.MIRAGE_GLASS.get(), RenderType.translucent());
            RenderTypeLookup.setRenderLayer(BlockInit.SPECTRUM_GLASS.get(), RenderType.translucent());
            RenderTypeLookup.setRenderLayer(BlockInit.TALL_CRYSTALLIZED_BUSH.get(), RenderType.cutoutMipped());
        }

        for (final RegistryObject<Block> reg : PokecubeLegends.DECORATION_TAB.getEntries())
        {
            final Block b = reg.get();
            if (b instanceof PlantBase) RenderTypeLookup.setRenderLayer(b, RenderType.cutout());
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
                    if (s != VoxelShapes.block())
                    {
                        fullCube = false;
                        break;
                    }
                    if (m == Material.GLASS) RenderTypeLookup.setRenderLayer(b, RenderType.translucent());
                }
                catch (final Exception e)
                {
                    fullCube = false;
                    break;
                }
            }
            if (!fullCube) RenderTypeLookup.setRenderLayer(b, RenderType.cutout());
            RenderTypeLookup.setRenderLayer(BlockInit.DISTORTIC_OW_GLASS.get(), RenderType.cutoutMipped());
			RenderTypeLookup.setRenderLayer(BlockInit.DISTORTIC_FRAMED_MIRROR.get(), RenderType.translucent());
        }

        for (final RegistryObject<Block> reg : PokecubeLegends.BLOCKS.getEntries())
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
                    if (s != VoxelShapes.block())
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
            if (!fullCube) RenderTypeLookup.setRenderLayer(b, RenderType.cutout());
        }

        // Renderer for raid spawn
        ClientRegistry.bindTileEntityRenderer(RaidSpawn.TYPE, Raid::new);

        // Register config gui
        ModList.get().getModContainerById(Reference.ID).ifPresent(c -> c.registerExtensionPoint(
                ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, parent) -> new ConfigGui(PokecubeLegends.config, parent)));

        // Register entity renderer for the wormhole
        RenderingRegistry.registerEntityRenderingHandler(EntityInit.WORMHOLE.get(), Wormhole::new);
    }
}
