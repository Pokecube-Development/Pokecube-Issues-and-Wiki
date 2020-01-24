package thut.bling.client.render;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelCustom;

public class Neck
{

    public static void renderNeck(final LivingEntity wearer, final ItemStack stack, final IModel model,
            final ResourceLocation[] textures, final int brightness)
    {
        if (!(model instanceof IModelCustom)) return;
        final ResourceLocation[] tex = textures.clone();
        final IModelCustom renderable = (IModelCustom) model;
        DyeColor ret;
        Color colour;
        int[] col;
        float s, dx, dy, dz;
        dx = 0;
        dy = -.0f;
        dz = -0.03f;
        s = 0.525f;
        if (wearer.getItemStackFromSlot(EquipmentSlotType.LEGS) == null) s = 0.465f;
        if (stack.hasTag() && stack.getTag().contains("gem")) tex[0] = new ResourceLocation(stack.getTag().getString(
                "gem"));
        else tex[0] = null;
        GL11.glPushMatrix();
        GL11.glRotated(90, 1, 0, 0);
        GL11.glRotated(180, 0, 0, 1);
        GL11.glTranslatef(dx, dy, dz);
        GL11.glScalef(s, s, s);
        final String colorpart = "main";
        final String itempart = "gem";
        ret = DyeColor.YELLOW;
        if (stack.hasTag() && stack.getTag().contains("dyeColour"))
        {
            final int damage = stack.getTag().getInt("dyeColour");
            ret = DyeColor.byId(damage);
        }
        colour = new Color(ret.getColorValue() + 0xFF000000);
        col = new int[] { colour.getRed(), colour.getGreen(), colour.getBlue(), 255, brightness };
        IExtendedModelPart part = model.getParts().get(colorpart);
        if (part != null)
        {
            part.setRGBAB(col);
            Minecraft.getInstance().textureManager.bindTexture(tex[1]);
            GlStateManager.scaled(1, 1, .1);
            renderable.renderPart(colorpart);
        }
        GL11.glColor3f(1, 1, 1);
        part = model.getParts().get(itempart);
        if (part != null && tex[0] != null)
        {
            Minecraft.getInstance().textureManager.bindTexture(tex[0]);
            GlStateManager.scaled(1, 1, 10);
            GlStateManager.translated(0, 0.01, -0.075);
            renderable.renderPart(itempart);
        }
        GL11.glPopMatrix();
    }

}
