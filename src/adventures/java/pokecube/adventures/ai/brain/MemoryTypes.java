package pokecube.adventures.ai.brain;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import pokecube.adventures.PokecubeAdv;

public class MemoryTypes
{
    public static final Supplier<MemoryModuleType<LivingEntity>> BATTLETARGET;

    static
    {
        BATTLETARGET = PokecubeAdv.MEMORIES.register("battle_target", () -> new MemoryModuleType<>(Optional.empty()));
    }

    public static void init()
    {}
}
