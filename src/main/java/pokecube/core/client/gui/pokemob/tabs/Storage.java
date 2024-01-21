package pokecube.core.client.gui.pokemob.tabs;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.utility.StoreTask;
import pokecube.core.client.gui.helper.TooltipArea;
import pokecube.core.client.gui.pokemob.GuiPokemob;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.core.network.pokemobs.PacketUpdateAI;
import pokecube.core.utils.Resources;
import thut.api.maths.Vector4;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

public class Storage extends Tab
{
    IPokemob pokemob;
    EditBox berry;
    EditBox storage;
    EditBox storageFace;
    EditBox empty;
    StoreTask ai;
    EditBox emptyFace;
    List<EditBox> textBoxes = Lists.newArrayList();

    public Storage(GuiPokemob parent)
    {
        super(parent, "storage");
        this.pokemob = menu.pokemob;
        this.ai = new StoreTask(this.pokemob);
        final CompoundTag tag = this.menu.data;
        this.ai.deserializeNBT(tag);
        this.icon = Resources.TAB_ICON_STORAGE;
    }

    @Override
    public void setEnabled(boolean active)
    {
        super.setEnabled(active);
        if (active)
        {
            this.menu.setMode(PacketPokemobGui.STORAGE);
        }
    }

    @Override
    public void init()
    {
        int xOffset = this.width / 2 - 10;
        final int yOffset = this.height / 2 - 77;
        xOffset += 29;
        final int dy = 13;
        final int ds = 10;

        this.addRenderableWidget(this.berry = new EditBox(parent.font, xOffset + 10, yOffset + dy + ds * 0, 50, 10,
                TComponent.literal("")));
        this.addRenderableWidget(this.storage = new EditBox(parent.font, xOffset + 10, yOffset + dy + ds * 1, 50, 10,
                TComponent.literal("")));
        this.addRenderableWidget(this.storageFace = new EditBox(parent.font, xOffset + 10, yOffset + dy + ds * 2, 50,
                10, TComponent.literal("")));
        this.addRenderableWidget(this.empty = new EditBox(parent.font, xOffset + 10, yOffset + dy + ds * 3, 50, 10,
                TComponent.literal("")));
        this.addRenderableWidget(this.emptyFace = new EditBox(parent.font, xOffset + 10, yOffset + dy + ds * 4, 50, 10,
                TComponent.literal("")));
        this.textBoxes = Lists.newArrayList(this.berry, this.storage, this.storageFace, this.empty, this.emptyFace);

        final CompoundTag nbt = this.ai.serializeNBT();
        final CompoundTag berry = nbt.getCompound("b");
        final CompoundTag storage = nbt.getCompound("s");
        final CompoundTag empty = nbt.getCompound("e");
        if (!berry.isEmpty())
            this.berry.setValue(berry.getInt("x") + " " + berry.getInt("y") + " " + berry.getInt("z"));
        if (!storage.isEmpty())
        {
            this.storage.setValue(storage.getInt("x") + " " + storage.getInt("y") + " " + storage.getInt("z"));
            this.storageFace.setValue(Direction.values()[storage.getByte("f")] + "");
        }
        else this.storageFace.setValue("UP");
        if (!empty.isEmpty())
        {
            this.empty.setValue(empty.getInt("x") + " " + empty.getInt("y") + " " + empty.getInt("z"));
            this.emptyFace.setValue(Direction.values()[empty.getByte("f")] + "");
        }
        else this.emptyFace.setValue("UP");

        final int k = (this.width - this.imageWidth) / 2;
        final int l = (this.height - this.imageHeight) / 2;
        this.addRenderableWidget(new TooltipArea(k + 63, l + 54, 16, 16,
                TComponent.translatable("pokemob.gui.slot.storage.off_hand"), (x, y) ->
                {
                    Slot offhand_slot = menu.slots.get(3);
                    if (offhand_slot.hasItem()) return false;
                    return PokecubeCore.getConfig().pokemobGuiTooltips;
                }, (b, pose, x, y) -> {
                    Component tooltip = b.getMessage();
                    var split = parent.font.split(tooltip, this.imageWidth);
                    parent.renderTooltip(pose, split, x, y);
                }).noAuto());
    }

    @Override
    public boolean keyPressed(int code, int unk1, int unk2)
    {
        if (code == GLFW.GLFW_KEY_ENTER)
        {
            this.sendUpdate();
            return true;
        }
        for (EditBox box : textBoxes) if (box.isFocused() && code != GLFW.GLFW_KEY_BACKSPACE
                && code != GLFW.GLFW_KEY_ESCAPE && !Screen.hasControlDown())
            return true;
        return false;
    }

    @Override
    public boolean charTyped(final char typedChar, final int keyCode)
    {
        final boolean ret = super.charTyped(typedChar, keyCode);
        if (keyCode == GLFW.GLFW_KEY_ENTER)
        {
            this.sendUpdate();
            return true;
        }
        return ret;
    }

    private BlockPos posFromText(final String text)
    {
        if (text.isEmpty()) return null;
        final String[] args = text.split(" ");
        if (args.length == 3) try
        {
            final int x = Integer.parseInt(args[0]);
            final int y = Integer.parseInt(args[1]);
            final int z = Integer.parseInt(args[2]);
            return new BlockPos(x, y, z);
        }
        catch (final NumberFormatException e)
        {
            // Send status message about not working here.
            System.err.println("Error with pos:" + text);
        }
        else if (args.length != 0) // Send status message about not working
            // here.
            System.err.println("Error with pos:" + text);
        return null;
    }

    private Direction dirFromText(String text)
    {
        text = ThutCore.trim(text);
        Direction dir = Direction.byName(text);
        if (dir == null)
        {
            if (!text.isEmpty()) // Send status message about not working here.
                System.err.println("Error with dir:" + text);
            dir = Direction.UP;
        }
        return dir;
    }

    /**
     * Draw the foreground layer for the ContainerScreen (everything in front of
     * the items)
     */
    @Override
    public void renderLabels(final PoseStack mat, final int mouseX, final int mouseY)
    {
        int x = 83;
        int y = 20;
        parent.font.draw(mat, I18n.get("pokemob.gui.berry"), x, y, 4210752);
        parent.font.draw(mat, I18n.get("pokemob.gui.store"), x, y + 10, 4210752);
        parent.font.draw(mat, I18n.get("pokemob.gui.face"), x, y + 20, 4210752);
        parent.font.draw(mat, I18n.get("pokemob.gui.empty"), x, y + 30, 4210752);
        parent.font.draw(mat, I18n.get("pokemob.gui.face"), x, y + 40, 4210752);
        y -= 5;

        if (this.berry.isMouseOver(mouseX, mouseY))
        {
            this.parent.renderTooltip(mat, TComponent.translatable("pokemob.gui.storage.berry"), x, y);
        }
        if (this.storage.isMouseOver(mouseX, mouseY))
        {
            this.parent.renderTooltip(mat, TComponent.translatable("pokemob.gui.storage.storage"), x, y);
        }
        if (this.storageFace.isMouseOver(mouseX, mouseY))
        {
            this.parent.renderTooltip(mat, TComponent.translatable("pokemob.gui.storage.storageFace"), x, y);
        }
        if (this.empty.isMouseOver(mouseX, mouseY))
        {
            this.parent.renderTooltip(mat, TComponent.translatable("pokemob.gui.storage.empty"), x, y);
        }
        if (this.emptyFace.isMouseOver(mouseX, mouseY))
        {
            this.parent.renderTooltip(mat, TComponent.translatable("pokemob.gui.storage.emptyFace"), x, y);
        }

        super.renderLabels(mat, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(final double x, final double y, final int mouseButton)
    {
        for (EditBox box : textBoxes) box.setFocused(false);
        final boolean ret = false;

        BlockPos newLink = null;
        boolean effect = false;
        final ItemStack carried = Minecraft.getInstance().player.containerMenu.getCarried();
        if (!carried.isEmpty() && carried.hasTag())
        {
            final CompoundTag link = carried.getTag().getCompound("link_pos");
            if (!link.isEmpty())
            {
                final Vector4 pos = new Vector4(link);
                newLink = new BlockPos((int) (pos.x - 0.5), (int) pos.y, (int) (pos.z - 0.5));
            }
        }
        for (final EditBox text : this.textBoxes)
        {
            if (newLink != null && text.isFocused()
                    && (text == this.berry || text == this.storage || text == this.empty))
            {
                text.setValue(newLink.getX() + " " + newLink.getY() + " " + newLink.getZ());
                effect = true;
            }
        }
        if (effect)
        {
            this.sendUpdate();
            return true;
        }
        return ret;
    }

    private void sendUpdate()
    {
        final BlockPos berryLoc = this.posFromText(this.berry.getValue());
        if (berryLoc == null) this.berry.setValue("");
        final BlockPos storageLoc = this.posFromText(this.storage.getValue());
        if (storageLoc == null) this.storage.setValue("");
        final BlockPos emptyInventory = this.posFromText(this.empty.getValue());
        if (emptyInventory == null) this.empty.setValue("");
        final Direction storageFace = this.dirFromText(this.storageFace.getValue());
        this.storageFace.setValue(storageFace + "");
        final Direction emptyFace = this.dirFromText(this.emptyFace.getValue());
        this.emptyFace.setValue(emptyFace + "");
        this.ai.berryLoc = berryLoc;
        this.ai.storageLoc = storageLoc;
        this.ai.storageFace = storageFace;
        this.ai.emptyFace = emptyFace;
        this.ai.emptyInventory = emptyInventory;
        PacketUpdateAI.sendUpdatePacket(this.pokemob, this.ai);

        // Send status message thingy
        this.parent.minecraft.player.displayClientMessage(TComponent.translatable("pokemob.gui.updatestorage"), true);
    }

    @Override
    public void renderBg(PoseStack mat, float partialTicks, int mouseX, int mouseY)
    {
        super.renderBg(mat, partialTicks, mouseX, mouseY);
        final int k = (this.width - this.imageWidth) / 2;
        final int l = (this.height - this.imageHeight) / 2;
        // The off-hand slot
        parent.blit(mat, k + 62, l + 53, 0, this.imageHeight + 72, 18, 18);
    }
}