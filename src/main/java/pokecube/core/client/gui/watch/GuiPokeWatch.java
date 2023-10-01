package pokecube.core.client.gui.watch;

import java.util.List;
import org.lwjgl.glfw.GLFW;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.client.gui.helper.TexButton;
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
        public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
        {
            final int x = (this.watch.width - 160) / 2 + 80;
            final int y = (this.watch.height - 160) / 2 + 70;
            graphics.drawCenteredString(this.font, I18n.get("pokewatch.title.blank"), x + 35, y - 15, 0xFFFFFFFF);
            super.render(graphics, mouseX, mouseY, partialTicks);
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
        WatchPage page = GuiPokeWatch.makePage(GuiPokeWatch.PAGELIST.get(index), this);
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
            //if (!this.changeFocus(flag)) this.changeFocus(flag);

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
        this.current_page.init();
        final int x = this.width / 2;
        final int y = this.height / 2 - 5;
        
        final TexButton home = this.addRenderableWidget(new TexButton.Builder(TComponent.literal(""), b -> {
            final int index = 0;
            this.changePage(index, true);
        }).bounds(x - 152,y - 85, 24, 24).setTexture(GuiPokeWatch.getWidgetTex())
        		.setRender(new UVImgRender(24, 108, 24, 24)).build());
        
        final TexButton pokedex = this.addRenderableWidget(new TexButton.Builder(TComponent.literal(""), b -> {
            final int index = 1;
            this.changePage(index, true);
        }).bounds(x + 128,y - 85, 24, 24).setTexture(GuiPokeWatch.getWidgetTex())
        		.setRender(new UVImgRender(0, 0, 24, 24)).build());
        
        final TexButton wiki = this.addRenderableWidget(new TexButton.Builder(TComponent.literal(""), b -> {
            final int index = 2;
            this.changePage(index, true);
        }).bounds(x + 128,y - 61, 24, 24).setTexture(GuiPokeWatch.getWidgetTex())
        		.setRender(new UVImgRender(96, 0, 24, 24)).build());
        
        final TexButton spawns = this.addRenderableWidget(new TexButton.Builder(TComponent.literal(""), b -> {
            final int index = 3;
            this.changePage(index, true);
        }).bounds(x + 128,y - 37, 24, 24).setTexture(GuiPokeWatch.getWidgetTex())
        		.setRender(new UVImgRender(48, 0, 24, 24)).build());
        
        final TexButton trainer = this.addRenderableWidget(new TexButton.Builder(TComponent.literal(""), b -> {
            final int index = 4;
            this.changePage(index, true);
        }).bounds(x + 128,y - 13, 24, 24).setTexture(GuiPokeWatch.getWidgetTex())
        		.setRender(new UVImgRender(72, 0, 24, 24)).build());
        
        final TexButton teleport = this.addRenderableWidget(new TexButton.Builder(TComponent.literal(""), b -> {
            final int index = 5;
            this.changePage(index, true);
        }).bounds(x + 128,y + 11, 24, 24).setTexture(GuiPokeWatch.getWidgetTex())
        		.setRender(new UVImgRender(24, 0, 24, 24)).build());
        
        final TexButton secretBases = this.addRenderableWidget(new TexButton.Builder(TComponent.literal(""), b -> {
            final int index = 6;
            this.changePage(index, true);
        }).bounds(x + 128,y + 35, 24, 24).setTexture(GuiPokeWatch.getWidgetTex())
        		.setRender(new UVImgRender(120, 0, 24, 24)).build());
        
        this.current_page.onPageOpened();
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks)
    {
        try
        {
            this.current_page.renderBackground(graphics);
            this.current_page.render(graphics, mouseX, mouseY, partialTicks);
        }
        catch (final Exception e)
        {
            this.handleError(e);
        }
        super.render(graphics, mouseX, mouseY, partialTicks);
    }
}
