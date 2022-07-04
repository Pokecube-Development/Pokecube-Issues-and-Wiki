package pokecube.world.gen.structures.processors;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureEntityInfo;
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
    @Nullable
    public StructureTemplate.StructureBlockInfo process(final LevelReader world, final BlockPos pos1, final BlockPos pos2,
            final StructureTemplate.StructureBlockInfo rawInfo, final StructureTemplate.StructureBlockInfo modInfo, final StructurePlaceSettings settings,
            @Nullable final StructureTemplate template)
    {
        return modInfo;
    }

    @Override
    public StructureEntityInfo processEntity(final LevelReader world, final BlockPos seedPos, final StructureEntityInfo rawEntityInfo,
            final StructureEntityInfo entityInfo, final StructurePlaceSettings placementSettings, final StructureTemplate template)
    {
        final StructureEvent.SpawnEntity event = new StructureEvent.SpawnEntity(entityInfo, rawEntityInfo);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getInfo();
    }

    @Override
    protected StructureProcessorType<?> getType()
    {
        return PokecubeStructureProcessors.STRUCTS.get();
    }

    static
    {
        CODEC = Codec.unit(() ->
        {
            return PokecubeStructureProcessor.PROCESSOR;
        });
    }
}
