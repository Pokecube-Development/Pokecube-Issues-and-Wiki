package pokecube.core.client.gui.watch.pokemob;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ClickEvent.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import pokecube.core.client.EventsHandlerClient;
import pokecube.core.client.gui.helper.ListHelper;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.helper.TexButton;
import pokecube.core.client.gui.helper.TexButton.UVImgRender;
import pokecube.core.client.gui.watch.GuiPokeWatch;
import pokecube.core.client.gui.watch.PokemobInfoPage;
import pokecube.core.client.gui.watch.util.LineEntry;
import pokecube.core.client.gui.watch.util.LineEntry.IClickListener;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.network.packets.PacketPokedex;

public class Description extends ListPage<LineEntry>
{
    public static final ResourceLocation TEX_DM = new ResourceLocation(PokecubeMod.ID,
            "textures/gui/pokewatchgui_desc.png");
    public static final ResourceLocation TEX_NM = new ResourceLocation(PokecubeMod.ID,
            "textures/gui/pokewatchgui_desc_nm.png");

    final PokemobInfoPage parent;

    public Description(final PokemobInfoPage parent)
    {
        super(parent, "description", Description.TEX_DM, Description.TEX_NM);
        this.parent = parent;
    }

    @Override
    public void init()
    {
        super.init();
        final PokedexEntry e = this.parent.pokemob.getPokedexEntry();
        if (PacketPokedex.haveConditions.contains(e))
        {
            final int x = this.watch.width / 2 + 10;
            final int y = this.watch.height / 2 + 22;
            final Component check_conditions = new TranslatableComponent("pokewatch.capture.check");
            final TexButton button = this.addRenderableWidget(new TexButton(x, y, 100, 12, check_conditions, b ->
            {
                PacketPokedex.sendCaptureCheck(e);
            }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new UVImgRender(0, 72, 100, 12)));
            button.setFGColor(0x444444);
        }
    }

    @Override
    public boolean handleComponentClicked(final Style component)
    {
        if (component != null)
        {
            final ClickEvent clickevent = component.getClickEvent();
            if (clickevent != null) if (clickevent.getAction() == Action.CHANGE_PAGE)
            {
                final PokedexEntry entry = Database.getEntry(clickevent.getValue());
                if (entry != null && entry != this.parent.pokemob.getPokedexEntry()) this.parent.initPages(
                        EventsHandlerClient.getRenderMob(entry, this.watch.player.getLevel()));
                return true;
            }
        }
        return false;
    }

    @Override
    public void initList()
    {
        super.initList();
        int offsetX = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 90;
        int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 30;

        final int height = this.font.lineHeight * 8;
        final int dx = 49;
        final int dy = -1;
        offsetY += dy;
        offsetX += dx;

        final int textColour = 0x333333;

        final IClickListener listen = new IClickListener()
        {
            @Override
            public boolean handleClick(final Style component)
            {
                return Description.this.handleComponentClicked(component);
            }

            @Override
            public void handleHovor(final PoseStack mat, final Style component, final int x, final int y)
            {
                Description.this.renderComponentHoverEffect(mat, component, x, y);
            }
        };
        MutableComponent line;
        final MutableComponent page = (MutableComponent) this.parent.pokemob.getPokedexEntry()
                .getDescription();
        this.list = new ScrollGui<>(this, this.minecraft, 107, height, this.font.lineHeight, offsetX, offsetY);
        final List<MutableComponent> list = ListHelper.splitText(page, 100, this.font, false);
        for (final Component element : list)
        {
            line = (MutableComponent) element;
            this.list.addEntry(new LineEntry(this.list, 0, 0, this.font, line, textColour).setClickListner(listen));
        }
    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
    }
}