package thut.bling.client.render;

import java.awt.Color;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thut.api.maths.vecmath.Vector3f;
import thut.core.client.render.animation.ModelHolder;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelCustom;
import thut.core.client.render.model.ModelFactory;
import thut.wearables.EnumWearable;

public class Util
{
    static Map<String, IModel>             customModels   = Maps.newHashMap();
    static Map<String, ResourceLocation[]> customTextures = Maps.newHashMap();

    public static IModel getCustomModel(final EnumWearable slot, final ItemStack stack)
    {

        if (stack.hasTag() && stack.getTag().contains("model"))
        {
            final String model = stack.getTag().getString("model");
            IModel imodel = Util.customModels.get(model);
            if (imodel == null)
            {
                final ResourceLocation loc = new ResourceLocation(model);
                imodel = ModelFactory.create(new ModelHolder(loc, null, null, model));
                if (model != null)
                {
                    Util.customModels.put(model, imodel);
                    return imodel;
                }
            }
            else return imodel;
        }
        return null;
    }

    public static ResourceLocation[] getCustomTextures(final EnumWearable slot, final ItemStack stack)
    {
        if (stack.hasTag() && stack.getTag().contains("tex"))
        {
            final String tex = stack.getTag().getString("tex");
            ResourceLocation[] textures = Util.customTextures.get(tex);
            if (textures == null)
            {
                textures = new ResourceLocation[2];
                textures[0] = new ResourceLocation(tex);
                if (stack.getTag().contains("tex2")) textures[1] = new ResourceLocation(stack.getTag().getString(
                        "tex2"));
                else textures[1] = textures[0];
                Util.customTextures.put(tex, textures);
                return textures;
            }
            else return textures;
        }
        return null;
    }

    public static void renderStandardModelWithGem(final ItemStack stack, final String colorpart, final String itempart,
            final IModel model, ResourceLocation[] tex, final int brightness, final Vector3f dr, final Vector3f ds)
    {
        if (!(model instanceof IModelCustom)) return;
        tex = tex.clone();
        final IModelCustom renderable = (IModelCustom) model;
        DyeColor ret = DyeColor.YELLOW;
        if (stack.hasTag() && stack.getTag().contains("dyeColour"))
        {
            final int damage = stack.getTag().getInt("dyeColour");
            ret = DyeColor.byId(damage);
        }
        final Color colour = new Color(ret.getColorValue() + 0xFF000000);
        final int[] col = new int[] { colour.getRed(), colour.getGreen(), colour.getBlue(), 255, brightness };
        IExtendedModelPart part = model.getParts().get(colorpart);

        if (stack.hasTag() && stack.getTag().contains("gem")) tex[0] = new ResourceLocation(stack.getTag().getString(
                "gem"));
        else tex[0] = null;
        GL11.glPushMatrix();
        GL11.glRotated(90, 1, 0, 0);
        GL11.glRotated(180, 0, 0, 1);
        GL11.glTranslatef(dr.x, dr.y, dr.z);
        GL11.glScalef(ds.x, ds.y, ds.z);
        if (part != null)
        {
            part.setRGBAB(col);
            Minecraft.getInstance().textureManager.bindTexture(tex[1]);
            renderable.renderPart(colorpart);
        }
        GL11.glColor3f(1, 1, 1);
        part = model.getParts().get(itempart);
        if (part != null && tex[0] != null)
        {
            Minecraft.getInstance().textureManager.bindTexture(tex[0]);
            renderable.renderPart(itempart);
        }
        GL11.glPopMatrix();
    }
}
