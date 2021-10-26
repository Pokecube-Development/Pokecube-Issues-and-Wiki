package pokecube.core.client.gui.pokemob;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import pokecube.core.PokecubeCore;
import pokecube.core.client.Resources;
import pokecube.core.client.gui.AnimationGui;
import pokecube.core.client.render.mobs.RenderMobOverlays;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;

public class GuiPokemobBase extends AbstractContainerScreen<ContainerPokemob>
{
    public static ResourceLocation SIZEMAP = new ResourceLocation(PokecubeCore.MODID, "pokemobs_gui_sizes.json");

    public static boolean autoScale = true;

    public static Map<PokedexEntry, Float> sizeMap = Maps.newHashMap();

    public static void initSizeMap()
    {
        Resource res = null;
        try
        {
            res = Minecraft.getInstance().getResourceManager().getResource(GuiPokemobBase.SIZEMAP);
            final InputStream in = res.getInputStream();
            final JsonObject json = PokedexEntryLoader.gson.fromJson(new InputStreamReader(in), JsonObject.class);
            for (final Entry<String, JsonElement> entry : json.entrySet())
            {
                final String key = entry.getKey();
                try
                {
                    final Float value = entry.getValue().getAsFloat();
                    GuiPokemobBase.sizeMap.put(Database.getEntry(key), value);
                }
                catch (final Exception e)
                {
                    PokecubeCore.LOGGER.error("Error loading size for {}", key);
                }
            }
            res.close();
        }
        catch (final IOException e)
        {
            e.printStackTrace();
        }

    }

    public static void renderMob(final LivingEntity entity, final int dx, final int dy, final float pitch,
            final float yaw, final float headPitch, final float headYaw, final float scale)
    {
        GuiPokemobBase.renderMob(new PoseStack(), entity, dx, dy, pitch, yaw, headPitch, headYaw, scale);
    }

    public static void renderMob(final PoseStack mat, final LivingEntity entity, final int dx, final int dy,
            final float pitch, final float yaw, final float headPitch, final float headYaw, float scale)
    {
        IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        LivingEntity renderMob = entity;
        final int j = dx;
        final int k = dy;
        scale *= 30;
        if (pokemob != null)
        {
            if (entity.isAddedToWorld()) pokemob = AnimationGui.getRenderMob(pokemob);

            pokemob.setSize(1);
            renderMob = pokemob.getEntity();
            float mobScale = 1;

            if (GuiPokemobBase.autoScale)
            {
                final Float value = GuiPokemobBase.sizeMap.get(pokemob.getPokedexEntry());
                if (value != null) mobScale = value * 2.0f;
                else
                {
                    final boolean stock = pokemob.getPokedexEntry().stock;
                    if (stock)
                    {
                        final thut.api.maths.vecmath.Vector3f dims = pokemob.getPokedexEntry().getModelSize();
                        mobScale = Math.max(dims.z, Math.max(dims.y, dims.x));
                    }
                    else mobScale = Math.max(renderMob.getBbHeight(), renderMob.getBbWidth());
                }
            }
            else
            {
                final thut.api.maths.vecmath.Vector3f dims = pokemob.getPokedexEntry().getModelSize();
                mobScale = Math.max(dims.z * mobScale, Math.max(dims.y * mobScale, dims.x * mobScale));
            }

            if (pokemob.getCombatState(CombatStates.DYNAMAX)) scale /= PokecubeCore.getConfig().dynamax_scale;
            else scale /= mobScale;
        }
        mat.pushPose();
        mat.translate(j + 55, k + 60, 50.0F);
        mat.scale(scale, scale, scale);
        final Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        final Quaternion quaternion1 = Vector3f.YP.rotationDegrees(yaw);
        quaternion.mul(quaternion1);
        quaternion.mul(Vector3f.XP.rotationDegrees(pitch));
        mat.mulPose(quaternion);
        final EntityRenderDispatcher entityrenderermanager = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternion1.conj();
        entityrenderermanager.overrideCameraOrientation(quaternion1);
        entityrenderermanager.setRenderShadow(false);
        final MultiBufferSource.BufferSource irendertypebuffer$impl = Minecraft.getInstance().renderBuffers()
                .bufferSource();
        RenderMobOverlays.enabled = false;
        entityrenderermanager.render(renderMob, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, mat, irendertypebuffer$impl, 15728880);
        RenderMobOverlays.enabled = true;
        irendertypebuffer$impl.endBatch();
        entityrenderermanager.setRenderShadow(true);
        mat.popPose();
    }

    public static void setPokemob(final IPokemob pokemobIn)
    {
        if (pokemobIn == null)
        {
            PokecubeCore.LOGGER.error("Error syncing pokemob", new IllegalArgumentException());
            return;
        }
    }

    protected EditBox name = new EditBox(null, 1 / 2, 1 / 2, 120, 10, new TextComponent(""));

    public GuiPokemobBase(final ContainerPokemob container, final Inventory inv)
    {
        super(container, inv, container.pokemob.getDisplayName());
    }

    @Override
    public boolean keyPressed(final int keyCode, final int p_keyPressed_2_, final int p_keyPressed_3_)
    {
        if (this.name.isFocused()) if (keyCode == GLFW.GLFW_KEY_ESCAPE) this.name.setFocused(false);
        else if (keyCode == GLFW.GLFW_KEY_ENTER)
        {
            String var = this.name.getValue();
            if (var.length() > 20)
            {
                var = var.substring(0, 20);
                this.name.setValue(var);
            }
            this.menu.pokemob.setPokemonNickname(var);
            return true;
        }
        else if (keyCode != GLFW.GLFW_KEY_BACKSPACE) return true;
        return super.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    protected void renderBg(final PoseStack mat, final float partialTicks, final int mouseX, final int mouseY)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, Resources.GUI_POKEMOB);
        final int k = (this.width - this.imageWidth) / 2;
        final int l = (this.height - this.imageHeight) / 2;
        this.blit(mat, k, l, 0, 0, this.imageWidth, this.imageHeight);
        if (this.menu.mode == 0) this.blit(mat, k + 79, l + 17, 0, this.imageHeight, 90, 18);
        this.blit(mat, k + 7, l + 35, 0, this.imageHeight + 54, 18, 18);
        if (this.menu.pokemob != null) GuiPokemobBase.renderMob(mat, this.menu.pokemob.getEntity(), k, l, 0, 0, 0, 0,
                1);
    }

    /**
     * Draw the foreground layer for the ContainerScreen (everything in front
     * of the items)
     */
    @Override
    protected void renderLabels(final PoseStack mat, final int mouseX, final int mouseY)
    {
        this.font.draw(mat, this.playerInventoryTitle.getString(), 8.0F, this.imageHeight - 96 + 2, 4210752);
    }

    @Override
    public void init()
    {
        super.init();
        final int xOffset = 80;
        final int yOffset = 77;
        final Component comp = new TextComponent("");
        this.name = new EditBox(this.font, this.width / 2 - xOffset, this.height / 2 - yOffset, 69, 10, comp);
        this.name.setTextColor(0xFFFFFFFF);
        this.name.textColorUneditable = 4210752;
        if (this.menu.pokemob != null) this.name.setValue(this.menu.pokemob.getDisplayName().getString());
        this.addRenderableWidget(this.name);
    }

    /** Draws the screen and all the components in it. */
    @Override
    public void render(final PoseStack mat, final int x, final int y, final float z)
    {
        super.renderBackground(mat);
        super.render(mat, x, y, z);
    }
}
