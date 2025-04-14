package pokecube.core.handlers.playerdata.advancements.triggers;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import pokecube.api.data.PokedexEntry;
import pokecube.api.utils.PokeType;
import pokecube.core.PokecubeCore;

import java.lang.reflect.Field;
import java.util.function.Consumer;

public class Triggers
{
    public static final DeferredHolder<CriterionTrigger<?>,CatchPokemobTrigger> CATCHPOKEMOB;
    public static final DeferredHolder<CriterionTrigger<?>,KillPokemobTrigger> KILLPOKEMOB;
    public static final DeferredHolder<CriterionTrigger<?>,HatchPokemobTrigger> HATCHPOKEMOB;
    public static final DeferredHolder<CriterionTrigger<?>,FirstPokemobTrigger> FIRSTPOKEMOB;
    public static final DeferredHolder<CriterionTrigger<?>,EvolvePokemobTrigger> EVOLVEPOKEMOB;
    public static final DeferredHolder<CriterionTrigger<?>,InspectPokemobTrigger> INSPECTPOKEMOB;
    public static final DeferredHolder<CriterionTrigger<?>,MegaEvolvePokemobTrigger> MEGAEVOLVEPOKEMOB;
    public static final DeferredHolder<CriterionTrigger<?>,BreedPokemobTrigger> BREEDPOKEMOB;
    public static final DeferredHolder<CriterionTrigger<?>,UseMoveTrigger> USEMOVE;

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

        FIRSTPOKEMOB = REGISTER.register(FirstPokemobTrigger.ID.getPath(), FirstPokemobTrigger::new);
        CATCHPOKEMOB = REGISTER.register(CatchPokemobTrigger.ID.getPath(), CatchPokemobTrigger::new);
        KILLPOKEMOB = REGISTER.register(KillPokemobTrigger.ID.getPath(), KillPokemobTrigger::new);
        HATCHPOKEMOB = REGISTER.register(HatchPokemobTrigger.ID.getPath(), HatchPokemobTrigger::new);
        EVOLVEPOKEMOB = REGISTER.register(EvolvePokemobTrigger.ID.getPath(), EvolvePokemobTrigger::new);
        INSPECTPOKEMOB = REGISTER.register(InspectPokemobTrigger.ID.getPath(), InspectPokemobTrigger::new);
        MEGAEVOLVEPOKEMOB = REGISTER.register(MegaEvolvePokemobTrigger.ID.getPath(), MegaEvolvePokemobTrigger::new);
        BREEDPOKEMOB = REGISTER.register(BreedPokemobTrigger.ID.getPath(), BreedPokemobTrigger::new);
        USEMOVE = REGISTER.register(UseMoveTrigger.ID.getPath(), UseMoveTrigger::new);
    }

    public static void init()
    {}
}
