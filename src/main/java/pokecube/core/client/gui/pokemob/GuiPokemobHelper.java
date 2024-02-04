package pokecube.core.client.gui.pokemob;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.PokecubeCore;
import pokecube.core.client.render.mobs.RenderMobOverlays;
import pokecube.core.database.Database;
import thut.api.AnimatedCaps;
import thut.api.util.JsonUtil;
import thut.core.common.ThutCore;
import thut.lib.AxisAngles;
import thut.lib.ResourceHelper;

public class GuiPokemobHelper
{
    public static ResourceLocation SIZEMAP = new ResourceLocation(PokecubeCore.MODID, "pokemobs_gui_sizes.json");

    public static boolean autoScale = true;

    public static Map<PokedexEntry, Float> sizeMap = Maps.newHashMap();

    public static void initSizeMap()
    {
        try
        {
            final BufferedReader reader = ResourceHelper.getReader(GuiPokemobHelper.SIZEMAP,
                    Minecraft.getInstance().getResourceManager());
            if (reader == null) throw new FileNotFoundException(GuiPokemobHelper.SIZEMAP.toString());
            final JsonObject json = JsonUtil.gson.fromJson(reader, JsonObject.class);
            for (final Entry<String, JsonElement> entry : json.entrySet())
            {
                final String key = entry.getKey();
                try
                {
                    final Float value = entry.getValue().getAsFloat();
                    GuiPokemobHelper.sizeMap.put(Database.getEntry(key), value);
                }
                catch (final Exception e)
                {
                    PokecubeAPI.LOGGER.error("Error loading size for {}", key);
                }
            }
            reader.close();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }

    }

    public static void renderMob(final LivingEntity entity, final int dx, final int dy, final float pitch,
            final float yaw, final float headPitch, final float headYaw, final float scale, float partialTicks)
    {
        GuiPokemobHelper.renderMob(new PoseStack(), entity, dx, dy, pitch, yaw, headPitch, headYaw, scale,
                partialTicks);
    }

    public static void renderMob(final PoseStack mat, final LivingEntity entity, final int dx, final int dy,
            final float pitch, final float yaw, final float headPitch, final float headYaw, float scale,
            float partialTicks)
    {
        IPokemob pokemob = PokemobCaps.getPokemobFor(entity);
        LivingEntity renderMob = entity;
        final int j = dx;
        final int k = dy;
        scale *= 30;
        if (pokemob != null)
        {
            if (!entity.isAddedToWorld()) pokemob.setSize(1);
            float mobScale = 1;
            if (GuiPokemobHelper.autoScale)
            {
                final Float value = GuiPokemobHelper.sizeMap.get(pokemob.getPokedexEntry());
                if (value != null) mobScale = value * 8.0f;
                else
                {
                    final boolean stock = pokemob.getPokedexEntry().stock;
                    if (stock)
                    {
                        var dims = pokemob.getPokedexEntry().getModelSize();
                        mobScale = Math.max(dims.z, Math.max(dims.y, dims.x));
                    }
                    else mobScale = Math.max(renderMob.getBbHeight(), renderMob.getBbWidth());
                }
            }
            else
            {
                var dims = pokemob.getPokedexEntry().getModelSize();
                mobScale = Math.max(dims.z * mobScale, Math.max(dims.y * mobScale, dims.x * mobScale));
            }
            scale /= mobScale;
            scale /= pokemob.getSize();
        }
        mat.pushPose();
        mat.translate(j + 55, k + 60, 50.0F);
        mat.scale(scale, scale, scale);
        var quaternion = AxisAngles.ZP.rotationDegrees(180.0F);
        var quaternion1 = AxisAngles.YP.rotationDegrees(180 - yaw);

        final Matrix3f norms = mat.last().normal().copy();
        mat.scale(1, 1, -1);
        mat.last().normal().load(norms);

        quaternion.mul(quaternion1);
        quaternion.mul(AxisAngles.XP.rotationDegrees(pitch));
        mat.mulPose(quaternion);
        Lighting.setupForEntityInInventory();
        final EntityRenderDispatcher entityrenderermanager = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternion1.conj();
        entityrenderermanager.overrideCameraOrientation(quaternion1);
        entityrenderermanager.setRenderShadow(false);
        final MultiBufferSource.BufferSource irendertypebuffer$impl = Minecraft.getInstance().renderBuffers()
                .bufferSource();
        RenderMobOverlays.enabled = false;
        // Disable the face culling that occurs if too far away
        double bak = ThutCore.getConfig().modelCullThreshold;
        ThutCore.getConfig().modelCullThreshold = -1;
        if (pokemob != null && !pokemob.getEntity().isAddedToWorld())
        {
            var animated = AnimatedCaps.getAnimated(pokemob.getEntity());
            animated.getChoices().clear();
            animated.getChoices().add("gui_render");
        }
        entityrenderermanager.render(renderMob, 0.0D, 0.0D, 0.0D, 0.0F, -0.1F, mat, irendertypebuffer$impl, LightTexture.FULL_BRIGHT);
        // Re-enable the face culling that occurs if too far away
        ThutCore.getConfig().modelCullThreshold = bak;
        RenderMobOverlays.enabled = true;
        irendertypebuffer$impl.endBatch();
        entityrenderermanager.setRenderShadow(true);
        mat.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }
}
