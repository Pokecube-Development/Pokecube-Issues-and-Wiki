package pokecube.nbtedit.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.lwjgl.glfw.GLFW;
import pokecube.nbtedit.nbt.NBTTree;
import pokecube.nbtedit.packets.CustomNBTPacket;
import pokecube.nbtedit.packets.EntityNBTPacket;
import pokecube.nbtedit.packets.TileNBTPacket;
import thut.lib.TComponent;

public class GuiEditNBTTree extends Screen
{
    public int entityOrX, y, z;
    private boolean entity;
    protected String screenTitle;
    private String customName = "";
    private final GuiNBTTree guiTree;

    private GuiEditNBTTree(CompoundTag tag)
    {
        super(TComponent.translatable("nbtedit.tree"));
        this.guiTree = new GuiNBTTree(new NBTTree(tag));
    }

    public GuiEditNBTTree(final BlockPos pos, final CompoundTag tag)
    {
        this(tag);
        this.entity = false;
        this.entityOrX = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.screenTitle = "NBTEdit -- TileEntity at " + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    public GuiEditNBTTree(final int entity, final CompoundTag tag)
    {
        this(tag);
        this.entity = true;
        this.entityOrX = entity;
        this.y = 0;
        this.z = 0;
        this.screenTitle = "NBTEdit -- EntityId #" + this.entityOrX;
    }

    public GuiEditNBTTree(final int entity, final String customName, final CompoundTag tag)
    {
        this(tag);
        this.entity = true;
        this.entityOrX = entity;
        this.customName = customName;
        this.y = 0;
        this.z = 0;
        this.screenTitle = "NBTEdit -- EntityId #" + this.entityOrX + " " + customName;
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

    public Entity getEntity()
    {
        return this.entity ? this.minecraft.level.getEntity(this.entityOrX) : null;
    }

    @Override
    public void init()
    {
        this.guiTree.initGUI(this.width, this.height, this.height - 35);
        this.guiTree.init(this.minecraft, this.width, this.height);

        this.addRenderableWidget(
                new Button.Builder(TComponent.literal("Save"), (b) -> this.quitWithSave()).bounds(this.width / 4 - 100,
                        this.height - 27, 200, 20).build());

        this.addRenderableWidget(new Button.Builder(TComponent.literal("Quit"), (b) -> this.quitWithoutSaving()).bounds(
                this.width * 3 / 4 - 100, this.height - 27, 200, 20).build());

        this.children.add(this.guiTree);

        this.renderables.add((graphics, x, y, t) -> {
            graphics.drawCenteredString(this.font, this.screenTitle, this.width / 2, 5, 16777215);
            this.guiTree.render(graphics, x, y, t);
        });
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
                final EntityNBTPacket p = new EntityNBTPacket(this.entityOrX,
                        this.guiTree.getNBTTree().toCompoundNBT());
                EntityNBTPacket.ASSEMBLER.sendToServer(p.getTag());
            }
            else
            {
                final CustomNBTPacket p = new CustomNBTPacket(this.entityOrX, this.customName,
                        this.guiTree.getNBTTree().toCompoundNBT());
                CustomNBTPacket.ASSEMBLER.sendToServer(p.getTag());
            }
        }
        else TileNBTPacket.ASSEMBLER.sendToServer(new TileNBTPacket(new BlockPos(this.entityOrX, this.y, this.z),
                this.guiTree.getNBTTree().toCompoundNBT()).getTag());
        Minecraft.getInstance().setScreen(null);

    }

    @Override
    public void tick()
    {
        if (!this.minecraft.player.isAlive()) this.quitWithoutSaving();
        else this.guiTree.tick();
    }

}
