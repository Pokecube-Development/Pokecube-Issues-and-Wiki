package pokecube.core.client.gui.pokemob;

import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import pokecube.core.PokecubeCore;
import pokecube.core.client.Resources;
import pokecube.core.client.render.mobs.RenderMobOverlays;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

public class GuiPokemobBase extends ContainerScreen<ContainerPokemob>
{
    public static void renderMob(final LivingEntity entity, final int width, final int height, final int unusedA,
            final int unusedB, final float xRenderAngle, final float yRenderAngle, final float zRenderAngle,
            float scale)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
        final int j = width;
        final int k = height;
        scale *= 40;
        if (pokemob != null)
        {
            final float mobScale = pokemob.getSize();
            final thut.api.maths.vecmath.Vector3f dims = pokemob.getPokedexEntry().getModelSize();
            scale *= Math.max(dims.z * mobScale, Math.max(dims.y * mobScale, dims.x * mobScale));
        }
        final float f = (float) Math.atan(xRenderAngle / 40.0F);
        final float f1 = (float) Math.atan(yRenderAngle / 40.0F);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(j + 55, k + 60, 50.0F);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
        final MatrixStack matrixstack = new MatrixStack();
        // matrixstack.translate(0.0D, 0.0D, 1000.0D);
        matrixstack.scale(scale, scale, scale);
        final Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        final Quaternion quaternion1 = Vector3f.XP.rotationDegrees(f1 * 20.0F);
        quaternion.multiply(quaternion1);
        matrixstack.rotate(quaternion);
        final float f2 = entity.renderYawOffset;
        final float f3 = entity.rotationYaw;
        final float f4 = entity.rotationPitch;
        final float f5 = entity.prevRotationYawHead;
        final float f6 = entity.rotationYawHead;
        entity.renderYawOffset = 180.0F + f * 20.0F;
        entity.rotationYaw = 180.0F + f * 40.0F;
        entity.rotationPitch = -f1 * 20.0F;
        entity.rotationYawHead = entity.rotationYaw;
        entity.prevRotationYawHead = entity.rotationYaw;
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
        entity.renderYawOffset = f2;
        entity.rotationYaw = f3;
        entity.rotationPitch = f4;
        entity.prevRotationYawHead = f5;
        entity.rotationYawHead = f6;
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

    private float           yRenderAngle = 10;
    private TextFieldWidget name         = new TextFieldWidget(null, 1 / 2, 1 / 2, 120, 10, "");

    private float           xRenderAngle = 0;

    public GuiPokemobBase(final ContainerPokemob container, final PlayerInventory inv)
    {
        super(container, inv, container.pokemob.getDisplayName());
        this.name.setText(container.pokemob.getDisplayName().getUnformattedComponentText().trim());
        this.name.setEnableBackgroundDrawing(false);
        this.name.enabledColor = 4210752;
    }

    @Override
    public boolean keyPressed(final int keyCode, final int p_keyPressed_2_, final int p_keyPressed_3_)
    {
        if (this.name.isFocused()) if (keyCode == GLFW.GLFW_KEY_ESCAPE) this.name.setFocused(false);
        else if (keyCode == GLFW.GLFW_KEY_ENTER && this.name.isFocused())
        {
            this.container.pokemob.setPokemonNickname(this.name.getText());
            return true;
        }
        return super.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.getMinecraft().getTextureManager().bindTexture(Resources.GUI_POKEMOB);
        final int k = (this.width - this.xSize) / 2;
        final int l = (this.height - this.ySize) / 2;
        this.blit(k, l, 0, 0, this.xSize, this.ySize);
        this.blit(k + 79, l + 17, 0, this.ySize, 90, 18);
        this.blit(k + 7, l + 35, 0, this.ySize + 54, 18, 18);
        this.yRenderAngle = -45;
        this.xRenderAngle = 0;
        if (this.container.pokemob != null) GuiPokemobBase.renderMob(this.container.pokemob.getEntity(), k, l,
                this.xSize, this.ySize, this.xRenderAngle, this.yRenderAngle, 0, 1);
    }

    /** Draw the foreground layer for the ContainerScreen (everything in front
     * of the items) */
    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY)
    {
        this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8.0F, this.ySize - 96 + 2,
                4210752);
    }

    @Override
    public void init()
    {
        super.init();
        final int xOffset = 80;
        final int yOffset = 77;
        this.name = new TextFieldWidget(this.font, this.width / 2 - xOffset, this.height / 2 - yOffset, 120, 10, "");
        this.name.setEnableBackgroundDrawing(false);
        if (this.container.pokemob != null)
            this.name.setText(this.container.pokemob.getDisplayName().getUnformattedComponentText().trim());
        this.name.setEnableBackgroundDrawing(false);
        this.name.setTextColor(0xFFFFFFFF);
        this.addButton(this.name);
    }

    /** Draws the screen and all the components in it. */
    @Override
    public void render(final int x, final int y, final float z)
    {
        super.renderBackground();
        super.render(x, y, z);
        final List<String> text = Lists.newArrayList();
        if (this.container.pokemob == null) return;
        if (!text.isEmpty()) this.renderTooltip(text, x, y);
    }
}
