package thut.bling.client.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thut.api.ModelHolder;
import thut.api.Tracker;
import thut.bling.ThutBling;
import thut.bling.data.GemData;
import thut.api.model.IExtendedModelPart;
import thut.api.model.IModel;
import thut.api.model.IModelCustom;
import thut.core.client.render.model.ModelFactory;
import thut.api.model.Material;
import thut.api.util.RegHelper;
import thut.wearables.EnumWearable;

public class Util
{
    public static Map<String, IModel> customModels = Maps.newHashMap();
    public static Map<String, ResourceLocation[]> customTextures = Maps.newHashMap();

    public static IModel getCustomModel(final ItemStack stack)
    {
        var data = stack.get(ThutBling.BLING_MODEL_DATA);
        if (data != null)
        {
            final String model = data.model();
            if (model.isBlank()) return null;
            IModel imodel = Util.customModels.get(model);
            if (imodel == null)
            {
                final ResourceLocation loc = ResourceLocation.parse(model);
                imodel = ModelFactory.create(new ModelHolder(loc));
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
        var data = stack.get(ThutBling.BLING_MODEL_DATA);
        if (data != null && !data.tex().isBlank())
        {
            final String tex = data.tex();
            ResourceLocation[] textures = Util.customTextures.get(tex);
            if (textures == null)
            {
                textures = new ResourceLocation[2];
                textures[0] = ResourceLocation.parse(tex);
                if (!data.tex2().isBlank()) textures[1] = ResourceLocation.parse(data.tex2());
                else textures[1] = textures[0];
                Util.customTextures.put(tex, textures);
                return textures;
            }
            else return textures;
        }
        return null;
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

            GemData data = stack.get(ThutBling.BLING_GEM_DATA);

            if (data != null) alpha = data.alpha();
            DyedItemColor dyeditemcolor = stack.get(DataComponents.DYED_COLOR);
            if (dyeditemcolor != null)
            {
                colour = new Color(dyeditemcolor.rgb());
            }
            else
            {
                DyeColor ret = DyeColor.BROWN;
                colour = new Color(ret.getTextColor());
            }
            try
            {
                for (final IExtendedModelPart part : model.getPartsList())
                {
                    if (texs != null) for (var m : part.getMaterials())
                    {
                        m.tex = texs[0];
                    }
                    part.setRGBABrO(colour.getRed(), colour.getGreen(), colour.getBlue(), alpha, brightness, overlay);
                    part.setRGBABrO(notColurable, 255, 255, 255, alpha, brightness, overlay);
                }
                renderable.render(mat, null);
            }
            catch (Exception ignored)
            {
                // NO-OP to not lag render thread
            }
        }
    }

    public static void renderStandardModelWithGem(final PoseStack mat, final MultiBufferSource buff,
            final ItemStack stack, final String colorpart, final String itempart, final IModel model,
            final int brightness, final int overlay, Predicate<Material> notColurable)
    {
        if (!(model instanceof IModelCustom renderable)) return;
        ResourceLocation tex0;
        Color colour;
        int alpha = 255;
        GemData data = stack.get(ThutBling.BLING_GEM_DATA);
        if (data != null)
        {
            alpha = data.alpha();
            if (data.gem().equals(GemData.EMPTY))
            {
                final ItemStack _gem = ItemStack.parseOptional(Minecraft.getInstance().level.registryAccess(),
                        data.gemTag());
                final ResourceLocation id = RegHelper.getKey(_gem);
                final String tex = id.getNamespace() + ":textures/item/" + id.getPath() + ".png";
                data = data.withToolGem(tex);
                stack.set(ThutBling.BLING_GEM_DATA, data);
            }
            tex0 = data.gem();
        }
        else tex0 = null;

        DyedItemColor dyeditemcolor = stack.get(DataComponents.DYED_COLOR);
        if (dyeditemcolor != null)
        {
            colour = new Color(dyeditemcolor.rgb());
        }
        else
        {
            DyeColor ret = DyeColor.BROWN;
            colour = new Color(ret.getTextColor());
        }

        Map<Material, ResourceLocation> toReset = Maps.newHashMap();
        List<Material> toClear = new ArrayList<>();
        for (final IExtendedModelPart part : model.getPartsList())
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
        renderable.render(mat, null);

        for (var entry : toReset.entrySet())
        {
            entry.getKey().tex = entry.getValue();
        }
        for (var material : toClear) material.tex = null;
    }

    private static long updated = -1;

    public static boolean shouldReloadModel()
    {
        boolean reload = Screen.hasAltDown() && Screen.hasControlDown() && Screen.hasShiftDown();
        if (!reload) return false;
        long tick = Tracker.instance().getTick();
        if (tick - updated < 100) return false;
        updated = tick;
        return true;
    }
}
