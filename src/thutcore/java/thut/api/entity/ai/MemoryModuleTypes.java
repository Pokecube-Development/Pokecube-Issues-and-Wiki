package thut.api.entity.ai;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.pathfinder.Path;

public class MemoryModuleTypes
{
    // Used for pathing
    public static final MemoryModuleType<Path> PATH = MemoryModuleType.PATH;
    public static final MemoryModuleType<WalkTarget> WALK_TARGET = MemoryModuleType.WALK_TARGET;
    public static final MemoryModuleType<Long> NOT_FOUND_PATH = MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE;

    // Misc
    public static final MemoryModuleType<PositionTracker> LOOK_TARGET = MemoryModuleType.LOOK_TARGET;

    public static final MemoryModuleType<AgeableMob> MATE_TARGET = MemoryModuleType.BREED_TARGET;
}
