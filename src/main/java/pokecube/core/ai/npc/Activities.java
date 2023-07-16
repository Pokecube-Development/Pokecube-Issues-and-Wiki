package pokecube.core.ai.npc;

import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.PokecubeCore;

public class Activities
{
    public static final Activity _STATIONARY = new Activity("pokecube:stationary");
    public static final Activity _BATTLE = new Activity("pokecube:battling");

    public static final RegistryObject<Activity> STATIONARY;
    public static final RegistryObject<Activity> BATTLE;

    static
    {
        STATIONARY = PokecubeCore.ACTIVITIES.register("stationary", () -> _STATIONARY);
        BATTLE = PokecubeCore.ACTIVITIES.register("battling", () -> _BATTLE);

    }

    public static void init()
    {}
}
