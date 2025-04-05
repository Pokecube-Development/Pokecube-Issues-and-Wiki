package pokecube.core.handlers.playerdata.advancements.triggers;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.api.data.PokedexEntry;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;

public class Triggers
{
    public static final Supplier<CatchPokemobTrigger> CATCHPOKEMOB;
    public static final Supplier<KillPokemobTrigger> KILLPOKEMOB;
    public static final Supplier<HatchPokemobTrigger> HATCHPOKEMOB;
    public static final Supplier<FirstPokemobTrigger> FIRSTPOKEMOB;
    public static final Supplier<EvolvePokemobTrigger> EVOLVEPOKEMOB;
    public static final Supplier<InspectPokemobTrigger> INSPECTPOKEMOB;
    public static final Supplier<MegaEvolvePokemobTrigger> MEGAEVOLVEPOKEMOB;
    public static final Supplier<BreedPokemobTrigger> BREEDPOKEMOB;
    public static final Supplier<UseMoveTrigger> USEMOVE;

    public static final LootContextParam<PokedexEntry> POKEDEX_ENTRY;
    public static final LootContextParam<PokeType> POKEMOB_TYPE;
    public static final LootContextParam<PokeType> MOVE_TYPE;
    public static final LootContextParam<String> MOVE_NAME;

    public static final LootContextParam<Integer> MOVE_POWER;
    public static final LootContextParam<Float> MOVE_DAMAGE;

    public static final LootContextParam<Integer> WITH_COUNT;
    public static final LootContextParam<Boolean> LENIENT_MATCH;

    public static LootContextParamSet registerSet(ResourceLocation registryName,
            Consumer<LootContextParamSet.Builder> builderConsumer)
    {
        LootContextParamSet.Builder lootcontextparamset$builder = new LootContextParamSet.Builder();
        builderConsumer.accept(lootcontextparamset$builder);
        LootContextParamSet lootcontextparamset = lootcontextparamset$builder.build();
        LootContextParamSet lootcontextparamset1 = LootContextParamSets.REGISTRY.put(registryName, lootcontextparamset);
        if (lootcontextparamset1 != null)
        {
            throw new IllegalStateException("Loot table parameter set " + registryName + " is already registered");
        }
        else
        {
            return lootcontextparamset;
        }
    }

    public static final DeferredRegister<CriterionTrigger<?>> REGISTER;

    static
    {
        // First the context params, so they exist for the below.

        POKEDEX_ENTRY = new LootContextParam<>(ResourceLocation.fromNamespaceAndPath("pokecube", "pokedex_entry"));
        POKEMOB_TYPE = new LootContextParam<>(ResourceLocation.fromNamespaceAndPath("pokecube", "pokemob_type"));
        MOVE_TYPE = new LootContextParam<>(ResourceLocation.fromNamespaceAndPath("pokecube", "move_type"));
        MOVE_NAME = new LootContextParam<>(ResourceLocation.fromNamespaceAndPath("pokecube", "move_name"));

        MOVE_POWER = new LootContextParam<>(ResourceLocation.fromNamespaceAndPath("pokecube", "move_power"));
        MOVE_DAMAGE = new LootContextParam<>(ResourceLocation.fromNamespaceAndPath("pokecube", "move_damage"));

        WITH_COUNT = new LootContextParam<>(ResourceLocation.fromNamespaceAndPath("pokecube", "with_count"));
        LENIENT_MATCH = new LootContextParam<>(ResourceLocation.fromNamespaceAndPath("pokecube", "lenient_match"));
        
        // Then the registry itself

        REGISTER = DeferredRegister.create(BuiltInRegistries.TRIGGER_TYPES, PokecubeCore.MODID);

        // Finally the things to register
        
        CATCHPOKEMOB = REGISTER.register(CatchPokemobTrigger.ID.getPath(), n -> new CatchPokemobTrigger());
        KILLPOKEMOB = REGISTER.register(KillPokemobTrigger.ID.getPath(), n -> new KillPokemobTrigger());
        HATCHPOKEMOB = REGISTER.register(HatchPokemobTrigger.ID.getPath(), n -> new HatchPokemobTrigger());
        FIRSTPOKEMOB = REGISTER.register(FirstPokemobTrigger.ID.getPath(), n -> new FirstPokemobTrigger());
        EVOLVEPOKEMOB = REGISTER.register(EvolvePokemobTrigger.ID.getPath(), n -> new EvolvePokemobTrigger());
        INSPECTPOKEMOB = REGISTER.register(InspectPokemobTrigger.ID.getPath(), n -> new InspectPokemobTrigger());
        MEGAEVOLVEPOKEMOB = REGISTER.register(MegaEvolvePokemobTrigger.ID.getPath(),
                n -> new MegaEvolvePokemobTrigger());
        BREEDPOKEMOB = REGISTER.register(BreedPokemobTrigger.ID.getPath(), n -> new BreedPokemobTrigger());
        USEMOVE = REGISTER.register(UseMoveTrigger.ID.getPath(), n -> new UseMoveTrigger());

    }

    public static void init()
    {}
}
