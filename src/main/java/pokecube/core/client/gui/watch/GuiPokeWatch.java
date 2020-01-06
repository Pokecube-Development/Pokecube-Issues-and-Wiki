package pokecube.core.client.gui.watch;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.client.gui.watch.util.WatchPage;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.network.packets.PacketPokedex;

public class GuiPokeWatch extends Screen
{
    private static class MissingPage extends WatchPage
    {

        public MissingPage(final pokecube.core.client.gui.watch.GuiPokeWatch watch)
        {
            super(new TranslationTextComponent("pokewatch.title.blank"), watch);
            this.font = Minecraft.getInstance().fontRenderer;
        }

        @Override
        public void render(final int mouseX, final int mouseY, final float partialTicks)
        {
            final int x = (this.watch.width - 160) / 2 + 80;
            final int y = (this.watch.height - 160) / 2 + 70;
            this.drawCenteredString(this.font, I18n.format("pokewatch.title.blank"), x, y, 0xFFFFFFFF);
            super.render(mouseX, mouseY, partialTicks);
        }

    }

    public static final ResourceLocation           TEXTURE  = new ResourceLocation(PokecubeMod.ID,
            "textures/gui/pokewatchgui.png");
    public static List<Class<? extends WatchPage>> PAGELIST = Lists.newArrayList();

    static
    {
        GuiPokeWatch.PAGELIST.add(StartPage.class);
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
            PokecubeCore.LOGGER.error("Error with making a page for watch", e);
            return null;
        }
    }

    public WatchPage current_page = null;

    public final IPokemob     pokemob;
    public final PlayerEntity player;
    public int                index = 0;

    public GuiPokeWatch(final PlayerEntity player, final IPokemob pokemob)
    {
        super(new TranslationTextComponent("pokecube.watch"));
        this.pokemob = pokemob;
        if (this.pokemob != null)
        {
            PacketPokedex.sendInspectPacket(this.pokemob);
            PacketPokedex.updateWatchEntry(this.pokemob.getPokedexEntry());
        }
        this.player = player;

        if (GuiPokeWatch.lastPage >= 0 && GuiPokeWatch.lastPage < GuiPokeWatch.PAGELIST.size()) this.current_page = this
                .createPage(GuiPokeWatch.lastPage);
        else if (!GuiPokeWatch.PAGELIST.isEmpty()) this.current_page = this.createPage(GuiPokeWatch.lastPage = 0);
        else this.current_page = new MissingPage(this);
        PacketPokedex.sendLocationSpawnsRequest();
    }

    public boolean canEdit(final IPokemob pokemob)
    {
        return pokemob.getEntity().addedToChunk && (pokemob.getOwner() == this.player
                || this.player.abilities.isCreativeMode);
    }

    public void changePage(final int newIndex)
    {
        if (newIndex == this.index) return;
        if (this.current_page != null) this.current_page.onPageClosed();
        this.index = newIndex;
        this.current_page = this.createPage(this.index);
        GuiPokeWatch.lastPage = this.index;
        this.current_page.init(this.minecraft, this.width, this.height);
        this.current_page.onPageOpened();
    }

    public WatchPage createPage(final int index)
    {
        return GuiPokeWatch.makePage(GuiPokeWatch.PAGELIST.get(index), this);
    }

    private void handleError(final Exception e)
    {
        PokecubeCore.LOGGER.warn("Error with page " + this.current_page.getTitle(), e);
        try
        {
            this.current_page.onPageClosed();
        }
        catch (final Exception e1)
        {
        }
        this.current_page.init();
        this.current_page.onPageOpened();
    }

    @Override
    public void init()
    {
        super.init();
        this.current_page.init();
        final int x = this.width / 2;
        final int y = this.height / 2 - 5;
        final String next = I18n.format("block.pc.next");
        final String prev = I18n.format("block.pc.previous");
        final String home = I18n.format("pokewatch.button.home");
        this.addButton(new Button(x + 26, y + 69, 50, 12, next, b ->
        {
            int index = this.index;
            if (index < GuiPokeWatch.PAGELIST.size() - 1) index++;
            else index = 0;
            this.changePage(index);
        }));
        this.addButton(new Button(x - 76, y + 69, 50, 12, prev, b ->
        {
            int index = this.index;
            if (index > 0) index--;
            else index = GuiPokeWatch.PAGELIST.size() - 1;
            this.changePage(index);
        }));
        this.addButton(new Button(x - 25, y + 69, 50, 12, home, b ->
        {
            final int index = 0;
            this.changePage(index);
        }));
        this.current_page.onPageOpened();
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks)
    {
        this.minecraft.textureManager.bindTexture(GuiPokeWatch.TEXTURE);
        final int j2 = (this.width - 160) / 2;
        final int k2 = (this.height - 160) / 2;
        this.blit(j2, k2, 0, 0, 160, 160);
        super.render(mouseX, mouseY, partialTicks);
        try
        {
            this.current_page.render(mouseX, mouseY, partialTicks);
        }
        catch (final Exception e)
        {
            this.handleError(e);
        }
    }
}
