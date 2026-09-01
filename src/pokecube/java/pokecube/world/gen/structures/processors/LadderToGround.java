package pokecube.world.gen.structures.processors;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import thut.api.item.ItemList;

public class LadderToGround extends StructureProcessor
{
    public static final MapCodec<StructureProcessor> CODEC;

    public static final ResourceLocation LADDER = ResourceLocation.fromNamespaceAndPath("pokecube", "ladders");
    public static final ResourceLocation VINE = ResourceLocation.fromNamespaceAndPath("pokecube", "vines");

    public static final StructureProcessor PROCESSOR = new LadderToGround();

    public LadderToGround()
    {}

    @Override
    @Nullable
    public StructureTemplate.StructureBlockInfo process(final LevelReader level, final BlockPos structure,
            final BlockPos part, final StructureTemplate.StructureBlockInfo old,
            final StructureTemplate.StructureBlockInfo blockInfo, final StructurePlaceSettings settings,
            final StructureTemplate ref)
    {
        boolean isLadder = ItemList.is(LADDER, blockInfo.state());
        boolean isVine = ItemList.is(VINE, blockInfo.state());
        if (!(isLadder || isVine)) return blockInfo;
        BlockPos p1 = blockInfo.pos();
        boolean isAir = level.isEmptyBlock(p1);
        boolean isWater = level.getBlockState(p1).getBlock() == Blocks.WATER;
        if (!(isAir || isWater)) return null;
        if (isVine && isWater) return null;
        return blockInfo;
    }

    @Override
    protected StructureProcessorType<?> getType()
    {
        return PokecubeStructureProcessors.LADDERS.get();
    }

    static
    {
        CODEC = MapCodec.unit(() -> LadderToGround.PROCESSOR);
    }
}
