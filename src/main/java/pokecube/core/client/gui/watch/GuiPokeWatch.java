package pokecube.core.client.gui.watch;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
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

        public MissingPage(final GuiPokeWatch watch)
        {
            super(new TranslationTextComponent("pokewatch.title.blank"), watch);
            this.font = Minecraft.getInstance().fontRenderer;
        }

        @Override
        public void render(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
        {
            final int x = (this.watch.width - 160) / 2 + 80;
            final int y = (this.watch.height - 160) / 2 + 70;
            AbstractGui.drawCenteredString(mat, this.font, I18n.format("pokewatch.title.blank"), x, y, 0xFFFFFFFF);
            super.render(mat, mouseX, mouseY, partialTicks);
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

    public static int GUIW = 160;
    public static int GUIH = 160;

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
            // Change to the pokemob info page on opening
            GuiPokeWatch.lastPage = 1;
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
        return pokemob.getEntity().addedToChunk && (this.player.getUniqueID().equals(pokemob.getOwnerId())
                || this.player.abilities.isCreativeMode);
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
        this.current_page = this.createPage(this.index);
        GuiPokeWatch.lastPage = this.index;
        this.current_page.init(this.minecraft, this.width, this.height);
        this.current_page.onPageOpened();
        this.setFocusedDefault(this.current_page);
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
    public boolean keyPressed(final int keyCode, final int b, final int c)
    {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc())
        {
            this.onClose();
            this.closeScreen();
            return true;
        }

        // We overwrite this to reverse the ordering of checking if tab was
        // pressed
        final boolean subpages = this.getListener() != null && this.getListener().keyPressed(keyCode, b, c);

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
        super.init();
        this.current_page = this.createPage(GuiPokeWatch.lastPage);
        this.current_page.init();
        final int x = this.width / 2;
        final int y = this.height / 2 - 5;
        final ITextComponent next = new TranslationTextComponent("block.pc.next");
        final ITextComponent prev = new TranslationTextComponent("block.pc.previous");
        final ITextComponent home = new TranslationTextComponent("pokewatch.button.home");
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
            this.changePage(index, true);
        }));
        this.current_page.onPageOpened();
    }

    @Override
    public void render(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        // TODO remove these sets once final sizes are chosen
        GuiPokeWatch.GUIW = 160;
        GuiPokeWatch.GUIH = 160;

        this.minecraft.textureManager.bindTexture(GuiPokeWatch.TEXTURE);
        final int j2 = (this.width - GuiPokeWatch.GUIW) / 2;
        final int k2 = (this.height - GuiPokeWatch.GUIH) / 2;
        this.blit(mat, j2, k2, 0, 0, GuiPokeWatch.GUIW, GuiPokeWatch.GUIH);
        super.render(mat, mouseX, mouseY, partialTicks);
        try
        {
            this.current_page.render(mat, mouseX, mouseY, partialTicks);
        }
        catch (final Exception e)
        {
            this.handleError(e);
        }
    }
}
