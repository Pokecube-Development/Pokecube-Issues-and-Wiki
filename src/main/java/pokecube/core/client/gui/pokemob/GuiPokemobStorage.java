package pokecube.core.client.gui.pokemob;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import pokecube.core.ai.tasks.utility.StoreTask;
import pokecube.core.entity.pokemobs.ContainerPokemob;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.network.pokemobs.PacketPokemobGui;
import pokecube.core.network.pokemobs.PacketUpdateAI;
import thut.api.maths.Vector4;
import thut.core.common.ThutCore;

public class GuiPokemobStorage extends GuiPokemobBase
{
    final Inventory playerInventory;
    final Container      pokeInventory;
    final IPokemob        pokemob;
    final Entity          entity;
    EditBox       berry;
    EditBox       storage;
    EditBox       storageFace;
    EditBox       empty;
    StoreTask          ai;
    EditBox       emptyFace;
    List<EditBox> textBoxes = Lists.newArrayList();

    public GuiPokemobStorage(final ContainerPokemob container, final Inventory playerInv)
    {
        super(container, playerInv);
        this.pokemob = container.pokemob;
        this.playerInventory = playerInv;
        this.pokeInventory = this.pokemob.getInventory();
        this.entity = this.pokemob.getEntity();
        this.ai = new StoreTask(this.pokemob);
        final CompoundTag tag = container.data.readNbt();
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
    protected void renderLabels(final PoseStack mat,final int mouseX, final int mouseY)
    {
        super.renderLabels(mat,mouseX, mouseY);
        final int x = 83;
        final int y = 20;
        this.font.draw(mat,I18n.get("pokemob.gui.berry"), x, y, 4210752);
        this.font.draw(mat,I18n.get("pokemob.gui.store"), x, y + 10, 4210752);
        this.font.draw(mat,I18n.get("pokemob.gui.face"), x, y + 20, 4210752);
        this.font.draw(mat,I18n.get("pokemob.gui.empty"), x, y + 30, 4210752);
        this.font.draw(mat,I18n.get("pokemob.gui.face"), x, y + 40, 4210752);
    }

    @Override
    public void init()
    {
        super.init();
        this.renderables.clear();
        int xOffset = this.width / 2 - 10;
        final int yOffset = this.height / 2 - 77;
        this.addRenderableWidget(new Button(xOffset + 60, yOffset, 30, 10, new TranslatableComponent("pokemob.gui.inventory"),
                b -> PacketPokemobGui.sendPagePacket(PacketPokemobGui.MAIN, this.entity.getId())));
        this.addRenderableWidget(new Button(xOffset + 30, yOffset, 30, 10, new TranslatableComponent("pokemob.gui.ai"), b -> PacketPokemobGui
                .sendPagePacket(PacketPokemobGui.AI, this.entity.getId())));
        this.addRenderableWidget(new Button(xOffset + 00, yOffset, 30, 10, new TranslatableComponent("pokemob.gui.routes"),
                b -> PacketPokemobGui.sendPagePacket(PacketPokemobGui.ROUTES, this.entity.getId())));
        xOffset += 29;
        final int dy = 13;
        final int ds = 10;
        this.addRenderableWidget(this.berry = new EditBox(this.font, xOffset + 10, yOffset + dy + ds * 0, 50, 10, new TextComponent("")));
        this.addRenderableWidget(this.storage = new EditBox(this.font, xOffset + 10, yOffset + dy + ds * 1, 50, 10, new TextComponent("")));
        this.addRenderableWidget(this.storageFace = new EditBox(this.font, xOffset + 10, yOffset + dy + ds * 2, 50, 10,
                new TextComponent("")));
        this.addRenderableWidget(this.empty = new EditBox(this.font, xOffset + 10, yOffset + dy + ds * 3, 50, 10, new TextComponent("")));
        this.addRenderableWidget(this.emptyFace = new EditBox(this.font, xOffset + 10, yOffset + dy + ds * 4, 50, 10,
                new TextComponent("")));
        this.textBoxes = Lists.newArrayList(this.berry, this.storage, this.storageFace, this.empty, this.emptyFace);

        final CompoundTag nbt = this.ai.serializeNBT();
        final CompoundTag berry = nbt.getCompound("b");
        final CompoundTag storage = nbt.getCompound("s");
        final CompoundTag empty = nbt.getCompound("e");
        if (!berry.isEmpty()) this.berry.setValue(berry.getInt("x") + " " + berry.getInt("y") + " " + berry.getInt("z"));
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
                if (newLink != null && text.isFocused() && (text == this.berry || text == this.storage
                        || text == this.empty))
                {
                    text.setValue(newLink.getX() + " " + newLink.getY() + " " + newLink.getZ());
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
    public void render(final PoseStack mat, final int i, final int j, final float f)
    {
        super.render(mat,i, j, f);
        this.renderTooltip(mat,i, j);
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
        this.minecraft.player.displayClientMessage(new TranslatableComponent("pokemob.gui.updatestorage"), true);
    }
}
