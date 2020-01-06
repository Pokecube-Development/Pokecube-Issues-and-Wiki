package pokecube.core.client.gui.pokemob;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.CompoundNBT;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.client.gui.helper.GuardEntry;
import pokecube.core.client.gui.helper.RouteEditHelper;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.handlers.events.EventsHandler;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.packets.PacketSyncRoutes;
import pokecube.core.network.pokemobs.PacketPokemobGui;

public class GuiPokemobRoutes extends GuiPokemobBase
{
    final PlayerInventory    playerInventory;
    final IInventory         pokeInventory;
    final IPokemob           pokemob;
    final Entity             entity;
    final IGuardAICapability guard;
    ScrollGui<GuardEntry>    list;
    int                      num;

    public GuiPokemobRoutes(final ContainerPokemob container, final PlayerInventory inv)
    {
        super(container, inv);
        this.pokemob = container.pokemob;
        this.playerInventory = inv;
        this.pokeInventory = this.pokemob.getInventory();
        this.entity = this.pokemob.getEntity();
        this.guard = this.entity.getCapability(EventsHandler.GUARDAI_CAP, null).orElse(null);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY)
    {
        super.renderBackground();
        final int k = (this.width - this.xSize) / 2;
        final int l = (this.height - this.ySize) / 2;
        final String number = this.num + "";
        this.font.drawString(number, k + 87 - this.font.getStringWidth(number), l + 62, 0xFF888888);
    }

    @Override
    public void init()
    {
        super.init();
        this.buttons.clear();
        final int xOffset = this.width / 2 - 10;
        final int yOffset = this.height / 2 - 77;
        this.addButton(new Button(xOffset + 60, yOffset, 30, 10, I18n.format("pokemob.gui.inventory"),
                b -> PacketPokemobGui.sendPagePacket(PacketPokemobGui.MAIN, this.entity.getEntityId())));
        this.addButton(new Button(xOffset + 30, yOffset, 30, 10, I18n.format("pokemob.gui.storage"),
                b -> PacketPokemobGui.sendPagePacket(PacketPokemobGui.STORAGE, this.entity.getEntityId())));
        this.addButton(new Button(xOffset + 00, yOffset, 30, 10, I18n.format("pokemob.gui.ai"), b -> PacketPokemobGui
                .sendPagePacket(PacketPokemobGui.AI, this.entity.getEntityId())));
        this.list = new ScrollGui<>(this, this.minecraft, 100, 50, 50, xOffset - 5, yOffset);
        final Function<CompoundNBT, CompoundNBT> function = t ->
        {
            PacketSyncRoutes.sendServerPacket(GuiPokemobRoutes.this.entity, t);
            return t;
        };
        final List<GuardEntry> entries = Lists.newArrayList();
        final int dx = 0;
        final int dy = 14;
        RouteEditHelper.getGuiList(entries, this.guard, function, this.entity, this, 60, dx, dy, 50);

        this.list.smoothScroll = false;
        this.addButton(new Button(xOffset + 45, yOffset + 54, 30, 10, "\u21e7", b ->
        {
            this.list.scroll(-50);
            this.num = (int) (this.list.getScrollAmount() / 50);
        }));
        this.addButton(new Button(xOffset + 15, yOffset + 54, 30, 10, "\u21e9", b ->
        {
            this.list.scroll(50);
            this.num = (int) (this.list.getScrollAmount() / 50);
        }));
        this.children.add(this.list);
    }

    @Override
    public void render(final int i, final int j, final float f)
    {
        super.render(i, j, f);
        this.list.render(i, j, f);
        this.renderHoveredToolTip(i, j);
    }
}
