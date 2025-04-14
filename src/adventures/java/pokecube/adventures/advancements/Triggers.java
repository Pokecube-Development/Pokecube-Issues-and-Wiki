package pokecube.adventures.advancements;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.advancements.triggers.BeatLeaderTrigger;
import pokecube.adventures.advancements.triggers.BeatTrainerTrigger;

import java.lang.reflect.Field;
import java.util.function.Supplier;

public class Triggers
{
    public static final Supplier<BeatLeaderTrigger> BEATLEADER;
    public static final Supplier<BeatTrainerTrigger> BEATTRAINER;

    public static final DeferredRegister<CriterionTrigger<?>> REGISTER;

    static
    {
        REGISTER = DeferredRegister.create(BuiltInRegistries.TRIGGER_TYPES, PokecubeAdv.MODID);
        
        BEATLEADER = REGISTER.register(BeatLeaderTrigger.ID.getPath(), n -> new BeatLeaderTrigger());
        BEATTRAINER = REGISTER.register(BeatTrainerTrigger.ID.getPath(), n -> new BeatTrainerTrigger());
    }

    public static void init()
    {}
}