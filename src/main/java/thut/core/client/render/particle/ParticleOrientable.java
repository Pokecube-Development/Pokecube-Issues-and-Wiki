package thut.core.client.render.particle;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.network.PacketBuffer;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;

public class ParticleOrientable extends ParticleBase
{
    Vector4 orientation;

    public ParticleOrientable(final int x, final int y)
    {
        super(x, y);
        this.billboard = false;
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
            GL11.glRotatef(-renderManager.playerViewY - 45, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        }
        this.setColour();

        final float alpha = (this.rgba >> 24 & 255) / 255f;
        final float red = (this.rgba >> 16 & 255) / 255f;
        final float green = (this.rgba >> 8 & 255) / 255f;
        final float blue = (this.rgba & 255) / 255f;

        // Tessellator.getInstance().getBuffer();
        // ResourceLocation texture;
        // texture = ParticleBase.TEXTUREMAP;
        // Minecraft.getInstance().getTextureManager().bindTexture(texture);
        final int num = this.getDuration() / this.animSpeed % this.tex.length;

        final int u = this.tex[num][0], v = this.tex[num][1];

        final Vector3 temp = Vector3.getNewVector();

        final double factor = this.lifetime - this.getDuration() + partialTicks;
        temp.set(this.velocity).scalarMultBy(factor);
        if (this.getDuration() > 149)
        {
            System.out.println(temp + " " + this.velocity.scalarMult(-this.lifetime));
            System.out.println(this.lifetime + " " + this.getDuration());
        }
        GlStateManager.translated(temp.x, temp.y, temp.z);

        if (this.orientation != null) this.orientation.glRotate();

        final double u1 = u * 1d / 16d, v1 = v * 1d / 16d;
        final double u2 = (u + 1) * 1d / 16d, v2 = (v + 1) * 1d / 16d;
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

        buffer.pos(0.0 - this.size, 0.0 - this.size, 0.0).tex(u1, v2).color(red, green, blue, alpha).endVertex();
        buffer.pos(0.0, 0.0 - this.size, 0.0).tex(u2, v2).color(red, green, blue, alpha).endVertex();
        buffer.pos(0.0, 0.0 + this.size, 0.0).tex(u2, v1).color(red, green, blue, alpha).endVertex();
        buffer.pos(0.0 - this.size, 0.0 + this.size, 0.0).tex(u1, v1).color(red, green, blue, alpha).endVertex();
        // Face 2
        buffer.pos(0.0 - this.size, 0.0 - this.size, 0.0).tex(u1, v2).color(red, green, blue, alpha).endVertex();
        buffer.pos(0.0 - this.size, 0.0 + this.size, 0.0).tex(u1, v1).color(red, green, blue, alpha).endVertex();
        buffer.pos(0.0, 0.0 + this.size, 0.0).tex(u2, v1).color(red, green, blue, alpha).endVertex();
        buffer.pos(0.0, 0.0 - this.size, 0.0).tex(u2, v2).color(red, green, blue, alpha).endVertex();

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
