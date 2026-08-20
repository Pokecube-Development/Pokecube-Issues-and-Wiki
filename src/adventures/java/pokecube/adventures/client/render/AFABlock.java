package pokecube.adventures.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import pokecube.adventures.blocks.afa.AfaTile;
import pokecube.adventures.blocks.statue.StatueEntity;
import pokecube.core.client.gui.pokemob.GuiPokemobHelper;
import thut.lib.AxisAngles;

public class AFABlock implements BlockEntityRenderer<AfaTile>
{
    public AFABlock(final BlockEntityRendererProvider.Context dispatcher)
    {}

    @Override
    public void render(AfaTile tile, float partialTicks, PoseStack matrixStackIn,
            MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        if (tile.pokemob == null) return;
        var info = StatueEntity.unpackStatue(tile);
        info.copy().setCopiedMob(tile.pokemob.getEntity());
        if (info.copy().getCopiedMob() == null || tile.ticks < 10) return;
        matrixStackIn.pushPose();
        matrixStackIn.translate(0, 1, 0);
        float size = 0.15f / GuiPokemobHelper.sizeMap.getOrDefault(tile.pokemob.getPokedexEntry(), 1.0f);
        matrixStackIn.scale(size, size, size);
        float shift = 0.5f / size;
        matrixStackIn.translate(shift, 0, shift);
        if(tile.rotates)
        {
            matrixStackIn.mulPose(AxisAngles.YN.rotationDegrees(tile.ticks + partialTicks));
        }
        matrixStackIn.translate(-shift, 0, -shift);
        combinedLightIn = LightTexture.FULL_BRIGHT;
        StatueBlock.renderStatue(info, tile.components(), matrixStackIn, bufferIn, combinedLightIn);
        matrixStackIn.popPose();
    }
}
