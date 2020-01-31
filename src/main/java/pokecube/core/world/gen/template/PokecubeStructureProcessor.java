package pokecube.core.world.gen.template;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraft.world.gen.feature.template.Template.EntityInfo;
import net.minecraftforge.common.MinecraftForge;
import pokecube.core.events.StructureEvent;

public class PokecubeStructureProcessor extends StructureProcessor
{
    public static IStructureProcessorType  TYPE;
    public static final StructureProcessor PROCESSOR = new PokecubeStructureProcessor();

    public PokecubeStructureProcessor()
    {
    }

    public PokecubeStructureProcessor(final Dynamic<?> p_deserialize_1_)
    {
    }

    @Override
    public BlockInfo process(final IWorldReader world, final BlockPos pos, final BlockInfo rawInfo,
            final BlockInfo info, final PlacementSettings settings, final Template template)
    {
        return super.process(world, pos, rawInfo, info, settings, template);
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
    protected IStructureProcessorType getType()
    {
        return PokecubeStructureProcessor.TYPE;
    }

    @Override
    protected <T> Dynamic<T> serialize0(final DynamicOps<T> ops)
    {
        return new Dynamic<>(ops, ops.emptyMap());
    }

}
