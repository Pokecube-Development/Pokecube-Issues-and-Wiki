/**
 *
 */
package pokecube.core.client.gui;

import java.util.List;

import thut.api.maths.vecmath.Vector3f;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.RenderComponentsUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.Resources;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.render.mobs.RenderMobOverlays;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.StatsCollector;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.utils.EntityTools;
import pokecube.core.utils.PokeType;
import thut.api.entity.IMobColourable;
import thut.core.common.handlers.PlayerDataHandler;

public class GuiPokedex extends Screen
{

    public static PokedexEntry pokedexEntry = null;

    public static void renderMob(final MobEntity entity, final Minecraft mc, final int dx, final int dy,
            final float scale, final int height, final int width, final int xSize, final int ySize,
            final float xHeadRenderAngle, final float yHeadRenderAngle, final float yaw)
    {
        try
        {
            float size = 0;
            int j = 0;
            int k = 0;

            final IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
            if (pokemob == null) return;
            final PokedexEntry pokedexEntry = pokemob.getPokedexEntry();
            final PokecubePlayerStats stats = PlayerDataHandler.getInstance().getPlayerData(Minecraft
                    .getInstance().player).getData(PokecubePlayerStats.class);
            final IMobColourable colourable = pokemob.getEntity() instanceof IMobColourable ? (IMobColourable) pokemob
                    .getEntity() : pokemob instanceof IMobColourable ? (IMobColourable) pokemob : null;
            if (colourable != null)
            {
                boolean fullColour = StatsCollector.getCaptured(pokedexEntry, Minecraft.getInstance().player) > 0
                        || StatsCollector.getHatched(pokedexEntry, Minecraft.getInstance().player) > 0
                        || mc.player.abilities.isCreativeMode;

                // Megas Inherit colouring from the base form.
                if (!fullColour && pokedexEntry.isMega) fullColour = StatsCollector.getCaptured(pokedexEntry
                        .getBaseForme(), Minecraft.getInstance().player) > 0 || StatsCollector.getHatched(pokedexEntry
                                .getBaseForme(), Minecraft.getInstance().player) > 0;

                // Set colouring accordingly.
                if (fullColour) colourable.setRGBA(255, 255, 255, 255);
                else if (stats.hasInspected(pokedexEntry)) colourable.setRGBA(127, 127, 127, 255);
                else colourable.setRGBA(15, 15, 15, 255);
            }
            // Reset some things that add special effects to rendered mobs.
            pokemob.setGeneralState(GeneralStates.EXITINGCUBE, false);
            pokemob.setGeneralState(GeneralStates.EVOLVING, false);

            final float mobScale = pokemob.getSize();
            final Vector3f dims = pokemob.getPokedexEntry().getModelSize();
            size = Math.max(dims.z * mobScale, Math.max(dims.y * mobScale, dims.x * mobScale));
            j = (width - xSize) / 2 + dx;
            k = (height - ySize) / 2 + dy;

            GL11.glPushMatrix();
            GL11.glTranslatef(j + 60, k + 100, 50F);
            final float zoom = 25F / size * scale;
            GL11.glScalef(zoom, zoom, zoom);
            GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
            final float f5 = k + 75 - 50 - ySize;
            GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);

            GL11.glRotatef(-135F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-(float) Math.atan(f5 / 40F) * 20F, 1.0F, 0.0F, 0.0F);
            entity.prevRenderYawOffset = yaw;
            entity.renderYawOffset = yaw;
            entity.rotationYaw = yaw;
            entity.prevRotationYaw = entity.rotationYaw;
            entity.rotationPitch = xHeadRenderAngle;
            entity.rotationYawHead = yHeadRenderAngle;
            entity.prevRotationYawHead = entity.rotationYawHead;
            entity.prevRotationPitch = entity.rotationPitch;

            entity.limbSwing = 0;
            entity.limbSwingAmount = 0;
            entity.prevLimbSwingAmount = 0;
            final PokeType flying = PokeType.getType("flying");
            entity.onGround = !pokemob.isType(flying);

            if (Screen.hasAltDown())
            {
                entity.onGround = true;
                entity.limbSwingAmount = 0.05f;
                entity.prevLimbSwingAmount = entity.limbSwingAmount - 0.5f;
            }

            GlStateManager.enableColorMaterial();
            RenderHelper.enableStandardItemLighting();
            RenderMobOverlays.enabled = false;
            final EntityRendererManager entityrenderermanager = Minecraft.getInstance().getRenderManager();
            entityrenderermanager.setPlayerViewY(180.0F);
            entityrenderermanager.setRenderShadow(false);
            entityrenderermanager.renderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
            entityrenderermanager.setRenderShadow(true);
            RenderMobOverlays.enabled = true;
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.activeTexture(GLX.GL_TEXTURE1);
            GlStateManager.disableTexture();
            GlStateManager.activeTexture(GLX.GL_TEXTURE0);
            if (entity instanceof IMobColourable) ((IMobColourable) entity).setRGBA(255, 255, 255, 255);

            GL11.glPopMatrix();

        }
        catch (final Throwable e)
        {
            e.printStackTrace();
        }
    }

    public IPokemob                pokemob      = null;
    protected PlayerEntity         PlayerEntity = null;
    protected ScrollGui<LineEntry> list;
    protected TextFieldWidget      pokemobTextField;
    /** The X size of the inventory window in pixels. */
    protected int                  xSize;

    /** The Y size of the inventory window in pixels. */
    protected int       ySize;
    private final float xRenderAngle     = 0;
    private final float yHeadRenderAngle = 10;
    private final float xHeadRenderAngle = 0;
    int                 prevX            = 0;

    int prevY = 0;

    /**
     *
     */
    public GuiPokedex(final IPokemob pokemob, final PlayerEntity PlayerEntity)
    {
        super(new TranslationTextComponent("pokecube.pokedex.gui"));
        this.xSize = 256;
        this.ySize = 197;
        this.pokemob = pokemob;
        this.PlayerEntity = PlayerEntity;

        if (pokemob != null) GuiPokedex.pokedexEntry = pokemob.getPokedexEntry();
        else
        {
            final String name = PokecubePlayerDataHandler.getCustomDataTag(PlayerEntity).getString("WEntry");
            GuiPokedex.pokedexEntry = Database.getEntry(name);
            if (GuiPokedex.pokedexEntry == null) GuiPokedex.pokedexEntry = Pokedex.getInstance().getFirstEntry();
        }
    }

    @Override
    public boolean charTyped(final char par1, final int par2)
    {
        if (Screen.hasAltDown()) return false;
        return super.charTyped(par1, par2);
    }

    private int getButtonId(final double x, final double y)
    {
        final int xConv = (int) (x - (this.width - this.xSize) / 2 - 74);
        final int yConv = (int) (y - (this.height - this.ySize) / 2 - 107);
        int button = 0;

        if (xConv >= 37 && xConv <= 42 && yConv >= 63 && yConv <= 67) button = 1;// Next
        else if (xConv >= 25 && xConv <= 30 && yConv >= 63 && yConv <= 67) button = 2;// Previous
        else if (xConv >= 32 && xConv <= 36 && yConv >= 58 && yConv <= 63) button = 3;// Next
                                                                                      // 10
        else if (xConv >= 32 && xConv <= 36 && yConv >= 69 && yConv <= 73) button = 4;// Previous
                                                                                      // 10
        else if (xConv >= -65 && xConv <= -58 && yConv >= 65 && yConv <= 72) button = 5;// Sound
        else if (xConv >= -55 && xConv <= 30 && yConv >= -60 && yConv <= 15) button = 10;// Rotate
                                                                                         // Mouse
                                                                                         // control
        return button;
    }

    // TODO mouse movement changing angle of mob.

    public void handleButton(final int button)
    {
        if (button == 1)
        {
            GuiPokedex.pokedexEntry = Pokedex.getInstance().getNext(GuiPokedex.pokedexEntry, 1);
            this.pokemobTextField.setText(I18n.format(GuiPokedex.pokedexEntry.getUnlocalizedName()));
            this.initList();
            PacketPokedex.updateWatchEntry(GuiPokedex.pokedexEntry);
        }
        else if (button == 2)
        {
            GuiPokedex.pokedexEntry = Pokedex.getInstance().getPrevious(GuiPokedex.pokedexEntry, 1);
            this.pokemobTextField.setText(I18n.format(GuiPokedex.pokedexEntry.getUnlocalizedName()));
            this.initList();
            PacketPokedex.updateWatchEntry(GuiPokedex.pokedexEntry);
        }
        else if (button == 3)
        {
            GuiPokedex.pokedexEntry = Pokedex.getInstance().getNext(GuiPokedex.pokedexEntry, 10);
            this.pokemobTextField.setText(I18n.format(GuiPokedex.pokedexEntry.getUnlocalizedName()));
            this.initList();
            PacketPokedex.updateWatchEntry(GuiPokedex.pokedexEntry);
        }
        else if (button == 4)
        {
            GuiPokedex.pokedexEntry = Pokedex.getInstance().getPrevious(GuiPokedex.pokedexEntry, 10);
            this.pokemobTextField.setText(I18n.format(GuiPokedex.pokedexEntry.getUnlocalizedName()));
            this.initList();
            PacketPokedex.updateWatchEntry(GuiPokedex.pokedexEntry);
        }
        if (button >= 1 && button <= 5 || button == 5)
        {
            float volume = 0.2F;
            if (button == 5) volume = 1F;
            this.minecraft.player.playSound(GuiPokedex.pokedexEntry.getSoundEvent(), volume, 1.0F);
        }
    }

    @Override
    public void init()
    {
        super.init();

        final int yOffset = this.height / 2 - 80;
        final int xOffset = this.width / 2;

        this.pokemobTextField = new TextFieldWidget(this.font, xOffset - 65, yOffset + 123, 110, 10, "");
        this.pokemobTextField.setEnableBackgroundDrawing(false);
        this.pokemobTextField.setEnabled(true);

        if (GuiPokedex.pokedexEntry != null) this.pokemobTextField.setText(I18n.format(GuiPokedex.pokedexEntry
                .getUnlocalizedName()));
        this.addButton(this.pokemobTextField);
        this.initList();
    }

    private void initList()
    {
        if (this.list != null) this.children.remove(this.list);
        final int offsetX = (this.width - 160) / 2 + 90;
        final int offsetY = (this.height - 160) / 2 + 12;
        final int height = 15 * this.font.FONT_HEIGHT;
        ITextComponent line;
        ITextComponent page = GuiPokedex.pokedexEntry.getDescription();
        this.list = new ScrollGui<>(this, this.minecraft, 110, height, this.font.FONT_HEIGHT, offsetX, offsetY);
        page = new StringTextComponent(page.getFormattedText());
        final List<ITextComponent> list = RenderComponentsUtil.splitText(page, 100, this.font, false, false);
        for (int j = 0; j < list.size(); j++)
        {
            line = list.get(j);
            this.list.addEntry(new LineEntry(this.list, offsetX, offsetY, this.font, line, 0xFFFFFF));
        }
        this.children.add(this.list);
    }

    @Override
    public boolean keyPressed(final int key, final int unk1, final int unk2)
    {
        if (key == GLFW.GLFW_KEY_ENTER && this.pokemobTextField.isFocused())
        {
            PokedexEntry entry = Database.getEntry(this.pokemobTextField.getText());
            if (entry == null)
            {
                for (final PokedexEntry e : Database.getSortedFormes())
                {
                    final String translated = I18n.format(e.getUnlocalizedName());
                    if (translated.equalsIgnoreCase(this.pokemobTextField.getText()))
                    {
                        Database.data2.put(translated, e);
                        entry = e;
                        break;
                    }
                }
                // If the pokedex entry is not actually registered, use old
                // entry.
                if (Pokedex.getInstance().getIndex(entry) == null) entry = null;
            }
            if (entry != null)
            {
                GuiPokedex.pokedexEntry = entry;
                this.initList();
                PacketPokedex.updateWatchEntry(GuiPokedex.pokedexEntry);
            }
            else this.pokemobTextField.setText(I18n.format(GuiPokedex.pokedexEntry.getUnlocalizedName()));
        }
        else if (key == GLFW.GLFW_KEY_UP)
        {
            this.handleButton(3);
            return true;
        }
        else if (key == GLFW.GLFW_KEY_DOWN)
        {
            this.handleButton(4);
            return true;
        }
        else if (key == GLFW.GLFW_KEY_LEFT)
        {
            this.handleButton(1);
            return true;
        }
        else if (key == GLFW.GLFW_KEY_RIGHT)
        {
            this.handleButton(2);
            return true;
        }
        return super.keyPressed(key, unk1, unk2);
    }

    /** Called when the mouse is clicked. */
    @Override
    public boolean mouseClicked(final double x, final double y, final int mouseButton)
    {
        final boolean ret = super.mouseClicked(x, y, mouseButton);
        if (ret) return true;
        final int button = this.getButtonId(x, y);

        if (button != 0) this.minecraft.getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        if (button == 14)
        {
            PacketPokedex.sendInspectPacket(true, Minecraft.getInstance().getLanguageManager().getCurrentLanguage()
                    .getCode());
            return true;
        }

        if (button == 10)
        {
            this.prevX = (int) x;
            this.prevY = (int) y;
            return true;
        }
        else
        {
            this.handleButton(button);
            return true;
        }
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTick)
    {
        // Draw background
        final Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bindTexture(Resources.GUI_POKEDEX);
        final int j2 = (this.width - this.xSize) / 2;
        final int k2 = (this.height - this.ySize) / 2;
        this.blit(j2, k2, 0, 0, this.xSize, this.ySize);

        // Draw mob
        GL11.glPushMatrix();
        final IPokemob renderMob = EventsHandlerClient.getRenderMob(GuiPokedex.pokedexEntry, this.PlayerEntity
                .getEntityWorld());
        if (!renderMob.getEntity().addedToChunk) EntityTools.copyEntityTransforms(renderMob.getEntity(),
                this.PlayerEntity);
        GlStateManager.enableDepthTest();
        GuiPokedex.renderMob(renderMob.getEntity(), minecraft, 0, 0, 1f, this.height, this.width, this.xSize,
                this.ySize, this.xHeadRenderAngle, this.yHeadRenderAngle, this.xRenderAngle);
        GL11.glPopMatrix();

        // Draw info about mob
        GL11.glPushMatrix();
        final int yOffset = this.height / 2 - 80;
        int xOffset = this.width / 2;
        final int nb = GuiPokedex.pokedexEntry != null ? GuiPokedex.pokedexEntry.getPokedexNb() : 0;
        final PokeType type1 = this.pokemob != null && GuiPokedex.pokedexEntry == this.pokemob.getPokedexEntry()
                ? this.pokemob.getType1()
                : GuiPokedex.pokedexEntry != null ? GuiPokedex.pokedexEntry.getType1() : PokeType.unknown;
        final PokeType type2 = this.pokemob != null && GuiPokedex.pokedexEntry == this.pokemob.getPokedexEntry()
                ? this.pokemob.getType2()
                : GuiPokedex.pokedexEntry != null ? GuiPokedex.pokedexEntry.getType2() : PokeType.unknown;
        this.drawCenteredString(this.font, "#" + nb, xOffset - 28, yOffset + 02, 0xffffff);
        try
        {
            this.drawCenteredString(this.font, PokeType.getTranslatedName(type1), xOffset - 88, yOffset + 137,
                    type1.colour);
            this.drawCenteredString(this.font, PokeType.getTranslatedName(type2), xOffset - 44, yOffset + 137,
                    type2.colour);
        }
        catch (final Exception e)
        {
        }
        GL11.glPopMatrix();

        // Draw default gui stuff.
        final int length = this.font.getStringWidth(this.pokemobTextField.getText()) / 2;
        xOffset = this.width / 2 - 65;
        this.pokemobTextField.x = xOffset - length;
        super.render(mouseX, mouseY, partialTick);

        // Draw description
        this.list.render(mouseX, mouseY, partialTick);
    }
}
