package pokecube.core.handlers.playerdata.advancements.triggers;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;

public class Triggers
{
    public static final CatchPokemobTrigger      CATCHPOKEMOB      = Triggers.register(new CatchPokemobTrigger());
    public static final KillPokemobTrigger       KILLPOKEMOB       = Triggers.register(new KillPokemobTrigger());
    public static final HatchPokemobTrigger      HATCHPOKEMOB      = Triggers.register(new HatchPokemobTrigger());
    public static final FirstPokemobTrigger      FIRSTPOKEMOB      = Triggers.register(new FirstPokemobTrigger());
    public static final EvolvePokemobTrigger     EVOLVEPOKEMOB     = Triggers.register(new EvolvePokemobTrigger());
    public static final InspectPokemobTrigger    INSPECTPOKEMOB    = Triggers.register(new InspectPokemobTrigger());
    public static final MegaEvolvePokemobTrigger MEGAEVOLVEPOKEMOB = Triggers.register(new MegaEvolvePokemobTrigger());
    public static final BreedPokemobTrigger      BREEDPOKEMOB      = Triggers.register(new BreedPokemobTrigger());
    public static final UseMoveTrigger           USEMOVE           = Triggers.register(new UseMoveTrigger());

    public static void init()
    {
    }

    @SuppressWarnings({ "rawtypes" })
    public static <T extends CriterionTrigger> T register(T criterion)
    {
        return CriteriaTriggers.register(criterion);
    }
}
