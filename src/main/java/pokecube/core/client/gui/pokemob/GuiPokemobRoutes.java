package pokecube.core.client.gui.pokemob;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.client.gui.helper.GuardEntry;
import pokecube.core.client.gui.helper.RouteEditHelper;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.packets.PacketSyncRoutes;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.core.utils.CapHolders;

public class GuiPokemobRoutes extends GuiPokemobBase
{
    final Inventory    playerInventory;
    final Container         pokeInventory;
    final IPokemob           pokemob;
    final Entity             entity;
    final IGuardAICapability guard;
    ScrollGui<GuardEntry>    list;
    int                      num;

    public GuiPokemobRoutes(final ContainerPokemob container, final Inventory inv)
    {
        super(container, inv);
        this.pokemob = container.pokemob;
        this.playerInventory = inv;
        this.pokeInventory = this.pokemob.getInventory();
        this.entity = this.pokemob.getEntity();
        this.guard = this.entity.getCapability(CapHolders.GUARDAI_CAP, null).orElse(null);
        container.setMode(PacketPokemobGui.ROUTES);
    }

    @Override
    protected void renderBg(final PoseStack mat, final float partialTicks, final int mouseX, final int mouseY)
    {
        super.renderBg(mat, partialTicks, mouseX, mouseY);
        final int k = (this.width - this.imageWidth) / 2;
        final int l = (this.height - this.imageHeight) / 2;
        final String number = this.num + "";
        this.font.draw(mat, number, k + 87 - this.font.width(number), l + 62, 0xFF888888);
    }

    @Override
    public void init()
    {
        super.init();
        this.renderables.clear();
        final int xOffset = this.width / 2 - 10;
        final int yOffset = this.height / 2 - 77;
        this.addRenderableWidget(new Button(xOffset + 60, yOffset, 30, 10, new TranslatableComponent("pokemob.gui.inventory"),
                b -> PacketPokemobGui.sendPagePacket(PacketPokemobGui.MAIN, this.entity.getId())));
        this.addRenderableWidget(new Button(xOffset + 30, yOffset, 30, 10, new TranslatableComponent("pokemob.gui.storage"),
                b -> PacketPokemobGui.sendPagePacket(PacketPokemobGui.STORAGE, this.entity.getId())));
        this.addRenderableWidget(new Button(xOffset + 00, yOffset, 30, 10, new TranslatableComponent("pokemob.gui.ai"),
                b -> PacketPokemobGui.sendPagePacket(PacketPokemobGui.AI, this.entity.getId())));

        this.list = new ScrollGui<>(this, this.minecraft, 92, 50, 50, xOffset, yOffset + 10);
        final Function<CompoundTag, CompoundTag> function = t ->
        {
            PacketSyncRoutes.sendServerPacket(GuiPokemobRoutes.this.entity, t);
            return t;
        };
        final int dx = 3;
        final int dy = 25;
        RouteEditHelper.getGuiList(this.list, this.guard, function, this.entity, this, 60, dx, dy, 50);

        this.list.smoothScroll = false;
        this.addRenderableWidget(new Button(xOffset + 45, yOffset + 54, 30, 10, new TextComponent("\u21e7"), b ->
        {
            this.list.scroll(-50);
            this.num = (int) (this.list.getScrollAmount() / 50);
        }));
        this.addRenderableWidget(new Button(xOffset + 15, yOffset + 54, 30, 10, new TextComponent("\u21e9"), b ->
        {
            this.list.scroll(50);
            this.num = (int) (this.list.getScrollAmount() / 50);
        }));
        this.children.add(this.list);
    }

    @Override
    public boolean keyPressed(final int keyCode, final int p_keyPressed_2_, final int p_keyPressed_3_)
    {
        this.name.setFocused(false);
        if (this.list.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_)) return true;
        return super.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    public void render(final PoseStack mat, final int i, final int j, final float f)
    {
        super.render(mat, i, j, f);
        this.list.render(mat, i, j, f);
        this.renderTooltip(mat, i, j);
    }
}
