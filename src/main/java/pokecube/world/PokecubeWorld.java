package pokecube.world;

import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.core.PokecubeCore;
import pokecube.world.gen.features.FeaturesInit;
import pokecube.world.gen.structures.PokecubeStructures;
import pokecube.world.gen.structures.processors.PokecubeStructureProcessors;
import pokecube.world.gen_old.WorldgenFeatures;
import pokecube.world.gen_old.WorldgenHandler;

public class PokecubeWorld
{

    public static final DeferredRegister<StructurePoolElementType<?>> POOL_ELEMENT_TYPES;
    public static final DeferredRegister<StructureProcessorType<?>> STRUCTURE_PROCESSOR_TYPES;
    public static final DeferredRegister<StructureFeature<?>> STRUCTURE_TYPES;
    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES;
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES;

    static
    {
        POOL_ELEMENT_TYPES = DeferredRegister.create(Registry.STRUCTURE_POOL_ELEMENT_REGISTRY, PokecubeCore.MODID);
        STRUCTURE_PROCESSOR_TYPES = DeferredRegister.create(Registry.STRUCTURE_PROCESSOR_REGISTRY, PokecubeCore.MODID);
        STRUCTURE_TYPES = DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, PokecubeCore.MODID);
        CONFIGURED_FEATURES = DeferredRegister.create(Registry.CONFIGURED_FEATURE_REGISTRY, PokecubeCore.MODID);
        PLACED_FEATURES = DeferredRegister.create(Registry.PLACED_FEATURE_REGISTRY, PokecubeCore.MODID);
    }

    public static void init(final IEventBus bus)
    {
        POOL_ELEMENT_TYPES.register(bus);
        STRUCTURE_PROCESSOR_TYPES.register(bus);
        STRUCTURE_TYPES.register(bus);
        CONFIGURED_FEATURES.register(bus);
        PLACED_FEATURES.register(bus);

        PokecubeStructureProcessors.init(bus);
        WorldgenFeatures.init(bus);
        FeaturesInit.init(bus);
        PokecubeStructures.init(bus);

        new WorldgenHandler(bus);
    }
}
