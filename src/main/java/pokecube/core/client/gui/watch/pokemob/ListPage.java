package pokecube.core.client.gui.watch.pokemob;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.resources.ResourceLocation;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.PokemobInfoPage;

public abstract class ListPage<T extends AbstractSelectionList.Entry<T>> extends PokeInfoPage
{
    ScrollGui<T> list;

    public ListPage(final PokemobInfoPage parent, final String title, final ResourceLocation day, final ResourceLocation night)
    {
        super(parent, title, day, night);
    }

    @Override
    void drawInfo(final PoseStack mat, final int mouseX, final int mouseY, final float partialTicks)
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
    public void render(final PoseStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
        super.render(mat, mouseX, mouseY, partialTicks);
        this.list.render(mat, mouseX, mouseY, partialTicks);
    }

}
