package pokecube.core.client.render.mobs;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
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
import pokecube.core.client.render.mobs.overlays.Target;
import pokecube.core.entity.pokemobs.EntityPokemob;
import thut.api.entity.multipart.IMultpart;

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
            Status.render(event, pokemob);
        }
        var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        // Render bounding boxes for sub-parts if they are not "real" ones
        if (dispatcher.shouldRenderHitBoxes() && event.getEntity() instanceof IMultpart<?, ?> multi
                && !multi.shouldSyncParts())
        {
            float dt = event.getPartialTick();
            var entity = event.getEntity();
            var parts = multi.getUseParts();
            var buffer = event.getMultiBufferSource().getBuffer(RenderType.lines());
            double d0 = -Mth.lerp(dt, entity.xOld, entity.getX());
            double d1 = -Mth.lerp(dt, entity.yOld, entity.getY());
            double d2 = -Mth.lerp(dt, entity.zOld, entity.getZ());
            var poseStack = event.getPoseStack();
            for (var p : parts)
            {
                poseStack.pushPose();
                double d3 = d0 + Mth.lerp(dt, p.xOld, p.getX());
                double d4 = d1 + Mth.lerp(dt, p.yOld, p.getY());
                double d5 = d2 + Mth.lerp(dt, p.zOld, p.getZ());
                poseStack.translate(d3, d4, d5);
                AABB aabb = p.getBoundingBox().move(-p.getX(), -p.getY(), -p.getZ());
                LevelRenderer.renderLineBox(poseStack, buffer, aabb, 1, 1, 0, 1);
                poseStack.popPose();
            }
        }
    }

    public static void renderNameplate(final RenderNameTagEvent event)
    {
        if (!RenderMobOverlays.enabled) return;
        if (event.getEntity() instanceof LivingEntity living && event.getPartialTick() >= 0)
        {
            if (PokecubeCore.getConfig().legacyHealthBars)
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
            // Otherwise we also disable this here. TODO maybe see if we need to handle name tags?
            if (PokecubeCore.getConfig().displayViewedInfo && PokecubeCore.getConfig().displayViewedArrow)
            {
                MultiBufferSource buf = event.getMultiBufferSource();
                PoseStack mat = event.getPoseStack();
                Minecraft mc = Minecraft.getInstance();
                Entity cameraEntity = mc.getCameraEntity();
                float partialTick = event.getPartialTick();
                int br = event.getPackedLight();
                // Now, add the target icon if we are the viewd thing.
                Target.renderTargetArrow(living, mat, buf, partialTick, cameraEntity, br);
                if (event.getEntity() instanceof EntityPokemob) event.setCanRender(TriState.FALSE);
            }
        }
    }
}
