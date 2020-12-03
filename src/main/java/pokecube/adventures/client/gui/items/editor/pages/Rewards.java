package pokecube.adventures.client.gui.items.editor.pages;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractList;
import net.minecraft.util.text.StringTextComponent;
import pokecube.adventures.capabilities.CapabilityHasRewards.IHasRewards;
import pokecube.adventures.client.gui.items.editor.EditorGui;
import pokecube.adventures.client.gui.items.editor.pages.Rewards.RewardOption;
import pokecube.adventures.client.gui.items.editor.pages.util.ListPage;
import pokecube.core.client.gui.helper.ScrollGui;

public class Rewards extends ListPage<RewardOption>
{
    public static class RewardOption extends AbstractList.AbstractListEntry<RewardOption>
    {

        final Rewards parent;

        final Minecraft mc;

        final int offsetY;
        final int index;
        final int guiHeight;

        final Button delete;
        final Button confirm;

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

            parent.children.add(this);

            this.rewards = parent.parent.rewards;

            this.index = index;

            this.reward = new TextFieldWidget(parent.font, 0, 0, 104, 10, new StringTextComponent(""));
            this.reward.setText("this should be a give command for the reward item.");

            this.chance = new TextFieldWidget(parent.font, 104, 0, 20, 10, new StringTextComponent(""));
            this.chance.setText("this should be the chance for the item.");

            this.confirm = new Button(0, 0, 10, 10, new StringTextComponent("Y"), b ->
            {
                b.playDownSound(this.mc.getSoundHandler());
                this.rewards.getRewards().remove(index);
                this.onUpdated();
                // Update the list for the page.
                this.parent.initList();
            });
            this.delete = new Button(0, 0, 10, 10, new StringTextComponent("x"), b ->
            {
                b.playDownSound(this.mc.getSoundHandler());
                this.confirm.active = !this.confirm.active;
            });
            this.delete.setFGColor(0xFFFF0000);
            if (index == this.rewards.getRewards().size()) this.delete.active = false;
            this.confirm.active = false;
        }

        @Override
        public void render(final MatrixStack mat, final int slotIndex, final int y, final int x, final int listWidth,
                final int slotHeight, final int mouseX, final int mouseY, final boolean isSelected,
                final float partialTicks)
        {
            boolean fits = true;
            this.reward.x = x - 2;
            this.reward.y = y - 4;
            this.chance.x = x - 2 + this.reward.getWidth();
            this.chance.y = y - 4 + this.reward.getWidth();
            this.delete.y = y - 5;
            this.delete.x = x - 1 + this.reward.getWidth() + this.chance.getWidth();
            this.confirm.y = y - 5;
            this.confirm.x = x - 2 + 10 + this.reward.getWidth() + this.chance.getWidth();
            fits = this.reward.y >= this.offsetY;
            fits = fits && this.reward.y + this.reward.getHeightRealms() <= this.offsetY + this.guiHeight;
            if (fits)
            {
                this.reward.render(mat, mouseX, mouseY, partialTicks);
                this.chance.render(mat, mouseX, mouseY, partialTicks);
                this.delete.render(mat, mouseX, mouseY, partialTicks);
                this.confirm.render(mat, mouseX, mouseY, partialTicks);
            }
        }

        @Override
        public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers)
        {
            if (keyCode == GLFW.GLFW_KEY_ENTER && (this.reward.isFocused() || this.chance.isFocused())) this.onUpdated();
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(final char codePoint, final int modifiers)
        {
            // TODO Auto-generated method stub
            return super.charTyped(codePoint, modifiers);
        }

        @Override
        public boolean mouseClicked(final double mouseX, final double mouseY, final int button)
        {
            // TODO Auto-generated method stub
            return super.mouseClicked(mouseX, mouseY, button);
        }

        public void onUpdated()
        {

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
        super.initList();
        final int offsetX = (this.parent.width - 160) / 2 + 10;
        final int offsetY = (this.parent.height - 160) / 2 + 24;
        final int height = 120;
        final int width = 146;
        this.list = new ScrollGui<>(this, this.minecraft, width, height, 10, offsetX, offsetY);
        for (int i = 0; i < this.parent.rewards.getRewards().size() + 1; i++)
            this.list.addEntry(new RewardOption(this.minecraft, this, height, offsetY, i));
    }

}
