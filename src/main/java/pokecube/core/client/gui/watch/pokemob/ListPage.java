package pokecube.core.client.gui.watch.pokemob;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.util.ResourceLocation;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.PokemobInfoPage;

public abstract class ListPage<T extends AbstractList.AbstractListEntry<T>> extends PokeInfoPage
{
    ScrollGui<T> list;

    public ListPage(final PokemobInfoPage parent, final String title, final ResourceLocation day, final ResourceLocation night)
    {
        super(parent, title, day, night);
    }

    @Override
    void drawInfo(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {

    }

    @Override
    public void init()
    {
        super.init();
        this.initList();
        this.children.add(this.list);
    }

    public void initList()
    {
        if (this.list != null) this.children.remove(this.list);
    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
        this.initList();
        this.children.add(this.list);
    }

    @Override
    public void render(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        super.render(mat, mouseX, mouseY, partialTicks);
        this.list.render(mat, mouseX, mouseY, partialTicks);
    }

}
