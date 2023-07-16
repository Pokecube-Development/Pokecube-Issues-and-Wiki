package pokecube.core.ai.npc;

import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.entity.schedule.ScheduleBuilder;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.PokecubeCore;

public class Schedules
{
    public static final RegistryObject<Schedule> ADULT;
    public static final RegistryObject<Schedule> CHILD;

    static
    {
        ADULT = PokecubeCore.SCHEDULES.register("adult_npc", () -> Schedules.makeAdult());
        CHILD = PokecubeCore.SCHEDULES.register("child_npc", () -> Schedules.makeChild());
    }

    public static void init()
    {}

    private static Schedule makeAdult()
    {
        final ScheduleBuilder builder = new ScheduleBuilder(new Schedule());
        builder
        //@formatter:off
        .changeActivityAt(10, Activity.IDLE)
        .changeActivityAt(10, Activities.STATIONARY.get())
        .changeActivityAt(2000,Activity.WORK)
        .changeActivityAt(9000, Activity.MEET)
        .changeActivityAt(11000, Activity.IDLE)
        .changeActivityAt(12000, Activity.REST);
        //@formatter:on
        return builder.build();
    }

    private static Schedule makeChild()
    {
        final ScheduleBuilder builder = new ScheduleBuilder(new Schedule());
        builder.changeActivityAt(10, Activity.IDLE).changeActivityAt(10, Activities.STATIONARY.get())
                .changeActivityAt(3000, Activity.PLAY).changeActivityAt(6000, Activity.IDLE)
                .changeActivityAt(10000, Activity.PLAY).changeActivityAt(12000, Activity.REST);
        return builder.build();
    }
}
