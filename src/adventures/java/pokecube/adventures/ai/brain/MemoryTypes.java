package pokecube.adventures.ai.brain;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import pokecube.adventures.PokecubeAdv;

public class MemoryTypes
{
    public static final Supplier<MemoryModuleType<LivingEntity>> BATTLETARGET;
    public static final Supplier<MemoryModuleType<Integer>> NO_SEEN_TARGET_TIMER;
    public static final Supplier<MemoryModuleType<Integer>> DE_AGRO_TIMER;


    static
    {
        BATTLETARGET = PokecubeAdv.MEMORIES.register("battle_target", () -> new MemoryModuleType<>(Optional.empty()));
        NO_SEEN_TARGET_TIMER = PokecubeAdv.MEMORIES.register("no_seen_target_timer", () -> new MemoryModuleType<>(Optional.empty()));
        DE_AGRO_TIMER = PokecubeAdv.MEMORIES.register("de_agro_timer", () -> new MemoryModuleType<>(Optional.empty()));
    }

    public static void init()
    {}
}
