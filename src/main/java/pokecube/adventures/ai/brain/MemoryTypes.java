package pokecube.adventures.ai.brain;

import java.util.Optional;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraftforge.event.RegistryEvent.Register;
import pokecube.adventures.PokecubeAdv;

public class MemoryTypes
{
    public static final MemoryModuleType<LivingEntity> BATTLETARGET = new MemoryModuleType<>(Optional.empty());

    public static final MemoryModuleType<Boolean> DUMMY = new MemoryModuleType<>(Optional.empty());

    public static void register(final Register<MemoryModuleType<?>> event)
    {
        event.getRegistry().register(MemoryTypes.BATTLETARGET.setRegistryName(PokecubeAdv.MODID, "battle_target"));
        event.getRegistry().register(MemoryTypes.DUMMY.setRegistryName(PokecubeAdv.MODID, "dummy"));
    }
}
