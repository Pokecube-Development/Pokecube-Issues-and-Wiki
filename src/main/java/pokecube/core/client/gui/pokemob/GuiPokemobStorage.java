package pokecube.core.client.gui.pokemob;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.ai.tasks.utility.AIStoreStuff;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.core.network.pokemobs.PacketUpdateAI;
import thut.api.maths.Vector4;
import thut.core.common.ThutCore;

public class GuiPokemobStorage extends GuiPokemobBase
{
    final PlayerInventory playerInventory;
    final IInventory      pokeInventory;
    final IPokemob        pokemob;
    final Entity          entity;
    TextFieldWidget       berry;
    TextFieldWidget       storage;
    TextFieldWidget       storageFace;
    TextFieldWidget       empty;
    AIStoreStuff          ai;
    TextFieldWidget       emptyFace;
    List<TextFieldWidget> textBoxes = Lists.newArrayList();

    public GuiPokemobStorage(final ContainerPokemob container, final PlayerInventory playerInv)
    {
        super(container, playerInv);
        this.pokemob = container.pokemob;
        this.playerInventory = playerInv;
        this.pokeInventory = this.pokemob.getInventory();
        this.entity = this.pokemob.getEntity();
        this.ai = new AIStoreStuff(this.pokemob);
        final CompoundNBT tag = container.data.readCompoundTag();
        this.ai.deserializeNBT(tag);
        container.setMode(PacketPokemobGui.STORAGE);
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
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY)
    {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        final int x = 83;
        final int y = 20;
        this.font.drawString(I18n.format("pokemob.gui.berry"), x, y, 4210752);
        this.font.drawString(I18n.format("pokemob.gui.store"), x, y + 10, 4210752);
        this.font.drawString(I18n.format("pokemob.gui.face"), x, y + 20, 4210752);
        this.font.drawString(I18n.format("pokemob.gui.empty"), x, y + 30, 4210752);
        this.font.drawString(I18n.format("pokemob.gui.face"), x, y + 40, 4210752);
    }

    @Override
    public void init()
    {
        super.init();
        this.buttons.clear();
        int xOffset = this.width / 2 - 10;
        final int yOffset = this.height / 2 - 77;
        this.addButton(new Button(xOffset + 60, yOffset, 30, 10, I18n.format("pokemob.gui.inventory"),
                b -> PacketPokemobGui.sendPagePacket(PacketPokemobGui.MAIN, this.entity.getEntityId())));
        this.addButton(new Button(xOffset + 30, yOffset, 30, 10, I18n.format("pokemob.gui.ai"), b -> PacketPokemobGui
                .sendPagePacket(PacketPokemobGui.AI, this.entity.getEntityId())));
        this.addButton(new Button(xOffset + 00, yOffset, 30, 10, I18n.format("pokemob.gui.routes"),
                b -> PacketPokemobGui.sendPagePacket(PacketPokemobGui.ROUTES, this.entity.getEntityId())));
        xOffset += 29;
        final int dy = 13;
        final int ds = 10;
        this.addButton(this.berry = new TextFieldWidget(this.font, xOffset + 10, yOffset + dy + ds * 0, 50, 10, ""));
        this.addButton(this.storage = new TextFieldWidget(this.font, xOffset + 10, yOffset + dy + ds * 1, 50, 10, ""));
        this.addButton(this.storageFace = new TextFieldWidget(this.font, xOffset + 10, yOffset + dy + ds * 2, 50, 10,
                ""));
        this.addButton(this.empty = new TextFieldWidget(this.font, xOffset + 10, yOffset + dy + ds * 3, 50, 10, ""));
        this.addButton(this.emptyFace = new TextFieldWidget(this.font, xOffset + 10, yOffset + dy + ds * 4, 50, 10,
                ""));
        this.textBoxes = Lists.newArrayList(this.berry, this.storage, this.storageFace, this.empty, this.emptyFace);

        final CompoundNBT nbt = this.ai.serializeNBT();
        final CompoundNBT berry = nbt.getCompound("b");
        final CompoundNBT storage = nbt.getCompound("s");
        final CompoundNBT empty = nbt.getCompound("e");
        if (!berry.isEmpty()) this.berry.setText(berry.getInt("x") + " " + berry.getInt("y") + " " + berry.getInt("z"));
        if (!storage.isEmpty())
        {
            this.storage.setText(storage.getInt("x") + " " + storage.getInt("y") + " " + storage.getInt("z"));
            this.storageFace.setText(Direction.values()[storage.getByte("f")] + "");
        }
        else this.storageFace.setText("UP");
        if (!empty.isEmpty())
        {
            this.empty.setText(empty.getInt("x") + " " + empty.getInt("y") + " " + empty.getInt("z"));
            this.emptyFace.setText(Direction.values()[empty.getByte("f")] + "");
        }
        else this.emptyFace.setText("UP");

    }

    /**
     * Called when the mouse is clicked.
     *
     * @return
     */
    @Override
    public boolean mouseClicked(final double x, final double y, final int mouseButton)
    {
        final boolean ret = super.mouseClicked(x, y, mouseButton);
        try
        {
            BlockPos newLink = null;
            final PlayerInventory inv = this.playerInventory;
            boolean effect = false;
            if (!inv.getItemStack().isEmpty() && inv.getItemStack().hasTag())
            {
                final CompoundNBT link = inv.getItemStack().getTag().getCompound("link_pos");
                if (!link.isEmpty())
                {
                    final Vector4 pos = new Vector4(link);
                    newLink = new BlockPos((int) (pos.x - 0.5), (int) pos.y, (int) (pos.z - 0.5));
                }
            }
            for (final TextFieldWidget text : this.textBoxes)
            {
                if (newLink != null && text.isFocused() && (text == this.berry || text == this.storage
                        || text == this.empty))
                {
                    text.setText(newLink.getX() + " " + newLink.getY() + " " + newLink.getZ());
                    effect = true;
                }
                if (ret) effect = true;
            }
            if (effect)
            {
                this.sendUpdate();
                return true;
            }
        }
        catch (final Exception e)
        {
            e.printStackTrace();
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

    @Override
    public void render(final int i, final int j, final float f)
    {
        super.render(i, j, f);
        this.renderHoveredToolTip(i, j);
    }

    private void sendUpdate()
    {
        final BlockPos berryLoc = this.posFromText(this.berry.getText());
        if (berryLoc == null) this.berry.setText("");
        final BlockPos storageLoc = this.posFromText(this.storage.getText());
        if (storageLoc == null) this.storage.setText("");
        final BlockPos emptyInventory = this.posFromText(this.empty.getText());
        if (emptyInventory == null) this.empty.setText("");
        final Direction storageFace = this.dirFromText(this.storageFace.getText());
        this.storageFace.setText(storageFace + "");
        final Direction emptyFace = this.dirFromText(this.emptyFace.getText());
        this.emptyFace.setText(emptyFace + "");
        this.ai.berryLoc = berryLoc;
        this.ai.storageLoc = storageLoc;
        this.ai.storageFace = storageFace;
        this.ai.emptyFace = emptyFace;
        this.ai.emptyInventory = emptyInventory;
        PacketUpdateAI.sendUpdatePacket(this.pokemob, this.ai);

        // Send status message thingy
        this.minecraft.player.sendStatusMessage(new TranslationTextComponent("pokemob.gui.updatestorage"), true);
    }
}
