package thut.bling.client.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.api.ModelHolder;
import thut.bling.ThutBling;
import thut.core.client.render.model.IExtendedModelPart;
import thut.core.client.render.model.IModel;
import thut.core.client.render.model.IModelCustom;
import thut.core.client.render.model.ModelFactory;
import thut.core.client.render.model.parts.Material;
import thut.wearables.EnumWearable;

public class Util
{

    // This is just a dummy texture for getting a fake initial renderer when we
    // have a json model
    public final static ResourceLocation DUMMY = new ResourceLocation(ThutBling.MODID, "textures/hologram.png");

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

    public static Map<String, IModel> customModels = Maps.newHashMap();
    public static Map<String, ResourceLocation[]> customTextures = Maps.newHashMap();

    public static IModel getCustomModel(final ItemStack stack)
    {
        if (stack.hasTag() && stack.getTag().contains("model"))
        {
            final String model = stack.getTag().getString("model");
            IModel imodel = Util.customModels.get(model);
            if (imodel == null)
            {
                final ResourceLocation loc = new ResourceLocation(model);
                imodel = ModelFactory.createScaled(new ModelHolder(loc));
                if (model != null)
                {
                    Util.customModels.put(model, imodel);
                    return imodel.isValid() ? imodel : null;
                }
            }
            else return imodel.isValid() ? imodel : null;
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

    @OnlyIn(Dist.CLIENT)
    public static Predicate<Material> IS_OVERLAY = m -> (m.name.contains("_overlay")
            || m.tex != null && m.tex.getPath().contains("_overlay"));

    public static void renderModel(PoseStack mat, MultiBufferSource buff, ItemStack stack, IModel model, int brightness,
            int overlay)
    {
        renderModel(mat, buff, stack, model, brightness, overlay, IS_OVERLAY);
    }

    public static void renderModel(PoseStack mat, MultiBufferSource buff, ItemStack stack, IModel model, int brightness,
            int overlay, Predicate<Material> notColurable)
    {
        renderModel(mat, buff, stack, "main", "gem", model, brightness, overlay, notColurable);
    }

    public static void renderModel(final PoseStack mat, final MultiBufferSource buff, final ItemStack stack,
            final String colorpart, final String itempart, final IModel model, final int brightness, final int overlay,
            Predicate<Material> notColurable)
    {
        if (!(model instanceof IModelCustom renderable)) return;
        if (!model.isLoaded() || !model.isValid()) return;
        if (model.getParts().containsKey("gem"))
        {
            Util.renderStandardModelWithGem(mat, buff, stack, colorpart, itempart, model, brightness, overlay,
                    IS_OVERLAY);
        }
        else
        {
            int alpha = 255;
            Color colour;
            ResourceLocation[] texs = getCustomTextures(null, stack);
            if (stack.hasTag() && stack.getTag().contains("alpha")) alpha = stack.getTag().getInt("alpha");
            if (stack.getItem() instanceof DyeableLeatherItem dyed)
            {
                colour = new Color(dyed.getColor(stack));
            }
            else
            {
                DyeColor ret = DyeColor.BROWN;
                if (stack.hasTag() && stack.getTag().contains("dyeColour"))
                {
                    final int damage = stack.getTag().getInt("dyeColour");
                    ret = DyeColor.byId(damage);
                }
                colour = new Color(ret.getTextColor());
            }
            try
            {
                for (final IExtendedModelPart part1 : model.getParts().values())
                {
                    if (texs != null) for (var m : part1.getMaterials())
                    {
                        m.tex = texs[0];
                    }
                    part1.setRGBABrO(colour.getRed(), colour.getGreen(), colour.getBlue(), alpha, brightness, overlay);
                    part1.setRGBABrO(notColurable, 255, 255, 255, alpha, brightness, overlay);
                }
                renderable.renderAll(mat, Util.makeBuilder(buff, Util.DUMMY));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void renderStandardModelWithGem(final PoseStack mat, final MultiBufferSource buff,
            final ItemStack stack, final String colorpart, final String itempart, final IModel model,
            final int brightness, final int overlay, Predicate<Material> notColurable)
    {
        if (!(model instanceof IModelCustom renderable)) return;
        ResourceLocation tex0 = null;
        ItemStack gem = ItemStack.EMPTY;
        Color colour;
        int alpha = 255;
        if (stack.hasTag() && stack.getTag().contains("alpha")) alpha = stack.getTag().getInt("alpha");

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

        if (stack.getItem() instanceof DyeableLeatherItem dyed)
        {
            colour = new Color(dyed.getColor(stack));
        }
        else
        {
            DyeColor ret = DyeColor.BROWN;
            if (stack.hasTag() && stack.getTag().contains("dyeColour"))
            {
                final int damage = stack.getTag().getInt("dyeColour");
                ret = DyeColor.byId(damage);
            }
            colour = new Color(ret.getTextColor());
        }
        Map<Material, ResourceLocation> toReset = Maps.newHashMap();
        List<Material> toClear = new ArrayList<>();
        for (final IExtendedModelPart part : model.getParts().values())
        {
            boolean isGem = false;
            if (tex0 != null && (isGem = part.getName().contains(itempart))) for (Material m : part.getMaterials())
            {
                if (m.tex != null) toReset.put(m, m.tex);
                else toClear.add(m);
                m.tex = tex0;
            }

            // Overlay texture is the fixed one, the rest can be recoloured.
            if (!isGem)
            {
                part.setRGBABrO(colour.getRed(), colour.getGreen(), colour.getBlue(), alpha, brightness, overlay);
                part.setRGBABrO(notColurable, 255, 255, 255, alpha, brightness, overlay);
            }
            else part.setRGBABrO(255, 255, 255, alpha, brightness, overlay);
        }
        renderable.renderAll(mat, Util.makeBuilder(buff, Util.DUMMY));

        for (var entry : toReset.entrySet())
        {
            entry.getKey().tex = entry.getValue();
        }
        for (var material : toClear) material.tex = null;
    }

    public static boolean shouldReloadModel()
    {
        return Screen.hasAltDown() && Screen.hasControlDown() && Screen.hasShiftDown();
    }
}
