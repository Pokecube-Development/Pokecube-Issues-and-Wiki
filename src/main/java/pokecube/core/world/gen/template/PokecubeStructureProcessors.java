package pokecube.core.world.gen.template;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.eventbus.api.IEventBus;

public class PokecubeStructureProcessors
{
    public static StructureProcessorType<?> FILLER = StructureProcessorType.register("pokecube:filter",
            FillerProcessor.CODEC);
    public static StructureProcessorType<?> EXTENDED = StructureProcessorType.register("pokecube:extrule",
            ExtendedRuleProcessor.CODEC);
    public static StructureProcessorType<?> NOTRULE = StructureProcessorType.register("pokecube:notrule",
            NotRuleProcessor.CODEC);
    public static StructureProcessorType<?> STRUCTS = StructureProcessorType.register("pokecube:structures",
            PokecubeStructureProcessor.CODEC);
    public static StructureProcessorType<?> MARKERAIR = StructureProcessorType.register("pokecube:marker_to_air",
            MarkerToAirProcessor.CODEC);
    public static StructureProcessorType<?> LADDERS = StructureProcessorType.register("pokecube:ladders_to_ground",
            MarkerToAirProcessor.CODEC);

    public static void init(final IEventBus bus)
    {

    }

}
