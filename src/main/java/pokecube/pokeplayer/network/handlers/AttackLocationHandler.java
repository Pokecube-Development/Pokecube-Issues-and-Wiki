package pokecube.pokeplayer.network.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.events.pokemob.combat.CommandAttackEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;

// Wrapper to ensure player attacks entity as pokeplayer
public class AttackLocationHandler extends pokecube.core.interfaces.pokemob.commandhandlers.AttackLocationHandler
{
    //Vector3 location;

    public AttackLocationHandler()
    {
    }

    public AttackLocationHandler(Vector3 location)
    {
        this.location = location.copy();
    }

    @Override
    public void handleCommand(IPokemob pokemob)
    {
        if (pokemob.getEntity().getPersistentData().getBoolean("is_a_player"))
        {
            int currentMove = pokemob.getMoveIndex();
            CommandAttackEvent evt = new CommandAttackEvent(pokemob.getEntity(), null);
            MinecraftForge.EVENT_BUS.post(evt);

            if (!evt.isCanceled() && currentMove != 5 && MovesUtils.canUseMove(pokemob))
            {
                pokemob.setCombatState(CombatStates.EXECUTINGMOVE, false);
                pokemob.setCombatState(CombatStates.NOITEMUSE, false);
                Move_Base move = MovesUtils.getMoveFromName(pokemob.getMoves()[currentMove]);
                // Send move use message first.
                ITextComponent mess = new TranslationTextComponent("pokemob.action.usemove",
                        pokemob.getDisplayName(),
                        new TranslationTextComponent(MovesUtils.getUnlocalizedMove(move.getName())));
                if (fromOwner()) pokemob.displayMessageToOwner(mess);

                // If too hungry, send message about that.
                if (pokemob.getHungerTime() > 0)
                {
                    mess = new TranslationTextComponent("pokemob.action.hungry", pokemob.getDisplayName());
                    if (fromOwner()) pokemob.displayMessageToOwner(mess);
                    return;
                }

                // Otherwise set the location for execution of move.
                pokemob.executeMove(null, location, 0);
            }
        }
        else
        {
            // Do default behaviour.
            pokecube.core.interfaces.pokemob.commandhandlers.AttackLocationHandler defaults = new pokecube.core.interfaces.pokemob.commandhandlers.AttackLocationHandler();
            ByteBuf buffer = Unpooled.buffer();
            this.writeToBuf(buffer);
            defaults.readFromBuf(buffer);
            defaults.handleCommand(pokemob);
        }
    }

    /*@Override
    public void writeToBuf(ByteBuf buf)
    {
        super.writeToBuf(buf);
        location.writeToBuff(buf);
    }

    @Override
    public void readFromBuf(ByteBuf buf)
    {
        super.readFromBuf(buf);
        location = Vector3.readFromBuff(buf);
    }*/
}
