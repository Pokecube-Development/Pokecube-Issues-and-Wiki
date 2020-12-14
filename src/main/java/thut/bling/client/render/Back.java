package thut.bling.client.render;

import java.awt.Color;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelCustom;

public class Back
{
    public static void renderBack(final MatrixStack mat, final IRenderTypeBuffer buff, final LivingEntity wearer,
            final ItemStack stack, final IModel model, final ResourceLocation[] textures, final int brightness,
            final int overlay)
    {
        if (!(model instanceof IModelCustom)) return;
        if (!model.isLoaded() || !model.isValid()) return;
        final IModelCustom renderable = (IModelCustom) model;

        DyeColor ret;
        Color colour;
        final ResourceLocation[] tex = textures.clone();
        float s;
        mat.push();
        s = 0.65f;
        mat.scale(s, -s, -s);
        mat.rotate(Vector3f.XP.rotationDegrees(90));
        mat.rotate(Vector3f.YP.rotationDegrees(180));
        mat.translate(0, -.18, -0.85);
        for (final IExtendedModelPart part1 : model.getParts().values())
            part1.setRGBABrO(255, 255, 255, 255, brightness, overlay);
        final IVertexBuilder buf0 = Util.makeBuilder(buff, tex[0]);
        renderable.renderAll(mat, buf0);
        mat.pop();
        mat.push();
        mat.scale(s, -s, -s);
        ret = DyeColor.RED;
        if (stack.hasTag() && stack.getTag().contains("dyeColour"))
        {
            final int damage = stack.getTag().getInt("dyeColour");
            ret = DyeColor.byId(damage);
        }
        colour = new Color(ret.getColorValue() + 0xFF000000);
        for (final IExtendedModelPart part1 : model.getParts().values())
            part1.setRGBABrO(colour.getRed(), colour.getGreen(), colour.getBlue(), 255, brightness, overlay);
        mat.rotate(Vector3f.XP.rotationDegrees(90));
        mat.rotate(Vector3f.YP.rotationDegrees(180));
        mat.translate(0, -.18, -0.85);
        final IVertexBuilder buf1 = Util.makeBuilder(buff, tex[1]);
        renderable.renderAll(mat, buf1);
        mat.pop();
    }
}
