package pokecube.adventures.advancements;

import pokecube.adventures.advancements.triggers.BeatLeaderTrigger;
import pokecube.adventures.advancements.triggers.BeatTrainerTrigger;

public class Triggers
{
    public static BeatLeaderTrigger  BEATLEADER  = pokecube.core.handlers.playerdata.advancements.triggers.Triggers
            .register(new BeatLeaderTrigger());
    public static BeatTrainerTrigger BEATTRAINER = pokecube.core.handlers.playerdata.advancements.triggers.Triggers
            .register(new BeatTrainerTrigger());

    public static void init()
    {
    }
}