package pokecube.api.entity.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.core.pokemob.combat.CommandAttackEvent;
import pokecube.api.moves.Move_Base;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.ai.tasks.idle.HungerTask;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;
import thut.api.maths.Vector3;

public class AttackLocationHandler extends DefaultHandler
{
    protected Vector3 location;

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
        PokecubeAPI.POKEMOB_BUS.post(evt);

        if (!evt.isCanceled() && currentMove != 5 && MovesUtils.canUseMove(pokemob))
        {
            final Move_Base move = MovesUtils.getMoveFromName(pokemob.getMoves()[currentMove]);
            // Send move use message first.
            Component mess = new TranslatableComponent("pokemob.action.usemove", pokemob.getDisplayName(),
                    new TranslatableComponent(MovesUtils.getUnlocalizedMove(move.getName())));
            if (this.fromOwner()) pokemob.displayMessageToOwner(mess);

            final float value = HungerTask.calculateHunger(pokemob);

            // If too hungry, send message about that.
            if (HungerTask.hitThreshold(value, HungerTask.HUNTTHRESHOLD))
            {
                mess = new TranslatableComponent("pokemob.action.hungry", pokemob.getDisplayName());
                if (this.fromOwner()) pokemob.displayMessageToOwner(mess);
                return;
            }

            // Otherwise set the location for execution of move.
            BrainUtils.setMoveUseTarget(pokemob.getEntity(), this.location);
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
