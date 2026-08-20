package pokecube.gimmicks.mega.abilities;

import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IHasCommands;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.commandhandlers.ChangeFormHandler;
import pokecube.gimmicks.mega.MegaEvolveHelper;

@AbilityProvider(name = "power-construct")
public class PowerConstruct extends Ability
{

    @Override
    public void onUpdate(final IPokemob mob)
    {
        if (mob.getHealth() <= mob.getMaxHealth() / 2.0f && !MegaEvolveHelper.isMega(mob))
        {
            mob.handleCommand(IHasCommands.Command.CHANGEFORM, new ChangeFormHandler());
        }

    }
}
