package pokecube.adventures.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.RegistryObject;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.entity.trainer.LeaderNpc;
import pokecube.adventures.entity.trainer.TrainerNpc;

public class EntityTypes
{
    private static final RegistryObject<EntityType<TrainerNpc>> TRAINER;
    private static final RegistryObject<EntityType<LeaderNpc>> LEADER;

    static
    {
        TRAINER = PokecubeAdv.ENTITIES.register("trainer",
                () -> EntityType.Builder.of(TrainerNpc::new, MobCategory.CREATURE)
                        .setCustomClientFactory((s, w) -> getTrainer().create(w)).build("trainer"));
        LEADER = PokecubeAdv.ENTITIES.register("leader",
                () -> EntityType.Builder.of(LeaderNpc::new, MobCategory.CREATURE)
                        .setCustomClientFactory((s, w) -> getLeader().create(w)).build("leader"));
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
