package thut.core.init;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ViewportEvent.ComputeCameraAngles;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import thut.api.entity.CopyCaps;
import thut.api.entity.ICopyMob;
import thut.api.level.structures.NamedVolumes.INamedStructure;
import thut.api.level.structures.StructureManager;
import thut.api.level.terrain.BiomeType;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;
import thut.api.maths.Vector3;
import thut.api.particle.ThutParticles;
import thut.core.client.gui.NpcScreen;
import thut.core.client.render.model.parts.Mesh;
import thut.core.client.render.particle.ParticleFactories;
import thut.core.client.render.wrappers.ModelWrapper;
import thut.core.common.ThutCore;
import thut.lib.RegHelper;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientInit
{
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = ThutCore.MODID, value = Dist.CLIENT)
    public static class ModInit
    {
        @SubscribeEvent
        public static void setupClient(final FMLClientSetupEvent event)
        {
            MenuScreens.register(RegistryObjects.NPC_MENU.get(), NpcScreen::new);
        }

        @SubscribeEvent
        public static void registerParticles(RegisterParticleProvidersEvent event)
        {
            event.registerSpecial(ThutParticles.AURORA, ParticleFactories.GENERICFACTORY);
            event.registerSpecial(ThutParticles.MISC, ParticleFactories.GENERICFACTORY);
            event.registerSpecial(ThutParticles.STRING, ParticleFactories.GENERICFACTORY);
            event.registerSpecial(ThutParticles.LEAF, ParticleFactories.GENERICFACTORY);
            event.registerSpecial(ThutParticles.POWDER, ParticleFactories.GENERICFACTORY);
        }
    }

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

    @SubscribeEvent
    public static void onRenderSetup(ComputeCameraAngles event)
    {
//        Tracker.timerEnd("render time", 5000);
        Mesh.windowScale = (float) Math.sqrt(Minecraft.getInstance().getWindow().getScreenHeight()
                * Minecraft.getInstance().getWindow().getScreenWidth() * 1e-3f);
        Mesh.verts = 0;
        Mesh.modelCullThreshold = ThutCore.getConfig().modelCullThreshold;
//        Tracker.timerStart();
    }

    @SubscribeEvent
    public static void textOverlay(final CustomizeGuiOverlayEvent.DebugText event)
    {
        final boolean debug = Minecraft.getInstance().options.renderDebug;
        if (!debug) return;
        final TerrainSegment t = TerrainManager.getInstance().getTerrainForEntity(Minecraft.getInstance().player);
        final Vector3 v = new Vector3().set(Minecraft.getInstance().player);
        final BiomeType type = t.getBiome(v);
        final String msg = "Sub-Biome: " + I18n.get(type.readableName) + " (" + type.name + ")";
        if (event.getLeft().contains(msg)) return;
        event.getLeft().add("");
        event.getLeft().add(msg);
        event.getLeft().add("");
        Level level = Minecraft.getInstance().level;

        var regi = level.registryAccess().registry(RegHelper.STRUCTURE_REGISTRY);
        Set<INamedStructure> structures = StructureManager.getNear(level.dimension(), v.getPos(), 5, true);
        if (regi.isPresent())
        {
            for (var info : structures)
            {
                Object o = info.getWrapped();
                if (o instanceof Structure feature)
                {
                    var tags = regi.get().getHolderOrThrow(regi.get().getResourceKey(feature).get()).tags().toList();
                    List<ResourceLocation> keys = Lists.newArrayList();
                    for (var tag : tags) if (!tag.toString().contains(":mixin_")) keys.add(tag.location());
                    event.getLeft().add(info.getName() + " " + keys);
                }
            }
        }
        if (Screen.hasAltDown())
        {
            event.getLeft().add("");
            Holder<Biome> holder = level.getBiome(v.getPos());
            List<TagKey<Biome>> tags = holder.getTagKeys().toList();
            List<ResourceLocation> msgs = Lists.newArrayList();
            for (var tag : tags) msgs.add(tag.location());
            for (var tag : msgs) event.getLeft().add(tag + "");
        }

    }

    static boolean isCustomStick(final ItemStack held)
    {
        return held.getHoverName().getString().toLowerCase(Locale.ROOT).contains("->");
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
            event.getRenderer().entityRenderDispatcher.render(entity, 0, 0, 0, 0, event.getPartialTick(),
                    event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
            event.getRenderer().entityRenderDispatcher.setRenderShadow(backup);
            event.setCanceled(true);
        }

        @SuppressWarnings("unchecked")
        var renderer = (LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>) event.getRenderer();
        if (renderer.getModel() instanceof ModelWrapper<LivingEntity> wrap)
        {
            var tex = renderer.getTextureLocation(living);
            wrap.setMob(living, event.getMultiBufferSource(), tex);
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
    public static void renderBounds(final RenderLevelStageEvent event)
    {
        if (event.getStage() != Stage.AFTER_SOLID_BLOCKS) return;

        ItemStack held;
        final Player player = Minecraft.getInstance().player;
        if (!(held = player.getMainHandItem()).isEmpty() || !(held = player.getOffhandItem()).isEmpty())
        {
            final Minecraft mc = Minecraft.getInstance();
            if (ClientInit.isCustomStick(held) && held.getTag() != null && held.getTag().contains("min"))
            {
                final Vec3 projectedView = mc.gameRenderer.getMainCamera().getPosition();
                Vec3 pointed = new Vec3(projectedView.x, projectedView.y, projectedView.z)
                        .add(mc.player.getViewVector(event.getPartialTick()));
                if (mc.hitResult != null && mc.hitResult.getType() == Type.BLOCK)
                {
                    final BlockHitResult result = (BlockHitResult) mc.hitResult;
                    pointed = new Vec3(result.getBlockPos().getX(), result.getBlockPos().getY(),
                            result.getBlockPos().getZ());
                    //
                }
                final Vector3 v = Vector3.readFromNBT(held.getTag().getCompound("min"), "");

                final AABB one = new AABB(v.getPos());
                final AABB two = new AABB(new BlockPos((int) pointed.x, (int) pointed.y, (int) pointed.z));

                final double minX = Math.min(one.minX, two.minX);
                final double minY = Math.min(one.minY, two.minY);
                final double minZ = Math.min(one.minZ, two.minZ);
                final double maxX = Math.max(one.maxX, two.maxX);
                final double maxY = Math.max(one.maxY, two.maxY);
                final double maxZ = Math.max(one.maxZ, two.maxZ);
                AABB box = new AABB(minX, minY, minZ, maxX, maxY, maxZ);

                final PoseStack matrix = event.getPoseStack();
                MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
                VertexConsumer builder = buffer.getBuffer(RenderType.LINES);
                Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                matrix.pushPose();
                matrix.translate(-camera.x, -camera.y, -camera.z);
                LevelRenderer.renderLineBox(matrix, builder, box, 1.0F, 0.0F, 0.0F, 1.0F);
                matrix.popPose();
                buffer.endBatch(RenderType.LINES);
            }
        }
    }
}
