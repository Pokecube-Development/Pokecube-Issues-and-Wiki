package pokecube.adventures.client.gui.trainer.editor.pages;

import java.util.function.Predicate;

import com.mojang.brigadier.StringReader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import pokecube.adventures.client.gui.trainer.editor.EditorGui;
import pokecube.adventures.client.gui.trainer.editor.pages.Rewards.RewardOption;
import pokecube.adventures.client.gui.trainer.editor.pages.util.ListPage;
import pokecube.adventures.network.PacketTrainer;
import pokecube.api.entity.trainers.IHasRewards;
import pokecube.api.entity.trainers.IHasRewards.Reward;
import pokecube.core.client.gui.helper.INotifiedEntry;
import pokecube.core.client.gui.helper.ScrollGui;
import thut.lib.TComponent;

public class Rewards extends ListPage<RewardOption>
{
    public static class RewardOption extends AbstractSelectionList.Entry<RewardOption> implements INotifiedEntry
    {

        final Rewards parent;

        final Minecraft mc;

        final int offsetY;
        final int index;
        final int guiHeight;

        final Button delete;
        final Button confirm;

        final Button apply;

        final EditBox reward;
        final EditBox chance;

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

            this.reward = new EditBox(parent.font, 0, 0, 150, 10, TComponent.literal(""));
            this.chance = new EditBox(parent.font, 0, 0, 25, 10, TComponent.literal(""));

            this.chance.setValue("1.0");

            final Predicate<String> floatValid = input -> {
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
            this.chance.setFilter(floatValid);

            this.reward.setMaxLength(1024);

            if (index < this.rewards.getRewards().size())
            {
                final Reward r = this.rewards.getRewards().get(index);
                final CompoundTag tag = r.stack.serializeNBT();
                this.reward.setValue(tag + "");
                this.chance.setValue(r.chance + "");
            }

            this.confirm = new Button.Builder(TComponent.literal("Y"), (b) -> {
                b.playDownSound(this.mc.getSoundManager());
                this.reward.setValue("");
                this.onUpdated();
            }).bounds(0, 0, 10, 10).build();

            this.delete = new Button.Builder(TComponent.literal("X"), (b) -> {
                b.playDownSound(this.mc.getSoundManager());
                this.confirm.active = !this.confirm.active;
            }).bounds(0, 0, 10, 10).build();
            this.delete.setFGColor(0xFFFF0000);

            if (index == this.rewards.getRewards().size()) this.delete.active = false;
            this.confirm.active = false;

            this.apply = new Button.Builder(TComponent.translatable("traineredit.button.apply"), (b) -> {
                b.playDownSound(this.mc.getSoundManager());
                this.onUpdated();
            }).bounds(0, 0, 45, 10).build();

            parent.addRenderableWidget(this.delete);
            parent.addRenderableWidget(this.confirm);
            parent.addRenderableWidget(this.apply);
            parent.addRenderableWidget(this.reward);
            parent.addRenderableWidget(this.chance);

            this.chance.moveCursorTo(-this.chance.getCursorPosition());
            this.reward.moveCursorTo(-this.reward.getCursorPosition());

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
        public void render(final GuiGraphics graphics, final int slotIndex, final int y, final int x, final int listWidth,
                           final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected,
                           final float partialTicks)
        {
            this.confirm.visible = true;
            this.delete.visible = true;
            this.reward.visible = true;
            this.chance.visible = true;
            this.apply.visible = true;

            this.apply.setX(x + 18 + this.reward.getWidth() + this.chance.getWidth());
            this.apply.setY(y - 5);
            this.reward.setX(x - 2);
            this.reward.setY(y - 4);
            this.chance.setX(x - 2 + this.reward.getWidth());
            this.chance.setY(y - 4);
            this.delete.setY(y - 5);
            this.delete.setX(x - 1 + this.reward.getWidth() + this.chance.getWidth());
            this.confirm.setY(y - 5);
            this.confirm.setX(x - 2 + 10 + this.reward.getWidth() + this.chance.getWidth());

            this.reward.render(graphics, mouseX, mouseY, partialTicks);
            this.chance.render(graphics, mouseX, mouseY, partialTicks);
            this.delete.render(graphics, mouseX, mouseY, partialTicks);
            this.confirm.render(graphics, mouseX, mouseY, partialTicks);
            this.apply.render(graphics, mouseX, mouseY, partialTicks);
        }

        public void onUpdated()
        {
            // We are clearing it
            if (this.reward.getValue().isEmpty() && this.index < this.rewards.getRewards().size())
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
                final NbtTagArgument arg = NbtTagArgument.nbtTag();
                final CompoundTag tag = (CompoundTag) arg.parse(new StringReader(this.reward.getValue()));
                final float chance = Float.parseFloat(this.chance.getValue());
                final Reward r = new Reward(ItemStack.of(tag), chance);
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
                Minecraft.getInstance().player.displayClientMessage(TComponent.literal("Errored format for reward!"),
                        true);
            }
            final Tag tag = this.rewards.serializeNBT();
            final PacketTrainer message = new PacketTrainer(PacketTrainer.UPDATETRAINER);
            final CompoundTag nbt = message.getTag();
            nbt.put("__rewards__", tag);
            nbt.putInt("I", this.parent.parent.entity.getId());
            PacketTrainer.ASSEMBLER.sendToServer(message);
        }
    }

    public Rewards(final EditorGui parent)
    {
        super(TComponent.literal(""), parent);
    }

    @Override
    public boolean isValid()
    {
        return parent.rewards != null;
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
        this.list = new ScrollGui<>(this, this.minecraft, width, height, 10, x + 10, y + 24);
        for (int i = 0; i < this.parent.rewards.getRewards().size() + 1; i++)
            this.list.addEntry(new RewardOption(this.minecraft, this, height, y + 24, i));

        x = this.width / 2;
        y = this.height / 2;

        this.addRenderableWidget(new Button.Builder(TComponent.translatable("traineredit.button.home"), (b) -> {
            this.closeCallback.run();
        }).bounds(x + 73, y + 64, 50, 12).build());
    }
}
