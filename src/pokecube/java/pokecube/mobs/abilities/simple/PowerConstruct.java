package pokecube.mobs.abilities.simple;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityManager;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IHasCommands;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.commandhandlers.ChangeFormHandler;
import pokecube.api.events.pokemobs.ChangeForm;
import pokecube.core.PokecubeCore;
import pokecube.gimmicks.mega.MegaEvolveHelper;
import thut.core.common.ThutCore;

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

    @Override
    public boolean canChange(IPokemob mob, PokedexEntry changeTo){ return true; }
}
