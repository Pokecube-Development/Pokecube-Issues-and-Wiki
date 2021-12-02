package pokecube.core.client.render.mobs;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.client.event.RenderLivingEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.render.mobs.overlays.Evolution;
import pokecube.core.client.render.mobs.overlays.ExitCube;
import pokecube.core.client.render.mobs.overlays.Health;
import pokecube.core.client.render.mobs.overlays.Status;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import thut.core.client.render.wrappers.ModelWrapper;

public class RenderMobOverlays
{
    public static boolean enabled = true;

    public static void renderSpecial(final RenderLivingEvent.Post<Mob, EntityModel<Mob>> event)
    {
        if (!RenderMobOverlays.enabled) return;
        final Minecraft mc = Minecraft.getInstance();
        final Entity cameraEntity = mc.getCameraEntity();
        final float partialTicks = event.getPartialTick();
        if (cameraEntity == null || !event.getEntity().isAlive()) return;
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(event.getEntity());
        if (pokemob != null && event.getEntity().canUpdate())
        {
            final PoseStack mat = event.getPoseStack();
            Evolution.render(pokemob, mat, event.getMultiBufferSource(), partialTicks);
            ExitCube.render(pokemob, mat, event.getMultiBufferSource(), partialTicks);

            final MultiBufferSource buf = event.getMultiBufferSource();
            if (PokecubeCore.getConfig().doHealthBars)
            {
                int br = event.getPackedLight();
                if (PokecubeCore.getConfig().brightbars) br = OverlayTexture.pack(15, false);
                if (PokecubeCore.getConfig().renderInF1 || Minecraft.renderNames()) Health.renderHealthBar(event
                        .getEntity(), mat, buf, partialTicks, cameraEntity, br);
            }

            if (pokemob != null) if (event.getRenderer().getModel() instanceof ModelWrapper<?>) Status.render(event
                    .getRenderer(), mat, buf, pokemob, partialTicks, event.getPackedLight());
        }
    }

}
