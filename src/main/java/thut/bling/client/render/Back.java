package thut.bling.client.render;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelCustom;

public class Back
{
    public static void renderBack(final LivingEntity wearer, final ItemStack stack, final IModel model,
            final ResourceLocation[] textures, final int brightness)
    {
        if (!(model instanceof IModelCustom)) return;
        final IModelCustom renderable = (IModelCustom) model;

        DyeColor ret;
        Color colour;
        int[] col;

        final ResourceLocation[] tex = textures.clone();
        final Minecraft minecraft = Minecraft.getInstance();
        float s;
        GlStateManager.pushMatrix();
        s = 0.65f;
        GL11.glScaled(s, -s, -s);
        minecraft.textureManager.bindTexture(tex[0]);
        GlStateManager.rotatef(90, 1, 0, 0);
        GlStateManager.rotatef(180, 0, 1, 0);
        GlStateManager.translated(0, -.18, -0.85);
        col = new int[] { 255, 255, 255, 255, brightness };
        for (final IExtendedModelPart part1 : model.getParts().values())
            part1.setRGBAB(col);
        renderable.renderAll();
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GL11.glScaled(s, -s, -s);
        minecraft.textureManager.bindTexture(tex[1]);
        ret = DyeColor.RED;
        if (stack.hasTag() && stack.getTag().contains("dyeColour"))
        {
            final int damage = stack.getTag().getInt("dyeColour");
            ret = DyeColor.byId(damage);
        }
        colour = new Color(ret.getColorValue() + 0xFF000000);
        col[0] = colour.getRed();
        col[1] = colour.getGreen();
        col[2] = colour.getBlue();
        for (final IExtendedModelPart part1 : model.getParts().values())
            part1.setRGBAB(col);
        GlStateManager.rotatef(90, 1, 0, 0);
        GlStateManager.rotatef(180, 0, 1, 0);
        GlStateManager.translated(0, -.18, -0.85);
        renderable.renderAll();
        GL11.glColor3f(1, 1, 1);
        GlStateManager.popMatrix();
    }
}
