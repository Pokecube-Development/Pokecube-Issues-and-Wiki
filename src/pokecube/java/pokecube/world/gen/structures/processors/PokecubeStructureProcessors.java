package pokecube.world.gen.structures.processors;

import java.util.function.Supplier;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.bus.api.IEventBus;
import pokecube.world.PokecubeWorld;

public class PokecubeStructureProcessors
{
    public static final Supplier<StructureProcessorType<?>> FILLER;
    public static final Supplier<StructureProcessorType<ExtendedRuleProcessor>> EXTENDED;
    public static final Supplier<StructureProcessorType<NotRuleProcessor>> NOTRULE;
    public static final Supplier<StructureProcessorType<?>> STRUCTS;
    public static final Supplier<StructureProcessorType<MarkerToAirProcessor>> MARKERAIR;
    public static final Supplier<StructureProcessorType<?>> LADDERS;
    public static final Supplier<StructureProcessorType<NoWaterlogProcessor>> NOWATERLOG;
    public static final Supplier<StructureProcessorType<?>> LOGSDOWN;

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
        NOWATERLOG = PokecubeWorld.STRUCTURE_PROCESSOR_TYPES.register("no_water_logging",
                () -> () -> NoWaterlogProcessor.CODEC);
        LOGSDOWN = PokecubeWorld.STRUCTURE_PROCESSOR_TYPES.register("logs_to_ground", () -> () -> LogsToGround.CODEC);
    }

    public static void init(final IEventBus bus)
    {

    }

}
