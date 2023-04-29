package pokecube.world;

import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import pokecube.core.PokecubeCore;
import pokecube.world.dimension.SecretBaseDimension;
import pokecube.world.gen.features.FeaturesInit;
import pokecube.world.gen.features.trees.foliage.FoliagePlacerTypes;
import pokecube.world.gen.features.trees.trunks.TrunkPlacerTypes;
import pokecube.world.gen.structures.PokecubeStructures;
import pokecube.world.gen.structures.processors.PokecubeStructureProcessors;
import thut.lib.RegHelper;

public class PokecubeWorld
{

    public static final DeferredRegister<StructurePoolElementType<?>> POOL_ELEMENT_TYPES;
    public static final DeferredRegister<StructureProcessorType<?>> STRUCTURE_PROCESSOR_TYPES;
    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES;
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES;
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES;
    public static final DeferredRegister<FoliagePlacerType<?>> FOLIAGE_PLACERS;
    public static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACERS;

    static
    {
        POOL_ELEMENT_TYPES = DeferredRegister.create(RegHelper.STRUCTURE_POOL_ELEMENT_REGISTRY, PokecubeCore.MODID);
        STRUCTURE_PROCESSOR_TYPES = DeferredRegister.create(RegHelper.STRUCTURE_PROCESSOR_REGISTRY, PokecubeCore.MODID);
        CONFIGURED_FEATURES = DeferredRegister.create(RegHelper.CONFIGURED_FEATURE_REGISTRY, PokecubeCore.MODID);
        STRUCTURE_TYPES = DeferredRegister.create(RegHelper.STRUCTURE_TYPE_REGISTRY, PokecubeCore.MODID);
        PLACED_FEATURES = DeferredRegister.create(RegHelper.PLACED_FEATURE_REGISTRY, PokecubeCore.MODID);
        FOLIAGE_PLACERS = DeferredRegister.create(RegHelper.FOLIAGE_PLACER_TYPE_REGISTRY, PokecubeCore.MODID);
        TRUNK_PLACERS = DeferredRegister.create(RegHelper.TRUNK_PLACER_TYPE_REGISTRY, PokecubeCore.MODID);
    }

    public static void init(final IEventBus bus)
    {
        POOL_ELEMENT_TYPES.register(bus);
        STRUCTURE_PROCESSOR_TYPES.register(bus);
        CONFIGURED_FEATURES.register(bus);
        STRUCTURE_TYPES.register(bus);
        PLACED_FEATURES.register(bus);
        FOLIAGE_PLACERS.register(bus);
        TRUNK_PLACERS.register(bus);

        PokecubeStructureProcessors.init(bus);
        SecretBaseDimension.onConstruct(bus);
        FeaturesInit.init(bus);
        PokecubeStructures.init(bus);
        FoliagePlacerTypes.init();
        TrunkPlacerTypes.init();

        WorldgenTags.initTags();
    }
}
