package pokecube.core.client.render.mobs;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import pokecube.core.client.render.mobs.overlays.Evolution;
import pokecube.core.client.render.mobs.overlays.ExitCube;
import pokecube.core.client.render.mobs.overlays.Health;
import pokecube.core.client.render.mobs.overlays.Status;
import pokecube.core.entity.pokemobs.EntityPokemob;

public class RenderMobOverlays
{
    public static boolean enabled = true;

    public static void renderPost(RenderLivingEvent.Post<Mob, EntityModel<Mob>> event)
    {
        if (!RenderMobOverlays.enabled) return;
        Minecraft mc = Minecraft.getInstance();
        Entity cameraEntity = mc.getCameraEntity();
        float partialTicks = event.getPartialTick();
        if (cameraEntity == null) return;
        final IPokemob pokemob = PokemobCaps.getPokemobFor(event.getEntity());
        if (pokemob != null) // TODO && event.getEntity().canUpdate() what was this for?
        {
            final PoseStack mat = event.getPoseStack();
            Evolution.render(pokemob, mat, event.getMultiBufferSource(), partialTicks);
            ExitCube.render(pokemob, mat, event.getMultiBufferSource(), partialTicks);
            if (pokemob != null) Status.render(event, pokemob);
        }
    }

    public static void renderNameplate(final RenderNameTagEvent event)
    {
        if (!RenderMobOverlays.enabled) return;
        if (event.getEntity() instanceof LivingEntity living && event.getPartialTick() >= 0)
        {
            if (PokecubeCore.getConfig().doHealthBars)
            {
                MultiBufferSource buf = event.getMultiBufferSource();
                PoseStack mat = event.getPoseStack();
                Minecraft mc = Minecraft.getInstance();
                Entity cameraEntity = mc.getCameraEntity();
                float partialTick = event.getPartialTick();
                int br = event.getPackedLight();
                if (PokecubeCore.getConfig().brightbars) br = OverlayTexture.pack(15, false);
                if (PokecubeCore.getConfig().renderInF1 || Minecraft.renderNames())
                {
                    Health.renderHealthBar(living, mat, buf, partialTick, cameraEntity, br);
                    if (event.getEntity() instanceof EntityPokemob) event.setCanRender(TriState.FALSE);
                }
            }
        }
    }
}
