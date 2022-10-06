package pokecube.core.client.gui.pokemob.tabs;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.client.gui.helper.GuardEntry;
import pokecube.core.client.gui.helper.RouteEditHelper;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.pokemob.GuiPokemob;
import pokecube.core.network.packets.PacketSyncRoutes;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.core.utils.CapHolders;
import thut.lib.TComponent;

public class Routes extends Tab
{
    Entity entity;
    IGuardAICapability guard;
    ScrollGui<GuardEntry> list;
    int num;

    public Routes(GuiPokemob parent)
    {
        super(parent, "routes");
        this.entity = this.menu.pokemob.getEntity();
        this.guard = this.entity.getCapability(CapHolders.GUARDAI_CAP, null).orElse(null);
    }

    @Override
    public void setEnabled(boolean active)
    {
        super.setEnabled(active);
        if (!active)
        {
            this.parent.children.remove(this.list);
        }
        else
        {
            this.parent.children.add(this.list);
            this.menu.setMode(PacketPokemobGui.ROUTES);
        }
    }

    @Override
    public void init()
    {
        final int xOffset = this.width / 2 - 10;
        final int yOffset = this.height / 2 - 77;

        this.list = new ScrollGui<>(parent, this.parent.minecraft, 92, 50, 50, xOffset, yOffset + 10);
        this.list.scrollBarDx = 0;
        this.list.scrollBarDy = 3;

        final Function<CompoundTag, CompoundTag> function = t -> {
            PacketSyncRoutes.sendServerPacket(Routes.this.entity, t);
            return t;
        };
        final int dx = 3;
        final int dy = 25;
        RouteEditHelper.getGuiList(this.list, this.guard, function, this.entity, this.parent, 60, dx, dy, 50);

        for (var entry : list.children)
        {
            parent.removeWidget(entry);
            entry.addOrRemove(this.parent::removeWidget);
            entry.addOrRemove(this::addRenderableWidget);
        }

        this.list.smoothScroll = false;
        this.addRenderableWidget(new Button(xOffset + 45, yOffset + 54, 30, 10, TComponent.literal("\u21e7"), b -> {
            this.list.scroll(-50);
        }));
        this.addRenderableWidget(new Button(xOffset + 15, yOffset + 54, 30, 10, TComponent.literal("\u21e9"), b -> {
            this.list.scroll(50);
        }));
    }

    @Override
    public void render(PoseStack mat, int x, int y, float f)
    {
        this.list.render(mat, x, y, f);
    }

    @Override
    public void renderBg(PoseStack mat, float partialTicks, int mouseX, int mouseY)
    {
        super.renderBg(mat, partialTicks, mouseX, mouseY);
        final int k = (this.width - this.imageWidth) / 2;
        final int l = (this.height - this.imageHeight) / 2;
        this.num = (int) (this.list.getScrollAmount() / 50);
        final String number = this.num + "";
        parent.font.draw(mat, number, k + 87 - parent.font.width(number), l + 62, 0xFF888888);
    }

    @Override
    public boolean keyPressed(int code, int unk1, int unk2)
    {
        return list.keyPressed(code, unk1, unk2);
    }
}