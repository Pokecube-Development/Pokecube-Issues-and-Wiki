package pokecube.core.ai.brain;

import java.util.Optional;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraftforge.event.RegistryEvent.Register;
import pokecube.core.PokecubeCore;

public class MemoryModules
{
    public static final MemoryModuleType<LivingEntity> ATTACKTARGET = new MemoryModuleType<>(Optional.empty());

    public static void register(final Register<MemoryModuleType<?>> event)
    {
        event.getRegistry().register(MemoryModules.ATTACKTARGET.setRegistryName(PokecubeCore.MODID, "attack_target"));
    }
}
