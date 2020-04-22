package thut.core.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraftforge.client.event.InputEvent.RawMouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import thut.api.maths.Vector3;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.core.client.render.particle.ParticleFactories;
import thut.core.client.render.particle.ThutParticles;
import thut.core.common.CommonProxy;
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

    private long lastMouseRightClickDown = 0;
    private long lastRightClickBlockMain = 0;
    private long lastRightClickBlockOff  = 0;
    private long lastRightClickItemMain  = 0;
    private long lastRightClickItemOff   = 0;

    @SubscribeEvent
    public void mouseFloodCtrl(final RawMouseEvent evt)
    {
        final ClientPlayerEntity player = Minecraft.getInstance().player;
        // We only handle these ingame anyway.
        if (player == null || evt.getAction() == GLFW.GLFW_RELEASE || ThutCore.conf.mouseFloodCtrl <= 0) return;
        final long time = System.currentTimeMillis();
        if (time - this.lastMouseRightClickDown < ThutCore.conf.mouseFloodCtrl)
        {
            evt.setCanceled(true);
            return;
        }
        this.lastMouseRightClickDown = time;
    }

    @SubscribeEvent
    public void mouseFloodCtrl(final RightClickBlock evt)
    {
        final ClientPlayerEntity player = Minecraft.getInstance().player;
        // We only handle these ingame anyway.
        if (player == null || ThutCore.conf.mouseFloodCtrl <= 0 || evt.getPlayer() != player) return;
        final long time = System.currentTimeMillis();
        final long ref = evt.getHand() == Hand.MAIN_HAND ? this.lastRightClickBlockMain : this.lastRightClickBlockOff;
        if (time - ref < ThutCore.conf.mouseFloodCtrl)
        {
            evt.setCanceled(true);
            return;
        }
        if (evt.getHand() == Hand.MAIN_HAND) this.lastRightClickBlockMain = time;
        else this.lastRightClickBlockOff = time;
    }

    @SubscribeEvent
    public void mouseFloodCtrl(final RightClickItem evt)
    {
        final ClientPlayerEntity player = Minecraft.getInstance().player;
        // We only handle these ingame anyway.
        if (player == null || ThutCore.conf.mouseFloodCtrl <= 0 || evt.getPlayer() != player) return;
        final long time = System.currentTimeMillis();
        final long ref = evt.getHand() == Hand.MAIN_HAND ? this.lastRightClickItemMain : this.lastRightClickItemOff;
        if (time - ref < ThutCore.conf.mouseFloodCtrl)
        {
            evt.setCanceled(true);
            return;
        }
        if (evt.getHand() == Hand.MAIN_HAND) this.lastRightClickItemMain = time;
        else this.lastRightClickItemOff = time;
    }

    @Override
    public PlayerEntity getPlayer()
    {
        return Minecraft.getInstance().player;
    }

    @Override
    public boolean isClientSide()
    {
        return EffectiveSide.get() == LogicalSide.CLIENT;
    }

    @Override
    public boolean isServerSide()
    {
        return EffectiveSide.get() == LogicalSide.SERVER;
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
        final String msg = "Sub-Biome: " + I18n.format(BiomeDatabase.getUnlocalizedNameFromType(num));
        event.getLeft().add("");
        event.getLeft().add(msg);
    }
}
