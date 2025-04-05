package pokecube.core.ai.npc;

import java.util.function.Supplier;

import net.minecraft.world.entity.schedule.Activity;
import pokecube.core.PokecubeCore;

public class Activities
{
    public static final Activity _STATIONARY = new Activity("pokecube:stationary");
    public static final Activity _BATTLE = new Activity("pokecube:battling");

    public static final Supplier<Activity> STATIONARY;
    public static final Supplier<Activity> BATTLE;

    static
    {
        STATIONARY = PokecubeCore.ACTIVITIES.register("stationary", () -> _STATIONARY);
        BATTLE = PokecubeCore.ACTIVITIES.register("battling", () -> _BATTLE);

    }

    public static void init()
    {}
}
