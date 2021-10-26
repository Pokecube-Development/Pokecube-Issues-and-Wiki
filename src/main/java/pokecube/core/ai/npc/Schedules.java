package pokecube.core.ai.npc;

import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.entity.schedule.ScheduleBuilder;
import net.minecraftforge.event.RegistryEvent;

public class Schedules
{
    public static final Schedule ADULT = Schedules.makeAdult();
    public static final Schedule CHILD = Schedules.makeChild();

    public static void register(final RegistryEvent.Register<Schedule> event)
    {
        Schedules.ADULT.setRegistryName("pokecube:adult_npc");
        event.getRegistry().register(Schedules.ADULT);
        Schedules.CHILD.setRegistryName("pokecube:child_npc");
        event.getRegistry().register(Schedules.CHILD);
    }

    private static Schedule makeAdult()
    {
        final ScheduleBuilder builder = new ScheduleBuilder(new Schedule());
        builder
        //@formatter:off
        .changeActivityAt(10, Activity.IDLE)
        .changeActivityAt(10, Activities.STATIONARY)
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
        builder.changeActivityAt(10, Activity.IDLE).changeActivityAt(10, Activities.STATIONARY).changeActivityAt(3000, Activity.PLAY).changeActivityAt(6000, Activity.IDLE)
                .changeActivityAt(10000, Activity.PLAY).changeActivityAt(12000, Activity.REST);
        return builder.build();
    }
}
