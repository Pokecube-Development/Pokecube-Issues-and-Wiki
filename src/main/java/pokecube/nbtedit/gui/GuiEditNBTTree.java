package pokecube.nbtedit.gui;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.nbtedit.nbt.NBTTree;
import pokecube.nbtedit.packets.CustomNBTPacket;
import pokecube.nbtedit.packets.EntityNBTPacket;
import pokecube.nbtedit.packets.PacketHandler;
import pokecube.nbtedit.packets.TileNBTPacket;

public class GuiEditNBTTree extends Screen
{

    public final int         entityOrX, y, z;
    private final boolean    entity;
    protected String         screenTitle;
    private String           customName = "";
    private final GuiNBTTree guiTree;

    public GuiEditNBTTree(final BlockPos pos, final CompoundNBT tag)
    {
        super(new TranslationTextComponent("nbtedit.tree"));
        this.entity = false;
        this.entityOrX = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.screenTitle = "NBTEdit -- TileEntity at " + pos.getX() + "," + pos.getY() + "," + pos.getZ();
        this.guiTree = new GuiNBTTree(new NBTTree(tag));
    }

    public GuiEditNBTTree(final int entity, final CompoundNBT tag)
    {
        super(new TranslationTextComponent("nbtedit.tree"));
        this.entity = true;
        this.entityOrX = entity;
        this.y = 0;
        this.z = 0;
        this.screenTitle = "NBTEdit -- EntityId #" + this.entityOrX;
        this.guiTree = new GuiNBTTree(new NBTTree(tag));
    }

    public GuiEditNBTTree(final int entity, final String customName, final CompoundNBT tag)
    {
        super(new TranslationTextComponent("nbtedit.tree"));
        this.entity = true;
        this.entityOrX = entity;
        this.customName = customName;
        this.y = 0;
        this.z = 0;
        this.screenTitle = "NBTEdit -- EntityId #" + this.entityOrX + " " + customName;
        this.guiTree = new GuiNBTTree(new NBTTree(tag));
    }

    @Override
    public boolean charTyped(final char par1, final int key)
    {
        final GuiEditNBT window = this.guiTree.getWindow();
        final boolean ret = super.charTyped(par1, key);
        if (window != null) return window.charTyped(par1, key) || ret;
        else if (key == 1)
        {
            if (this.guiTree.isEditingSlot()) return this.guiTree.stopEditingSlot();
            else this.quitWithoutSaving();
        }
        else if (key == GLFW.GLFW_KEY_DELETE) return this.guiTree.deleteSelected();
        else if (key == GLFW.GLFW_KEY_ENTER) return this.guiTree.editSelected();
        else if (key == GLFW.GLFW_KEY_UP) return this.guiTree.arrowKeyPressed(true);
        else if (key == GLFW.GLFW_KEY_DOWN) return this.guiTree.arrowKeyPressed(false);
        else return this.guiTree.charTyped(par1, key);
        return ret;
    }

    public int getBlockX()
    {
        return this.entity ? 0 : this.entityOrX;
    }

    public Entity getEntity()
    {
        return this.entity ? this.minecraft.world.getEntityByID(this.entityOrX) : null;
    }

    @Override
    public void init()
    {
        this.minecraft.keyboardListener.enableRepeatEvents(true);
        this.guiTree.initGUI(this.width, this.height, this.height - 35);
        this.addButton(new Button(this.width / 4 - 100, this.height - 27, 200, 20, "Save", b -> this.quitWithSave()));
        this.addButton(new Button(this.width * 3 / 4 - 100, this.height - 27, 200, 20, "Quit", b -> this
                .quitWithoutSaving()));
        this.children.add(this.guiTree);
    }

    @Override
    public boolean isPauseScreen()
    {
        return true;
    }

    public boolean isTileEntity()
    {
        return !this.entity;
    }

    @Override
    public boolean mouseClicked(final double x, final double y, final int t)
    {
        final boolean ret = super.mouseClicked(x, y, t);
        return ret;
    }

    @Override
    public boolean mouseScrolled(final double x, final double y, final double dir)
    {
        boolean ret = super.mouseScrolled(x, y, dir);
        final double ofs = dir;

        if (ofs != 0)
        {
            this.guiTree.shift(ofs >= 1 ? 6 : -6);
            ret = true;
        }
        return ret;
    }

    @Override
    public void onClose()
    {
        this.minecraft.keyboardListener.enableRepeatEvents(false);
    }

    private void quitWithoutSaving()
    {
        Minecraft.getInstance().displayGuiScreen(null);
    }

    private void quitWithSave()
    {
        if (this.entity)
        {
            if (this.customName.isEmpty()) PacketHandler.INSTANCE.sendToServer(new EntityNBTPacket(this.entityOrX,
                    this.guiTree.getNBTTree().toCompoundNBT()));
            else PacketHandler.INSTANCE.sendToServer(new CustomNBTPacket(this.entityOrX, this.customName, this.guiTree
                    .getNBTTree().toCompoundNBT()));
        }
        else PacketHandler.INSTANCE.sendToServer(new TileNBTPacket(new BlockPos(this.entityOrX, this.y, this.z),
                this.guiTree.getNBTTree().toCompoundNBT()));
        Minecraft.getInstance().displayGuiScreen(null);

    }

    @Override
    public void render(final int x, final int y, final float par3)
    {
        this.renderBackground();
        this.guiTree.render(x, y, par3);
        this.drawCenteredString(this.font, this.screenTitle, this.width / 2, 5, 16777215);
        super.render(x, y, par3);
    }

    @Override
    public void tick()
    {
        if (!this.minecraft.player.isAlive()) this.quitWithoutSaving();
        else this.guiTree.tick();
    }

}
