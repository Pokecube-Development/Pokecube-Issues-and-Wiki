package pokecube.adventures.client.gui.items.editor.pages;

import java.io.IOException;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.INBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs.LevelMode;
import pokecube.adventures.capabilities.CapabilityNPCAIStates;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.TrainerCaps;
import pokecube.adventures.client.gui.items.editor.EditorGui;
import pokecube.adventures.client.gui.items.editor.pages.util.Page;
import pokecube.adventures.network.PacketTrainer;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.utils.CapHolders;
import pokecube.core.utils.TimePeriod;

public class AI extends Page
{
    TextFieldWidget roamDistance;
    TextFieldWidget startTime;
    TextFieldWidget endTime;
    TextFieldWidget resetTime;
    TextFieldWidget battleCooldown;
    TextFieldWidget faceDirection;

    public AI(final EditorGui parent)
    {
        super(new StringTextComponent(""), parent);
    }

    protected void actionPerformed(final int id) throws IOException
    {
        ITextComponent mess;
        PacketTrainer packet;
        INBT tag;
        final DefaultPokemobs trainer = (DefaultPokemobs) this.parent.trainer;
        switch (id)
        {
        case 0:
            this.parent.changePage(0);
            break;
        case 1:
            this.parent.aiStates.setAIState(IHasNPCAIStates.STATIONARY, !this.parent.aiStates.getAIState(
                    IHasNPCAIStates.STATIONARY));

            final IGuardAICapability guard = this.parent.guard;
            guard.getPrimaryTask().setPos(this.parent.entity.getPosition());
            guard.getPrimaryTask().setActiveTime(!this.parent.aiStates.getAIState(IHasNPCAIStates.STATIONARY)
                    ? new TimePeriod(0, 0)
                    : TimePeriod.fullDay);
            this.sendGuardUpdate();
            this.sendAIUpdate();
            mess = new TranslationTextComponent("traineredit.set.stationary." + this.parent.aiStates.getAIState(
                    IHasNPCAIStates.STATIONARY));
            this.parent.mc.player.sendStatusMessage(mess, true);
            break;
        case 2:
            this.parent.aiStates.setAIState(IHasNPCAIStates.FIXEDDIRECTION, !this.parent.aiStates.getAIState(
                    IHasNPCAIStates.FIXEDDIRECTION));
            this.sendAIUpdate();
            mess = new TranslationTextComponent("traineredit.set.norotates." + this.parent.aiStates.getAIState(
                    IHasNPCAIStates.FIXEDDIRECTION));
            this.parent.mc.player.sendStatusMessage(mess, true);
            break;
        case 3:
            trainer.notifyDefeat = !trainer.notifyDefeat;
            this.onPageClosed();
            packet = new PacketTrainer(PacketTrainer.UPDATETRAINER);
            tag = CapabilityHasPokemobs.storage.writeNBT(TrainerCaps.HASPOKEMOBS_CAP, this.parent.trainer, null);
            packet.data.put("T", tag);
            packet.data.putInt("I", this.parent.entity.getEntityId());
            PokecubeAdv.packets.sendToServer(packet);
            this.onPageOpened();
            mess = new TranslationTextComponent("traineredit.set.notify." + trainer.notifyDefeat);
            this.parent.mc.player.sendStatusMessage(mess, true);
            break;
        case 4:
            this.parent.aiStates.setAIState(IHasNPCAIStates.PERMFRIENDLY, !this.parent.aiStates.getAIState(
                    IHasNPCAIStates.PERMFRIENDLY));
            this.sendAIUpdate();
            mess = new TranslationTextComponent("traineredit.set.friendly." + this.parent.aiStates.getAIState(
                    IHasNPCAIStates.PERMFRIENDLY));
            this.parent.mc.player.sendStatusMessage(mess, true);
            break;
        case 5:
            trainer.setLevelMode(LevelMode.values()[(trainer.getLevelMode().ordinal() + 1) % LevelMode
                    .values().length]);
            this.onPageClosed();
            packet = new PacketTrainer(PacketTrainer.UPDATETRAINER);
            tag = CapabilityHasPokemobs.storage.writeNBT(TrainerCaps.HASPOKEMOBS_CAP, this.parent.trainer, null);
            packet.data.put("T", tag);
            packet.data.putInt("I", this.parent.entity.getEntityId());
            PokecubeAdv.packets.sendToServer(packet);
            this.onPageOpened();
            final String levelsButton = I18n.format("traineredit.button.levels." + this.parent.trainer.getLevelMode());
            mess = new TranslationTextComponent("traineredit.set.levels", levelsButton);
            this.parent.mc.player.sendStatusMessage(mess, true);
            break;
        case 6:
            this.parent.aiStates.setAIState(IHasNPCAIStates.MATES, !this.parent.aiStates.getAIState(
                    IHasNPCAIStates.MATES));
            this.sendAIUpdate();
            mess = new TranslationTextComponent("traineredit.set.mates." + this.parent.aiStates.getAIState(
                    IHasNPCAIStates.MATES));
            this.parent.mc.player.sendStatusMessage(mess, true);
            break;
        case 7:
            this.parent.aiStates.setAIState(IHasNPCAIStates.INVULNERABLE, !this.parent.aiStates.getAIState(
                    IHasNPCAIStates.INVULNERABLE));
            this.sendAIUpdate();
            mess = new TranslationTextComponent("traineredit.set.invulnerable." + this.parent.aiStates.getAIState(
                    IHasNPCAIStates.INVULNERABLE));
            this.parent.mc.player.sendStatusMessage(mess, true);
            break;
        case 8:
            this.parent.aiStates.setAIState(IHasNPCAIStates.TRADES, !this.parent.aiStates.getAIState(
                    IHasNPCAIStates.TRADES));
            this.sendAIUpdate();
            mess = new TranslationTextComponent("traineredit.set.trade." + this.parent.aiStates.getAIState(
                    IHasNPCAIStates.TRADES));
            this.parent.mc.player.sendStatusMessage(mess, true);
            break;
        case 9:
            packet = new PacketTrainer(PacketTrainer.UPDATETRAINER);
            // Reset defeat list.
            packet.data.putBoolean("RDL", true);
            packet.data.putInt("I", this.parent.entity.getEntityId());
            PokecubeAdv.packets.sendToServer(packet);
            break;
        }
    }

    @Override
    public boolean keyPressed(final int keyCode, final int p_keyPressed_2_, final int p_keyPressed_3_)
    {
        if (keyCode == GLFW.GLFW_KEY_ENTER) for (int i = 0; i < 6; i++)
            if (this.updateField(i)) return true;

        return super.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_);
    }

    private boolean updateField(final int i)
    {
        TextFieldWidget field = null;

        switch (i)
        {
        case 0:
            field = this.roamDistance;
            break;
        case 1:
            field = this.startTime;
            break;
        case 2:
            field = this.endTime;
            break;
        case 3:
            field = this.resetTime;
            break;
        case 4:
            field = this.battleCooldown;
            break;
        case 5:
            field = this.faceDirection;
            break;
        }

        if (field == null || !field.isFocused()) return false;
        final String value = field.getText();
        float argFloat;
        int argInt;
        TimePeriod time = null;
        float start, end;
        final IGuardAICapability guard = this.parent.guard;
        TimePeriod old = guard.getPrimaryTask().getActiveTime();
        if (old == null) old = new TimePeriod(0, 0);
        start = (float) old.startTime;
        end = (float) old.endTime;
        PacketTrainer packet;
        INBT tag;
        ITextComponent mess = null;
        switch (i)
        {
        case 0:
            argFloat = value.isEmpty() ? 0 : Float.parseFloat(value);
            guard.getPrimaryTask().setRoamDistance(argFloat);
            mess = new TranslationTextComponent("traineredit.set.guarddist", argFloat);
            this.sendGuardUpdate();
            break;
        case 1:
            start = value.isEmpty() ? 0 : Float.parseFloat(value);
            end = Float.parseFloat(this.endTime.getText());
            time = new TimePeriod(start, end);
            guard.getPrimaryTask().setActiveTime(time);
            this.sendGuardUpdate();
            break;
        case 2:
            end = value.isEmpty() ? 0 : Float.parseFloat(value);
            start = Float.parseFloat(this.startTime.getText());
            time = new TimePeriod(start, end);
            guard.getPrimaryTask().setActiveTime(time);
            this.sendGuardUpdate();
            break;
        case 3:
            argInt = value.isEmpty() ? 0 : Integer.parseInt(value);
            ((DefaultPokemobs) this.parent.trainer).resetTime = argInt;
            this.onPageClosed();
            packet = new PacketTrainer(PacketTrainer.UPDATETRAINER);
            tag = CapabilityHasPokemobs.storage.writeNBT(TrainerCaps.HASPOKEMOBS_CAP, this.parent.trainer, null);
            packet.data.put("T", tag);
            packet.data.putInt("I", this.parent.entity.getEntityId());
            PokecubeAdv.packets.sendToServer(packet);
            this.onPageOpened();
            mess = new TranslationTextComponent("traineredit.set.cooldown_p", argInt);
            break;
        case 4:
            argInt = value.isEmpty() ? 0 : Integer.parseInt(value);
            ((DefaultPokemobs) this.parent.trainer).battleCooldown = argInt;
            this.onPageClosed();
            packet = new PacketTrainer(PacketTrainer.UPDATETRAINER);
            tag = CapabilityHasPokemobs.storage.writeNBT(TrainerCaps.HASPOKEMOBS_CAP, this.parent.trainer, null);
            packet.data.put("T", tag);
            packet.data.putInt("I", this.parent.entity.getEntityId());
            PokecubeAdv.packets.sendToServer(packet);
            this.onPageOpened();
            mess = new TranslationTextComponent("traineredit.set.cooldown_g", argInt);
            break;
        case 5:
            argFloat = value.isEmpty() ? 0 : Float.parseFloat(value);
            this.parent.aiStates.setDirection(argFloat);
            this.onPageClosed();
            this.sendAIUpdate();
            this.onPageOpened();
            mess = new TranslationTextComponent("traineredit.set.look", argFloat);
            break;
        }
        if (time != null) mess = new TranslationTextComponent("traineredit.set.guardtime", time.startTick,
                time.endTick);
        if (mess != null) this.parent.mc.player.sendStatusMessage(mess, true);
        return mess != null || time != null;
    }

    private void sendGuardUpdate()
    {
        this.onPageClosed();
        final PacketTrainer packet = new PacketTrainer(PacketTrainer.UPDATETRAINER);
        final IGuardAICapability guard = this.parent.guard;
        final INBT tag = CapHolders.GUARDAI_CAP.getStorage().writeNBT(CapHolders.GUARDAI_CAP, guard, null);
        packet.data.put("T", tag);
        packet.data.putByte("V", (byte) 4);
        packet.data.putInt("I", this.parent.entity.getEntityId());
        PokecubeAdv.packets.sendToServer(packet);
        this.onPageOpened();
    }

    private void sendAIUpdate()
    {
        this.onPageClosed();
        final PacketTrainer packet = new PacketTrainer(PacketTrainer.UPDATETRAINER);
        final INBT tag = CapabilityNPCAIStates.storage.writeNBT(TrainerCaps.AISTATES_CAP, this.parent.aiStates, null);
        packet.data.put("T", tag);
        packet.data.putByte("V", (byte) 3);
        packet.data.putInt("I", this.parent.entity.getEntityId());
        PokecubeAdv.packets.sendToServer(packet);
        this.onPageOpened();
    }
}
