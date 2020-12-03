package pokecube.core.client.gui.watch;

import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.client.gui.watch.util.WatchPage;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;

public class SecretBaseRadarPage extends WatchPage
{
    public static Set<Vector4>   bases         = Sets.newHashSet();
    public static Vector4        closestMeteor = null;
    public static float          baseRange     = 64;
    private static boolean       meteors       = false;
    int                          button        = -1;
    private final ITextComponent meteorTitle;
    private final ITextComponent baseTitle;

    public SecretBaseRadarPage(final GuiPokeWatch watch)
    {
        super(new TranslationTextComponent(""), watch);
        this.meteorTitle = new TranslationTextComponent("pokewatch.title.meteorradar");
        this.baseTitle = new TranslationTextComponent("pokewatch.title.baseradar");
    }

    @Override
    public ITextComponent getTitle()
    {
        return SecretBaseRadarPage.meteors ? this.meteorTitle : this.baseTitle;
    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
        final int x = this.watch.width / 2;
        final int y = this.watch.height / 2 - 5;
        this.addButton(new Button(x + 64, y - 70, 12, 12, new StringTextComponent(""),
                b -> SecretBaseRadarPage.meteors = !SecretBaseRadarPage.meteors));
    }

    @Override
    public void render(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {

        GL11.glPushMatrix();
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 80;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 8;

        GL11.glTranslated(x, y + 72, 0);
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
        vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos(xCoord + minU, yCoord + maxV, zCoord).color(r, g, b, a).endVertex();
        vertexbuffer.pos(xCoord + maxU, yCoord + maxV, zCoord).color(r, g, b, a).endVertex();
        vertexbuffer.pos(xCoord + maxU, yCoord + minV, zCoord).color(r, g, b, a).endVertex();
        vertexbuffer.pos(xCoord + minU, yCoord + minV, zCoord).color(r, g, b, a).endVertex();
        tessellator.draw();
        r = 1;
        g = 0;
        final Vector3 here = Vector3.getNewVector().set(this.watch.player);
        final double angle = -this.watch.player.rotationYaw % 360 + 180;
        GL11.glRotated(angle, 0, 0, 1);
        if (!SecretBaseRadarPage.meteors) for (final Vector4 c : SecretBaseRadarPage.bases)
        {
            final Vector3 loc = Vector3.getNewVector().set(c.x, c.y, c.z);
            GL11.glPushMatrix();
            final Vector3 v = here.subtract(loc);
            v.reverse();
            final double max = 30;
            final double hDistSq = v.x * v.x + v.z * v.z;
            final float vDist = (float) Math.abs(v.y);
            v.set(v.normalize());
            a = (64 - vDist) / SecretBaseRadarPage.baseRange;
            a = Math.min(a, 1);
            a = Math.max(a, 0.125f);
            final double dist = max * Math.sqrt(hDistSq) / SecretBaseRadarPage.baseRange;
            v.scalarMultBy(dist);
            GL11.glTranslated(v.x, v.z, 0);
            xCoord = v.x;
            yCoord = v.y;
            vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            vertexbuffer.pos(xCoord + minU, yCoord + maxV, zCoord).color(r, g, b, a).endVertex();
            vertexbuffer.pos(xCoord + maxU, yCoord + maxV, zCoord).color(r, g, b, a).endVertex();
            vertexbuffer.pos(xCoord + maxU, yCoord + minV, zCoord).color(r, g, b, a).endVertex();
            vertexbuffer.pos(xCoord + minU, yCoord + minV, zCoord).color(r, g, b, a).endVertex();
            tessellator.draw();
            GL11.glPopMatrix();
        }
        g = 1;
        if (SecretBaseRadarPage.meteors && SecretBaseRadarPage.closestMeteor != null)
        {
            final Vector3 loc = Vector3.getNewVector().set(SecretBaseRadarPage.closestMeteor.x + 0.5,
                    SecretBaseRadarPage.closestMeteor.y + 0.5, SecretBaseRadarPage.closestMeteor.z + 0.5);
            GL11.glPushMatrix();
            final Vector3 v = here.subtract(loc);
            v.reverse();
            final double max = 30;
            final double hDistSq = v.x * v.x + v.z * v.z;
            v.y = 0;
            v.set(v.normalize());
            a = 1;
            double dist = hDistSq / (SecretBaseRadarPage.baseRange * SecretBaseRadarPage.baseRange);
            dist = Math.min(dist, max);
            v.scalarMultBy(dist);
            GL11.glTranslated(v.x, v.z, 0);
            xCoord = v.x;
            yCoord = v.y;
            vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            vertexbuffer.pos(xCoord + minU, yCoord + maxV, zCoord).color(r, g, b, a).endVertex();
            vertexbuffer.pos(xCoord + maxU, yCoord + maxV, zCoord).color(r, g, b, a).endVertex();
            vertexbuffer.pos(xCoord + maxU, yCoord + minV, zCoord).color(r, g, b, a).endVertex();
            vertexbuffer.pos(xCoord + minU, yCoord + minV, zCoord).color(r, g, b, a).endVertex();
            tessellator.draw();
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
        GlStateManager.enableTexture();
        AbstractGui.drawCenteredString(mat, this.font, this.getTitle().getString(), x, y, 0x78C850);
        super.render(mat, mouseX, mouseY, partialTicks);
    }
}
