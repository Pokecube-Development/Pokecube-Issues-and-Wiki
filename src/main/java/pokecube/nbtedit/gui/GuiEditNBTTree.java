package pokecube.nbtedit.gui;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import pokecube.nbtedit.nbt.NBTTree;
import pokecube.nbtedit.packets.CustomNBTPacket;
import pokecube.nbtedit.packets.EntityNBTPacket;
import pokecube.nbtedit.packets.TileNBTPacket;

public class GuiEditNBTTree extends Screen
{

    public final int         entityOrX, y, z;
    private final boolean    entity;
    protected String         screenTitle;
    private String           customName = "";
    private final GuiNBTTree guiTree;

    public GuiEditNBTTree(final BlockPos pos, final CompoundTag tag)
    {
        super(new TranslatableComponent("nbtedit.tree"));
        this.entity = false;
        this.entityOrX = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.screenTitle = "NBTEdit -- TileEntity at " + pos.getX() + "," + pos.getY() + "," + pos.getZ();
        this.guiTree = new GuiNBTTree(new NBTTree(tag));
    }

    public GuiEditNBTTree(final int entity, final CompoundTag tag)
    {
        super(new TranslatableComponent("nbtedit.tree"));
        this.entity = true;
        this.entityOrX = entity;
        this.y = 0;
        this.z = 0;
        this.screenTitle = "NBTEdit -- EntityId #" + this.entityOrX;
        this.guiTree = new GuiNBTTree(new NBTTree(tag));
    }

    public GuiEditNBTTree(final int entity, final String customName, final CompoundTag tag)
    {
        super(new TranslatableComponent("nbtedit.tree"));
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
        return this.entity ? this.minecraft.level.getEntity(this.entityOrX) : null;
    }

    @Override
    public void init()
    {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.guiTree.initGUI(this.width, this.height, this.height - 35);
        this.addRenderableWidget(new Button(this.width / 4 - 100, this.height - 27, 200, 20, new TextComponent("Save"),
                b -> this.quitWithSave()));
        this.addRenderableWidget(new Button(this.width * 3 / 4 - 100, this.height - 27, 200, 20, new TextComponent("Quit"),
                b -> this.quitWithoutSaving()));
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
    public void removed()
    {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private void quitWithoutSaving()
    {
        Minecraft.getInstance().setScreen(null);
    }

    private void quitWithSave()
    {
        if (this.entity)
        {
            if (this.customName.isEmpty())
            {
                final EntityNBTPacket p = new EntityNBTPacket(this.entityOrX, this.guiTree.getNBTTree()
                        .toCompoundNBT());
                EntityNBTPacket.ASSEMBLER.sendToServer(p);
            }
            else
            {
                final CustomNBTPacket p = new CustomNBTPacket(this.entityOrX, this.customName, this.guiTree.getNBTTree()
                        .toCompoundNBT());
                CustomNBTPacket.ASSEMBLER.sendToServer(p);
            }
        }
        else TileNBTPacket.ASSEMBLER.sendToServer(new TileNBTPacket(new BlockPos(this.entityOrX, this.y, this.z),
                this.guiTree.getNBTTree().toCompoundNBT()));
        Minecraft.getInstance().setScreen(null);

    }

    @Override
    public void render(final PoseStack mat, final int x, final int y, final float par3)
    {
        this.renderBackground(mat);
        this.guiTree.render(mat, x, y, par3);
        GuiComponent.drawCenteredString(mat, this.font, this.screenTitle, this.width / 2, 5, 16777215);
        super.render(mat, x, y, par3);
    }

    @Override
    public void tick()
    {
        if (!this.minecraft.player.isAlive()) this.quitWithoutSaving();
        else this.guiTree.tick();
    }

}
