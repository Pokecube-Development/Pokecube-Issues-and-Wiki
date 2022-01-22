package thut.bling.client.render;

import java.awt.Color;
import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import thut.api.ModelHolder;
import thut.api.maths.vecmath.Vec3f;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelCustom;
import thut.core.client.render.model.ModelFactory;
import thut.core.client.render.model.parts.Material;
import thut.wearables.EnumWearable;

public class Util
{
    public static RenderType getType(final ResourceLocation loc, final boolean alpha)
    {
        final String id = loc + (alpha ? "alpha" : "none");

        final RenderType.CompositeState.CompositeStateBuilder builder = RenderType.CompositeState.builder();
        // No blur, No MipMap
        builder.setTextureState(new RenderStateShard.TextureStateShard(loc, false, false));

        builder.setTransparencyState(Material.DEFAULTTRANSP);

        builder.setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER);

        // These are needed in general for world lighting
        builder.setLightmapState(RenderStateShard.LIGHTMAP);
        builder.setOverlayState(RenderStateShard.OVERLAY);

        builder.setCullState(RenderStateShard.NO_CULL);

        final RenderType.CompositeState rendertype$state = builder.createCompositeState(true);
        return RenderType.create(id, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES, 256, true, false,
                rendertype$state);
    }

    static Map<String, IModel> customModels = Maps.newHashMap();
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
                if (stack.getTag().contains("tex2"))
                    textures[1] = new ResourceLocation(stack.getTag().getString("tex2"));
                else textures[1] = textures[0];
                Util.customTextures.put(tex, textures);
                return textures;
            }
            else return textures;
        }
        return null;
    }

    public static VertexConsumer makeBuilder(final MultiBufferSource buff, final ResourceLocation loc)
    {
        return buff.getBuffer(Util.getType(loc, true));
    }

    public static VertexConsumer makeBuilder(final MultiBufferSource buff, final ResourceLocation loc,
            final boolean alpha)
    {
        return buff.getBuffer(Util.getType(loc, alpha));
    }

    public static void renderStandardModelWithGem(final PoseStack mat, final MultiBufferSource buff,
            final ItemStack stack, final String colorpart, final String itempart, final IModel model,
            final ResourceLocation[] tex, final Vec3f dr, final Vec3f ds, final int brightness, final int overlay)
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
        final Color colour = new Color(ret.getTextColor() + 0xFF000000);
        IExtendedModelPart part = model.getParts().get(colorpart);
        if (stack.hasTag() && stack.getTag().contains("gemTag"))
        {
            gem = ItemStack.of(stack.getTag().getCompound("gemTag"));
            @SuppressWarnings("deprecation")
            final ResourceLocation sprite = Minecraft.getInstance().getItemRenderer().getItemModelShaper()
                    .getItemModel(gem).getParticleIcon().getName();
            final String namespace = sprite.getNamespace();
            final String val = "textures/" + sprite.getPath();
            tex0 = new ResourceLocation(namespace, val + ".png");
        }
        else tex0 = null;
        mat.pushPose();
        mat.translate(dr.x, dr.y, dr.z);
        mat.scale(ds.x, ds.y, ds.z);
        if (part != null)
        {
            part.setRGBABrO(colour.getRed(), colour.getGreen(), colour.getBlue(), 255, brightness, overlay);
            final VertexConsumer buf1 = Util.makeBuilder(buff, tex1);
            renderable.renderPart(mat, buf1, colorpart);
        }
        part = model.getParts().get(itempart);
        if (part != null && tex0 != null)
        {
            final VertexConsumer buf0 = Util.makeBuilder(buff, tex0);
            renderable.renderPart(mat, buf0, itempart);
        }
        else if (part != null && !gem.isEmpty())
        {
            // TODO confirm this works
            final VertexConsumer buf0 = buff.getBuffer(ItemBlockRenderTypes.getRenderType(gem, false));
            renderable.renderPart(mat, buf0, itempart);
        }
        mat.popPose();
    }
}
