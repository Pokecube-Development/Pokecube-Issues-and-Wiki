package pokecube.adventures.client.gui.items.editor.pages;

import java.util.function.Predicate;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.brigadier.StringReader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.command.arguments.NBTTagArgument;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import pokecube.adventures.capabilities.CapabilityHasRewards.IHasRewards;
import pokecube.adventures.capabilities.CapabilityHasRewards.Reward;
import pokecube.adventures.client.gui.items.editor.EditorGui;
import pokecube.adventures.client.gui.items.editor.pages.Rewards.RewardOption;
import pokecube.adventures.client.gui.items.editor.pages.util.ListPage;
import pokecube.adventures.network.PacketTrainer;
import pokecube.core.client.gui.helper.INotifiedEntry;
import pokecube.core.client.gui.helper.ScrollGui;

public class Rewards extends ListPage<RewardOption>
{
    public static class RewardOption extends AbstractList.AbstractListEntry<RewardOption> implements INotifiedEntry
    {

        final Rewards parent;

        final Minecraft mc;

        final int offsetY;
        final int index;
        final int guiHeight;

        final Button delete;
        final Button confirm;

        final Button apply;

        final TextFieldWidget reward;
        final TextFieldWidget chance;

        final IHasRewards rewards;

        public RewardOption(final Minecraft mc, final Rewards parent, final int height, final int offsetY,
                final int index)
        {
            this.mc = mc;
            this.offsetY = offsetY;
            this.guiHeight = height;
            this.parent = parent;

            this.rewards = parent.parent.rewards;

            this.index = index;

            this.reward = new TextFieldWidget(parent.font, 0, 0, 150, 10, new StringTextComponent(""));
            this.chance = new TextFieldWidget(parent.font, 0, 0, 25, 10, new StringTextComponent(""));

            this.chance.setText("1.0");

            final Predicate<String> floatValid = input ->
            {
                try
                {
                    Float.parseFloat(input);
                    return true;
                }
                catch (final NumberFormatException e)
                {
                    return input.isEmpty();
                }
            };
            this.chance.setValidator(floatValid);

            this.reward.setMaxStringLength(1024);

            if (index < this.rewards.getRewards().size())
            {
                final Reward r = this.rewards.getRewards().get(index);
                final CompoundNBT tag = r.stack.serializeNBT();
                this.reward.setText(tag + "");
                this.chance.setText(r.chance + "");
            }

            this.confirm = new Button(0, 0, 10, 10, new StringTextComponent("Y"), b ->
            {
                b.playDownSound(this.mc.getSoundHandler());
                this.reward.setText("");
                this.onUpdated();
            });
            this.delete = new Button(0, 0, 10, 10, new StringTextComponent("x"), b ->
            {
                b.playDownSound(this.mc.getSoundHandler());
                this.confirm.active = !this.confirm.active;
            });
            this.delete.setFGColor(0xFFFF0000);
            if (index == this.rewards.getRewards().size()) this.delete.active = false;
            this.confirm.active = false;

            this.apply = new Button(0, 0, 45, 10, new StringTextComponent("Apply"), b ->
            {
                b.playDownSound(this.mc.getSoundHandler());
                this.onUpdated();
            });

            parent.addButton(this.delete);
            parent.addButton(this.confirm);
            parent.addButton(this.apply);
            parent.addButton(this.reward);
            parent.addButton(this.chance);

            this.chance.setCursorPosition(-this.chance.getCursorPosition());
            this.reward.setCursorPosition(-this.reward.getCursorPosition());

            this.delete.visible = false;
            this.confirm.visible = false;
            this.apply.visible = false;
            this.reward.visible = false;
            this.chance.visible = false;
        }

        @Override
        public void preRender(final int slotIndex, final int x, final int y, final int listWidth, final int slotHeight,
                final int mouseX, final int mouseY, final boolean isSelected, final float partialTicks)
        {
            this.delete.visible = false;
            this.confirm.visible = false;
            this.reward.visible = false;
            this.chance.visible = false;
            this.apply.visible = false;
        }

        @Override
        public void render(final MatrixStack mat, final int slotIndex, final int y, final int x, final int listWidth,
                final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected,
                final float partialTicks)
        {
            this.confirm.visible = true;
            this.delete.visible = true;
            this.reward.visible = true;
            this.chance.visible = true;
            this.apply.visible = true;

            this.apply.x = x + 18 + this.reward.getWidth() + this.chance.getWidth();
            this.apply.y = y - 5;
            this.reward.x = x - 2;
            this.reward.y = y - 4;
            this.chance.x = x - 2 + this.reward.getWidth();
            this.chance.y = y - 4;
            this.delete.y = y - 5;
            this.delete.x = x - 1 + this.reward.getWidth() + this.chance.getWidth();
            this.confirm.y = y - 5;
            this.confirm.x = x - 2 + 10 + this.reward.getWidth() + this.chance.getWidth();

            this.reward.render(mat, mouseX, mouseY, partialTicks);
            this.chance.render(mat, mouseX, mouseY, partialTicks);
            this.delete.render(mat, mouseX, mouseY, partialTicks);
            this.confirm.render(mat, mouseX, mouseY, partialTicks);
            this.apply.render(mat, mouseX, mouseY, partialTicks);
        }

        public void onUpdated()
        {
            // We are clearing it
            if (this.reward.getText().isEmpty() && this.index < this.rewards.getRewards().size())
            {
                this.rewards.getRewards().remove(this.index);
                this.delete.visible = false;
                this.confirm.visible = false;
                this.reward.visible = false;
                this.chance.visible = false;
                this.apply.visible = false;
                this.parent.initList();
            }
            // We are editing or adding it
            else try
            {
                final NBTTagArgument arg = NBTTagArgument.func_218085_a();
                final CompoundNBT tag = (CompoundNBT) arg.parse(new StringReader(this.reward.getText()));
                System.out.println(tag);
                final float chance = Float.parseFloat(this.chance.getText());
                final Reward r = new Reward(ItemStack.read(tag), chance);
                if (this.index == this.rewards.getRewards().size())
                {
                    this.rewards.getRewards().add(r);
                    // Update the list for the page.
                    this.parent.initList();
                }
                else this.rewards.getRewards().set(this.index, r);
            }
            catch (final Exception e)
            {
                Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent("Errored format for reward!"),
                        true);
            }
            if (this.rewards instanceof ICapabilitySerializable)
            {
                final ICapabilitySerializable<? extends INBT> ser = (ICapabilitySerializable<?>) this.rewards;
                final INBT tag = ser.serializeNBT();
                final PacketTrainer message = new PacketTrainer(PacketTrainer.UPDATETRAINER);
                final CompoundNBT nbt = message.getTag();
                nbt.put("__rewards__", tag);
                nbt.putInt("I", this.parent.parent.entity.getEntityId());
                PacketTrainer.ASSEMBLER.sendToServer(message);
            }
        }

    }

    public Rewards(final EditorGui parent)
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
        this.list = new ScrollGui<>(this, this.minecraft, width, height, 10, x + 10, y + 24);
        for (int i = 0; i < this.parent.rewards.getRewards().size() + 1; i++)
            this.list.addEntry(new RewardOption(this.minecraft, this, height, y + 24, i));

        x = this.width / 2;
        y = this.height / 2;
        this.addButton(new Button(x + 73, y + 64, 50, 12, new TranslationTextComponent("traineredit.button.home"), b ->
        {
            this.closeCallback.run();
        }));
    }
}
