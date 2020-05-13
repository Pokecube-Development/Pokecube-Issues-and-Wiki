package thut.core.client.render.particle;

import org.lwjgl.opengl.GL11;

import com.mojang.brigadier.StringReader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.network.PacketBuffer;
import thut.api.maths.Vector4;

public class ParticleOrientable extends ParticleBase
{
    public Vector4 orientation;

    public ParticleOrientable(final int x, final int y)
    {
        super(x, y);
        this.billboard = false;
    }

    @Override
    protected ParticleBase read(final StringReader reader)
    {
        super.read(reader);
        return this;
    }

    @Override
    public ParticleBase read(final PacketBuffer buffer)
    {
        super.read(buffer);
        this.orientation = new Vector4(buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        return this;
    }

    @Override
    public void renderParticle(final BufferBuilder buffer, final ActiveRenderInfo entityIn, final float partialTicks,
            final float rotationX, final float rotationZ, final float rotationYZ, final float rotationXY,
            final float rotationXZ)
    {
        GL11.glPushMatrix();

        if (this.billboard)
        {
            final EntityRendererManager renderManager = Minecraft.getInstance().getRenderManager();
            GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        }
        this.setColour();

        final float alpha = (this.rgba >> 24 & 255) / 255f;
        final float red = (this.rgba >> 16 & 255) / 255f;
        final float green = (this.rgba >> 8 & 255) / 255f;
        final float blue = (this.rgba & 255) / 255f;

        final int num = this.getDuration() / this.animSpeed % this.tex.length;
        final int u = this.tex[num][0], v = this.tex[num][1];
        final double u1 = u * 1d / 16d, v1 = v * 1d / 16d;
        final double u2 = (u + 1) * 1d / 16d, v2 = (v + 1) * 1d / 16d;
        Minecraft.getInstance().getTextureManager().bindTexture(ParticleBase.TEXTUREMAP);
        final double x0 = -this.size, y0 = -this.size, z0 = 0;
        final double x1 = 0, y1 = this.size;

        if (this.orientation != null) this.orientation.glRotate();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        // Face 1
        buffer.pos(x0, y0, z0).tex(u1, v2).color(red, green, blue, alpha).endVertex();
        buffer.pos(x1, y0, z0).tex(u2, v2).color(red, green, blue, alpha).endVertex();
        buffer.pos(x1, y1, z0).tex(u2, v1).color(red, green, blue, alpha).endVertex();
        buffer.pos(x0, y1, z0).tex(u1, v1).color(red, green, blue, alpha).endVertex();
        // Face 2
        buffer.pos(x0, y0, z0).tex(u1, v2).color(red, green, blue, alpha).endVertex();
        buffer.pos(x0, y1, z0).tex(u1, v1).color(red, green, blue, alpha).endVertex();
        buffer.pos(x1, y1, z0).tex(u2, v1).color(red, green, blue, alpha).endVertex();
        buffer.pos(x1, y0, z0).tex(u2, v2).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().draw();
        GL11.glPopMatrix();
    }

    public ParticleOrientable setOrientation(final Vector4 orientation)
    {
        this.orientation = orientation;
        return this;
    }

    @Override
    public void write(final PacketBuffer buffer)
    {
        super.write(buffer);
        buffer.writeFloat(this.orientation.x);
        buffer.writeFloat(this.orientation.y);
        buffer.writeFloat(this.orientation.z);
        buffer.writeFloat(this.orientation.w);
    }

}
