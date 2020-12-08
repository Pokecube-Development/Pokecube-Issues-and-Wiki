package pokecube.core.client.gui.watch;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.client.gui.watch.TeleportsPage.TeleOption;
import pokecube.core.client.gui.watch.util.ListPage;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.pokemob.commandhandlers.TeleportHandler;
import pokecube.core.network.packets.PacketPokedex;
import thut.api.entity.ThutTeleporter.TeleDest;

public class TeleportsPage extends ListPage<TeleOption>
{
    public static class TeleOption extends AbstractList.AbstractListEntry<TeleOption>
    {
        final TeleportsPage   parent;
        final int             offsetY;
        final Minecraft       mc;
        final TeleDest        dest;
        final TextFieldWidget text;
        final Button          delete;
        final Button          confirm;
        final Button          moveUp;
        final Button          moveDown;
        final int             guiHeight;

        public TeleOption(final Minecraft mc, final int offsetY, final TeleDest dest, final TextFieldWidget text,
                final int height, final TeleportsPage parent)
        {
            this.dest = dest;
            this.text = text;
            this.mc = mc;
            this.offsetY = offsetY;
            this.guiHeight = height;
            this.parent = parent;
            this.confirm = new Button(0, 0, 10, 10, new StringTextComponent("Y"), b ->
            {
                b.playDownSound(this.mc.getSoundHandler());
                // Send packet for removal server side
                PacketPokedex.sendRemoveTelePacket(this.dest.index);
                // Also remove it client side so we update now.
                TeleportHandler.unsetTeleport(this.dest.index, this.parent.watch.player.getCachedUniqueIdString());
                // Update the list for the page.
                this.parent.initList();
            });
            this.delete = new Button(0, 0, 10, 10, new StringTextComponent("x"), b ->
            {
                b.playDownSound(this.mc.getSoundHandler());
                this.confirm.active = !this.confirm.active;
            });
            this.delete.setFGColor(0xFFFF0000);
            this.confirm.active = false;
            this.moveUp = new Button(0, 0, 10, 10, new StringTextComponent("\u21e7"), b ->
            {
                b.playDownSound(this.mc.getSoundHandler());
                PacketPokedex.sendReorderTelePacket(this.dest.index, this.dest.index - 1);
                // Update the list for the page.
                this.parent.initList();
            });
            this.moveDown = new Button(0, 0, 10, 10, new StringTextComponent("\u21e9"), b ->
            {
                b.playDownSound(this.mc.getSoundHandler());
                PacketPokedex.sendReorderTelePacket(this.dest.index, this.dest.index + 1);
                // Update the list for the page.
                this.parent.initList();
            });
            this.moveUp.active = dest.index != 0;
            this.moveDown.active = dest.index != parent.locations.size() - 1;
        }

        @Override
        public boolean keyPressed(final int keyCode, final int p_keyPressed_2_, final int p_keyPressed_3_)
        {
            if (this.text.isFocused())
            {
                if (keyCode == GLFW.GLFW_KEY_ENTER)
                {
                    if (!this.text.getText().equals(this.dest.getName()))
                    {
                        PacketPokedex.sendRenameTelePacket(this.text.getText(), this.dest.index);
                        this.dest.setName(this.text.getText());
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

            if (keyCode == GLFW.GLFW_KEY_ENTER) if (!this.text.getText().equals(this.dest.getName()))
            {
                PacketPokedex.sendRenameTelePacket(this.text.getText(), this.dest.index);
                this.dest.setName(this.text.getText());
                return true;
            }
            return super.charTyped(typedChar, keyCode);
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseEvent)
        {
            boolean fits = true;
            fits = this.text.y >= this.offsetY;
            fits = fits && mouseX - this.text.x >= 0;
            fits = fits && mouseX - this.text.x <= this.text.getWidth();
            fits = fits && this.text.y + this.text.getHeightRealms() <= this.offsetY + this.guiHeight;
            this.text.setFocused(fits);
            if (this.delete.isMouseOver(mouseX, mouseY))
            {
                this.delete.playDownSound(this.mc.getSoundHandler());
                this.confirm.active = !this.confirm.active;
            }
            else if (this.confirm.isMouseOver(mouseX, mouseY) && this.confirm.active)
            {
                this.confirm.playDownSound(this.mc.getSoundHandler());
                // Send packet for removal server side
                PacketPokedex.sendRemoveTelePacket(this.dest.index);
                // Also remove it client side so we update now.
                TeleportHandler.unsetTeleport(this.dest.index, this.parent.watch.player.getCachedUniqueIdString());
                // Update the list for the page.
                this.parent.initList();
            }
            else if (this.moveUp.isMouseOver(mouseX, mouseY) && this.moveUp.active)
            {
                this.moveUp.playDownSound(this.mc.getSoundHandler());
                PacketPokedex.sendReorderTelePacket(this.dest.index, this.dest.index - 1);
                // Update the list for the page.
                this.parent.initList();
            }
            else if (this.moveDown.isMouseOver(mouseX, mouseY) && this.moveDown.active)
            {
                this.moveDown.playDownSound(this.mc.getSoundHandler());
                PacketPokedex.sendReorderTelePacket(this.dest.index, this.dest.index + 1);
                // Update the list for the page.
                this.parent.initList();
            }
            return fits;
        }

        @Override
        public void render(final MatrixStack mat, final int slotIndex, final int y, final int x, final int listWidth,
                final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected,
                final float partialTicks)
        {
            boolean fits = true;
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
            fits = this.text.y >= this.offsetY;
            fits = fits && this.text.y + this.text.getHeightRealms() <= this.offsetY + this.guiHeight;
            if (fits)
            {
                this.text.render(mat, mouseX, mouseY, partialTicks);
                this.delete.render(mat, mouseX, mouseY, partialTicks);
                this.confirm.render(mat, mouseX, mouseY, partialTicks);
                this.moveUp.render(mat, mouseX, mouseY, partialTicks);
                this.moveDown.render(mat, mouseX, mouseY, partialTicks);
            }
        }
    }

    protected List<TeleDest> locations;

    public TeleportsPage(final GuiPokeWatch watch)
    {
        super(new TranslationTextComponent("pokewatch.title.teleports"), watch);
    }

    public static final ResourceLocation           TEXTURE_BASE  = new ResourceLocation(PokecubeMod.ID,
    		"textures/gui/pokewatchgui_teleport.png");
    
    @Override
    public void render(final MatrixStack mat, final int mouseX, final int mouseY, final float partialTicks)
    {
    	this.minecraft.textureManager.bindTexture(TeleportsPage.TEXTURE_BASE);
    	int offsetX = (this.watch.width - GuiPokeWatch.GUIW) / 2;
        int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2;
    	this.blit(mat, offsetX, offsetY, 0, 0, GuiPokeWatch.GUIW, GuiPokeWatch.GUIH);
    }
    
    @Override
    public void initList()
    {
        super.initList();
        this.locations = TeleportHandler.getTeleports(this.watch.player.getCachedUniqueIdString());
        final int offsetX = (this.watch.width - GuiPokeWatch.GUIW) / 2 + 55;
        final int offsetY = (this.watch.height - GuiPokeWatch.GUIH) / 2 + 27;
        final int height = 120;
        final int width = 146;
        this.list = new ScrollGui<>(this, this.minecraft, width, height, 10, offsetX, offsetY);
        for (final TeleDest d : this.locations)
        {
            final TextFieldWidget name = new TextFieldWidget(this.font, 0, 0, 104, 10, new StringTextComponent(""));
            name.setText(d.getName());
            this.list.addEntry(new TeleOption(this.minecraft, offsetY, d, name, height, this));
        }
        this.children.add(this.list);
    }
}
