package pokecube.adventures.client.gui.trainer.editor.pages;

import java.util.function.Function;
import java.util.function.Predicate;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.client.gui.trainer.editor.EditorGui;
import pokecube.adventures.client.gui.trainer.editor.pages.util.Page;
import pokecube.adventures.network.PacketTrainer;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.trainers.IHasNPCAIStates.AIState;
import pokecube.core.client.gui.helper.GuardEntry;
import pokecube.core.client.gui.helper.RouteEditHelper;
import pokecube.core.client.gui.helper.ScrollGui;
import pokecube.core.network.packets.PacketSyncRoutes;
import thut.lib.TComponent;

public class AI extends Page
{
    ScrollGui<GuardEntry> guardList;

    EditBox resetTimeLose;
    EditBox resetTimeWin;
    EditBox battleCooldown;
    EditBox faceDirection;

    Runnable callback = () -> {
        if (Minecraft.getInstance().screen == this.parent)
        {
            // Re-add the callback
            this.parent.guard.attachChangeListener(this.callback);
            // Re-initialise the list
            this.onPageOpened();
        }
    };

    public AI(final EditorGui parent)
    {
        super(TComponent.literal(""), parent);
        parent.guard.attachChangeListener(callback);
    }

    @Override
    public boolean isValid()
    {
        return parent.aiStates != null;
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

        final Function<CompoundTag, CompoundTag> function = t -> {
            PacketSyncRoutes.sendServerPacket(this.parent.entity, t);
            this.onPageOpened();
            return t;
        };
        int dx = 3;
        int dy = 25;
        if (parent.guard != null)
        {
            RouteEditHelper.getGuiList(this.guardList, this.parent.guard, function, this.parent.entity, this, 60, dx,
                    dy, 50);
            this.children.add(this.guardList);
        }
        dx = -121;
        dy = -73;
        final int sy = 12;
        int i = 0;

        this.resetTimeLose = new EditBox(this.font, x + dx, y + dy + sy * i++, 50, 10, TComponent.literal(""));
        this.resetTimeWin = new EditBox(this.font, x + dx, y + dy + sy * i++, 50, 10, TComponent.literal(""));
        this.battleCooldown = new EditBox(this.font, x + dx, y + dy + sy * i++, 50, 10, TComponent.literal(""));
        this.faceDirection = new EditBox(this.font, x + dx, y + dy + sy * i++, 30, 10, TComponent.literal(""));

        final Predicate<String> intValid = input -> {
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

        this.resetTimeLose.setFilter(intValid);
        this.resetTimeWin.setFilter(intValid);
        this.battleCooldown.setFilter(intValid);
        this.faceDirection.setFilter(floatValid);

        this.faceDirection.setValue(this.parent.aiStates.getDirection() + "");
        this.addRenderableWidget(this.faceDirection);

        if (this.parent.trainer instanceof DefaultPokemobs trainer)
        {
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
            final OnPress action = b -> {
                final boolean flag = !this.parent.aiStates.getAIState(state);
                this.parent.aiStates.setAIState(state, flag);
                b.setFGColor(flag ? 0x00FF00 : 0xFF0000);
                this.onChanged();
            };
            final Button press = new Button(x - 123, y - 30 + index * 12, 100, 12, TComponent.literal(state.name()),
                    action);
            press.setFGColor(this.parent.aiStates.getAIState(state) ? 0x00FF00 : 0xFF0000);
            this.addRenderableWidget(press);
        }

        this.addRenderableWidget(
                new Button(x + 73, y + 64, 50, 12, TComponent.translatable("traineredit.button.home"), b ->
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
        Tag tag = this.parent.aiStates.serializeNBT();
        try
        {
            parent.aiStates.setDirection(Float.parseFloat(this.faceDirection.getValue()));
        }
        catch (NumberFormatException e)
        {
            PokecubeAPI.LOGGER.error(e);
        }
        PacketTrainer message = new PacketTrainer(PacketTrainer.UPDATETRAINER);
        message.getTag().putInt("I", this.parent.entity.getId());
        message.getTag().put("__ai__", tag);
        if (this.parent.trainer instanceof DefaultPokemobs trainer)
        {
            try
            {
                trainer.resetTimeLose = Integer.parseInt(this.resetTimeLose.getValue());
                trainer.resetTimeWin = Integer.parseInt(this.resetTimeWin.getValue());
                trainer.battleCooldown = Integer.parseInt(this.battleCooldown.getValue());
            }
            catch (NumberFormatException e)
            {
                PokecubeAPI.LOGGER.error(e);
            }
            tag = trainer.serializeNBT();
            message.getTag().put("__T__", tag);
        }
        PacketTrainer.ASSEMBLER.sendToServer(message);
    }
}
