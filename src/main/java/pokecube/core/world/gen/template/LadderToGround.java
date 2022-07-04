package pokecube.core.world.gen.template;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import thut.api.item.ItemList;

public class LadderToGround extends StructureProcessor
{
    public static final Codec<StructureProcessor> CODEC;

    public static final ResourceLocation LADDER = new ResourceLocation("pokecube", "ladders");

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
        boolean isLadder = ItemList.is(LADDER, blockInfo.state);
        if (!isLadder) return blockInfo;
        BlockPos p1 = old.pos.offset(structure);
        return level.isEmptyBlock(p1) ? blockInfo : null;
    }

    @Override
    protected StructureProcessorType<?> getType()
    {
        return PokecubeStructureProcessors.LADDERS.get();
    }

    static
    {
        CODEC = Codec.unit(() -> {
            return LadderToGround.PROCESSOR;
        });
    }
}
