package pokecube.core.interfaces.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.tasks.idle.AIHungry;
import pokecube.core.events.pokemob.combat.CommandAttackEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;
import thut.api.maths.Vector3;

public class AttackLocationHandler extends DefaultHandler
{
    Vector3 location;

    public AttackLocationHandler()
    {
    }

    public AttackLocationHandler(final Vector3 location)
    {
        this.location = location.copy();
    }

    @Override
    public void handleCommand(final IPokemob pokemob)
    {
        final int currentMove = pokemob.getMoveIndex();
        final CommandAttackEvent evt = new CommandAttackEvent(pokemob.getEntity(), null);
        PokecubeCore.POKEMOB_BUS.post(evt);

        if (!evt.isCanceled() && currentMove != 5 && MovesUtils.canUseMove(pokemob))
        {
            final Move_Base move = MovesUtils.getMoveFromName(pokemob.getMoves()[currentMove]);
            // Send move use message first.
            ITextComponent mess = new TranslationTextComponent("pokemob.action.usemove", pokemob.getDisplayName(),
                    new TranslationTextComponent(MovesUtils.getUnlocalizedMove(move.getName())));
            if (this.fromOwner()) pokemob.displayMessageToOwner(mess);

            final float value = AIHungry.calculateHunger(pokemob);

            // If too hungry, send message about that.
            if (AIHungry.hitThreshold(value, AIHungry.HUNTTHRESHOLD))
            {
                mess = new TranslationTextComponent("pokemob.action.hungry", pokemob.getDisplayName());
                if (this.fromOwner()) pokemob.displayMessageToOwner(mess);
                return;
            }

            // Otherwise set the location for execution of move.
            pokemob.setCombatState(CombatStates.NEWEXECUTEMOVE, true);
            pokemob.setTargetPos(this.location);
        }
    }

    @Override
    public void readFromBuf(final ByteBuf buf)
    {
        super.readFromBuf(buf);
        this.location = Vector3.readFromBuff(buf);
    }

    @Override
    public void writeToBuf(final ByteBuf buf)
    {
        super.writeToBuf(buf);
        this.location.writeToBuff(buf);
    }
}
