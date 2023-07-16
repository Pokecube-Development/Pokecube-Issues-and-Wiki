package pokecube.core.ai.npc;

import net.minecraft.world.entity.schedule.Activity;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.PokecubeCore;

public class Activities
{
    public static final RegistryObject<Activity> STATIONARY;
    public static final RegistryObject<Activity> BATTLE;

    static
    {
        STATIONARY = PokecubeCore.ACTIVITIES.register("stationary", () -> new Activity("pokecube:stationary"));
        BATTLE = PokecubeCore.ACTIVITIES.register("battling", () -> new Activity("pokecube:battling"));

    }

    public static void init()
    {}
}
