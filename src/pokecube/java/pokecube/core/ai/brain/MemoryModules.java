package pokecube.core.ai.brain;

import com.mojang.serialization.Codec;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.ai.tasks.utility.GatherItems;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import thut.api.entity.ai.MemoryModuleTypes;
import thut.api.maths.Vector3;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class MemoryModules extends MemoryModuleTypes
{
    // Used for combat
    public static final Supplier<MemoryModuleType<LivingEntity>> ATTACKTARGET;
    public static final Supplier<MemoryModuleType<LivingEntity>> TARGETOWNER;
    public static final Supplier<MemoryModuleType<LivingEntity>> TRACKEDTARGET;
    public static final Supplier<MemoryModuleType<UUID>> ATTACKTARGETID;
    public static final Supplier<MemoryModuleType<LivingEntity>> HUNTTARGET;
    public static final Supplier<MemoryModuleType<PositionTracker>> MOVE_TARGET;
    public static final Supplier<MemoryModuleType<PositionTracker>> LEAP_TARGET;
    public static final Supplier<MemoryModuleType<LivingEntity>> HUNTED_BY;
    public static final Supplier<MemoryModuleType<Vector3>> COMBAT_CENTRE;

    public static final Supplier<MemoryModuleType<Integer>> TIMER_SWAPMOVE;
    public static final Supplier<MemoryModuleType<Integer>> TIMER_SWAPTARGET;
    public static final Supplier<MemoryModuleType<Integer>> TIMER_FORGETTARGET;

    public static final Supplier<MemoryModuleType<Boolean>> ATTACKDELAY;
    public static final Supplier<MemoryModuleType<Integer>> TIMER_LEAP;
    public static final Supplier<MemoryModuleType<Integer>> TIMER_DODGE;
    public static final Supplier<MemoryModuleType<Boolean>> CALLED_HELP;

    // Used for idle tasks
    public static final Supplier<MemoryModuleType<GlobalPos>> NEST_POS;
    public static final Supplier<MemoryModuleType<GlobalPos>> WORK_POS;

    public static final Supplier<MemoryModuleType<GatherItems.GatherDetails>> GATHER_DETAILS;

    public static final Supplier<MemoryModuleType<Integer>> OUT_OF_NEST_TIMER;
    public static final Supplier<MemoryModuleType<Integer>> NO_NEST_TIMER;
    public static final Supplier<MemoryModuleType<Integer>> NO_WORK_TIMER;

    public static final Supplier<MemoryModuleType<Integer>> JOB_TYPE;
    public static final Supplier<MemoryModuleType<CompoundTag>> JOB_INFO;
    public static final Supplier<MemoryModuleType<Boolean>> GOING_HOME;

    public static final Supplier<MemoryModuleType<EntityPokemobEgg>> EGG;

    public static final Supplier<MemoryModuleType<List<NearBlock>>> VISIBLE_BLOCKS;
    public static final Supplier<MemoryModuleType<List<ItemEntity>>> VISIBLE_ITEMS;
    public static final Supplier<MemoryModuleType<List<Projectile>>> VISIBLE_PROJECTILES;

    public static final Supplier<MemoryModuleType<List<AgeableMob>>> POSSIBLE_MATES;
    public static final Supplier<MemoryModuleType<List<LivingEntity>>> HERD_MEMBERS;

    // Used to decide if we want to do the mixin for brain activation
    public static final Supplier<MemoryModuleType<Boolean>> DUMMY;// Boolean

    static
    {
        // Used for combat
        ATTACKTARGET = PokecubeCore.MEMORIES.register("attack_target", () -> new MemoryModuleType<>(Optional.empty()));
        TARGETOWNER = PokecubeCore.MEMORIES.register("attack_target_owner",
                () -> new MemoryModuleType<>(Optional.empty()));
        TRACKEDTARGET = PokecubeCore.MEMORIES.register("attack_target_tracked",
                () -> new MemoryModuleType<>(Optional.empty()));
        ATTACKTARGETID = PokecubeCore.MEMORIES.register("attack_target_id",
                () -> new MemoryModuleType<>(Optional.empty()));
        HUNTTARGET = PokecubeCore.MEMORIES.register("hunt_target", () -> new MemoryModuleType<>(Optional.empty()));
        HUNTED_BY = PokecubeCore.MEMORIES.register("hunted_by", () -> new MemoryModuleType<>(Optional.empty()));
        MOVE_TARGET = PokecubeCore.MEMORIES.register("move_target", () -> new MemoryModuleType<>(Optional.empty()));
        LEAP_TARGET = PokecubeCore.MEMORIES.register("leap_target", () -> new MemoryModuleType<>(Optional.empty()));
        COMBAT_CENTRE = PokecubeCore.MEMORIES.register("combat_centre", () -> new MemoryModuleType<>(Optional.empty()));
        ATTACKDELAY = PokecubeCore.MEMORIES.register("no_attack_timer", () -> new MemoryModuleType<>(Optional.empty()));
        TIMER_SWAPMOVE = PokecubeCore.MEMORIES.register("same_attack_timer",
                () -> new MemoryModuleType<>(Optional.empty()));
        TIMER_LEAP = PokecubeCore.MEMORIES.register("leap_timer", () -> new MemoryModuleType<>(Optional.empty()));
        TIMER_DODGE = PokecubeCore.MEMORIES.register("dodge_timer", () -> new MemoryModuleType<>(Optional.empty()));
        CALLED_HELP = PokecubeCore.MEMORIES.register("called_help", () -> new MemoryModuleType<>(Optional.empty()));

        TIMER_SWAPTARGET = PokecubeCore.MEMORIES.register("swap_target_timer",
                () -> new MemoryModuleType<>(Optional.empty()));
        TIMER_FORGETTARGET = PokecubeCore.MEMORIES.register("forget_target_timer",
                () -> new MemoryModuleType<>(Optional.empty()));

        // Used for idle tasks
        NEST_POS = PokecubeCore.MEMORIES.register("nest_pos",
                () -> new MemoryModuleType<>(Optional.of(GlobalPos.CODEC)));
        WORK_POS = PokecubeCore.MEMORIES.register("work_pos",
                () -> new MemoryModuleType<>(Optional.of(GlobalPos.CODEC)));

        GATHER_DETAILS = PokecubeCore.MEMORIES.register("gather_details",
                () -> new MemoryModuleType<>(Optional.empty()));

        OUT_OF_NEST_TIMER = PokecubeCore.MEMORIES.register("out_of_nest_timer",
                () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
        NO_NEST_TIMER = PokecubeCore.MEMORIES.register("no_nest_timer",
                () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
        NO_WORK_TIMER = PokecubeCore.MEMORIES.register("no_work_timer",
                () -> new MemoryModuleType<>(Optional.of(Codec.INT)));

        JOB_TYPE = PokecubeCore.MEMORIES.register("job_type", () -> new MemoryModuleType<>(Optional.of(Codec.INT)));

        JOB_INFO = PokecubeCore.MEMORIES.register("job_info",
                () -> new MemoryModuleType<>(Optional.of(CompoundTag.CODEC)));

        GOING_HOME = PokecubeCore.MEMORIES.register("go_home", () -> new MemoryModuleType<>(Optional.of(Codec.BOOL)));

        EGG = PokecubeCore.MEMORIES.register("egg", () -> new MemoryModuleType<>(Optional.empty()));

        VISIBLE_BLOCKS = PokecubeCore.MEMORIES.register("visible_blocks",
                () -> new MemoryModuleType<>(Optional.empty()));
        VISIBLE_ITEMS = PokecubeCore.MEMORIES.register("visible_items", () -> new MemoryModuleType<>(Optional.empty()));
        VISIBLE_PROJECTILES = PokecubeCore.MEMORIES.register("visible_projectiles",
                () -> new MemoryModuleType<>(Optional.empty()));

        POSSIBLE_MATES = PokecubeCore.MEMORIES.register("mate_options", () -> new MemoryModuleType<>(Optional.empty()));

        HERD_MEMBERS = PokecubeCore.MEMORIES.register("herd_members", () -> new MemoryModuleType<>(Optional.empty()));

        DUMMY = PokecubeCore.MEMORIES.register("dummy", () -> new MemoryModuleType<>(Optional.empty()));

    }

    public static void init()
    {}
}
