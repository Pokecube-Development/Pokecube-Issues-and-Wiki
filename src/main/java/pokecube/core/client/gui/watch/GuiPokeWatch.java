package pokecube.core.client.gui.watch;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.client.gui.helper.TexButton;
import pokecube.core.client.gui.helper.TexButton.ShiftedTooltip;
import pokecube.core.client.gui.helper.TexButton.UVImgRender;
import pokecube.core.client.gui.watch.util.WatchPage;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.network.packets.PacketPokedex;
import pokecube.core.utils.Resources;
import thut.lib.TComponent;

public class GuiPokeWatch extends Screen
{
    private static class MissingPage extends WatchPage
    {

        public MissingPage(final GuiPokeWatch watch)
        {
            super(TComponent.translatable("pokewatch.title.blank"), watch, GuiPokeWatch.TEX_DM, GuiPokeWatch.TEX_NM);
            this.font = Minecraft.getInstance().font;
        }

        @Override
        public void render(final PoseStack mat, final int mouseX, final int mouseY, final float partialTicks)
        {
            final int x = (this.watch.width - 160) / 2 + 80;
            final int y = (this.watch.height - 160) / 2 + 70;
            GuiComponent.drawCenteredString(mat, this.font, I18n.get("pokewatch.title.blank"), x + 35, y - 15,
                    0xFFFFFFFF);
            super.render(mat, mouseX, mouseY, partialTicks);
        }
    }

    public static class UVHolder
    {
        private int uOffset = 0;
        private int vOffset = 0;

        private int buttonX = 0;
        private int buttonY = 0;

        private int index = 0;

        public UVHolder(int x, int y, int u, int v, int index)
        {
            this.buttonX = x;
            this.buttonY = y;
            this.uOffset = u;
            this.vOffset = v;
            this.index = index;
        }

        public void makeButton(GuiPokeWatch gui)
        {
            final int x = gui.width / 2;
            final int y = gui.height / 2 - 5;
            WatchPage page = gui.createPage(index);
            gui.addRenderableWidget(new TexButton(x + buttonX, y + buttonY, 24, 24, page.getTitle(), b -> {
                gui.changePage(this.index, true);
            }, new ShiftedTooltip(-129 , -96 - buttonY)).setTex(GuiPokeWatch.getWidgetTex()).noName()
                    .setRender(new UVImgRender(uOffset, vOffset, 24, 24)));
        }
    }

    public static final ResourceLocation makeWatchTexture(final String tex)
    {
        return new ResourceLocation(PokecubeMod.ID, "textures/gui/watch/" + tex + ".png");
    }

    public static final ResourceLocation TEX_DM = GuiPokeWatch.makeWatchTexture("pokewatchgui_start");
    public static final ResourceLocation TEX_NM = GuiPokeWatch.makeWatchTexture("pokewatchgui_start_nm");

    public static final ResourceLocation TEXTURE_BASE = GuiPokeWatch.makeWatchTexture("pokewatchgui");

    public static List<Class<? extends WatchPage>> PAGELIST = Lists.newArrayList();

    static
    {
        GuiPokeWatch.PAGELIST.add(StartWatch.class);
        GuiPokeWatch.PAGELIST.add(PokemobInfoPage.class);
        GuiPokeWatch.PAGELIST.add(WikiPage.class);
        GuiPokeWatch.PAGELIST.add(SpawnsPage.class);
        GuiPokeWatch.PAGELIST.add(ProgressPage.class);
        GuiPokeWatch.PAGELIST.add(TeleportsPage.class);
        GuiPokeWatch.PAGELIST.add(SecretBaseRadarPage.class);
    }

    public static int lastPage = -1;

    private static WatchPage makePage(final Class<? extends WatchPage> clazz, final GuiPokeWatch parent)
    {
        try
        {
            return clazz.getConstructor(GuiPokeWatch.class).newInstance(parent);
        }
        catch (final Exception e)
        {
            PokecubeAPI.LOGGER.error("Error with making a page for watch", e);
            return null;
        }
    }

    public static ResourceLocation getWidgetTex()
    {
        return GuiPokeWatch.nightMode ? Resources.WIDGETS_NM : Resources.WIDGETS;
    }

    public static boolean nightMode = false;

    public static int GUIW = 256;
    public static int GUIH = 180;

    public WatchPage current_page = null;

    public final IPokemob pokemob;
    public final LivingEntity target;
    public final Player player;
    public int index = 0;

    public GuiPokeWatch(final Player player, final LivingEntity target)
    {
        super(TComponent.translatable("pokecube.watch"));
        this.target = target;
        this.pokemob = PokemobCaps.getPokemobFor(target);
        if (this.pokemob != null)
        {
            PacketPokedex.sendInspectPacket(this.pokemob);
            PacketPokedex.updateWatchEntry(this.pokemob.getPokedexEntry());
            // Change to the pokemob info page on opening
            GuiPokeWatch.lastPage = 1;
        }
        this.player = player;

        if (GuiPokeWatch.lastPage >= 0 && GuiPokeWatch.lastPage < GuiPokeWatch.PAGELIST.size())
            this.current_page = this.createPage(GuiPokeWatch.lastPage);
        else if (!GuiPokeWatch.PAGELIST.isEmpty()) this.current_page = this.createPage(GuiPokeWatch.lastPage = 0);
        else this.current_page = new MissingPage(this);
        PacketPokedex.sendLocationSpawnsRequest();
    }

    public boolean canEdit(final IPokemob pokemob)
    {
        // Not a real mob
        if (!pokemob.getEntity().isAddedToWorld()) return false;
        // Otherwise, check if owned, or if player is creative mode
        return this.player.getUUID().equals(pokemob.getOwnerId()) || this.player.getAbilities().instabuild;
    }

    public void changePage(final int newIndex)
    {
        this.changePage(newIndex, false);
    }

    public void changePage(final int newIndex, final boolean force)
    {
        if (!force && newIndex == this.index) return;
        if (this.current_page != null) this.current_page.onPageClosed();
        this.index = newIndex;
        GuiPokeWatch.lastPage = this.index;
        this.init(this.minecraft, this.width, this.height);
    }

    public WatchPage createPage(final int index)
    {
        if (this.itemRenderer == null) this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        WatchPage page = GuiPokeWatch.makePage(GuiPokeWatch.PAGELIST.get(index), this);
        page.itemRenderer = this.itemRenderer;
        return page;
    }

    private void handleError(final Exception e)
    {
        if (this.current_page != null) PokecubeAPI.LOGGER.warn("Error with page " + this.current_page.getTitle(), e);
        else
        {
            PokecubeAPI.LOGGER.warn("Error with null page", e);
            return;
        }
        this.current_page.onPageClosed();
        this.current_page.init();
        this.current_page.onPageOpened();
    }

    @Override
    public boolean keyPressed(final int keyCode, final int b, final int c)
    {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc())
        {
            this.removed();
            this.onClose();
            return true;
        }

        // We overwrite this to reverse the ordering of checking if tab was
        // pressed
        final boolean subpages = this.getFocused() != null && this.getFocused().keyPressed(keyCode, b, c);

        if (subpages) return true;
        if (keyCode == GLFW.GLFW_KEY_TAB)
        {
            final boolean flag = !Screen.hasShiftDown();
            if (!this.changeFocus(flag)) this.changeFocus(flag);

            return true;
        }
        return false;
    }

    @Override
    public void init()
    {
        this.renderables.clear();
        this.children.clear();
        super.init();
        this.current_page = this.createPage(GuiPokeWatch.lastPage);
        this.current_page.itemRenderer = this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.current_page.init();

        List<UVHolder> buttons = new ArrayList<>();
        buttons.add(new UVHolder(-153, -85, 24, 108, 0)); // home
        buttons.add(new UVHolder(129, -85, 0, 0, 1)); // Pokedex
        buttons.add(new UVHolder(129, -60, 96, 0, 2)); // wiki
        buttons.add(new UVHolder(129, -35, 48, 0, 3)); // spawns
        buttons.add(new UVHolder(129, -10, 72, 0, 4)); // trainer
        buttons.add(new UVHolder(129, 15, 24, 0, 5)); // teleport
        buttons.add(new UVHolder(129, 40, 120, 0, 6)); // bases/meteors
        buttons.forEach(uv -> uv.makeButton(this));
        this.current_page.onPageOpened();
    }

    @Override
    public void render(final PoseStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        try
        {
            this.current_page.renderBackground(mat);
            this.current_page.render(mat, mouseX, mouseY, partialTicks);
        }
        catch (final Exception e)
        {
            this.handleError(e);
        }
        super.render(mat, mouseX, mouseY, partialTicks);
    }
}
