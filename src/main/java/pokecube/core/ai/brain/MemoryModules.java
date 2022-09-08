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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.sensors.NearBlocks.NearBlock;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import thut.api.entity.ai.MemoryModuleTypes;

public class MemoryModules extends MemoryModuleTypes
{
    // Used for combat
    public static final RegistryObject<MemoryModuleType<LivingEntity>> ATTACKTARGET;
    public static final RegistryObject<MemoryModuleType<LivingEntity>> HUNTTARGET;
    public static final RegistryObject<MemoryModuleType<PositionTracker>> MOVE_TARGET;
    public static final RegistryObject<MemoryModuleType<PositionTracker>> LEAP_TARGET;
    public static final RegistryObject<MemoryModuleType<LivingEntity>> HUNTED_BY;

    // Used for idle tasks
    public static final RegistryObject<MemoryModuleType<GlobalPos>> NEST_POS;
    public static final RegistryObject<MemoryModuleType<GlobalPos>> WORK_POS;

    public static final RegistryObject<MemoryModuleType<Integer>> OUT_OF_NEST_TIMER;
    public static final RegistryObject<MemoryModuleType<Integer>> NO_NEST_TIMER;
    public static final RegistryObject<MemoryModuleType<Integer>> NO_WORK_TIMER;

    public static final RegistryObject<MemoryModuleType<Integer>> JOB_TYPE;
    public static final RegistryObject<MemoryModuleType<CompoundTag>> JOB_INFO;
    public static final RegistryObject<MemoryModuleType<Boolean>> GOING_HOME;

    public static final RegistryObject<MemoryModuleType<EntityPokemobEgg>> EGG;

    public static final RegistryObject<MemoryModuleType<List<NearBlock>>> VISIBLE_BLOCKS;
    public static final RegistryObject<MemoryModuleType<List<ItemEntity>>> VISIBLE_ITEMS;
    public static final RegistryObject<MemoryModuleType<List<Projectile>>> VISIBLE_PROJECTILES;

    public static final RegistryObject<MemoryModuleType<List<AgeableMob>>> POSSIBLE_MATES;
    public static final RegistryObject<MemoryModuleType<List<LivingEntity>>> HERD_MEMBERS;

    // Used to decide if we want to do the mixin for brain activation
    public static final RegistryObject<MemoryModuleType<Boolean>> DUMMY;// Boolean

    static
    {
        // Used for combat
        ATTACKTARGET = PokecubeCore.MEMORIES.register("attack_target", () -> new MemoryModuleType<>(Optional.empty()));
        HUNTTARGET = PokecubeCore.MEMORIES.register("hunt_target", () -> new MemoryModuleType<>(Optional.empty()));
        HUNTED_BY = PokecubeCore.MEMORIES.register("hunted_by", () -> new MemoryModuleType<>(Optional.empty()));
        MOVE_TARGET = PokecubeCore.MEMORIES.register("move_target", () -> new MemoryModuleType<>(Optional.empty()));
        LEAP_TARGET = PokecubeCore.MEMORIES.register("leap_target", () -> new MemoryModuleType<>(Optional.empty()));

        // Used for idle tasks
        NEST_POS = PokecubeCore.MEMORIES.register("nest_pos",
                () -> new MemoryModuleType<>(Optional.of(GlobalPos.CODEC)));
        WORK_POS = PokecubeCore.MEMORIES.register("work_pos",
                () -> new MemoryModuleType<>(Optional.of(GlobalPos.CODEC)));

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
