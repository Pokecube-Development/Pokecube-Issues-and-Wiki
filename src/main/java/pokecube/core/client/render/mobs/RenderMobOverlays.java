package pokecube.core.client.render.mobs;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import pokecube.api.entity.SharedAttributes;
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

    public static void renderPost(final RenderLivingEvent.Post<Mob, EntityModel<Mob>> event)
    {
        if (!RenderMobOverlays.enabled) return;
        Minecraft mc = Minecraft.getInstance();
        Entity cameraEntity = mc.getCameraEntity();
        float partialTicks = event.getPartialTick();
        if (cameraEntity == null || !event.getEntity().isAlive()) return;
        final IPokemob pokemob = PokemobCaps.getPokemobFor(event.getEntity());
        if (pokemob != null && event.getEntity().canUpdate())
        {
            final PoseStack mat = event.getPoseStack();
            Evolution.render(pokemob, mat, event.getMultiBufferSource(), partialTicks);
            ExitCube.render(pokemob, mat, event.getMultiBufferSource(), partialTicks);
            final MultiBufferSource buf = event.getMultiBufferSource();
            if (pokemob != null)
                Status.render(event.getRenderer(), mat, buf, pokemob, partialTicks, event.getPackedLight());
        }
    }

    public static void renderPre(final RenderLivingEvent.Pre<Mob, EntityModel<Mob>> event)
    {
        float scale = (float) SharedAttributes.getScale(event.getEntity());
        if (scale != 1) event.getPoseStack().scale(scale, scale, scale);
    }

    public static void renderNameplate(final RenderNameplateEvent event)
    {
        if (event.getEntity() instanceof LivingEntity living && event.getPartialTick() >= 0)
        {
            float scale = 1 / (float) SharedAttributes.getScale(living);
            if (scale != 1) event.getPoseStack().scale(scale, scale, scale);

            if (PokecubeCore.getConfig().doHealthBars)
            {
                MultiBufferSource buf = event.getMultiBufferSource();
                PoseStack mat = event.getPoseStack();
                Minecraft mc = Minecraft.getInstance();
                Entity cameraEntity = mc.getCameraEntity();
                float partialTicks = event.getPartialTick();
                int br = event.getPackedLight();
                if (PokecubeCore.getConfig().brightbars) br = OverlayTexture.pack(15, false);
                if (PokecubeCore.getConfig().renderInF1 || Minecraft.renderNames())
                {
                    Health.renderHealthBar(living, mat, buf, partialTicks, cameraEntity, br);
                    if (event.getEntity() instanceof EntityPokemob)
                        event.setResult(Result.DENY);
                }
            }
        }
    }
}
