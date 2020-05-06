package pokecube.legends.client.render.block;

import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import pokecube.legends.tileentity.RaidSpawn;
import pokecube.legends.tileentity.RaidSpawn.BeamSegment;

public class Raid extends TileEntityRenderer<RaidSpawn>
{
    private static final ResourceLocation TEXTURE_BEACON_BEAM = new ResourceLocation("textures/entity/beacon_beam.png");

    @Override
    public void render(final RaidSpawn tileEntityIn, final double x, final double y, final double z,
            final float partialTicks, final int destroyStage)
    {
        this.func_217651_a(x, y, z, partialTicks, tileEntityIn.getBeamSegments(), tileEntityIn.getWorld()
                .getGameTime());
    }

    private void func_217651_a(final double p_217651_1_, final double p_217651_3_, final double p_217651_5_,
            final double p_217651_7_, final List<BeamSegment> p_217651_9_, final long p_217651_10_)
    {
        GlStateManager.alphaFunc(516, 0.1F);
        this.bindTexture(Raid.TEXTURE_BEACON_BEAM);
        GlStateManager.disableFog();
        int i = 0;

        for (int j = 0; j < p_217651_9_.size(); ++j)
        {
            final BeamSegment beacontileentity$beamsegment = p_217651_9_.get(j);
            Raid.func_217652_a(p_217651_1_, p_217651_3_, p_217651_5_, p_217651_7_, p_217651_10_, i, j == p_217651_9_
                    .size() - 1 ? 1024 : beacontileentity$beamsegment.getHeight(), beacontileentity$beamsegment
                            .getColors());
            i += beacontileentity$beamsegment.getHeight();
        }

        GlStateManager.enableFog();
    }

    private static void func_217652_a(final double p_217652_0_, final double p_217652_2_, final double p_217652_4_,
            final double p_217652_6_, final long p_217652_8_, final int p_217652_10_, final int p_217652_11_,
            final float[] p_217652_12_)
    {
        Raid.renderBeamSegment(p_217652_0_, p_217652_2_, p_217652_4_, p_217652_6_, 1.0D, p_217652_8_, p_217652_10_,
                p_217652_11_, p_217652_12_, 0.2D, 0.25D);
    }

    public static void renderBeamSegment(final double x, final double y, final double z, final double partialTicks,
            final double textureScale, final long totalWorldTime, final int yOffset, final int height,
            final float[] colors, final double beamRadius, final double glowRadius)
    {
        final int i = yOffset + height;
        GlStateManager.texParameter(3553, 10242, 10497);
        GlStateManager.texParameter(3553, 10243, 10497);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.pushMatrix();
        GlStateManager.translated(x + 0.5D, y, z + 0.5D);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        final double d0 = Math.floorMod(totalWorldTime, 40L) + partialTicks;
        final double d1 = height < 0 ? d0 : -d0;
        final double d2 = MathHelper.frac(d1 * 0.2D - MathHelper.floor(d1 * 0.1D));
        final float f = colors[0];
        final float f1 = colors[1];
        final float f2 = colors[2];
        GlStateManager.pushMatrix();
        GlStateManager.rotated(d0 * 2.25D - 45.0D, 0.0D, 1.0D, 0.0D);
        double d3 = 0.0D;
        double d5 = 0.0D;
        double d6 = -beamRadius;
        final double d9 = -beamRadius;
        double d12 = -1.0D + d2;
        double d13 = height * textureScale * (0.5D / beamRadius) + d12;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(0.0D, i, beamRadius).tex(1.0D, d13).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(0.0D, yOffset, beamRadius).tex(1.0D, d12).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(beamRadius, yOffset, 0.0D).tex(0.0D, d12).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(beamRadius, i, 0.0D).tex(0.0D, d13).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(0.0D, i, d9).tex(1.0D, d13).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(0.0D, yOffset, d9).tex(1.0D, d12).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(d6, yOffset, 0.0D).tex(0.0D, d12).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(d6, i, 0.0D).tex(0.0D, d13).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(beamRadius, i, 0.0D).tex(1.0D, d13).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(beamRadius, yOffset, 0.0D).tex(1.0D, d12).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(0.0D, yOffset, d9).tex(0.0D, d12).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(0.0D, i, d9).tex(0.0D, d13).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(d6, i, 0.0D).tex(1.0D, d13).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(d6, yOffset, 0.0D).tex(1.0D, d12).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(0.0D, yOffset, beamRadius).tex(0.0D, d12).color(f, f1, f2, 1.0F).endVertex();
        bufferbuilder.pos(0.0D, i, beamRadius).tex(0.0D, d13).color(f, f1, f2, 1.0F).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.depthMask(false);
        d3 = -glowRadius;
        final double d4 = -glowRadius;
        d5 = -glowRadius;
        d6 = -glowRadius;
        d12 = -1.0D + d2;
        d13 = height * textureScale + d12;
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(d3, i, d4).tex(1.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(d3, yOffset, d4).tex(1.0D, d12).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(glowRadius, yOffset, d5).tex(0.0D, d12).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(glowRadius, i, d5).tex(0.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(glowRadius, i, glowRadius).tex(1.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(glowRadius, yOffset, glowRadius).tex(1.0D, d12).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(d6, yOffset, glowRadius).tex(0.0D, d12).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(d6, i, glowRadius).tex(0.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(glowRadius, i, d5).tex(1.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(glowRadius, yOffset, d5).tex(1.0D, d12).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(glowRadius, yOffset, glowRadius).tex(0.0D, d12).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(glowRadius, i, glowRadius).tex(0.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(d6, i, glowRadius).tex(1.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(d6, yOffset, glowRadius).tex(1.0D, d12).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(d3, yOffset, d4).tex(0.0D, d12).color(f, f1, f2, 0.125F).endVertex();
        bufferbuilder.pos(d3, i, d4).tex(0.0D, d13).color(f, f1, f2, 0.125F).endVertex();
        tessellator.draw();
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture();
        GlStateManager.depthMask(true);
    }

    public boolean isGlobalRenderer(final BeaconTileEntity te)
    {
        return true;
    }
}
