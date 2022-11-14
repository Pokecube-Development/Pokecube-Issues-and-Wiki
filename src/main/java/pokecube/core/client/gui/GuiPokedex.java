/**
 *
 */
package pokecube.core.client.gui;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import pokecube.api.data.Pokedex;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.utils.PokeType;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.pokemob.GuiPokemobHelper;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.database.Database;
import pokecube.core.eventhandlers.StatsCollector;
import pokecube.core.handlers.PokecubePlayerDataHandler;
import pokecube.core.handlers.playerdata.PokecubePlayerStats;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.utils.EntityTools;
import pokecube.core.utils.Resources;
import thut.core.common.handlers.PlayerDataHandler;
import thut.lib.TComponent;

public class GuiPokedex extends Screen
{
    public static PokedexEntry pokedexEntry = null;

    public IPokemob pokemob = null;
    protected Player PlayerEntity = null;
    protected ScrollGui<LineEntry> list;
    protected EditBox pokemobTextField;
    /** The X size of the inventory window in pixels. */
    protected int xSize;

    /** The Y size of the inventory window in pixels. */
    protected int ySize;
    int prevX = 0;

    int prevY = 0;

    /**
     *
     */
    public GuiPokedex(final IPokemob pokemob, final Player PlayerEntity)
    {
        super(TComponent.translatable("pokecube.pokedex.gui"));
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

    public void handleButton(final int button)
    {
        if (button == 1)
        {
            GuiPokedex.pokedexEntry = Pokedex.getInstance().getNext(GuiPokedex.pokedexEntry, 1);
            this.initList();
            PacketPokedex.updateWatchEntry(GuiPokedex.pokedexEntry);
        }
        else if (button == 2)
        {
            GuiPokedex.pokedexEntry = Pokedex.getInstance().getPrevious(GuiPokedex.pokedexEntry, 1);
            this.initList();
            PacketPokedex.updateWatchEntry(GuiPokedex.pokedexEntry);
        }
        else if (button == 3)
        {
            GuiPokedex.pokedexEntry = Pokedex.getInstance().getNext(GuiPokedex.pokedexEntry, 10);
            this.initList();
            PacketPokedex.updateWatchEntry(GuiPokedex.pokedexEntry);
        }
        else if (button == 4)
        {
            GuiPokedex.pokedexEntry = Pokedex.getInstance().getPrevious(GuiPokedex.pokedexEntry, 10);
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

        this.pokemobTextField = new EditBox(this.font, xOffset - 65, yOffset + 123, 110, 10, TComponent.literal(""));
        this.pokemobTextField.setBordered(false);
        this.pokemobTextField.setEditable(true);

        if (GuiPokedex.pokedexEntry != null)
            this.pokemobTextField.setValue(I18n.get(GuiPokedex.pokedexEntry.getUnlocalizedName()));
        this.addRenderableWidget(this.pokemobTextField);
        this.initList();
    }

    private void initList()
    {
        if (this.list != null) this.children.remove(this.list);
        final int offsetX = (this.width - 160) / 2 + 90;
        final int offsetY = (this.height - 160) / 2 + 12;
        final int height = 15 * this.font.lineHeight;

        this.list = new ScrollGui<>(this, this.minecraft, 110, height, this.font.lineHeight, offsetX, offsetY);

        MutableComponent page;

        page = TComponent.translatable("entity.pokecube." + GuiPokedex.pokedexEntry.getTrimmedName() + ".dexDesc");
        this.pokemobTextField.setValue(I18n.get(GuiPokedex.pokedexEntry.getUnlocalizedName()));
        var list = Lists.newArrayList(this.font.split(page, 100));
        list.add(TComponent.literal("").getVisualOrderText());
        var holder = this.pokemob != null ? this.pokemob.getCustomHolder() : null;
        page = pokedexEntry.getDescription(holder);
        list.addAll(this.font.split(page, 100));

        final IClickListener listen = new IClickListener()
        {
            @Override
            public boolean handleClick(final Style component)
            {
                if (component != null)
                {
                    final ClickEvent clickevent = component.getClickEvent();
                    if (clickevent != null) if (clickevent.getAction() == Action.CHANGE_PAGE)
                    {
                        final PokedexEntry entry = Database.getEntry(clickevent.getValue());
                        if (entry != null)
                        {
                            GuiPokedex.pokedexEntry = entry;
                            GuiPokedex.this.initList();
                            PacketPokedex.updateWatchEntry(GuiPokedex.pokedexEntry);
                        }
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void handleHovor(final PoseStack mat, final Style component, final int x, final int y)
            {}
        };
        for (var line : list)
            this.list.addEntry(new LineEntry(this.list, 0, 0, this.font, line, 0xFFFFFF).setClickListner(listen));

        this.children.add(this.list);
    }

    @Override
    public boolean keyPressed(final int key, final int unk1, final int unk2)
    {
        if (key == GLFW.GLFW_KEY_ENTER && this.pokemobTextField.isFocused())
        {
            PokedexEntry entry = Database.getEntry(this.pokemobTextField.getValue());
            if (entry == null)
            {
                for (final PokedexEntry e : Database.getSortedFormes())
                {
                    final String translated = I18n.get(e.getUnlocalizedName());
                    if (translated.equalsIgnoreCase(this.pokemobTextField.getValue()))
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
            else this.pokemobTextField.setValue(I18n.get(GuiPokedex.pokedexEntry.getUnlocalizedName()));
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

        if (button != 0)
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        if (button == 14)
        {
            PacketPokedex.sendInspectPacket(true, Minecraft.getInstance().getLanguageManager().getSelected().getCode());
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
    public void render(final PoseStack mat, final int mouseX, final int mouseY, final float partialTick)
    {
        // Draw background
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderTexture(0, Resources.GUI_POKEDEX);
        final int j2 = (this.width - this.xSize) / 2;
        final int k2 = (this.height - this.ySize) / 2;
        this.blit(mat, j2, k2, 0, 0, this.xSize, this.ySize);

        // Draw mob
        final IPokemob renderMob = EventsHandlerClient.getRenderMob(GuiPokedex.pokedexEntry,
                this.PlayerEntity.getLevel());
        if (!renderMob.getEntity().isAddedToWorld())
            EntityTools.copyEntityTransforms(renderMob.getEntity(), this.PlayerEntity);

        final PokedexEntry pokedexEntry = renderMob.getPokedexEntry();
        final PokecubePlayerStats stats = PlayerDataHandler.getInstance().getPlayerData(Minecraft.getInstance().player)
                .getData(PokecubePlayerStats.class);
        boolean fullColour = StatsCollector.getCaptured(pokedexEntry, Minecraft.getInstance().player) > 0
                || StatsCollector.getHatched(pokedexEntry, Minecraft.getInstance().player) > 0
                || this.minecraft.player.getAbilities().instabuild;

        // Megas Inherit colouring from the base form.
        if (!fullColour && pokedexEntry.isMega())
            fullColour = StatsCollector.getCaptured(pokedexEntry.getBaseForme(), Minecraft.getInstance().player) > 0
                    || StatsCollector.getHatched(pokedexEntry.getBaseForme(), Minecraft.getInstance().player) > 0;
        // Set colouring accordingly.
        if (fullColour) renderMob.setRGBA(255, 255, 255, 255);
        else if (stats.hasInspected(pokedexEntry)) renderMob.setRGBA(127, 127, 127, 255);
        else renderMob.setRGBA(15, 15, 15, 255);

        GlStateManager._enableDepthTest();
        final float yaw = Util.getMillis() / 20;
        final float pitch = 0;
        final float hx = 0;
        final float hy = yaw;
        GuiPokemobHelper.renderMob(renderMob.getEntity(), j2, k2 + 40, pitch, yaw, hx, hy, 1, partialTick);

        // Draw info about mob
        final int yOffset = this.height / 2 - 80;
        int xOffset = this.width / 2;
        final int nb = GuiPokedex.pokedexEntry != null ? GuiPokedex.pokedexEntry.getPokedexNb() : 0;
        final PokeType type1 = this.pokemob != null && GuiPokedex.pokedexEntry == this.pokemob.getPokedexEntry()
                ? this.pokemob.getType1()
                : GuiPokedex.pokedexEntry != null ? GuiPokedex.pokedexEntry.getType1() : PokeType.unknown;
        final PokeType type2 = this.pokemob != null && GuiPokedex.pokedexEntry == this.pokemob.getPokedexEntry()
                ? this.pokemob.getType2()
                : GuiPokedex.pokedexEntry != null ? GuiPokedex.pokedexEntry.getType2() : PokeType.unknown;
        GuiComponent.drawCenteredString(mat, this.font, "#" + nb, xOffset - 28, yOffset + 02, 0xffffff);
        try
        {
            GuiComponent.drawCenteredString(mat, this.font, PokeType.getTranslatedName(type1), xOffset - 88,
                    yOffset + 137, type1.colour);
            GuiComponent.drawCenteredString(mat, this.font, PokeType.getTranslatedName(type2), xOffset - 44,
                    yOffset + 137, type2.colour);
        }
        catch (final Exception e)
        {}

        // Draw default gui stuff.
        final int length = this.font.width(this.pokemobTextField.getValue()) / 2;
        xOffset = this.width / 2 - 65;
        this.pokemobTextField.x = xOffset - length;
        super.render(mat, mouseX, mouseY, partialTick);

        // Draw description
        this.list.render(mat, mouseX, mouseY, partialTick);
    }
}
