package pokecube.core.world.gen.template;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import pokecube.core.PokecubeCore;

public class PokecubeStructureProcessors
{
    public static RegistryObject<StructureProcessorType<?>> FILLER = PokecubeCore.RegistryEvents.STRUCTPROCTYPE
            .register("filter", () -> () -> FillerProcessor.CODEC);
    public static RegistryObject<StructureProcessorType<?>> EXTENDED = PokecubeCore.RegistryEvents.STRUCTPROCTYPE
            .register("extrule", () -> () -> FillerProcessor.CODEC);
    public static RegistryObject<StructureProcessorType<?>> NOTRULE = PokecubeCore.RegistryEvents.STRUCTPROCTYPE
            .register("notrule", () -> () -> FillerProcessor.CODEC);
    public static RegistryObject<StructureProcessorType<?>> STRUCTS = PokecubeCore.RegistryEvents.STRUCTPROCTYPE
            .register("structures", () -> () -> FillerProcessor.CODEC);
    public static RegistryObject<StructureProcessorType<?>> MARKERAIR = PokecubeCore.RegistryEvents.STRUCTPROCTYPE
            .register("marker_to_air", () -> () -> FillerProcessor.CODEC);
    public static RegistryObject<StructureProcessorType<?>> LADDERS = PokecubeCore.RegistryEvents.STRUCTPROCTYPE
            .register("ladders_to_ground", () -> () -> FillerProcessor.CODEC);

    public static void init(final IEventBus bus)
    {

    }

}
