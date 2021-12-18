package pokecube.core.ai.brain;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraftforge.event.RegistryEvent.Register;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.tasks.ants.AntTasks;
import pokecube.core.ai.tasks.bees.BeeTasks;
import pokecube.core.ai.tasks.burrows.BurrowTasks;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;

public class MemoryModules
{
    // Used for combat
    public static final MemoryModuleType<LivingEntity> ATTACKTARGET = new MemoryModuleType<>(Optional.empty());
    public static final MemoryModuleType<LivingEntity> HUNTTARGET = new MemoryModuleType<>(Optional.empty());
    public static final MemoryModuleType<PositionTracker> MOVE_TARGET = new MemoryModuleType<>(Optional.empty());
    public static final MemoryModuleType<PositionTracker> LEAP_TARGET = new MemoryModuleType<>(Optional.empty());
    public static final MemoryModuleType<LivingEntity> HUNTED_BY = new MemoryModuleType<>(Optional.empty());

    // Used for idle tasks
    public static final MemoryModuleType<GlobalPos> NEST_POS = new MemoryModuleType<>(Optional.of(GlobalPos.CODEC));
    public static final MemoryModuleType<GlobalPos> WORK_POS = new MemoryModuleType<>(Optional.of(GlobalPos.CODEC));

    public static final MemoryModuleType<Integer> OUT_OF_NEST_TIMER = new MemoryModuleType<>(Optional.of(Codec.INT));
    public static final MemoryModuleType<Integer> NO_NEST_TIMER = new MemoryModuleType<>(Optional.of(Codec.INT));
    public static final MemoryModuleType<Integer> NO_WORK_TIMER = new MemoryModuleType<>(Optional.of(Codec.INT));

    public static final MemoryModuleType<Integer> JOB_TYPE = new MemoryModuleType<>(Optional.of(Codec.INT));

    public static final MemoryModuleType<CompoundTag> JOB_INFO = new MemoryModuleType<>(Optional.of(CompoundTag.CODEC));

    public static final MemoryModuleType<Boolean> GOING_HOME = new MemoryModuleType<>(Optional.of(Codec.BOOL));

    // Used for pathing
    public static final MemoryModuleType<Path> PATH = MemoryModuleType.PATH;
    public static final MemoryModuleType<WalkTarget> WALK_TARGET = MemoryModuleType.WALK_TARGET;
    public static final MemoryModuleType<Long> NOT_FOUND_PATH = MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE;

    // Misc
    public static final MemoryModuleType<PositionTracker> LOOK_TARGET = MemoryModuleType.LOOK_TARGET;

    public static final MemoryModuleType<EntityPokemobEgg> EGG = new MemoryModuleType<>(Optional.empty());

    public static final MemoryModuleType<List<NearBlock>> VISIBLE_BLOCKS = new MemoryModuleType<>(Optional.empty());
    public static final MemoryModuleType<List<ItemEntity>> VISIBLE_ITEMS = new MemoryModuleType<>(Optional.empty());

    public static final MemoryModuleType<List<AgeableMob>> POSSIBLE_MATES = new MemoryModuleType<>(Optional.empty());
    public static final MemoryModuleType<AgeableMob> MATE_TARGET = MemoryModuleType.BREED_TARGET;

    public static final MemoryModuleType<List<LivingEntity>> HERD_MEMBERS = new MemoryModuleType<>(Optional.empty());

    public static void register(final Register<MemoryModuleType<?>> event)
    {
        event.getRegistry().register(MemoryModules.ATTACKTARGET.setRegistryName(PokecubeCore.MODID, "attack_target"));
        event.getRegistry().register(MemoryModules.HUNTTARGET.setRegistryName(PokecubeCore.MODID, "hunt_target"));
        event.getRegistry().register(MemoryModules.HUNTED_BY.setRegistryName(PokecubeCore.MODID, "hunted_by"));
        event.getRegistry().register(MemoryModules.MOVE_TARGET.setRegistryName(PokecubeCore.MODID, "move_target"));
        event.getRegistry().register(MemoryModules.LEAP_TARGET.setRegistryName(PokecubeCore.MODID, "leap_target"));

        event.getRegistry()
                .register(MemoryModules.VISIBLE_BLOCKS.setRegistryName(PokecubeCore.MODID, "visible_blocks"));
        event.getRegistry().register(MemoryModules.VISIBLE_ITEMS.setRegistryName(PokecubeCore.MODID, "visible_items"));

        event.getRegistry().register(MemoryModules.POSSIBLE_MATES.setRegistryName(PokecubeCore.MODID, "mate_options"));

        event.getRegistry().register(MemoryModules.NEST_POS.setRegistryName(PokecubeCore.MODID, "nest_pos"));
        event.getRegistry().register(MemoryModules.WORK_POS.setRegistryName(PokecubeCore.MODID, "work_pos"));
        event.getRegistry().register(MemoryModules.GOING_HOME.setRegistryName(PokecubeCore.MODID, "go_home"));

        event.getRegistry().register(AntTasks.JOB_TYPE.setRegistryName(PokecubeCore.MODID, "job_type"));
        event.getRegistry().register(AntTasks.JOB_INFO.setRegistryName(PokecubeCore.MODID, "job_info"));

        event.getRegistry().register(MemoryModules.NO_NEST_TIMER.setRegistryName(PokecubeCore.MODID, "no_nest_timer"));
        event.getRegistry().register(MemoryModules.NO_WORK_TIMER.setRegistryName(PokecubeCore.MODID, "no_work_timer"));

        event.getRegistry()
                .register(MemoryModules.OUT_OF_NEST_TIMER.setRegistryName(PokecubeCore.MODID, "out_of_nest_timer"));

        event.getRegistry().register(MemoryModules.EGG.setRegistryName(PokecubeCore.MODID, "egg"));

        BeeTasks.registerMems(event);
        AntTasks.registerMems(event);
        BurrowTasks.registerMems(event);
    }
}
