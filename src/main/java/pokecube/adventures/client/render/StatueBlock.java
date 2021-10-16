package pokecube.adventures.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.LivingEntity;
import pokecube.adventures.blocks.statue.StatueEntity;
import thut.api.entity.CopyCaps;
import thut.api.entity.ICopyMob;

public class StatueBlock extends TileEntityRenderer<StatueEntity>
{
    public StatueBlock(final TileEntityRendererDispatcher dispatcher)
    {
        super(dispatcher);
    }

    @Override
    public void render(final StatueEntity tile, final float partialTicks, final MatrixStack matrixStackIn,
            final IRenderTypeBuffer bufferIn, final int combinedLightIn, final int combinedOverlayIn)
    {

        final ICopyMob copy = CopyCaps.get(tile);
        if (copy == null || copy.getCopiedMob() == null) return;
        final LivingEntity copied = copy.getCopiedMob();
        final Minecraft mc = Minecraft.getInstance();
        tile.checkMob();
        mc.getEntityRenderDispatcher().setRenderShadow(false);
        mc.getEntityRenderDispatcher().render(copied, 0.5f, 0, 0.5f, partialTicks, 0, matrixStackIn, bufferIn,
                combinedLightIn);
    }
}
