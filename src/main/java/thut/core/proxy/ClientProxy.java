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
import net.minecraft.entity.LivingEntity;
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
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import thut.api.entity.CopyCaps;
import thut.api.entity.ICopyMob;
import thut.api.maths.Vector3;
import thut.api.particle.ThutParticles;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.core.client.gui.ConfigGui;
import thut.core.client.render.particle.ParticleFactories;
import thut.core.common.ThutCore;

public class ClientProxy extends CommonProxy
{

    public static void line(final IVertexBuilder builder, final Matrix4f positionMatrix, final float dx1,
            final float dy1, final float dz1, final float dx2, final float dy2, final float dz2, final float r,
            final float g, final float b, final float a)
    {
        builder.vertex(positionMatrix, dx1, dy1, dz1).color(r, g, b, a).endVertex();
        builder.vertex(positionMatrix, dx2, dy2, dz2).color(r, g, b, a).endVertex();
    }

    public static void line(final IVertexBuilder builder, final Matrix4f positionMatrix, final Vector3f start,
            final Vector3f end, final float r, final float g, final float b, final float a)
    {
        ClientProxy.line(builder, positionMatrix, start.x(), start.y(), start.z(), end.x(), end.y(), end.z(), r, g, b,
                a);
    }

    private boolean initParticles = false;

    @Override
    public DynamicRegistries getRegistries()
    {
        // This is null on single player, so we have an integrated server
        if (Minecraft.getInstance().getCurrentServer() == null) return super.getRegistries();
        if (Minecraft.getInstance().level == null) return null;
        return Minecraft.getInstance().level.registryAccess();
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
    }

    @SubscribeEvent
    public void startup(final Load event)
    {
        if (this.initParticles) return;
        this.initParticles = true;
        Minecraft.getInstance().particleEngine.register(ThutParticles.AURORA, ParticleFactories.GENERICFACTORY);
        Minecraft.getInstance().particleEngine.register(ThutParticles.MISC, ParticleFactories.GENERICFACTORY);
        Minecraft.getInstance().particleEngine.register(ThutParticles.STRING, ParticleFactories.GENERICFACTORY);
        Minecraft.getInstance().particleEngine.register(ThutParticles.LEAF, ParticleFactories.GENERICFACTORY);
        Minecraft.getInstance().particleEngine.register(ThutParticles.POWDER, ParticleFactories.GENERICFACTORY);
    }

    @SubscribeEvent
    public void textOverlay(final RenderGameOverlayEvent.Text event)
    {
        final boolean debug = Minecraft.getInstance().options.renderDebug;
        if (!debug) return;
        final TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(Minecraft.getInstance().player);
        final Vector3 v = Vector3.getNewVector().set(Minecraft.getInstance().player);
        final BiomeType type = t.getBiome(v);
        final String msg = "Sub-Biome: " + I18n.get(type.readableName) + " (" + type.name + ")";
        event.getLeft().add("");
        event.getLeft().add(msg);

        if (Screen.hasAltDown())
        {
            event.getLeft().add("");
            final Biome b = v.getBiome(Minecraft.getInstance().level);
            final RegistryKey<Biome> key = BiomeDatabase.getKey(b);
            event.getLeft().add(key.location() + ": " + BiomeDictionary.getTypes(key) + ", " + b.getBiomeCategory());
        }
    }

    BiomeType getSubbiome(final ItemStack held)
    {
        if (held.getHoverName().getString().toLowerCase(Locale.ROOT).startsWith("subbiome->"))
        {
            final String[] args = held.getHoverName().getString().split("->");
            if (args.length != 2) return null;
            return BiomeType.getBiome(args[1].trim());
        }
        return null;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void renderMob(final RenderLivingEvent.Pre<?, ?> event)
    {
        final LivingEntity living = event.getEntity();
        final ICopyMob copied = CopyCaps.get(living);
        if (copied != null && copied.getCopiedMob() != null)
        {
            final LivingEntity entity = copied.getCopiedMob();
            final boolean backup = event.getRenderer().getDispatcher().camera.isInitialized();
            event.getRenderer().getDispatcher().setRenderShadow(false);
            event.getRenderer().getDispatcher().render(entity, 0, 0, 0, event.getPartialRenderTick(), 0, event
                    .getMatrixStack(), event.getBuffers(), event.getLight());
            event.getRenderer().getDispatcher().setRenderShadow(backup);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void renderHand(final RenderHandEvent event)
    {
        final PlayerEntity player = Minecraft.getInstance().player;
        final ICopyMob copied = CopyCaps.get(player);
        if (copied != null && copied.getCopiedMob() != null) event.setCanceled(true);
    }

    @SubscribeEvent
    public void RenderBounds(final RenderWorldLastEvent event)
    {
        ItemStack held;
        final PlayerEntity player = Minecraft.getInstance().player;
        if (!(held = player.getMainHandItem()).isEmpty() || !(held = player.getOffhandItem()).isEmpty())
        {
            if (this.getSubbiome(held) == null) return;
            if (held.getTag() != null && held.getTag().contains("min"))
            {
                final Minecraft mc = Minecraft.getInstance();
                final Vector3d projectedView = mc.gameRenderer.getMainCamera().getPosition();
                Vector3d pointed = new Vector3d(projectedView.x, projectedView.y, projectedView.z).add(mc.player
                        .getViewVector(event.getPartialTicks()));
                if (mc.hitResult != null && mc.hitResult.getType() == Type.BLOCK)
                {
                    final BlockRayTraceResult result = (BlockRayTraceResult) mc.hitResult;
                    pointed = new Vector3d(result.getBlockPos().getX(), result.getBlockPos().getY(), result
                            .getBlockPos().getZ());
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

                mat.pushPose();

                final Matrix4f positionMatrix = mat.last().pose();

                final IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
                final IVertexBuilder builder = buffer.getBuffer(RenderType.LINES);
                for (final Pair<Vector3f, Vector3f> line : lines)
                    ClientProxy.line(builder, positionMatrix, line.getLeft(), line.getRight(), 1, 0, 0, 1f);
                mat.popPose();
            }
        }
    }
}
