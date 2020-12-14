package pokecube.pokeplayer.util.handlers;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.events.pokemob.combat.CommandAttackEvent;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.commandhandlers.AttackLocationHandler;
import pokecube.core.moves.MovesUtils;

public class AttackLocation extends AttackLocationHandler
{
    @Override
    public void handleCommand(final IPokemob pokemob)
    {
        // Use default handling, which just agros stuff.
        if (!pokemob.getEntity().getPersistentData().getBoolean("is_a_player"))
        {
            super.handleCommand(pokemob);
            return;
        }
        final int currentMove = pokemob.getMoveIndex();
        final CommandAttackEvent evt = new CommandAttackEvent(pokemob.getEntity(), null);
        MinecraftForge.EVENT_BUS.post(evt);

        if (!evt.isCanceled() && currentMove != 5 && MovesUtils.canUseMove(pokemob))
        {
            pokemob.setCombatState(CombatStates.EXECUTINGMOVE, false);
            pokemob.setCombatState(CombatStates.NOITEMUSE, false);
            final Move_Base move = MovesUtils.getMoveFromName(pokemob.getMoves()[currentMove]);
            // Send move use message first.
            ITextComponent mess = new TranslationTextComponent("pokemob.action.usemove", pokemob.getDisplayName(),
                    new TranslationTextComponent(MovesUtils.getUnlocalizedMove(move.getName())));
            if (this.fromOwner()) pokemob.displayMessageToOwner(mess);

            // If too hungry, send message about that.
            if (pokemob.getHungerTime() > 0)
            {
                mess = new TranslationTextComponent("pokemob.action.hungry", pokemob.getDisplayName());
                if (this.fromOwner()) pokemob.displayMessageToOwner(mess);
                return;
            }
            // Otherwise set the location for execution of move.
            pokemob.executeMove(null, this.location, 0);
        }
    }
}
