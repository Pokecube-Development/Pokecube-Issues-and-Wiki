package pokecube.adventures.client.gui.trainer.editor.pages;

import java.util.function.Function;
import java.util.function.Predicate;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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

    EditBox resetTimeLose;
    EditBox resetTimeWin;
    EditBox battleCooldown;
    EditBox faceDirection;

    public AI(final EditorGui parent)
    {
        super(new TextComponent(""), parent);
    }

    @Override
    public void onPageOpened()
    {
        this.children.clear();
        this.renderables.clear();
        super.onPageOpened();

        final int x = this.width / 2;
        final int y = this.height / 2;

        this.guardList = new ScrollGui<>(this, this.minecraft, 92, 120, 35, x + 30, y - 65);

        final Function<CompoundTag, CompoundTag> function = t ->
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

        this.resetTimeLose = new EditBox(this.font, x + dx, y + dy + sy * i++, 50, 10, new TextComponent(
                ""));
        this.resetTimeWin = new EditBox(this.font, x + dx, y + dy + sy * i++, 50, 10, new TextComponent(
                ""));
        this.battleCooldown = new EditBox(this.font, x + dx, y + dy + sy * i++, 50, 10, new TextComponent(
                ""));
        this.faceDirection = new EditBox(this.font, x + dx, y + dy + sy * i++, 30, 10, new TextComponent(
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

        this.resetTimeLose.setFilter(intValid);
        this.resetTimeWin.setFilter(intValid);
        this.battleCooldown.setFilter(intValid);
        this.faceDirection.setFilter(floatValid);

        this.faceDirection.setValue(this.parent.aiStates.getDirection() + "");
        this.addRenderableWidget(this.faceDirection);

        if (this.parent.trainer instanceof DefaultPokemobs)
        {
            final DefaultPokemobs trainer = (DefaultPokemobs) this.parent.trainer;

            this.resetTimeLose.setValue(trainer.resetTimeLose + "");
            this.resetTimeWin.setValue(trainer.resetTimeWin + "");
            this.battleCooldown.setValue(trainer.battleCooldown + "");

            this.addRenderableWidget(this.resetTimeLose);
            this.addRenderableWidget(this.resetTimeWin);
            this.addRenderableWidget(this.battleCooldown);
        }

        int index = 0;
        for (final AIState state : AIState.values())
        {
            if (state.isTemporary()) continue;
            index++;
            final OnPress action = b ->
            {
                final boolean flag = !this.parent.aiStates.getAIState(state);
                this.parent.aiStates.setAIState(state, flag);
                b.setFGColor(flag ? 0x00FF00 : 0xFF0000);
                this.onChanged();
            };
            final Button press = new Button(x - 123, y - 30 + index * 12, 100, 12, new TextComponent(state
                    .name()), action);
            press.setFGColor(this.parent.aiStates.getAIState(state) ? 0x00FF00 : 0xFF0000);
            this.addRenderableWidget(press);
        }

        this.addRenderableWidget(new Button(x + 73, y + 64, 50, 12, new TranslatableComponent("traineredit.button.home"), b ->
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
    public void render(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks)
    {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.guardList.render(matrixStack, mouseX, mouseY, partialTicks);

        final int x = this.parent.width / 2 - 67;
        final int y = this.parent.height / 2 - 72;
        final int dy = 12;
        int i = 0;
        this.font.draw(matrixStack, I18n.get("Loss Reset Time"), x, y + dy * i++, 0xFFFFFFFF);
        this.font.draw(matrixStack, I18n.get("Win Reset Time"), x, y + dy * i++, 0xFFFFFFFF);
        this.font.draw(matrixStack, I18n.get("Battle Cooldown"), x, y + dy * i++, 0xFFFFFFFF);
        this.font.draw(matrixStack, I18n.get("Fixed Facing"), x - 20, y + dy * i++, 0xFFFFFFFF);

        this.font.draw(matrixStack, I18n.get("Guard Locations"), x + 100, y, 0xFFFFFFFF);
    }

    private void onChanged()
    {
        if (this.parent.aiStates instanceof ICapabilitySerializable)
        {
            final ICapabilitySerializable<? extends Tag> ser = (ICapabilitySerializable<?>) this.parent.aiStates;
            final Tag tag = ser.serializeNBT();
            final PacketTrainer message = new PacketTrainer(PacketTrainer.UPDATETRAINER);
            message.getTag().putInt("I", this.parent.entity.getId());
            message.getTag().put("__ai__", tag);
            PacketTrainer.ASSEMBLER.sendToServer(message);
        }
    }
}
