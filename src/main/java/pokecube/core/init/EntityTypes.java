package pokecube.core.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraftforge.registries.RegistryObject;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.Pokedex;
import pokecube.api.data.PokedexEntry;
import pokecube.api.events.init.InitDatabase;
import pokecube.api.events.init.RegisterPokemobsEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.entity.boats.GenericBoat;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.pokecubes.EntityPokecube;
import pokecube.core.entity.pokemobs.EntityPokemob;
import pokecube.core.entity.pokemobs.PokemobType;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.moves.damage.EntityMoveUse;

public class EntityTypes
{
    public static final RegistryObject<EntityType<GenericBoat>> BOAT;
    public static final RegistryObject<EntityType<EntityPokemobEgg>> EGG;
    public static final RegistryObject<EntityType<EntityMoveUse>> MOVE;
    public static final RegistryObject<EntityType<NpcMob>> NPC;
    public static final RegistryObject<EntityType<EntityPokecube>> POKECUBE;

    static
    {
        BOAT = PokecubeCore.ENTITIES.register("boat",
                () -> EntityType.Builder.<GenericBoat>of(GenericBoat::new, MobCategory.MISC).sized(1.375F, 0.5625F)
                        .clientTrackingRange(10).build("boat"));
        EGG = PokecubeCore.ENTITIES.register("egg",
                () -> EntityType.Builder.of(EntityPokemobEgg::new, MobCategory.CREATURE).noSummon().fireImmune()
                        .sized(0.35f, 0.35f).build("egg"));
        MOVE = PokecubeCore.ENTITIES.register("move_use",
                () -> EntityType.Builder.of(EntityMoveUse::new, MobCategory.MISC).noSummon().fireImmune()
                        .setTrackingRange(64).setShouldReceiveVelocityUpdates(true).setUpdateInterval(1)
                        .sized(0.5f, 0.5f).setCustomClientFactory((spawnEntity, world) ->
                        {
                            return getMove().create(world);
                        }).build("move_use"));
        NPC = PokecubeCore.ENTITIES.register("npc", () -> EntityType.Builder.of(NpcMob::new, MobCategory.CREATURE)
                .setCustomClientFactory((s, w) -> getNpc().create(w)).build("pokecube:npc"));
        POKECUBE = PokecubeCore.ENTITIES.register("pokecube",
                () -> EntityType.Builder.of(EntityPokecube::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(32).setUpdateInterval(1).noSummon().fireImmune().sized(0.25f, 0.25f)
                        .build("pokecube"));
    }

    public static void init()
    {}

    private static PokemobType<TamableAnimal> makePokemobEntityType(PokedexEntry entry)
    {
        final PokemobType<TamableAnimal> type = new PokemobType<>(EntityPokemob::new, entry);
        PokecubeCore.typeMap.put(type, entry);
        return type;
    }

    public static void registerPokemobs()
    {
        Database.init();
        PokecubeAPI.POKEMOB_BUS.post(new RegisterPokemobsEvent.Pre());
        PokecubeAPI.POKEMOB_BUS.post(new RegisterPokemobsEvent.Register());
        PokedexEntryLoader.postInit();
        for (final PokedexEntry entry : Database.getSortedFormes())
        {
            if (entry.dummy) continue;
            if (!entry.stock) continue;
            Pokedex.getInstance().registerPokemon(entry);
            if (entry.generated) continue;
            try
            {
                PokecubeCore.ENTITIES.register(entry.getTrimmedName(), () -> makePokemobEntityType(entry));
            }
            catch (final Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        PokecubeAPI.POKEMOB_BUS.post(new RegisterPokemobsEvent.Post());
        Database.postInit();
        PokecubeAPI.POKEMOB_BUS.post(new InitDatabase.Post());
    }

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

    public static EntityType<GenericBoat> getBoat()
    {
        return BOAT.get();
    }

    public static EntityType<EntityPokecube> getPokecube()
    {
        return POKECUBE.get();
    }
}
