package thut.core.proxy;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import thut.api.maths.Vector3;
import thut.api.particle.ThutParticles;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.core.client.gui.ConfigGui;
import thut.core.client.render.animation.CapabilityAnimation;
import thut.core.client.render.particle.ParticleFactories;
import thut.core.common.ThutCore;

public class ClientProxy extends CommonProxy
{

    public static void line(final IVertexBuilder builder, final Matrix4f positionMatrix, final float dx1,
            final float dy1, final float dz1, final float dx2, final float dy2, final float dz2, final float r,
            final float g, final float b, final float a)
    {
        builder.pos(positionMatrix, dx1, dy1, dz1).color(r, g, b, a).endVertex();
        builder.pos(positionMatrix, dx2, dy2, dz2).color(r, g, b, a).endVertex();
    }

    public static void line(final IVertexBuilder builder, final Matrix4f positionMatrix, final Vector3f start,
            final Vector3f end, final float r, final float g, final float b, final float a)
    {
        ClientProxy.line(builder, positionMatrix, start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end
                .getZ(), r, g, b, a);
    }

    private boolean initParticles = false;

    @Override
    public DynamicRegistries getRegistries()
    {
        // This is null on single player, so we have an integrated server
        if (Minecraft.getInstance().getCurrentServerData() == null) return super.getRegistries();
        return Minecraft.getInstance().world.func_241828_r();
    }

    @Override
    public void loaded(final FMLLoadCompleteEvent event)
    {
        super.loaded(event);
    }

    @Override
    public void setupClient(final FMLClientSetupEvent event)
    {
        super.setupClient(event);
        MinecraftForge.EVENT_BUS.register(this);

        // Register config gui
        ModList.get().getModContainerById(ThutCore.MODID).ifPresent(c -> c.registerExtensionPoint(
                ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, parent) -> new ConfigGui(ThutCore.conf, parent)));
    }

    @Override
    public void setup(final FMLCommonSetupEvent event)
    {
        super.setup(event);
        CapabilityAnimation.setup();
    }

    @SubscribeEvent
    public void startup(final Load event)
    {
        if (this.initParticles) return;
        this.initParticles = true;
        Minecraft.getInstance().particles.registerFactory(ThutParticles.AURORA, ParticleFactories.GENERICFACTORY);
        Minecraft.getInstance().particles.registerFactory(ThutParticles.MISC, ParticleFactories.GENERICFACTORY);
        Minecraft.getInstance().particles.registerFactory(ThutParticles.STRING, ParticleFactories.GENERICFACTORY);
        Minecraft.getInstance().particles.registerFactory(ThutParticles.LEAF, ParticleFactories.GENERICFACTORY);
        Minecraft.getInstance().particles.registerFactory(ThutParticles.POWDER, ParticleFactories.GENERICFACTORY);
    }

    @SubscribeEvent
    public void textOverlay(final RenderGameOverlayEvent.Text event)
    {
        final boolean debug = Minecraft.getInstance().gameSettings.showDebugInfo;
        if (!debug) return;
        final TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(Minecraft.getInstance().player);
        final Vector3 v = Vector3.getNewVector().set(Minecraft.getInstance().player);
        final int num = t.getBiome(v);
        final BiomeType type = BiomeType.getType(num);
        final String msg = "Sub-Biome: " + I18n.format(type.readableName) + " (" + type.name + ")";
        event.getLeft().add("");
        event.getLeft().add(msg);

        if (Screen.hasAltDown())
        {
            event.getLeft().add("");
            final Biome b = v.getBiome(Minecraft.getInstance().world);
            final RegistryKey<Biome> key = BiomeDatabase.getKey(b);
            event.getLeft().add(key.getLocation() + ": " + BiomeDictionary.getTypes(key) + ", " + b.getCategory());
        }
    }

    BiomeType getSubbiome(final ItemStack held)
    {
        if (held.getDisplayName().getString().toLowerCase(Locale.ROOT).startsWith("subbiome->"))
        {
            final String[] args = held.getDisplayName().getString().split("->");
            if (args.length != 2) return null;
            return BiomeType.getBiome(args[1].trim());
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void RenderBounds(final RenderWorldLastEvent event)
    {
        ItemStack held;
        final PlayerEntity player = Minecraft.getInstance().player;
        if (!(held = player.getHeldItemMainhand()).isEmpty() || !(held = player.getHeldItemOffhand()).isEmpty())
        {
            if (this.getSubbiome(held) == null) return;
            if (held.getTag() != null && held.getTag().contains("min"))
            {
                final Minecraft mc = Minecraft.getInstance();
                final Vector3d projectedView = mc.gameRenderer.getActiveRenderInfo().getProjectedView();
                Vector3d pointed = new Vector3d(projectedView.x, projectedView.y, projectedView.z).add(mc.player
                        .getLook(event.getPartialTicks()));
                if (mc.objectMouseOver != null && mc.objectMouseOver.getType() == Type.BLOCK)
                {
                    final BlockRayTraceResult result = (BlockRayTraceResult) mc.objectMouseOver;
                    pointed = new Vector3d(result.getPos().getX(), result.getPos().getY(), result.getPos().getZ());
                    //
                }
                final Vector3 v = Vector3.readFromNBT(held.getTag().getCompound("min"), "");

                final AxisAlignedBB one = new AxisAlignedBB(v.getPos());
                final AxisAlignedBB two = new AxisAlignedBB(new BlockPos(pointed));

                final double minX = Math.min(one.minX, two.minX);
                final double minY = Math.min(one.minY, two.minY);
                final double minZ = Math.min(one.minZ, two.minZ);
                final double maxX = Math.max(one.maxX, two.maxX);
                final double maxY = Math.max(one.maxY, two.maxY);
                final double maxZ = Math.max(one.maxZ, two.maxZ);

                final MatrixStack mat = event.getMatrixStack();
                mat.translate(-projectedView.x, -projectedView.y, -projectedView.z);

                final List<Pair<Vector3f, Vector3f>> lines = Lists.newArrayList();

                lines.add(Pair.of(new Vector3f((float) minX, (float) minY, (float) minZ), new Vector3f((float) maxX,
                        (float) minY, (float) minZ)));
                lines.add(Pair.of(new Vector3f((float) minX, (float) maxY, (float) minZ), new Vector3f((float) maxX,
                        (float) maxY, (float) minZ)));
                lines.add(Pair.of(new Vector3f((float) minX, (float) minY, (float) maxZ), new Vector3f((float) maxX,
                        (float) minY, (float) maxZ)));
                lines.add(Pair.of(new Vector3f((float) minX, (float) maxY, (float) maxZ), new Vector3f((float) maxX,
                        (float) maxY, (float) maxZ)));

                lines.add(Pair.of(new Vector3f((float) minX, (float) minY, (float) minZ), new Vector3f((float) minX,
                        (float) minY, (float) maxZ)));
                lines.add(Pair.of(new Vector3f((float) maxX, (float) minY, (float) minZ), new Vector3f((float) maxX,
                        (float) minY, (float) maxZ)));
                lines.add(Pair.of(new Vector3f((float) minX, (float) maxY, (float) minZ), new Vector3f((float) minX,
                        (float) maxY, (float) maxZ)));
                lines.add(Pair.of(new Vector3f((float) maxX, (float) maxY, (float) minZ), new Vector3f((float) maxX,
                        (float) maxY, (float) maxZ)));

                lines.add(Pair.of(new Vector3f((float) minX, (float) minY, (float) minZ), new Vector3f((float) minX,
                        (float) maxY, (float) minZ)));
                lines.add(Pair.of(new Vector3f((float) maxX, (float) minY, (float) minZ), new Vector3f((float) maxX,
                        (float) maxY, (float) minZ)));
                lines.add(Pair.of(new Vector3f((float) minX, (float) minY, (float) maxZ), new Vector3f((float) minX,
                        (float) maxY, (float) maxZ)));
                lines.add(Pair.of(new Vector3f((float) maxX, (float) minY, (float) maxZ), new Vector3f((float) maxX,
                        (float) maxY, (float) maxZ)));

                mat.push();

                final Matrix4f positionMatrix = mat.getLast().getMatrix();

                final IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
                final IVertexBuilder builder = buffer.getBuffer(RenderType.LINES);
                for (final Pair<Vector3f, Vector3f> line : lines)
                    ClientProxy.line(builder, positionMatrix, line.getLeft(), line.getRight(), 1, 0, 0, 1f);
                mat.pop();
            }
        }
    }
}
