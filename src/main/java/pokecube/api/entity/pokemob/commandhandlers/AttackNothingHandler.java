package pokecube.api.entity.pokemob.commandhandlers;

import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.core.pokemob.combat.CommandAttackEvent;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;

public class AttackNothingHandler extends DefaultHandler
{

    public AttackNothingHandler()
    {
    }

    @Override
    public void handleCommand(IPokemob pokemob)
    {
        final CommandAttackEvent event = new CommandAttackEvent(pokemob.getEntity(), null);
        PokecubeAPI.POKEMOB_BUS.post(event);
        if (!event.isCanceled()) pokemob.executeMove(pokemob.getEntity(), null, 0);
    }
}
