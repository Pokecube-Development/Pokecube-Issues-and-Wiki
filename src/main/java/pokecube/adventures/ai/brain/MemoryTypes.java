package pokecube.adventures.ai.brain;

import java.util.Optional;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraftforge.registries.RegistryObject;
import pokecube.adventures.PokecubeAdv;

public class MemoryTypes
{
    public static final RegistryObject<MemoryModuleType<LivingEntity>> BATTLETARGET;

    static
    {
        BATTLETARGET = PokecubeAdv.MEMORIES.register("battle_target", () -> new MemoryModuleType<>(Optional.empty()));
    }

    public static void init()
    {}
}
