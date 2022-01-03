package thut.core.init;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import thut.api.entity.CopyCaps;
import thut.api.entity.ICopyMob;
import thut.api.inventory.npc.NpcContainer;
import thut.api.maths.Vector3;
import thut.api.particle.ThutParticles;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.BiomeType;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.core.client.gui.NpcScreen;
import thut.core.client.render.particle.ParticleFactories;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientInit
{
    public static void line(final VertexConsumer builder, final Matrix4f positionMatrix, final float dx1,
            final float dy1, final float dz1, final float dx2, final float dy2, final float dz2, final float r,
            final float g, final float b, final float a)
    {
        builder.vertex(positionMatrix, dx1, dy1, dz1).color(r, g, b, a).normal(0, 1, 0).endVertex();
        builder.vertex(positionMatrix, dx2, dy2, dz2).color(r, g, b, a).normal(0, 1, 0).endVertex();
    }

    public static void line(final VertexConsumer builder, final Matrix4f positionMatrix, final Vector3f start,
            final Vector3f end, final float r, final float g, final float b, final float a)
    {
        ClientInit.line(builder, positionMatrix, start.x(), start.y(), start.z(), end.x(), end.y(), end.z(), r, g, b,
                a);
    }

    private static boolean initParticles = false;

    @SubscribeEvent
    public static void startup(final Load event)
    {
        if (ClientInit.initParticles) return;
        ClientInit.initParticles = true;
        Minecraft.getInstance().particleEngine.register(ThutParticles.AURORA, ParticleFactories.GENERICFACTORY);
        Minecraft.getInstance().particleEngine.register(ThutParticles.MISC, ParticleFactories.GENERICFACTORY);
        Minecraft.getInstance().particleEngine.register(ThutParticles.STRING, ParticleFactories.GENERICFACTORY);
        Minecraft.getInstance().particleEngine.register(ThutParticles.LEAF, ParticleFactories.GENERICFACTORY);
        Minecraft.getInstance().particleEngine.register(ThutParticles.POWDER, ParticleFactories.GENERICFACTORY);
    }

    @SubscribeEvent
    public static void textOverlay(final RenderGameOverlayEvent.Text event)
    {
        final boolean debug = Minecraft.getInstance().options.renderDebug;
        if (!debug) return;
        final TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(Minecraft.getInstance().player);
        final Vector3 v = Vector3.getNewVector().set(Minecraft.getInstance().player);
        final BiomeType type = t.getBiome(v);
        final String msg = "Sub-Biome: " + I18n.get(type.readableName) + " (" + type.name + ")";
        if (event.getLeft().contains(msg)) return;
        event.getLeft().add("");
        event.getLeft().add(msg);
        
        if (Screen.hasAltDown())
        {
            event.getLeft().add("");
            final Biome b = v.getBiome(Minecraft.getInstance().level);
            final ResourceKey<Biome> key = BiomeDatabase.getKey(b);
            event.getLeft().add(key.location() + ": " + BiomeDictionary.getTypes(key) + ", " + b.getBiomeCategory());
        }
    }

    static BiomeType getSubbiome(final ItemStack held)
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
    public static void renderMob(final RenderLivingEvent.Pre<?, ?> event)
    {
        final LivingEntity living = event.getEntity();
        final ICopyMob copied = CopyCaps.get(living);
        if (copied != null && copied.getCopiedMob() != null)
        {
            final LivingEntity entity = copied.getCopiedMob();
            final boolean backup = event.getRenderer().entityRenderDispatcher.camera.isInitialized();
            event.getRenderer().entityRenderDispatcher.setRenderShadow(false);
            event.getRenderer().entityRenderDispatcher.render(entity, 0, 0, 0, 0, event.getPartialTick(), event
                    .getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
            event.getRenderer().entityRenderDispatcher.setRenderShadow(backup);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void renderHand(final RenderHandEvent event)
    {
        final Player player = Minecraft.getInstance().player;
        final ICopyMob copied = CopyCaps.get(player);
        if (copied != null && copied.getCopiedMob() != null) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void setupClient(final FMLClientSetupEvent event)
    {
        MenuScreens.register(NpcContainer.TYPE, NpcScreen::new);
    }
    
    @SubscribeEvent
    public static void RenderBounds(final RenderLevelLastEvent event)
    {
        ItemStack held;
        final Player player = Minecraft.getInstance().player;
        if (!(held = player.getMainHandItem()).isEmpty() || !(held = player.getOffhandItem()).isEmpty())
        {
            if (ClientInit.getSubbiome(held) == null) return;
            if (held.getTag() != null && held.getTag().contains("min"))
            {
                final Minecraft mc = Minecraft.getInstance();
                final Vec3 projectedView = mc.gameRenderer.getMainCamera().getPosition();
                Vec3 pointed = new Vec3(projectedView.x, projectedView.y, projectedView.z).add(mc.player.getViewVector(
                        event.getPartialTick()));
                if (mc.hitResult != null && mc.hitResult.getType() == Type.BLOCK)
                {
                    final BlockHitResult result = (BlockHitResult) mc.hitResult;
                    pointed = new Vec3(result.getBlockPos().getX(), result.getBlockPos().getY(), result.getBlockPos()
                            .getZ());
                    //
                }
                final Vector3 v = Vector3.readFromNBT(held.getTag().getCompound("min"), "");

                final AABB one = new AABB(v.getPos());
                final AABB two = new AABB(new BlockPos(pointed));

                final double minX = Math.min(one.minX, two.minX);
                final double minY = Math.min(one.minY, two.minY);
                final double minZ = Math.min(one.minZ, two.minZ);
                final double maxX = Math.max(one.maxX, two.maxX);
                final double maxY = Math.max(one.maxY, two.maxY);
                final double maxZ = Math.max(one.maxZ, two.maxZ);

                final PoseStack mat = event.getPoseStack();
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
                lines.add(Pair.of(new Vector3f((float) maxX, (float) minY, (float) maxZ),
                        new Vector3f((float) maxX, (float) maxY, (float) maxZ)));

                mat.pushPose();

                final Matrix4f positionMatrix = mat.last().pose();

                final MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
                final VertexConsumer builder = buffer.getBuffer(RenderType.LINES);
                for (final Pair<Vector3f, Vector3f> line : lines)
                    ClientInit.line(builder, positionMatrix, line.getLeft(), line.getRight(), 1, 0, 0, 1f);
                mat.popPose();
            }
        }
    }
}
