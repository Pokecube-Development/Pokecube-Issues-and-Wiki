package pokecube.core.client.gui.helper;

import java.util.function.Function;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.ai.routes.GuardAICapability;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.ai.routes.IGuardAICapability.IGuardTask;
import pokecube.core.utils.TimePeriod;

public class GuardEntry extends AbstractList.AbstractListEntry<GuardEntry>
{

    final int                                index;
    public final TextFieldWidget             location;
    public final TextFieldWidget             timeperiod;
    public final TextFieldWidget             variation;
    final Screen                             parent;
    final IGuardAICapability                 guard;
    final Entity                             entity;
    final Button                             delete;
    final Button                             confirm;
    final Button                             moveUp;
    final Button                             moveDown;
    final Function<CompoundNBT, CompoundNBT> function;
    final int                                guiX;
    final int                                guiY;
    final int                                guiHeight;

    public GuardEntry(final int index, final IGuardAICapability guard, final Entity entity, final Screen parent,
            final TextFieldWidget location, final TextFieldWidget timeperiod, final TextFieldWidget variation,
            final Function<CompoundNBT, CompoundNBT> function, final int dx, final int dy, final int dh)
    {
        this.guard = guard;
        this.parent = parent;
        this.location = location;
        this.timeperiod = timeperiod;
        this.variation = variation;
        this.index = index;
        this.entity = entity;
        this.delete = new Button(0, 0, 10, 10, "x", b -> this.deleteClicked(b));
        this.delete.setFGColor(0xFFFF0000);
        this.confirm = new Button(0, 0, 10, 10, "Y", b -> this.confirmClicked(b));
        this.confirm.active = false;
        this.moveUp = new Button(0, 0, 10, 10, "\u21e7", b -> this.moveUpClicked(b));
        this.moveDown = new Button(0, 0, 10, 10, "\u21e9", b -> this.moveDownClicked(b));
        this.moveUp.active = index > 0 && index < guard.getTasks().size();
        this.moveDown.active = index < guard.getTasks().size() - 1;
        this.function = function;
        this.guiX = dx;
        this.guiY = dy;
        this.guiHeight = dh;
    }

    @Override
    public boolean charTyped(final char typedChar, final int keyCode)
    {
        this.location.charTyped(typedChar, keyCode);
        this.timeperiod.charTyped(typedChar, keyCode);
        this.variation.charTyped(typedChar, keyCode);

        if (keyCode == GLFW.GLFW_KEY_TAB) if (this.location.isFocused())
        {
            this.location.setFocused2(false);
            this.timeperiod.setFocused2(true);
        }
        else if (this.timeperiod.isFocused())
        {
            this.timeperiod.setFocused2(false);
            this.variation.setFocused2(true);
        }
        else if (this.variation.isFocused())
        {
            this.variation.setFocused2(false);
            this.location.setFocused2(true);
        }
        if (keyCode != GLFW.GLFW_KEY_ENTER) return false;
        if (!(this.location.isFocused() || this.timeperiod.isFocused() || this.variation.isFocused())) return false;
        this.update();
        return true;
    }

    private void confirmClicked(final Button b)
    {
        this.confirm.playDownSound(this.parent.getMinecraft().getSoundHandler());
        // Send packet for removal server side
        this.delete();
    }

    private void delete()
    {
        final CompoundNBT data = new CompoundNBT();
        final CompoundNBT tag = new CompoundNBT();
        tag.putBoolean("GU", true);
        tag.putInt("I", this.index);
        data.put("T", tag);
        data.putInt("I", this.entity.getEntityId());
        if (this.index < this.guard.getTasks().size()) this.guard.getTasks().remove(this.index);
        this.function.apply(data);
    }

    private void deleteClicked(final Button b)
    {
        this.delete.playDownSound(this.parent.getMinecraft().getSoundHandler());
        this.confirm.active = !this.confirm.active;
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseEvent)
    {
        this.delete.mouseClicked(mouseX, mouseY, mouseEvent);
        this.confirm.mouseClicked(mouseX, mouseY, mouseEvent);
        this.moveUp.mouseClicked(mouseX, mouseY, mouseEvent);
        this.moveDown.mouseClicked(mouseX, mouseY, mouseEvent);
        return false;
        // final int offsetY = this.parent.height / 2 - 60;
        // final int guiHeight = 100;
        // boolean buy1Fits = true;
        // buy1Fits = this.location.y >= offsetY;
        // buy1Fits = buy1Fits && mouseX - this.location.x >= 0;
        // buy1Fits = buy1Fits && mouseX - this.location.x <=
        // this.location.width;
        // buy1Fits = buy1Fits && mouseY - this.location.y >= 0;
        // buy1Fits = buy1Fits && mouseY - this.location.y <=
        // this.location.height;
        // buy1Fits = buy1Fits && this.location.y + this.location.height <=
        // offsetY + guiHeight;
        // this.location.setFocused(buy1Fits);
        // boolean buy2Fits = true;
        // buy2Fits = this.timeperiod.y >= offsetY;
        // buy2Fits = buy2Fits && mouseX - this.timeperiod.x >= 0;
        // buy2Fits = buy2Fits && mouseX - this.timeperiod.x <=
        // this.timeperiod.width;
        // buy2Fits = buy2Fits && mouseY - this.timeperiod.y >= 0;
        // buy2Fits = buy2Fits && mouseY - this.timeperiod.y <=
        // this.timeperiod.height;
        // buy2Fits = buy2Fits && this.timeperiod.y + this.timeperiod.height <=
        // offsetY + guiHeight;
        // this.timeperiod.setFocused(buy2Fits);
        // boolean sellFits = true;
        // sellFits = this.variation.y >= offsetY;
        // sellFits = sellFits && mouseX - this.variation.x >= 0;
        // sellFits = sellFits && mouseX - this.variation.x <=
        // this.variation.width;
        // sellFits = sellFits && mouseY - this.variation.y >= 0;
        // sellFits = sellFits && mouseY - this.variation.y <=
        // this.variation.height;
        // sellFits = sellFits && this.variation.y + this.variation.height <=
        // offsetY + guiHeight;
        // this.variation.setFocused(sellFits);
        // return buy1Fits || buy2Fits || sellFits;
    }

    public void moveDownClicked(final Button b)
    {
        this.moveDown.playDownSound(this.parent.getMinecraft().getSoundHandler());
        // Update the list for the page.
        this.reOrder(1);
    }

    public void moveUpClicked(final Button b)
    {
        this.moveUp.playDownSound(this.parent.getMinecraft().getSoundHandler());
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
            final ITextComponent mess = new TranslationTextComponent("traineredit.info.pos.formaterror");
            this.parent.getMinecraft().player.sendStatusMessage(mess, true);
        }
        else if (args.length != 0)
        {
            // Send status message about not working here.
            final ITextComponent mess = new TranslationTextComponent("traineredit.info.pos.formatinfo");
            this.parent.getMinecraft().player.sendStatusMessage(mess, true);
        }
        return null;
    }

    @Override
    public void render(final int slotIndex, final int x, final int y, final int listWidth, final int slotHeight,
            final int mouseX, final int mouseY, final boolean isSelected, final float partialTicks)
    {
        // final int width = 10;
        // int dx = 4 + this.guiX;
        // int dy = -5 + this.guiY;
        // this.delete.y = y + dy;
        // this.delete.x = x - dx + width;
        // this.confirm.y = y + dy;
        // this.confirm.x = x - dx + 10 + width;
        // this.moveUp.y = y + dy + width;
        // this.moveUp.x = x - dx + width;
        // this.moveDown.y = y + dy + width;
        // this.moveDown.x = x - dx + 10 + width;
        // dx += 26;
        // dy += 1;
        // boolean fits = true;
        // this.location.x = x - 2 + dx;
        // this.location.y = y + dy;
        // this.timeperiod.y = y + dy + 10;
        // this.timeperiod.x = x - 2 + dx;
        // this.variation.y = y + dy + 20;
        // this.variation.x = x - 2 + dx;
        // dy = -60;
        // final int offsetY = this.parent.height / 2 - this.guiY + dy;
        // fits = this.location.y >= offsetY;
        // fits = fits && this.location.y + 2 * this.location.height <= offsetY
        // + this.guiHeight;
        // if (fits)
        {
            RenderHelper.disableStandardItemLighting();
            this.location.render(mouseX, mouseY, partialTicks);
            this.timeperiod.render(mouseX, mouseY, partialTicks);
            this.variation.render(mouseX, mouseY, partialTicks);

            this.delete.render(mouseX, mouseY, partialTicks);
            this.confirm.render(mouseX, mouseY, partialTicks);
            this.moveUp.render(mouseX, mouseY, partialTicks);
            this.moveDown.render(mouseX, mouseY, partialTicks);
            GL11.glColor3f(1, 1, 1);
        }
    }

    public void reOrder(final int dir)
    {
        final CompoundNBT data = new CompoundNBT();
        final CompoundNBT tag = new CompoundNBT();
        tag.putBoolean("GU", true);
        tag.putInt("I", this.index);
        tag.putInt("N", dir);
        data.put("T", tag);
        data.putInt("I", this.entity.getEntityId());
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
            final ITextComponent mess = new TranslationTextComponent("traineredit.info.time.formaterror");
            this.parent.getMinecraft().player.sendStatusMessage(mess, true);
        }
        else if (args.length != 0)
        {
            // Send status message about not working here.
            final ITextComponent mess = new TranslationTextComponent("traineredit.info.time.formatinfo");
            this.parent.getMinecraft().player.sendStatusMessage(mess, true);
        }
        return null;
    }

    public void update()
    {
        final BlockPos loc = this.posFromText(this.location.getText());
        final TimePeriod time = this.timeFromText(this.timeperiod.getText());
        float dist = 2;
        try
        {
            dist = Float.parseFloat(this.variation.getText());
        }
        catch (final NumberFormatException e)
        {
            final ITextComponent mess = new TranslationTextComponent("traineredit.info.dist.formatinfo");
            this.parent.getMinecraft().player.sendStatusMessage(mess, true);
            return;
        }
        if (loc != null && time != null)
        {
            final CompoundNBT data = new CompoundNBT();
            final CompoundNBT tag = new CompoundNBT();
            tag.putBoolean("GU", true);
            tag.putInt("I", this.index);
            // TODO generalize this maybe?
            final IGuardTask task = this.index < this.guard.getTasks().size() ? this.guard.getTasks().get(this.index)
                    : new GuardAICapability.GuardTask();
            if (this.index >= this.guard.getTasks().size()) this.guard.getTasks().add(task);
            task.setPos(loc);
            task.setActiveTime(time);
            task.setRoamDistance(dist);
            final INBT var = task.serialze();
            tag.put("V", var);
            data.put("T", tag);
            data.putInt("I", this.entity.getEntityId());
            this.function.apply(data);
        }
    }
}
