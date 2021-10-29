package pokecube.adventures.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.entity.LivingEntity;
import pokecube.adventures.blocks.statue.StatueEntity;
import thut.api.entity.CopyCaps;
import thut.api.entity.ICopyMob;

public class StatueBlock implements BlockEntityRenderer<StatueEntity>
{
    public StatueBlock(final BlockEntityRendererProvider.Context dispatcher)
    {
    }

    @Override
    public void render(final StatueEntity tile, final float partialTicks, final PoseStack matrixStackIn,
            final MultiBufferSource bufferIn, final int combinedLightIn, final int combinedOverlayIn)
    {

        final ICopyMob copy = CopyCaps.get(tile);
        tile.checkMob();
        if (copy == null || copy.getCopiedMob() == null || tile.ticks++ < 10) return;
        final LivingEntity copied = copy.getCopiedMob();
        final Minecraft mc = Minecraft.getInstance();
        mc.getEntityRenderDispatcher().setRenderShadow(false);
        mc.getEntityRenderDispatcher().render(copied, 0.5f, 0, 0.5f, partialTicks, 1, matrixStackIn, bufferIn,
                combinedLightIn);
    }
}
