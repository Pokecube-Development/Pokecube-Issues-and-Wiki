package pokecube.legends.tileentity;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import pokecube.core.blocks.maxspot.MaxTile;
import pokecube.legends.blocks.customblocks.RaidSpawnBlock;
import pokecube.legends.blocks.customblocks.RaidSpawnBlock.State;

public class RaidSpawn extends MaxTile
{
    public static BlockEntityType<RaidSpawn> TYPE;

    private static final List<BeamSegment> empty = ImmutableList.of();

    private final List<BeamSegment> normal = Lists.newArrayList();
    private final List<BeamSegment> rare   = Lists.newArrayList();

    public RaidSpawn(final BlockPos pos, final BlockState state)
    {
        this(RaidSpawn.TYPE, pos, state);
    }

    public RaidSpawn(final BlockEntityType<?> tileEntityTypeIn, final BlockPos pos, final BlockState state)
    {
        super(tileEntityTypeIn, pos, state);
        float[] colours = { 0.83f, 0.0f, 0.0f, 1 };
        BeamSegment seg = new BeamSegment(colours);
        this.normal.add(seg);
        colours = new float[] { 0.98f, 0.74f, 0.14f, 1 };
        seg = new BeamSegment(colours);
        this.rare.add(seg);
    }

    @OnlyIn(Dist.CLIENT)
    public List<BeamSegment> getBeamSegments()
    {
        final BlockState blocks = this.level.getBlockState(this.getBlockPos());
        if (!blocks.hasProperty(RaidSpawnBlock.ACTIVE)) return RaidSpawn.empty;
        final State state = blocks.getValue(RaidSpawnBlock.ACTIVE);
        switch (state)
        {
        case EMPTY:
            return RaidSpawn.empty;
        case NORMAL:
            return this.normal;
        case RARE:
            return this.rare;
        default:
            break;
        }

        return RaidSpawn.empty;
    }

    @Override
    public AABB getRenderBoundingBox()
    {
        return IForgeBlockEntity.INFINITE_EXTENT_AABB;
    }

    public static class BeamSegment
    {
        private final float[] colors;
        private int           height;

        public BeamSegment(final float[] colorsIn)
        {
            this.colors = colorsIn;
            this.height = 1;
        }

        protected void incrementHeight()
        {
            ++this.height;
        }

        /**
         * Returns RGB (0 to 1.0) colors of this beam segment
         */
        @OnlyIn(Dist.CLIENT)
        public float[] getColors()
        {
            return this.colors;
        }

        @OnlyIn(Dist.CLIENT)
        public int getHeight()
        {
            return this.height;
        }
    }
}
