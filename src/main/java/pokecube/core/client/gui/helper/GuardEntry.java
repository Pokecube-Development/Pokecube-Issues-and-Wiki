package pokecube.core.client.gui.helper;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import pokecube.core.ai.routes.GuardAICapability;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.ai.routes.IGuardAICapability.IGuardTask;
import pokecube.core.utils.TimePeriod;
import thut.api.maths.Vector4;
import thut.lib.TComponent;

public class GuardEntry extends AbstractSelectionList.Entry<GuardEntry> implements INotifiedEntry
{

    final int index;
    public final EditBox location;
    public final EditBox timeperiod;
    public final EditBox variation;
    final Screen parent;
    final IGuardAICapability guard;
    final Entity entity;
    final Button delete;
    final Button confirm;
    final Button moveUp;
    final Button moveDown;
    final Button update;
    final Function<CompoundTag, CompoundTag> function;
    final int guiX;
    final int guiY;
    final int guiHeight;

    public GuardEntry(final int index, final IGuardAICapability guard, final Entity entity, final Screen parent,
            final EditBox location, final EditBox timeperiod, final EditBox variation,
            final Function<CompoundTag, CompoundTag> function, final int dx, final int dy, final int dh)
    {
        this.guard = guard;
        this.parent = parent;
        this.location = location;
        this.timeperiod = timeperiod;
        this.variation = variation;
        this.index = index;
        this.entity = entity;
        this.delete = new Button(-200, 0, 10, 10, TComponent.literal("x"), b -> this.deleteClicked(b),
                (b, pose, x, y) ->
                {
                    if (!b.active) return;
                    Component tooltip = TComponent.translatable("pokecube.gui.delete.start.desc");
                    parent.renderTooltip(pose, tooltip, x, y);
                });
        this.delete.setFGColor(0xFFFF0000);
        this.confirm = new Button(-200, 0, 10, 10, TComponent.literal("Y"), b -> this.confirmClicked(b),
                (b, pose, x, y) ->
                {
                    if (!b.active) return;
                    Component tooltip = TComponent.translatable("pokecube.gui.delete.confirm.desc");
                    parent.renderTooltip(pose, tooltip, x, y);
                });
        this.confirm.active = false;

        if (index == guard.getTasks().size()) this.delete.active = false;

        this.moveUp = new Button(-200, 0, 10, 10, TComponent.literal("\u21e7"), b -> this.moveUpClicked(b),
                (b, pose, x, y) ->
                {
                    if (!b.active) return;
                    Component tooltip = TComponent.translatable("pokecube.gui.move.up.desc");
                    parent.renderTooltip(pose, tooltip, x, y);
                });
        this.moveDown = new Button(-200, 0, 10, 10, TComponent.literal("\u21e9"), b -> this.moveDownClicked(b),
                (b, pose, x, y) ->
                {
                    if (!b.active) return;
                    Component tooltip = TComponent.translatable("pokecube.gui.move.down.desc");
                    parent.renderTooltip(pose, tooltip, x, y);
                });

        this.update = new Button(-200, 0, 20, 10, TComponent.literal("btn"), b -> this.update(), (b, pose, x, y) -> {
            Component tooltip = TComponent.translatable("pokemob.route.btn.desc");
            parent.renderTooltip(pose, tooltip, x, y);
        });

        this.moveUp.active = index > 0 && index < guard.getTasks().size();
        this.moveDown.active = index < guard.getTasks().size() - 1;
        this.function = function;
        this.guiX = dx;
        this.guiY = dy;
        this.guiHeight = dh;

        @SuppressWarnings("unchecked")
        final List<GuiEventListener> list = (List<GuiEventListener>) parent.children();
        // Add us first so we can add linker-clicking to the location field
        list.add(this);
        this.addOrRemove(parent::addRenderableWidget);
    }

    public void addOrRemove(Consumer<AbstractWidget> remover)
    {
        this.delete.visible = false;
        this.confirm.visible = false;
        this.moveUp.visible = false;
        this.moveDown.visible = false;
        this.update.visible = false;
        this.location.visible = false;
        this.timeperiod.visible = false;
        this.variation.visible = false;
        
        remover.accept(this.delete);
        remover.accept(this.confirm);
        remover.accept(this.moveUp);
        remover.accept(this.update);
        remover.accept(this.moveDown);
        remover.accept(this.location);
        remover.accept(this.timeperiod);
        remover.accept(this.variation);
    }

    @Override
    public boolean keyPressed(final int keyCode, final int p_keyPressed_2_, final int p_keyPressed_3_)
    {
        if (keyCode != GLFW.GLFW_KEY_ENTER) return false;
        boolean active = this.variation.isFocused() || this.variation.isHoveredOrFocused();
        active = active || this.location.isFocused() || this.location.isHoveredOrFocused();
        active = active || this.timeperiod.isFocused() || this.timeperiod.isHoveredOrFocused();
        if (!active) return false;
        this.update();
        return true;
    }

    private void confirmClicked(final Button b)
    {
        this.confirm.playDownSound(this.parent.getMinecraft().getSoundManager());
        // Send packet for removal server side
        this.delete();
    }

    private void delete()
    {
        final CompoundTag data = new CompoundTag();
        final CompoundTag tag = new CompoundTag();
        tag.putBoolean("GU", true);
        tag.putInt("I", this.index);
        data.put("T", tag);
        data.putInt("I", this.entity.getId());
        if (this.index < this.guard.getTasks().size()) this.guard.getTasks().remove(this.index);
        this.function.apply(data);
    }

    private void deleteClicked(final Button b)
    {
        this.delete.playDownSound(this.parent.getMinecraft().getSoundManager());
        this.confirm.active = !this.confirm.active;
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseEvent)
    {
        final boolean ret = false;
        BlockPos newLink = null;
        final ItemStack carried = Minecraft.getInstance().player.containerMenu.getCarried();
        boolean effect = false;
        if (!carried.isEmpty() && carried.hasTag())
        {
            final CompoundTag link = carried.getTag().getCompound("link_pos");
            if (!link.isEmpty())
            {
                final Vector4 pos = new Vector4(link);
                newLink = new BlockPos((int) (pos.x - 0.5), (int) pos.y, (int) (pos.z - 0.5));
            }
        }
        final EditBox text = this.location;
        {
            if (newLink != null && text.isFocused())
            {
                text.setValue(newLink.getX() + " " + newLink.getY() + " " + newLink.getZ());
                effect = true;
            }
            if (ret) effect = true;
        }
        if (effect)
        {
            this.update();
            return true;
        }
        return false;
    }

    public void moveDownClicked(final Button b)
    {
        this.moveDown.playDownSound(this.parent.getMinecraft().getSoundManager());
        // Update the list for the page.
        this.reOrder(1);
    }

    public void moveUpClicked(final Button b)
    {
        this.moveUp.playDownSound(this.parent.getMinecraft().getSoundManager());
        // Update the list for the page.
        this.reOrder(-1);
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
            final Component mess = TComponent.translatable("traineredit.info.pos.formaterror");
            this.parent.getMinecraft().player.displayClientMessage(mess, true);
        }
        else if (args.length != 0)
        {
            // Send status message about not working here.
            final Component mess = TComponent.translatable("traineredit.info.pos.formatinfo");
            this.parent.getMinecraft().player.displayClientMessage(mess, true);
        }
        return null;
    }

    @Override
    public void preRender(final int slotIndex, final int x, final int y, final int listWidth, final int slotHeight,
            final int mouseX, final int mouseY, final boolean isSelected, final float partialTicks)
    {
        this.delete.visible = false;
        this.confirm.visible = false;
        this.moveUp.visible = false;
        this.moveDown.visible = false;
        this.location.visible = false;
        this.timeperiod.visible = false;
        this.variation.visible = false;
        this.update.visible = false;
    }

    @Override
    public void render(final PoseStack mat, final int slotIndex, int y, int x, final int listWidth,
            final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected,
            final float partialTicks)
    {
        this.delete.visible = true;
        this.confirm.visible = true;
        this.moveUp.visible = true;
        this.moveDown.visible = true;
        this.location.visible = true;
        this.timeperiod.visible = true;
        this.variation.visible = true;
        this.update.visible = true;

        x += this.guiX;
        y += this.guiY;

        this.location.x = x - 2;
        this.location.y = y - 4;

        this.timeperiod.x = x - 2;
        this.timeperiod.y = y - 4 - 10;

        this.variation.x = x - 2;
        this.variation.y = y - 4 - 20;

        this.delete.y = y - 5;
        this.delete.x = x - 1 + this.location.getWidth();
        this.confirm.y = y - 5;
        this.confirm.x = x - 2 + 11 + this.location.getWidth();
        this.moveUp.y = y - 5 - 10;
        this.moveUp.x = x - 1 + this.location.getWidth();
        this.moveDown.y = y - 5 - 10;
        this.moveDown.x = x - 2 + 11 + this.location.getWidth();

        this.update.y = y - 5 - 20;
        this.update.x = x - 1 + this.location.getWidth();
    }

    public void reOrder(final int dir)
    {
        final CompoundTag data = new CompoundTag();
        final CompoundTag tag = new CompoundTag();
        tag.putBoolean("GU", true);
        tag.putInt("I", this.index);
        tag.putInt("N", dir);
        data.put("T", tag);
        data.putInt("I", this.entity.getId());
        final int index1 = tag.getInt("I");
        final int index2 = index1 + tag.getInt("N");
        final IGuardTask temp = this.guard.getTasks().get(index1);
        this.guard.getTasks().set(index1, this.guard.getTasks().get(index2));
        this.guard.getTasks().set(index2, temp);
        this.function.apply(data);
    }

    public TimePeriod timeFromText(final String text)
    {
        if (text.isEmpty()) return null;
        final String[] args = text.split(" ");
        if (args.length == 2) try
        {
            final int x = Integer.parseInt(args[0]);
            final int y = Integer.parseInt(args[1]);
            return new TimePeriod(x, y);
        }
        catch (final NumberFormatException e)
        {
            // Send status message about not working here.
            final Component mess = TComponent.translatable("traineredit.info.time.formaterror");
            this.parent.getMinecraft().player.displayClientMessage(mess, true);
        }
        else if (args.length != 0)
        {
            // Send status message about not working here.
            final Component mess = TComponent.translatable("traineredit.info.time.formatinfo");
            this.parent.getMinecraft().player.displayClientMessage(mess, true);
        }
        return null;
    }

    public void update()
    {
        final BlockPos loc = this.posFromText(this.location.getValue());
        final TimePeriod time = this.timeFromText(this.timeperiod.getValue());
        float dist = 2;
        try
        {
            dist = Float.parseFloat(this.variation.getValue());
        }
        catch (final NumberFormatException e)
        {
            final Component mess = TComponent.translatable("traineredit.info.dist.formatinfo");
            this.parent.getMinecraft().player.displayClientMessage(mess, false);
            return;
        }
        if (loc != null && time != null)
        {
            final CompoundTag data = new CompoundTag();
            final CompoundTag tag = new CompoundTag();
            tag.putBoolean("GU", true);
            tag.putInt("I", this.index);
            // TODO generalize this maybe?
            final IGuardTask task = this.index < this.guard.getTasks().size() ? this.guard.getTasks().get(this.index)
                    : new GuardAICapability.GuardTask();
            if (this.index >= this.guard.getTasks().size()) this.guard.getTasks().add(task);
            task.setPos(loc);
            task.setActiveTime(time);
            task.setRoamDistance(dist);
            final Tag var = task.serialze();
            tag.put("V", var);
            data.put("T", tag);
            data.putInt("I", this.entity.getId());
            this.function.apply(data);
            final Component mess = TComponent.translatable("pokemob.route.updated");
            this.parent.getMinecraft().player.displayClientMessage(mess, false);
        }
    }
}
