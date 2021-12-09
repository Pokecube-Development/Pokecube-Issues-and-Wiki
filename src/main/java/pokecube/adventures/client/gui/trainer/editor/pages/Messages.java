package pokecube.adventures.client.gui.trainer.editor.pages;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import pokecube.adventures.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.adventures.capabilities.utils.Action;
import pokecube.adventures.capabilities.utils.BattleAction;
import pokecube.adventures.capabilities.utils.MessageState;
import pokecube.adventures.client.gui.trainer.editor.EditorGui;
import pokecube.adventures.client.gui.trainer.editor.pages.Messages.MessageOption;
import pokecube.adventures.client.gui.trainer.editor.pages.util.ListPage;
import pokecube.adventures.network.PacketTrainer;
import pokecube.core.client.gui.helper.INotifiedEntry;
import pokecube.core.client.gui.helper.ScrollGui;

public class Messages extends ListPage<MessageOption>
{

    public static class MessageOption extends AbstractSelectionList.Entry<MessageOption> implements INotifiedEntry
    {

        final Messages parent;

        final Minecraft mc;

        final int offsetY;
        final int index;
        final int guiHeight;

        final Button apply;

        final EditBox message;
        final EditBox action;

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

            this.message = new EditBox(parent.font, 0, 0, 170, 10, new TextComponent(""));
            this.action = new EditBox(parent.font, 0, 0, 170, 10, new TextComponent(""));

            final MessageState state = MessageState.values()[this.index];
            this.message.setValue(this.messages.getMessage(state));
            final Action act = this.messages.getAction(state);
            if (act != null)
            {
                String msg = act.getCommand();
                if (act instanceof BattleAction) msg = "action:initiate_battle";
                this.action.setValue(msg);
            }

            this.message.setMaxLength(1024);
            this.action.setMaxLength(1024);

            this.apply = new Button(0, 0, 50, 10, new TextComponent("Apply"), b -> {
                b.playDownSound(this.mc.getSoundManager());
                this.onUpdated();
            });

            parent.addRenderableWidget(this.apply);
            parent.addRenderableWidget(this.message);
            parent.addRenderableWidget(this.action);

            this.action.moveCursorTo(-this.action.getCursorPosition());
            this.message.moveCursorTo(-this.message.getCursorPosition());

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
        public void render(final PoseStack mat, final int slotIndex, final int y, final int x, final int listWidth,
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

            this.parent.font.draw(mat, MessageState.values()[this.index].name(), x, y - 5, 0xFFFFFF);
            this.message.render(mat, mouseX, mouseY, partialTicks);
            this.action.render(mat, mouseX, mouseY, partialTicks);
            this.apply.render(mat, mouseX, mouseY, partialTicks);
        }

        public void onUpdated()
        {
            final String act = this.action.getValue();
            Action newAction = null;
            if (act.equals("action:initiate_battle")) newAction = new BattleAction();
            else if (!act.isEmpty()) newAction = new Action(act);
            final String msg = this.message.getValue();
            final MessageState state = MessageState.values()[this.index];
            this.messages.setAction(state, newAction);
            this.messages.setMessage(state, msg);

            final Tag tag = this.messages.serializeNBT();
            final PacketTrainer message = new PacketTrainer(PacketTrainer.UPDATETRAINER);
            final CompoundTag nbt = message.getTag();
            nbt.put("__messages__", tag);
            nbt.putInt("I", this.parent.parent.entity.getId());
            PacketTrainer.ASSEMBLER.sendToServer(message);
        }
    }

    public Messages(final EditorGui parent)
    {
        super(new TextComponent(""), parent);
    }

    @Override
    public void initList()
    {
        this.children.clear();
        this.renderables.clear();
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
        this.addRenderableWidget(
                new Button(x + 73, y + 64, 50, 12, new TranslatableComponent("traineredit.button.home"), b ->
                {
                    this.closeCallback.run();
                }));
    }
}
