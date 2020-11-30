package pokecube.core.world.gen.template;

import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraftforge.eventbus.api.IEventBus;

public class PokecubeStructureProcessors
{
    public static IStructureProcessorType<?> FILTER   = IStructureProcessorType.register("pokecube:filter",
            FillerProcessor.CODEC);
    public static IStructureProcessorType<?> EXTENDED = IStructureProcessorType.register("pokecube:extrule",
            ExtendedRuleProcessor.CODEC);
    public static IStructureProcessorType<?> NOTRULE  = IStructureProcessorType.register("pokecube:notrule",
            NotRuleProcessor.CODEC);
    public static IStructureProcessorType<?> STRUCTS  = IStructureProcessorType.register("pokecube:structures",
            PokecubeStructureProcessor.CODEC);

    public static void init(final IEventBus bus)
    {

    }

}
