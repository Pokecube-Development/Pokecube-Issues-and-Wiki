package pokecube.core.ai.npc;

import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.schedule.Schedule;
import net.minecraft.entity.ai.brain.schedule.ScheduleBuilder;
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
        builder.add(10, Activity.IDLE).add(10, Activities.STATIONARY).add(10, Activities.BATTLE).add(2000,
                Activity.WORK).add(9000, Activity.MEET).add(11000, Activity.IDLE).add(12000, Activity.REST);
        return builder.build();
    }

    private static Schedule makeChild()
    {
        final ScheduleBuilder builder = new ScheduleBuilder(new Schedule());
        builder.add(10, Activity.IDLE).add(10, Activities.STATIONARY).add(10, Activities.BATTLE).add(3000,
                Activity.PLAY).add(6000, Activity.IDLE).add(10000, Activity.PLAY).add(12000, Activity.REST);
        return builder.build();
    }
}
