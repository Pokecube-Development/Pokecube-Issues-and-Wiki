package pokecube.adventures.client.gui.items.editor;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.client.gui.items.editor.pages.AI;
import pokecube.adventures.client.gui.items.editor.pages.Messages;
import pokecube.adventures.client.gui.items.editor.pages.Pokemob;
import pokecube.adventures.client.gui.items.editor.pages.Rewards;
import pokecube.adventures.client.gui.items.editor.pages.Routes;
import pokecube.adventures.client.gui.items.editor.pages.Trades;
import pokecube.adventures.client.gui.items.editor.pages.Trainer;
import pokecube.adventures.client.gui.items.editor.pages.util.Page;
import pokecube.core.PokecubeCore;

public class EditorGui extends Screen
{
    private static class MissingPage extends Page
    {

        public MissingPage(final EditorGui watch)
        {
            super(new TranslationTextComponent("pokewatch.title.blank"), watch);
            this.font = Minecraft.getInstance().fontRenderer;
        }

        @Override
        public void render(final int mouseX, final int mouseY, final float partialTicks)
        {
            final int x = (this.parent.width - 160) / 2 + 80;
            final int y = (this.parent.height - 160) / 2 + 70;
            this.drawCenteredString(this.font, I18n.format("pokewatch.title.blank"), x, y, 0xFFFFFFFF);
            super.render(mouseX, mouseY, partialTicks);
        }

    }

    public static final ResourceLocation      TEXTURE  = new ResourceLocation(PokecubeAdv.ID,
            "textures/gui/traineredit.png");
    public static List<Class<? extends Page>> PAGELIST = Lists.newArrayList();

    static
    {
        EditorGui.PAGELIST.add(Trainer.class);
        EditorGui.PAGELIST.add(AI.class);
        EditorGui.PAGELIST.add(Messages.class);
        EditorGui.PAGELIST.add(Pokemob.class);
        EditorGui.PAGELIST.add(Rewards.class);
        EditorGui.PAGELIST.add(Routes.class);
        EditorGui.PAGELIST.add(Trades.class);
    }

    public static int lastPage = -1;

    private static Page makePage(final Class<? extends Page> clazz, final EditorGui parent)
    {
        try
        {
            return clazz.getConstructor(EditorGui.class).newInstance(parent);
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error with making a page for watch", e);
            return null;
        }
    }

    public Page current_page = null;

    // public final IPokemob pokemob;
    // public final PlayerEntity player;
    public int index = 0;

    protected EditorGui(final ITextComponent titleIn)
    {
        super(titleIn);
        // TODO Auto-generated constructor stub
    }

    public void changePage(final int newIndex)
    {
        if (newIndex == this.index) return;
        if (this.current_page != null) this.current_page.onPageClosed();
        this.index = newIndex;
        this.current_page = this.createPage(this.index);
        EditorGui.lastPage = this.index;
        this.current_page.init(this.minecraft, this.width, this.height);
        this.current_page.onPageOpened();
    }

    public Page createPage(final int index)
    {
        return EditorGui.makePage(EditorGui.PAGELIST.get(index), this);
    }

}
