package pokecube.core.client.render.mobs;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.render.mobs.overlays.Evolution;
import pokecube.core.client.render.mobs.overlays.ExitCube;
import pokecube.core.client.render.mobs.overlays.Health;
import pokecube.core.client.render.mobs.overlays.Status;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.core.client.render.model.IModelRenderer;
import thut.core.client.render.wrappers.ModelWrapper;

public class RenderMobOverlays
{
    public static boolean enabled = true;

    @SubscribeEvent
    public static void renderSpecial(@SuppressWarnings("rawtypes") final RenderLivingEvent.Pre event)
    {
        if (!RenderMobOverlays.enabled) return;
        final Minecraft mc = Minecraft.getInstance();
        if (!PokecubeCore.getConfig().renderInF1 && !Minecraft.isGuiEnabled()) return;
        final Entity cameraEntity = mc.getRenderViewEntity();
        final float partialTicks = event.getPartialRenderTick();
        if (cameraEntity == null || !event.getEntity().isAlive()) return;
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(event.getEntity());
        if (pokemob != null)
        {
            final MatrixStack mat = event.getMatrixStack();
            Evolution.render(pokemob, mat, event.getBuffers(), partialTicks);
            ExitCube.render(pokemob, mat, event.getBuffers(), partialTicks);

            final IRenderTypeBuffer buf = event.getBuffers();
            if (PokecubeCore.getConfig().doHealthBars)
                Health.renderHealthBar(event.getEntity(), mat, buf, partialTicks, cameraEntity);
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public static void renderSpecial(@SuppressWarnings("rawtypes") final RenderLivingEvent.Post event)
    {

        if (!RenderMobOverlays.enabled) return;
        final Minecraft mc = Minecraft.getInstance();
        if (!PokecubeCore.getConfig().renderInF1 && !Minecraft.isGuiEnabled()) return;
        final Entity cameraEntity = mc.getRenderViewEntity();
        final float partialTicks = event.getPartialRenderTick();
        final MatrixStack mat = event.getMatrixStack();
        final IRenderTypeBuffer buf = event.getBuffers();
        if (cameraEntity == null || !event.getEntity().isAlive()) return;

        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(event.getEntity());
        if (pokemob != null) if (event.getRenderer().getEntityModel() instanceof ModelWrapper)
        {
            final ModelWrapper<?> wrapper = (ModelWrapper<?>) event.getRenderer().getEntityModel();
            Status.render((IModelRenderer<MobEntity>) wrapper.renderer, mat, buf, pokemob.getEntity(), 0, 0, 0, 1,
                    partialTicks);
        }
    }

}
