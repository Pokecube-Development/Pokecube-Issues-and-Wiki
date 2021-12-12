package pokecube.adventures.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import pokecube.adventures.blocks.statue.StatueEntity;
import pokecube.core.client.render.mobs.overlays.Status.StatusTexturer;
import thut.api.entity.CopyCaps;
import thut.api.entity.ICopyMob;
import thut.core.client.render.texturing.IPartTexturer;
import thut.core.client.render.wrappers.ModelWrapper;

public class StatueBlock implements BlockEntityRenderer<StatueEntity>
{
    public StatueBlock(final BlockEntityRendererProvider.Context dispatcher)
    {}

    public static void renderStatue(LivingEntity copied, final float partialTicks, final PoseStack matrixStackIn,
            final MultiBufferSource bufferIn, final int combinedLightIn, final int combinedOverlayIn)
    {
        final Minecraft mc = Minecraft.getInstance();
        mc.getEntityRenderDispatcher().setRenderShadow(false);
        mc.getEntityRenderDispatcher().render(copied, 0.5f, 0, 0.5f, partialTicks, 1, matrixStackIn, bufferIn,
                combinedLightIn);
        CompoundTag tag = copied.getPersistentData();
        if (tag.contains("statue:over_tex")
                && mc.getEntityRenderDispatcher().getRenderer(copied) instanceof LivingEntityRenderer<?, ?> renderer)
        {
            ResourceLocation tex = new ResourceLocation(tag.getString("statue:over_tex"));
            StatusTexturer newTexer = new StatusTexturer(tex);
            newTexer.alpha = tag.contains("statue:over_tex_a") ? tag.getInt("statue:over_tex_a") : 200;
            newTexer.animated = false;
            final ModelWrapper<?> wrap = (ModelWrapper<?>) renderer.getModel();
            final IPartTexturer texer = wrap.renderer.getTexturer();
            wrap.renderer.setTexturer(newTexer);
            if (newTexer != null)
            {
                newTexer.bindObject(copied);
                wrap.getParts().forEach((n, p) -> {
                    p.applyTexture(bufferIn, tex, newTexer);
                });
            }
            mc.getEntityRenderDispatcher().render(copied, 0.5f, 0, 0.5f, partialTicks, 1, matrixStackIn, bufferIn,
                    combinedLightIn);
            wrap.renderer.setTexturer(texer);
        }
    }

    @Override
    public void render(final StatueEntity tile, final float partialTicks, final PoseStack matrixStackIn,
            final MultiBufferSource bufferIn, final int combinedLightIn, final int combinedOverlayIn)
    {

        final ICopyMob copy = CopyCaps.get(tile);
        tile.checkMob();
        if (copy == null || copy.getCopiedMob() == null || tile.ticks++ < 10) return;
        final LivingEntity copied = copy.getCopiedMob();
        renderStatue(copied, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
    }
}
