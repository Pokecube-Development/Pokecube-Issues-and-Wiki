package thut.bling.client.render;

import java.awt.Color;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thut.api.ModelHolder;
import thut.api.maths.vecmath.Vector3f;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelCustom;
import thut.core.client.render.model.ModelFactory;
import thut.wearables.EnumWearable;

public class Util
{
    public static RenderType getType(final ResourceLocation loc, final boolean alpha)
    {
        return alpha ? RenderType.makeType("thutbling:bling_a", DefaultVertexFormats.ENTITY, GL11.GL_TRIANGLES, 256,
                true, false, RenderType.State.getBuilder().texture(new RenderState.TextureState(loc, true, false))
                        .diffuseLighting(new RenderState.DiffuseLightingState(true)).alpha(new RenderState.AlphaState(
                                0.003921569F)).cull(new RenderState.CullState(false)).lightmap(
                                        new RenderState.LightmapState(true)).overlay(new RenderState.OverlayState(true))
                        .build(false))
                : RenderType.makeType("thutbling:bling_b", DefaultVertexFormats.ENTITY, GL11.GL_TRIANGLES, 256, true,
                        false, RenderType.State.getBuilder().texture(new RenderState.TextureState(loc, true, false))
                                .diffuseLighting(new RenderState.DiffuseLightingState(true)).cull(
                                        new RenderState.CullState(false)).lightmap(new RenderState.LightmapState(true))
                                .overlay(new RenderState.OverlayState(true)).build(false));
    }

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

    public static IVertexBuilder makeBuilder(final IRenderTypeBuffer buff, final ResourceLocation loc)
    {
        return buff.getBuffer(Util.getType(loc, true));
    }

    public static IVertexBuilder makeBuilder(final IRenderTypeBuffer buff, final ResourceLocation loc,
            final boolean alpha)
    {
        return buff.getBuffer(Util.getType(loc, alpha));
    }

    public static void renderStandardModelWithGem(final MatrixStack mat, final IRenderTypeBuffer buff,
            final ItemStack stack, final String colorpart, final String itempart, final IModel model,
            final ResourceLocation[] tex, final Vector3f dr, final Vector3f ds, final int brightness, final int overlay)
    {
        if (!(model instanceof IModelCustom)) return;
        ResourceLocation tex0 = tex[0];
        final ResourceLocation tex1 = tex[1];
        ItemStack gem = ItemStack.EMPTY;
        final IModelCustom renderable = (IModelCustom) model;
        DyeColor ret = DyeColor.YELLOW;
        if (stack.hasTag() && stack.getTag().contains("dyeColour"))
        {
            final int damage = stack.getTag().getInt("dyeColour");
            ret = DyeColor.byId(damage);
        }
        final Color colour = new Color(ret.getColorValue() + 0xFF000000);
        IExtendedModelPart part = model.getParts().get(colorpart);

        if (stack.hasTag() && stack.getTag().contains("gemTag"))
        {
            gem = ItemStack.read(stack.getTag().getCompound("gemTag"));
            final ResourceLocation sprite = Minecraft.getInstance().getItemRenderer().getItemModelMesher()
                    .getParticleIcon(gem).getName();
            final String namespace = sprite.getNamespace();
            final String val = "textures/" + sprite.getPath();
            tex0 = new ResourceLocation(namespace, val + ".png");
        }
        else tex0 = null;
        mat.push();
        mat.translate(dr.x, dr.y, dr.z);
        mat.scale(ds.x, ds.y, ds.z);
        if (part != null)
        {
            part.setRGBABrO(colour.getRed(), colour.getGreen(), colour.getBlue(), 255, brightness, overlay);
            final IVertexBuilder buf1 = Util.makeBuilder(buff, tex1);
            renderable.renderPart(mat, buf1, colorpart);
        }
        part = model.getParts().get(itempart);
        if (part != null && tex0 != null)
        {
            final IVertexBuilder buf0 = Util.makeBuilder(buff, tex0);
            renderable.renderPart(mat, buf0, itempart);
        }
        else if (part != null && !gem.isEmpty())
        {
            // TODO confirm this works
            final IVertexBuilder buf0 = buff.getBuffer(RenderTypeLookup.func_239219_a_(gem, false));
            renderable.renderPart(mat, buf0, itempart);
        }
        mat.pop();
    }
}
