package thut.bling.client.render;

import java.awt.Color;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import thut.bling.ThutBling;
import thut.core.client.render.json.JsonModel;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelCustom;

public class Back
{
    // This is just a dummy texture for getting a fake initial renderer when we
    // have a json model
    private final static ResourceLocation DUMMY = new ResourceLocation(ThutBling.MODID, "textures/hologram.png");

    public static void renderBack(final PoseStack mat, final MultiBufferSource buff, final LivingEntity wearer,
            final ItemStack stack, final IModel model, final ResourceLocation[] textures, final int brightness,
            final int overlay)
    {
        if (!(model instanceof IModelCustom renderable)) return;
        if (!model.isLoaded() || !model.isValid()) return;

        Color colour;
        if (stack.getItem() instanceof DyeableLeatherItem dyed)
        {
            colour = new Color(dyed.getColor(stack) + 0xFF000000);
        }
        else
        {
            DyeColor ret = DyeColor.RED;
            if (stack.hasTag() && stack.getTag().contains("dyeColour"))
            {
                final int damage = stack.getTag().getInt("dyeColour");
                ret = DyeColor.byId(damage);
            }
            colour = new Color(ret.getTextColor() + 0xFF000000);
        }
        if (renderable instanceof JsonModel)
        {
            mat.pushPose();
            float s = 1.0f;
            mat.scale(s, -s, -s);
            mat.mulPose(Vector3f.YP.rotationDegrees(180));
            mat.translate(-0.5, -.6, -0.16);
            for (final IExtendedModelPart part1 : model.getParts().values())
            {
                // Overlay texture is the fixed one, the rest can be recoloured.
                if (!part1.getMaterials().get(0).name.contains("overlay"))
                    part1.setRGBABrO(colour.getRed(), colour.getGreen(), colour.getBlue(), 255, brightness, overlay);
                else part1.setRGBABrO(255, 255, 255, 255, brightness, overlay);
            }
            final VertexConsumer buf1 = Util.makeBuilder(buff, DUMMY);
            renderable.renderAll(mat, buf1);
            mat.popPose();
        }
        else
        {
            final ResourceLocation[] tex = textures.clone();
            float s;
            mat.pushPose();
            s = 0.65f;
            mat.scale(s, -s, -s);
            mat.mulPose(Vector3f.YP.rotationDegrees(180));
            mat.translate(0, -.55, 0.35);
            for (final IExtendedModelPart part1 : model.getParts().values())
                part1.setRGBABrO(255, 255, 255, 255, brightness, overlay);
            final VertexConsumer buf0 = Util.makeBuilder(buff, tex[0]);
            renderable.renderAll(mat, buf0);
            mat.popPose();
            mat.pushPose();
            mat.scale(s, -s, -s);
            for (final IExtendedModelPart part1 : model.getParts().values())
                part1.setRGBABrO(colour.getRed(), colour.getGreen(), colour.getBlue(), 255, brightness, overlay);
            mat.mulPose(Vector3f.YP.rotationDegrees(180));
            mat.translate(0, -.55, 0.35);
            final VertexConsumer buf1 = Util.makeBuilder(buff, tex[1]);
            renderable.renderAll(mat, buf1);
            mat.popPose();
        }
    }
}
