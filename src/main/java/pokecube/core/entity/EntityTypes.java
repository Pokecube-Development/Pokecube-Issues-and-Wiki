package pokecube.core.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.PokecubeCore;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.moves.animations.EntityMoveUse;

public class EntityTypes
{
    public static final RegistryObject<EntityType<EntityPokecube>> POKECUBE;
    public static final RegistryObject<EntityType<EntityPokemobEgg>> EGG;
    public static final RegistryObject<EntityType<NpcMob>> NPC;
    public static final RegistryObject<EntityType<EntityMoveUse>> MOVE;

    static
    {
        POKECUBE = PokecubeCore.ENTITIES.register("pokecube",
                () -> EntityType.Builder.of(EntityPokecube::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(32).setUpdateInterval(1).noSummon().fireImmune().sized(0.25f, 0.25f)
                        .build("pokecube"));
        EGG = PokecubeCore.ENTITIES.register("egg",
                () -> EntityType.Builder.of(EntityPokemobEgg::new, MobCategory.CREATURE).noSummon().fireImmune()
                        .sized(0.35f, 0.35f).build("egg"));
        NPC = PokecubeCore.ENTITIES.register("npc", () -> EntityType.Builder.of(NpcMob::new, MobCategory.CREATURE)
                .setCustomClientFactory((s, w) -> getNpc().create(w)).build("pokecube:npc"));
        MOVE = PokecubeCore.ENTITIES.register("move_use",
                () -> EntityType.Builder.of(EntityMoveUse::new, MobCategory.MISC).noSummon().fireImmune()
                        .setTrackingRange(64).setShouldReceiveVelocityUpdates(true).setUpdateInterval(1)
                        .sized(0.5f, 0.5f).setCustomClientFactory((spawnEntity, world) ->
                        {
                            return getMove().create(world);
                        }).build("move_use"));
    }

    public static void init()
    {}

    public static EntityType<NpcMob> getNpc()
    {
        return NPC.get();
    }

    public static EntityType<EntityMoveUse> getMove()
    {
        return MOVE.get();
    }

    public static EntityType<EntityPokemobEgg> getEgg()
    {
        return EGG.get();
    }

    public static EntityType<EntityPokecube> getPokecube()
    {
        return POKECUBE.get();
    }
}
