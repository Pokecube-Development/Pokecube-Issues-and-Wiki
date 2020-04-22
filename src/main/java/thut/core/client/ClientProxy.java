package thut.core.client;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
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
    private boolean initParticles = false;

    private static long lastMouseRightClickDown = 0;
    private static long lastRightClickBlockMain = 0;
    private static long lastRightClickBlockOff  = 0;
    private static long lastRightClickItemMain  = 0;
    private static long lastRightClickItemOff   = 0;

    @SubscribeEvent
    public static void mouseFloodCtrl(final RawMouseEvent evt)
    {
        final ClientPlayerEntity player = Minecraft.getInstance().player;
        // We only handle these ingame anyway.
        if (player == null || evt.getAction() == GLFW.GLFW_RELEASE || ThutCore.conf.mouseFloodCtrl <= 0) return;
        final long time = System.currentTimeMillis();
        if (time - ClientProxy.lastMouseRightClickDown < ThutCore.conf.mouseFloodCtrl)
        {
            evt.setCanceled(true);
            return;
        }
        ClientProxy.lastMouseRightClickDown = time;
    }

    @SubscribeEvent
    public static void mouseFloodCtrl(final RightClickBlock evt)
    {
        final ClientPlayerEntity player = Minecraft.getInstance().player;
        // We only handle these ingame anyway.
        if (player == null || ThutCore.conf.mouseFloodCtrl <= 0 || evt.getPlayer() != player) return;
        final long time = System.currentTimeMillis();
        final long ref = evt.getHand() == Hand.MAIN_HAND ? ClientProxy.lastRightClickBlockMain
                : ClientProxy.lastRightClickBlockOff;
        if (time - ref < ThutCore.conf.mouseFloodCtrl)
        {
            evt.setCanceled(true);
            return;
        }
        if (evt.getHand() == Hand.MAIN_HAND) ClientProxy.lastRightClickBlockMain = time;
        else ClientProxy.lastRightClickBlockOff = time;
    }

    @SubscribeEvent
    public static void mouseFloodCtrl(final RightClickItem evt)
    {
        final ClientPlayerEntity player = Minecraft.getInstance().player;
        // We only handle these ingame anyway.
        if (player == null || ThutCore.conf.mouseFloodCtrl <= 0 || evt.getPlayer() != player) return;
        final long time = System.currentTimeMillis();
        final long ref = evt.getHand() == Hand.MAIN_HAND ? ClientProxy.lastRightClickItemMain
                : ClientProxy.lastRightClickItemOff;
        if (time - ref < ThutCore.conf.mouseFloodCtrl)
        {
            evt.setCanceled(true);
            return;
        }
        if (evt.getHand() == Hand.MAIN_HAND) ClientProxy.lastRightClickItemMain = time;
        else ClientProxy.lastRightClickItemOff = time;
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
