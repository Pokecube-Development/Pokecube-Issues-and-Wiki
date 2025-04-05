package pokecube.adventures.init;

import java.util.function.Supplier;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.entity.trainer.LeaderNpc;
import pokecube.adventures.entity.trainer.TrainerNpc;

public class EntityTypes
{
    private static final Supplier<EntityType<TrainerNpc>> TRAINER;
    private static final Supplier<EntityType<LeaderNpc>> LEADER;

    static
    {
        TRAINER = PokecubeAdv.ENTITIES.register("trainer",
                () -> EntityType.Builder.of(TrainerNpc::new, MobCategory.CREATURE).build("trainer"));
        LEADER = PokecubeAdv.ENTITIES.register("leader",
                () -> EntityType.Builder.of(LeaderNpc::new, MobCategory.CREATURE).build("leader"));
    }

    public static final EntityType<TrainerNpc> getTrainer()
    {
        return TRAINER.get();
    }

    public static final EntityType<LeaderNpc> getLeader()
    {
        return LEADER.get();
    }

    public static void init()
    {}
}
