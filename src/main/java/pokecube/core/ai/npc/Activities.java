package pokecube.core.ai.npc;

import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraftforge.event.RegistryEvent;

public class Activities
{
    public static final Activity STATIONARY = new Activity("pokecube:stationary");
    public static final Activity BATTLE     = new Activity("pokecube:battling");

    public static void register(final RegistryEvent.Register<Activity> event)
    {
        Activities.STATIONARY.setRegistryName(Activities.STATIONARY.getName());
        event.getRegistry().register(Activities.STATIONARY);
        Activities.BATTLE.setRegistryName(Activities.BATTLE.getName());
        event.getRegistry().register(Activities.BATTLE);
    }
}
