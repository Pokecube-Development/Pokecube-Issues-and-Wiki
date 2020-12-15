package pokecube.core.client.gui.watch;

import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.client.gui.helper.TexButton;
import pokecube.core.client.gui.helper.TexButton.UVImgRender;
import pokecube.core.client.gui.watch.util.WatchPage;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class SecretBaseRadarPage extends WatchPage
{
    public static final ResourceLocation TEX_DM = new ResourceLocation(PokecubeMod.ID,
            "textures/gui/pokewatchgui_meteor.png");
    public static final ResourceLocation TEX_NM = new ResourceLocation(PokecubeMod.ID,
            "textures/gui/pokewatchgui_meteor_nm.png");

    public static Set<BlockPos> bases   = Sets.newHashSet();
    public static Set<BlockPos> meteors = Sets.newHashSet();

    public static float    baseRange  = 64;
    private static boolean meteorMode = false;

    private final ITextComponent meteorTitle;
    private final ITextComponent baseTitle;

    public SecretBaseRadarPage(final GuiPokeWatch watch)
    {
        super(new TranslationTextComponent(""), watch, SecretBaseRadarPage.TEX_DM, SecretBaseRadarPage.TEX_NM);
        this.meteorTitle = new TranslationTextComponent("pokewatch.title.meteorradar");
        this.baseTitle = new TranslationTextComponent("pokewatch.title.baseradar");
    }

    @Override
    public ITextComponent getTitle()
    {
        return SecretBaseRadarPage.meteorMode ? this.meteorTitle : this.baseTitle;
    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
        final int x = this.watch.width / 2;
        final int y = this.watch.height / 2 - 5;
        this.addButton(new TexButton(x + 95, y - 70, 12, 12, new StringTextComponent(""),
                b -> SecretBaseRadarPage.meteorMode = !SecretBaseRadarPage.meteorMode).setTex(GuiPokeWatch
                        .getWidgetTex()).setRender(new UVImgRender(200, 0, 12, 12)));
    }

    @Override
    public void render(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        GL11.glPushMatrix();
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2;

        GL11.glTranslated(x + 126, y + 72, 0);
        double xCoord = 0;
        double yCoord = 0;
        final float zCoord = this.getBlitOffset();
        final float maxU = 1;
        final float maxV = 1;
        final float minU = -1;
        final float minV = -1;
        float r = 0;
        float g = 1;
        final float b = 0;
        float a = 1;
        GlStateManager.disableTexture();
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder vertexbuffer = tessellator.getBuffer();
        r = 1;
        g = SecretBaseRadarPage.meteorMode ? 1 : 0;
        final Vector3 here = Vector3.getNewVector().set(this.watch.player);
        final float angle = -this.watch.player.rotationYaw % 360 + 180;
        GL11.glRotated(angle, 0, 0, 1);

        final Set<BlockPos> coords = SecretBaseRadarPage.meteorMode ? SecretBaseRadarPage.meteors
                : SecretBaseRadarPage.bases;
        final float range = SecretBaseRadarPage.meteorMode ? SecretBaseRadarPage.baseRange * 10
                : SecretBaseRadarPage.baseRange;

        for (final BlockPos c : coords)
        {
            final Vector3 loc = Vector3.getNewVector().set(c);
            GL11.glPushMatrix();
            final Vector3 v = loc.subtract(here);
            final double max = 55;
            final double hDistSq = v.x * v.x + v.z * v.z;
            final float vDist = (float) Math.abs(v.y);
            v.y = 0;
            v.norm();
            a = (64 - vDist) / 64;
            a = Math.min(a, 1);
            a = Math.max(a, 0.125f);
            if (SecretBaseRadarPage.meteorMode) a = 1;
            double dist = max * Math.sqrt(hDistSq) / range;
            dist = Math.min(dist, max);
            v.scalarMultBy(dist);

            xCoord = v.x;
            yCoord = v.z;

            vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            vertexbuffer.pos(xCoord + minU, yCoord + maxV, zCoord).color(r, g, b, a).endVertex();
            vertexbuffer.pos(xCoord + maxU, yCoord + maxV, zCoord).color(r, g, b, a).endVertex();
            vertexbuffer.pos(xCoord + maxU, yCoord + minV, zCoord).color(r, g, b, a).endVertex();
            vertexbuffer.pos(xCoord + minU, yCoord + minV, zCoord).color(r, g, b, a).endVertex();
            tessellator.draw();
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
        AbstractGui.drawCenteredString(mat, this.font, this.getTitle().getString(), x + 128, y + 8, 0x78C850);

        super.render(mat, mouseX, mouseY, partialTicks);
    }
}
