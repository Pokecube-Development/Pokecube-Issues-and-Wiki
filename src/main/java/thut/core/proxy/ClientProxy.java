package thut.core.proxy;

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
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import thut.api.maths.Vector3;
import thut.api.particle.ThutParticles;
import thut.api.terrain.BiomeDatabase;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.core.client.gui.ConfigGui;
import thut.core.client.render.particle.ParticleFactories;
import thut.core.common.ThutCore;

public class ClientProxy extends CommonProxy
{
    private boolean initParticles = false;

    private long lastMouseRightClickDown = 0;
    private long lastRightClickBlockMain = 0;
    private long lastRightClickBlockOff  = 0;
    private long lastRightClickItemMain  = 0;
    private long lastRightClickItemOff   = 0;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
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

        // Register config gui
        ModList.get().getModContainerById(ThutCore.MODID).ifPresent(c -> c.registerExtensionPoint(
                ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, parent) -> new ConfigGui(ThutCore.conf, parent)));
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
