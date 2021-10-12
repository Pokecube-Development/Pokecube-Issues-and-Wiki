package thut.bling.client.render;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelCustom;

public class Hat
{

    public static void renderHat(final PoseStack mat, final MultiBufferSource buff, final LivingEntity wearer,
            final ItemStack stack, final IModel model, final ResourceLocation[] textures, final int brightness,
            final int overlay)
    {
        if (!(model instanceof IModelCustom)) return;
        final IModelCustom renderable = (IModelCustom) model;
        if (!model.isLoaded() || !model.isValid()) return;

        DyeColor ret;
        Color colour;
        final ResourceLocation[] tex = textures.clone();
        float s;
        mat.pushPose();
        s = 0.285f;
        mat.scale(s, -s, -s);
        for (final IExtendedModelPart part1 : model.getParts().values())
            part1.setRGBABrO(255, 255, 255, 255, brightness, overlay);
        final VertexConsumer buf0 = Util.makeBuilder(buff, tex[0]);
        renderable.renderAll(mat, buf0);
        mat.popPose();
        mat.pushPose();
        mat.scale(s * 0.995f, -s * 0.995f, -s * 0.995f);

        RenderSystem.setShader(GameRenderer::getRendertypeEntityTranslucentCullShader);
        RenderSystem.setShaderTexture(0, tex[1]);

        ret = DyeColor.RED;
        if (stack.hasTag() && stack.getTag().contains("dyeColour"))
        {
            final int damage = stack.getTag().getInt("dyeColour");
            ret = DyeColor.byId(damage);
        }
        colour = new Color(ret.getTextColor() + 0xFF000000);
        for (final IExtendedModelPart part1 : model.getParts().values())
            part1.setRGBABrO(colour.getRed(), colour.getGreen(), colour.getBlue(), 255, brightness, overlay);
        final VertexConsumer buf1 = Util.makeBuilder(buff, tex[1]);
        renderable.renderAll(mat, buf1);
        GL11.glColor3f(1, 1, 1);
        mat.popPose();
    }
}
