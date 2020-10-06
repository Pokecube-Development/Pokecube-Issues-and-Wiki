package pokecube.core.client.gui.pokemob;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.StringTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.Resources;
import pokecube.core.client.render.mobs.RenderMobOverlays;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

public class GuiPokemobBase extends ContainerScreen<ContainerPokemob>
{
    public static void renderMob(final LivingEntity entity, final int dx, final int dy, final float pitch,
            final float yaw, final float headPitch, final float headYaw, float scale)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        final int j = dx;
        final int k = dy;
        scale *= 30;
        if (pokemob != null)
        {
            final float mobScale = pokemob.getSize();
            final thut.api.maths.vecmath.Vector3f dims = pokemob.getPokedexEntry().getModelSize();
            scale /= Math.max(dims.z * mobScale, Math.max(dims.y * mobScale, dims.x * mobScale));
        }
        RenderSystem.pushMatrix();
        RenderSystem.translatef(j + 55, k + 60, 50.0F);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
        final MatrixStack matrixstack = new MatrixStack();

        matrixstack.getLast().getNormal().mul(Vector3f.YP.rotationDegrees(50));

        matrixstack.scale(scale, scale, scale);
        final Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        final Quaternion quaternion1 = Vector3f.YP.rotationDegrees(yaw);
        quaternion.multiply(quaternion1);
        quaternion.multiply(Vector3f.XP.rotationDegrees(pitch));
        matrixstack.rotate(quaternion);
        final EntityRendererManager entityrenderermanager = Minecraft.getInstance().getRenderManager();
        quaternion1.conjugate();
        entityrenderermanager.setCameraOrientation(quaternion1);
        entityrenderermanager.setRenderShadow(false);
        final IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().getRenderTypeBuffers()
                .getBufferSource();
        RenderMobOverlays.enabled = false;
        entityrenderermanager.renderEntityStatic(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixstack,
                irendertypebuffer$impl, 15728880);
        RenderMobOverlays.enabled = true;
        irendertypebuffer$impl.finish();
        entityrenderermanager.setRenderShadow(true);
        RenderSystem.popMatrix();
    }

    public static void setPokemob(final IPokemob pokemobIn)
    {
        if (pokemobIn == null)
        {
            PokecubeCore.LOGGER.error("Error syncing pokemob", new IllegalArgumentException());
            return;
        }
    }

    protected TextFieldWidget name = new TextFieldWidget(null, 1 / 2, 1 / 2, 120, 10, new StringTextComponent(""));

    public GuiPokemobBase(final ContainerPokemob container, final PlayerInventory inv)
    {
        super(container, inv, container.pokemob.getDisplayName());
        this.name.setText(container.pokemob.getDisplayName().getUnformattedComponentText().trim());
        this.name.enabledColor = 4210752;
    }

    @Override
    public boolean keyPressed(final int keyCode, final int p_keyPressed_2_, final int p_keyPressed_3_)
    {
        if (this.name.isFocused()) if (keyCode == GLFW.GLFW_KEY_ESCAPE) this.name.setFocused(false);
        else if (keyCode == GLFW.GLFW_KEY_ENTER)
        {
            this.container.pokemob.setPokemonNickname(this.name.getText());
            return true;
        }
        else if (keyCode != GLFW.GLFW_KEY_BACKSPACE) return true;
        return super.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final MatrixStack mat, final float partialTicks, final int mouseX, final int mouseY)
    {
        this.getMinecraft().getTextureManager().bindTexture(Resources.GUI_POKEMOB);
        final int k = (this.width - this.xSize) / 2;
        final int l = (this.height - this.ySize) / 2;
        this.blit(mat, k, l, 0, 0, this.xSize, this.ySize);
        if (this.container.mode == 0) this.blit(mat, k + 79, l + 17, 0, this.ySize, 90, 18);
        this.blit(mat, k + 7, l + 35, 0, this.ySize + 54, 18, 18);
        if (this.container.pokemob != null)
        {
            final boolean prev = this.container.pokemob.getEntity().addedToChunk;
            this.container.pokemob.getEntity().addedToChunk = false;
            GuiPokemobBase.renderMob(this.container.pokemob.getEntity(), k, l, 0, 0, 0, 0, 1);
            this.container.pokemob.getEntity().addedToChunk = prev;
        }
    }

    /**
     * Draw the foreground layer for the ContainerScreen (everything in front
     * of the items)
     */
    @Override
    protected void drawGuiContainerForegroundLayer(final MatrixStack mat, final int mouseX, final int mouseY)
    {
        this.font.drawString(mat, this.playerInventory.getDisplayName().getString(), 8.0F, this.ySize - 96 + 2,
                4210752);
    }

    @Override
    public void init()
    {
        super.init();
        final int xOffset = 80;
        final int yOffset = 77;
        this.name = new TextFieldWidget(this.font, this.width / 2 - xOffset, this.height / 2 - yOffset, 69, 10, new StringTextComponent(""));
        if (this.container.pokemob != null) this.name.setText(this.container.pokemob.getDisplayName()
                .getUnformattedComponentText().trim());
        this.name.setTextColor(0xFFFFFFFF);
        this.addButton(this.name);
    }

    /** Draws the screen and all the components in it. */
    @Override
    public void render(final MatrixStack mat, final int x, final int y, final float z)
    {
        super.renderBackground(mat);
        super.render(mat, x, y, z);
        final List<String> text = Lists.newArrayList();
        if (this.container.pokemob == null) return;
        if (!text.isEmpty()) this.renderTooltip(text, x, y);
    }
}
