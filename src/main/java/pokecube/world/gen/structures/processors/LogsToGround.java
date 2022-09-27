package pokecube.world.gen.structures.processors;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class LogsToGround extends StructureProcessor
{
    public static final Codec<StructureProcessor> CODEC;

    public static final ResourceLocation LADDER = new ResourceLocation("pokecube", "ladders");
    public static final ResourceLocation VINE = new ResourceLocation("pokecube", "vines");

    public static final StructureProcessor PROCESSOR = new LogsToGround();

    public LogsToGround()
    {}

    @Override
    @Nullable
    public StructureTemplate.StructureBlockInfo process(final LevelReader level, final BlockPos structure,
            final BlockPos part, final StructureTemplate.StructureBlockInfo old,
            final StructureTemplate.StructureBlockInfo blockInfo, final StructurePlaceSettings settings,
            final StructureTemplate ref)
    {
        BlockPos p1 = old.pos;
        // This means we are at the base of the structure.
        down:
        if (p1.getY() == 0 && blockInfo.state.hasProperty(RotatedPillarBlock.AXIS)
                && blockInfo.state.getValue(RotatedPillarBlock.AXIS) == Axis.Y
                && level instanceof WorldGenRegion region)
        {
            int y = blockInfo.pos.getY() - 1;
            int y_max = level.getHeight(Types.OCEAN_FLOOR_WG, blockInfo.pos.getX(), blockInfo.pos.getZ());
            if (y_max == level.getMinBuildHeight()) break down;
            boolean try_place = y >= y_max;
            while (try_place)
            {
                BlockPos test = blockInfo.pos.atY(y);
                BlockState state = level.getBlockState(test);
                try_place = state.getMaterial().isReplaceable() || state.getMaterial().isLiquid();
                try_place &= !level.isOutsideBuildHeight(test);
                if (try_place) region.setBlock(test, blockInfo.state, 2);
                y--;
                try_place &= y >= y_max;
            }
        }
        return blockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType()
    {
        return PokecubeStructureProcessors.LOGSDOWN.get();
    }

    static
    {
        CODEC = Codec.unit(() -> {
            return LogsToGround.PROCESSOR;
        });
    }
}
