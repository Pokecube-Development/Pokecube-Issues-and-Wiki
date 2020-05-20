package pokecube.core.ai.brain;

import java.util.Optional;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.pathfinding.Path;
import net.minecraftforge.event.RegistryEvent.Register;
import pokecube.core.PokecubeCore;

public class MemoryModules
{
    // Used for combat
    public static final MemoryModuleType<LivingEntity> ATTACKTARGET = new MemoryModuleType<>(Optional.empty());
    public static final MemoryModuleType<LivingEntity> HUNTTARGET   = new MemoryModuleType<>(Optional.empty());

    // Used for pathing
    public static final MemoryModuleType<Path>       PATH        = MemoryModuleType.PATH;
    public static final MemoryModuleType<WalkTarget> WALK_TARGET = MemoryModuleType.WALK_TARGET;

    public static void register(final Register<MemoryModuleType<?>> event)
    {
        event.getRegistry().register(MemoryModules.ATTACKTARGET.setRegistryName(PokecubeCore.MODID, "attack_target"));
        event.getRegistry().register(MemoryModules.HUNTTARGET.setRegistryName(PokecubeCore.MODID, "hunt_target"));
    }
}
