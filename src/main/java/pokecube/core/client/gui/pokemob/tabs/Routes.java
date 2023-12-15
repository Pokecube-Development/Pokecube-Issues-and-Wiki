package pokecube.core.client.gui.pokemob.tabs;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
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
import pokecube.core.utils.Resources;
import thut.lib.TComponent;

public class Routes extends Tab
{
    Entity entity;
    IGuardAICapability guard;
    ScrollGui<GuardEntry> list;
    List<EditBox> locations = Lists.newArrayList();
    List<EditBox> timeperiods = Lists.newArrayList();
    List<EditBox> variations = Lists.newArrayList();
    int num;

    Runnable callback = () -> {
        if (Minecraft.getInstance().screen == this.parent)
        {
            // This does 1 set of disabling.
            this.setEnabled(false);
            // then we clear
            this.clear();
            // Then re-initialise
            this.init();
            // Then re-add the callback
            this.guard.attachChangeListener(this.callback);
            // and finally re-enable.
            this.setEnabled(true);
        }
    };

    public Routes(GuiPokemob parent)
    {
        super(parent, "routes");
        this.entity = this.menu.pokemob.getEntity();
        this.guard = CapHolders.getGuardAI(entity);
        this.guard.attachChangeListener(callback);
        this.icon = Resources.TAB_ICON_ROUTES;
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
        locations.clear();
        timeperiods.clear();
        variations.clear();

        final int xOffset = this.width / 2 - 10;
        final int yOffset = this.height / 2 - 78;

        this.list = new ScrollGui<>(parent, this.parent.minecraft, 95, 52, 52, xOffset, yOffset + 10);
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

            this.locations.add(entry.location);
            this.timeperiods.add(entry.timeperiod);
            this.variations.add(entry.variation);
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
    public void renderLabels(PoseStack mat, int mouseX, int mouseY)
    {
        int x = 0;
        int y = 20;
        AtomicBoolean rendered = new AtomicBoolean(false);
        locations.forEach(box -> {
            if (box.isVisible() && box.isMouseOver(mouseX, mouseY))
            {
                this.parent.renderTooltip(mat, TComponent.translatable("pokemob.route.location.tooltip"), x, y);
                rendered.set(true);
            }
        });
        // Only render if didn't overlap previous box
        if (!rendered.get()) timeperiods.forEach(box -> {
            if (box.isVisible() && box.isMouseOver(mouseX, mouseY))
            {
                this.parent.renderTooltip(mat, TComponent.translatable("pokemob.route.timeperiod.tooltip"), x, y);
                rendered.set(true);
            }
        });
        // Only render if didn't overlap previous box
        if (!rendered.get()) variations.forEach(box -> {
            if (box.isVisible() && box.isMouseOver(mouseX, mouseY))
            {
                this.parent.renderTooltip(mat, TComponent.translatable("pokemob.route.variation.tooltip"), x, y);
                rendered.set(true);
            }
        });
        super.renderLabels(mat, mouseX, mouseY);
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