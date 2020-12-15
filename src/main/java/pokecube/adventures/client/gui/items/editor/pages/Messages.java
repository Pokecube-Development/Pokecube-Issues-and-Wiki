package pokecube.adventures.client.gui.items.editor.pages;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import pokecube.adventures.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.adventures.capabilities.utils.Action;
import pokecube.adventures.capabilities.utils.BattleAction;
import pokecube.adventures.capabilities.utils.MessageState;
import pokecube.adventures.client.gui.items.editor.EditorGui;
import pokecube.adventures.client.gui.items.editor.pages.Messages.MessageOption;
import pokecube.adventures.client.gui.items.editor.pages.util.ListPage;
import pokecube.adventures.network.PacketTrainer;
import pokecube.core.client.gui.helper.INotifiedEntry;
import pokecube.core.client.gui.helper.ScrollGui;

public class Messages extends ListPage<MessageOption>
{

    public static class MessageOption extends AbstractList.AbstractListEntry<MessageOption> implements INotifiedEntry
    {

        final Messages parent;

        final Minecraft mc;

        final int offsetY;
        final int index;
        final int guiHeight;

        final Button apply;

        final TextFieldWidget message;
        final TextFieldWidget action;

        final IHasMessages messages;

        public MessageOption(final Minecraft mc, final Messages parent, final int height, final int offsetY,
                final int index)
        {
            this.mc = mc;
            this.offsetY = offsetY;
            this.guiHeight = height;
            this.parent = parent;

            this.messages = parent.parent.messages;

            this.index = index;

            this.message = new TextFieldWidget(parent.font, 0, 0, 170, 10, new StringTextComponent(""));
            this.action = new TextFieldWidget(parent.font, 0, 0, 170, 10, new StringTextComponent(""));

            final MessageState state = MessageState.values()[this.index];
            this.message.setText(this.messages.getMessage(state));
            final Action act = this.messages.getAction(state);
            if (act != null)
            {
                String msg = act.getCommand();
                if (act instanceof BattleAction) msg = "action:initiate_battle";
                this.action.setText(msg);
            }

            this.message.setMaxStringLength(1024);
            this.action.setMaxStringLength(1024);

            this.apply = new Button(0, 0, 50, 10, new StringTextComponent("Apply"), b ->
            {
                b.playDownSound(this.mc.getSoundHandler());
                this.onUpdated();
            });

            parent.addButton(this.apply);
            parent.addButton(this.message);
            parent.addButton(this.action);

            this.action.setCursorPosition(-this.action.getCursorPosition());
            this.message.setCursorPosition(-this.message.getCursorPosition());

            this.apply.visible = false;
            this.message.visible = false;
            this.action.visible = false;
        }

        @Override
        public void preRender(final int slotIndex, final int x, final int y, final int listWidth, final int slotHeight,
                final int mouseX, final int mouseY, final boolean isSelected, final float partialTicks)
        {
            this.message.visible = false;
            this.action.visible = false;
            this.apply.visible = false;
        }

        @Override
        public void render(final MatrixStack mat, final int slotIndex, final int y, final int x, final int listWidth,
                final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected,
                final float partialTicks)
        {
            this.message.visible = true;
            this.action.visible = true;
            this.apply.visible = true;

            final int dy = 10;

            this.apply.x = x - 2 + this.message.getWidth();
            this.apply.y = y - 4 + dy;
            this.message.x = x - 2;
            this.message.y = y - 4 + dy;
            this.action.x = x - 2;
            this.action.y = y - 4 + dy + 12;

            this.parent.font.drawString(mat, MessageState.values()[this.index].name(), x, y - 5, 0xFFFFFF);
            this.message.render(mat, mouseX, mouseY, partialTicks);
            this.action.render(mat, mouseX, mouseY, partialTicks);
            this.apply.render(mat, mouseX, mouseY, partialTicks);
        }

        public void onUpdated()
        {
            final String act = this.action.getText();
            Action newAction = null;
            if (act.equals("action:initiate_battle")) newAction = new BattleAction();
            else if (!act.isEmpty()) newAction = new Action(act);
            final String msg = this.message.getText();
            final MessageState state = MessageState.values()[this.index];
            this.messages.setAction(state, newAction);
            this.messages.setMessage(state, msg);
            if (this.messages instanceof ICapabilitySerializable)
            {
                final ICapabilitySerializable<? extends INBT> ser = (ICapabilitySerializable<?>) this.messages;
                final INBT tag = ser.serializeNBT();
                final PacketTrainer message = new PacketTrainer(PacketTrainer.UPDATETRAINER);
                final CompoundNBT nbt = message.getTag();
                nbt.put("__messages__", tag);
                nbt.putInt("I", this.parent.parent.entity.getEntityId());
                PacketTrainer.ASSEMBLER.sendToServer(message);
            }
        }

    }

    public Messages(final EditorGui parent)
    {
        super(new StringTextComponent(""), parent);
    }

    @Override
    public void initList()
    {
        this.children.clear();
        this.buttons.clear();
        super.initList();
        int x = (this.parent.width - 256) / 2;
        int y = (this.parent.height - 160) / 2;
        final int height = 120;
        final int width = 245;
        this.list = new ScrollGui<>(this, this.minecraft, width, height, 40, x + 10, y + 24);
        for (int i = 0; i < MessageState.values().length; i++)
            this.list.addEntry(new MessageOption(this.minecraft, this, height, y + 24, i));
        this.children.add(this.list);
        x = this.width / 2;
        y = this.height / 2;
        this.addButton(new Button(x + 73, y + 64, 50, 12, new TranslationTextComponent("traineredit.button.home"), b ->
        {
            this.closeCallback.run();
        }));
    }
}
