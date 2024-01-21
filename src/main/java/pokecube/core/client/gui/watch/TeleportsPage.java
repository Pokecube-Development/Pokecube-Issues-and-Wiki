package pokecube.core.client.gui.watch;

import java.util.List;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import pokecube.api.entity.pokemob.commandhandlers.TeleportHandler;
import pokecube.core.client.gui.helper.INotifiedEntry;
import pokecube.core.client.gui.helper.ListEditBox;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.helper.TexButton;
import pokecube.core.client.gui.watch.TeleportsPage.TeleOption;
import pokecube.core.client.gui.watch.util.ListPage;
import pokecube.core.network.packets.PacketPokedex;
import thut.api.entity.teleporting.TeleDest;
import thut.lib.TComponent;

public class TeleportsPage extends ListPage<TeleOption>
{
    public static class TeleOption extends AbstractSelectionList.Entry<TeleOption> implements INotifiedEntry
    {
        final TeleportsPage parent;
        final int offsetY;
        final Minecraft mc;
        final TeleDest dest;
        final EditBox text;
        final Button delete;
        final Button confirm;
        final Button moveUp;
        final Button moveDown;
        final int guiHeight;

        public TeleOption(final Minecraft mc, final int offsetY, final TeleDest dest, final EditBox text,
                final int height, final TeleportsPage parent)
        {
            this.dest = dest;
            this.text = text;
            this.mc = mc;
            this.offsetY = offsetY;
            this.guiHeight = height;
            this.parent = parent;
            this.confirm = new Button(0, 0, 10, 10, TComponent.literal("Y"), b -> {
                b.playDownSound(this.mc.getSoundManager());
                // Send packet for removal server side
                PacketPokedex.sendRemoveTelePacket(this.dest.index);
                // Also remove it client side so we update now.
                TeleportHandler.unsetTeleport(this.dest.index, this.parent.watch.player.getStringUUID());
                // Update the list for the page.
                this.parent.initList();
            }, (b, pose, x, y) -> {
                if (!b.active) return;
                Component tooltip = TComponent.translatable("pokecube.gui.delete.confirm.desc");
                parent.renderTooltip(pose, tooltip, x, y);
            });
            this.delete = new Button(0, 0, 10, 10, TComponent.literal("x"), b -> {
                b.playDownSound(this.mc.getSoundManager());
                this.confirm.active = !this.confirm.active;
            }, (b, pose, x, y) -> {
                if (!b.active) return;
                Component tooltip = TComponent.translatable("pokecube.gui.delete.start.desc");
                parent.renderTooltip(pose, tooltip, x, y);
            });
            this.delete.setFGColor(0xFFFF0000);
            this.confirm.active = false;
            this.moveUp = new Button(0, 0, 10, 10, TComponent.literal("\u21e7"), b -> {
                b.playDownSound(this.mc.getSoundManager());
                this.parent.scheduleUpdate(() -> {
                    PacketPokedex.sendReorderTelePacket(this.dest.index, this.dest.index - 1);
                    // Update the list for the page.
                    this.parent.initList();
                });
            }, (b, pose, x, y) -> {
                if (!b.active) return;
                Component tooltip = TComponent.translatable("pokecube.gui.move.up.desc");
                parent.renderTooltip(pose, tooltip, x, y);
            });
            this.moveDown = new Button(0, 0, 10, 10, TComponent.literal("\u21e9"), b -> {
                b.playDownSound(this.mc.getSoundManager());
                this.parent.scheduleUpdate(() -> {
                    PacketPokedex.sendReorderTelePacket(this.dest.index, this.dest.index + 1);
                    // Update the list for the page.
                    this.parent.initList();
                });
            }, (b, pose, x, y) -> {
                if (!b.active) return;
                Component tooltip = TComponent.translatable("pokecube.gui.move.down.desc");
                parent.renderTooltip(pose, tooltip, x, y);
            });
            this.moveUp.active = dest.index != 0;
            this.moveDown.active = dest.index != parent.locations.size() - 1;

            @SuppressWarnings("unchecked")
            final List<GuiEventListener> list = (List<GuiEventListener>) parent.children();
            // Add us first so we can add linker-clicking to the location field
            list.add(this);
            this.addOrRemove(parent::addRenderableWidget);
        }

        @Override
        public void addOrRemove(Consumer<AbstractWidget> remover)
        {
            this.delete.visible = false;
            this.confirm.visible = false;
            this.moveUp.visible = false;
            this.moveDown.visible = false;
            this.text.visible = false;

            remover.accept(this.delete);
            remover.accept(this.confirm);
            remover.accept(this.moveUp);
            remover.accept(this.moveDown);
            remover.accept(this.text);
        }

        @Override
        public boolean keyPressed(final int keyCode, final int p_keyPressed_2_, final int p_keyPressed_3_)
        {
            if (this.text.isFocused())
            {
                if (keyCode == GLFW.GLFW_KEY_ENTER)
                {
                    if (!this.text.getValue().equals(this.dest.getName()))
                    {
                        PacketPokedex.sendRenameTelePacket(this.text.getValue(), this.dest.index);
                        this.dest.setName(this.text.getValue());
                        this.text.setFocused(false);
                        return true;
                    }
                    return false;
                }
                return this.text.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
            }
            return super.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
        }

        @Override
        public boolean charTyped(final char typedChar, final int keyCode)
        {
            if (this.text.isFocused()) return this.text.charTyped(typedChar, keyCode);

            if (keyCode == GLFW.GLFW_KEY_ENTER) if (!this.text.getValue().equals(this.dest.getName()))
            {
                PacketPokedex.sendRenameTelePacket(this.text.getValue(), this.dest.index);
                this.dest.setName(this.text.getValue());
                this.text.setFocused(false);
                return true;
            }
            return super.charTyped(typedChar, keyCode);
        }

        @Override
        public void preRender(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
                boolean isSelected, float partialTicks)
        {
            this.delete.visible = false;
            this.confirm.visible = false;
            this.moveUp.visible = false;
            this.moveDown.visible = false;
            this.text.visible = false;
        }

        @Override
        public void render(final PoseStack mat, final int slotIndex, final int y, final int x, final int listWidth,
                final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected,
                final float partialTicks)
        {
            this.delete.visible = true;
            this.confirm.visible = true;
            this.moveUp.visible = true;
            this.moveDown.visible = true;
            this.text.visible = true;

            this.text.x = x - 2;
            this.text.y = y - 4;
            this.delete.y = y - 5;
            this.delete.x = x - 1 + this.text.getWidth();
            this.confirm.y = y - 5;
            this.confirm.x = x - 2 + 10 + this.text.getWidth();
            this.moveUp.y = y - 5;
            this.moveUp.x = x - 2 + 18 + this.text.getWidth();
            this.moveDown.y = y - 5;
            this.moveDown.x = x - 2 + 26 + this.text.getWidth();
        }
    }

    public static final ResourceLocation TEX_DM = GuiPokeWatch.makeWatchTexture("pokewatchgui_teleport");
    public static final ResourceLocation TEX_NM = GuiPokeWatch.makeWatchTexture("pokewatchgui_teleport_nm");

    protected List<TeleDest> locations;

    public TeleportsPage(final GuiPokeWatch watch)
    {
        super(TComponent.translatable("pokewatch.title.teleports"), watch, TeleportsPage.TEX_DM, TeleportsPage.TEX_NM);
    }

    @Override
    public void onPageOpened()
    {
        super.onPageOpened();
        final int x = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 90;
        final int y = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 30;

        this.addRenderableWidget(new TexButton(x - 108, y + 102, 17, 17,
                TComponent.literal(""), b ->
        {
            GuiPokeWatch.nightMode = !GuiPokeWatch.nightMode;
            this.watch.init();
        }).setTex(GuiPokeWatch.getWidgetTex()).setRender(new TexButton.UVImgRender(110, 72, 17, 17)));
    }

    protected double scroll = 0;

    @Override
    public void initList()
    {
        if (this.list != null) this.scroll = this.list.getScrollAmount();
        super.initList();
        this.locations = TeleportHandler.getTeleports(this.watch.player.getStringUUID());
        final int offsetX = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 14;
        final int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 37;
        final int height = 100; // 10 teleport lines
        final int width = 230; // 260

        if (GuiPokeWatch.nightMode)
        {
            this.list = new ScrollGui<TeleOption>(this, this.minecraft, width, height, 10, offsetX, offsetY)
                    .setScrollBarColor(255, 150, 79)
                    .setScrollBarDarkBorder(211, 81, 29)
                    .setScrollBarGrayBorder(244, 123, 58)
                    .setScrollBarLightBorder(255, 190, 111)
                    .setScrollColor(244, 123, 58)
                    .setScrollDarkBorder(211, 81, 29)
                    .setScrollLightBorder(255, 190, 111);
        } else {
            this.list = new ScrollGui<TeleOption>(this, this.minecraft, width, height, 10, offsetX, offsetY)
                    .setScrollBarColor(83, 175, 255)
                    .setScrollBarDarkBorder(39, 75, 142)
                    .setScrollBarGrayBorder(69, 132, 249)
                    .setScrollBarLightBorder(255, 255, 255)
                    .setScrollColor(69, 132, 249)
                    .setScrollDarkBorder(39, 75, 142)
                    .setScrollLightBorder(255, 255, 255);
        }

        for (final TeleDest d : this.locations)
        {
            final EditBox name = new ListEditBox(this.font, 0, 0, 104, 10, TComponent.literal(""))
                    .registerPreFocus(this);
            name.setValue(d.getName());
            this.list.addEntry(new TeleOption(this.minecraft, offsetY, d, name, height, this));
        }
        this.children.add(this.list);
        this.list.setScrollAmount(this.scroll);
    }
}
