package pokecube.world.gen.structures.processors;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import pokecube.world.PokecubeWorld;

public class PokecubeStructureProcessors
{
    public static final RegistryObject<StructureProcessorType<?>> FILLER;
    public static final RegistryObject<StructureProcessorType<ExtendedRuleProcessor>> EXTENDED;
    public static final RegistryObject<StructureProcessorType<NotRuleProcessor>> NOTRULE;
    public static final RegistryObject<StructureProcessorType<?>> STRUCTS;
    public static final RegistryObject<StructureProcessorType<MarkerToAirProcessor>> MARKERAIR;
    public static final RegistryObject<StructureProcessorType<?>> LADDERS;

    static
    {
        FILLER = PokecubeWorld.STRUCTURE_PROCESSOR_TYPES.register("filter", () -> () -> FillerProcessor.CODEC);
        EXTENDED = PokecubeWorld.STRUCTURE_PROCESSOR_TYPES.register("extrule", () -> () -> ExtendedRuleProcessor.CODEC);
        NOTRULE = PokecubeWorld.STRUCTURE_PROCESSOR_TYPES.register("notrule", () -> () -> NotRuleProcessor.CODEC);
        STRUCTS = PokecubeWorld.STRUCTURE_PROCESSOR_TYPES.register("structures",
                () -> () -> PokecubeStructureProcessor.CODEC);
        MARKERAIR = PokecubeWorld.STRUCTURE_PROCESSOR_TYPES.register("marker_to_air",
                () -> () -> MarkerToAirProcessor.CODEC);
        LADDERS = PokecubeWorld.STRUCTURE_PROCESSOR_TYPES.register("ladders_to_ground",
                () -> () -> LadderToGround.CODEC);
    }

    public static void init(final IEventBus bus)
    {

    }

}
