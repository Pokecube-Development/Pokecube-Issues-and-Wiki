package pokecube.adventures.client.gui.trainer.editor.pages;

import java.util.function.Function;
import java.util.function.Predicate;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates.AIState;
import pokecube.adventures.client.gui.trainer.editor.EditorGui;
import pokecube.adventures.client.gui.trainer.editor.pages.util.Page;
import pokecube.adventures.network.PacketTrainer;
import pokecube.core.client.gui.helper.GuardEntry;
import pokecube.core.client.gui.helper.RouteEditHelper;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.network.packets.PacketSyncRoutes;

public class AI extends Page
{
    ScrollGui<GuardEntry> guardList;

    TextFieldWidget resetTimeLose;
    TextFieldWidget resetTimeWin;
    TextFieldWidget battleCooldown;
    TextFieldWidget faceDirection;

    public AI(final EditorGui parent)
    {
        super(new StringTextComponent(""), parent);
    }

    @Override
    public void onPageOpened()
    {
        this.children.clear();
        this.buttons.clear();
        super.onPageOpened();

        final int x = this.width / 2;
        final int y = this.height / 2;

        this.guardList = new ScrollGui<>(this, this.minecraft, 92, 120, 35, x + 30, y - 65);

        final Function<CompoundNBT, CompoundNBT> function = t ->
        {
            PacketSyncRoutes.sendServerPacket(this.parent.entity, t);
            this.onPageOpened();
            return t;
        };
        int dx = 3;
        int dy = 25;
        RouteEditHelper.getGuiList(this.guardList, this.parent.guard, function, this.parent.entity, this, 60, dx, dy,
                50);
        this.children.add(this.guardList);

        dx = -121;
        dy = -73;
        final int sy = 12;
        int i = 0;

        this.resetTimeLose = new TextFieldWidget(this.font, x + dx, y + dy + sy * i++, 50, 10, new StringTextComponent(
                ""));
        this.resetTimeWin = new TextFieldWidget(this.font, x + dx, y + dy + sy * i++, 50, 10, new StringTextComponent(
                ""));
        this.battleCooldown = new TextFieldWidget(this.font, x + dx, y + dy + sy * i++, 50, 10, new StringTextComponent(
                ""));
        this.faceDirection = new TextFieldWidget(this.font, x + dx, y + dy + sy * i++, 30, 10, new StringTextComponent(
                ""));

        final Predicate<String> intValid = input ->
        {
            try
            {
                Integer.parseInt(input);
                return true;
            }
            catch (final NumberFormatException e)
            {
                return input.isEmpty();
            }
        };
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

        this.resetTimeLose.setValidator(intValid);
        this.resetTimeWin.setValidator(intValid);
        this.battleCooldown.setValidator(intValid);
        this.faceDirection.setValidator(floatValid);

        this.faceDirection.setText(this.parent.aiStates.getDirection() + "");
        this.addButton(this.faceDirection);

        if (this.parent.trainer instanceof DefaultPokemobs)
        {
            final DefaultPokemobs trainer = (DefaultPokemobs) this.parent.trainer;

            this.resetTimeLose.setText(trainer.resetTimeLose + "");
            this.resetTimeWin.setText(trainer.resetTimeWin + "");
            this.battleCooldown.setText(trainer.battleCooldown + "");

            this.addButton(this.resetTimeLose);
            this.addButton(this.resetTimeWin);
            this.addButton(this.battleCooldown);
        }

        int index = 0;
        for (final AIState state : AIState.values())
        {
            if (state.isTemporary()) continue;
            index++;
            final IPressable action = b ->
            {
                final boolean flag = !this.parent.aiStates.getAIState(state);
                this.parent.aiStates.setAIState(state, flag);
                b.setFGColor(flag ? 0x00FF00 : 0xFF0000);
                this.onChanged();
            };
            final Button press = new Button(x - 123, y - 30 + index * 12, 100, 12, new StringTextComponent(state
                    .name()), action);
            press.setFGColor(this.parent.aiStates.getAIState(state) ? 0x00FF00 : 0xFF0000);
            this.addButton(press);
        }

        this.addButton(new Button(x + 73, y + 64, 50, 12, new TranslationTextComponent("traineredit.button.home"), b ->
        {
            this.closeCallback.run();
        }));
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers)
    {
        if (keyCode == GLFW.GLFW_KEY_ENTER)
        {
            this.onChanged();
            return true;
        }
        if (this.guardList.keyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(final MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks)
    {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.guardList.render(matrixStack, mouseX, mouseY, partialTicks);

        final int x = this.parent.width / 2 - 67;
        final int y = this.parent.height / 2 - 72;
        final int dy = 12;
        int i = 0;
        this.font.drawString(matrixStack, I18n.format("Loss Reset Time"), x, y + dy * i++, 0xFFFFFFFF);
        this.font.drawString(matrixStack, I18n.format("Win Reset Time"), x, y + dy * i++, 0xFFFFFFFF);
        this.font.drawString(matrixStack, I18n.format("Battle Cooldown"), x, y + dy * i++, 0xFFFFFFFF);
        this.font.drawString(matrixStack, I18n.format("Fixed Facing"), x - 20, y + dy * i++, 0xFFFFFFFF);

        this.font.drawString(matrixStack, I18n.format("Guard Locations"), x + 100, y, 0xFFFFFFFF);
    }

    private void onChanged()
    {
        if (this.parent.aiStates instanceof ICapabilitySerializable)
        {
            final ICapabilitySerializable<? extends INBT> ser = (ICapabilitySerializable<?>) this.parent.aiStates;
            final INBT tag = ser.serializeNBT();
            final PacketTrainer message = new PacketTrainer(PacketTrainer.UPDATETRAINER);
            message.getTag().putInt("I", this.parent.entity.getEntityId());
            message.getTag().put("__ai__", tag);
            PacketTrainer.ASSEMBLER.sendToServer(message);
        }
    }
}
