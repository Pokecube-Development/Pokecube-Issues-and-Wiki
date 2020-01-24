package thut.bling.client.render;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thut.bling.ThutBling;
import thut.core.client.render.model.IModel;

public class Eye
{

    public static void renderEye(final LivingEntity wearer, final ItemStack stack, final IModel model,
            final ResourceLocation[] textures, final int brightness)
    {
        // TODO eye by model instead of texture.
        GlStateManager.pushMatrix();
        Minecraft.getInstance().textureManager.bindTexture(new ResourceLocation(ThutBling.MODID,
                "textures/items/eye.png"));
        GL11.glTranslated(-0.26, -0.175, -0.251);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder vertexbuffer = tessellator.getBuffer();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        final double height = 0.5;
        final double width = 0.5;
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        vertexbuffer.pos(0.0D, height, 0.0D).tex(0.0D, 1).color(255, 255, 255, 255).endVertex();
        vertexbuffer.pos(width, height, 0.0D).tex(1, 1).color(255, 255, 255, 255).endVertex();
        vertexbuffer.pos(width, 0.0D, 0.0D).tex(1, 0).color(255, 255, 255, 255).endVertex();
        vertexbuffer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0).color(255, 255, 255, 255).endVertex();
        tessellator.draw();
        GL11.glPopMatrix();
    }
}
