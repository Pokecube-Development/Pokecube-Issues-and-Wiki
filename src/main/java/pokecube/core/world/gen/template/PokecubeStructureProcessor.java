package pokecube.core.world.gen.template;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.EntityInfo;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.events.StructureEvent;

public class PokecubeStructureProcessor extends StructureProcessor
{
    public static final Codec<StructureProcessor> CODEC;

    public static final StructureProcessor PROCESSOR = new PokecubeStructureProcessor();

    public PokecubeStructureProcessor()
    {
    }

    public PokecubeStructureProcessor(final Dynamic<?> p_deserialize_1_)
    {
    }

    @Override
    public EntityInfo processEntity(final IWorldReader world, final BlockPos seedPos, final EntityInfo rawEntityInfo,
            final EntityInfo entityInfo, final PlacementSettings placementSettings, final Template template)
    {
        final StructureEvent.SpawnEntity event = new StructureEvent.SpawnEntity(entityInfo, rawEntityInfo);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getInfo();
    }

    @Override
    protected IStructureProcessorType<?> getType()
    {
        return PokecubeStructureProcessors.STRUCTS;
    }

    static
    {
        CODEC = Codec.unit(() ->
        {
            return PokecubeStructureProcessor.PROCESSOR;
        });
    }
}
