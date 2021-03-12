package pokecube.legends.tileentity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeTileEntity;
import pokecube.core.blocks.maxspot.MaxTile;
import pokecube.legends.blocks.customblocks.RaidSpawnBlock;
import pokecube.legends.blocks.customblocks.RaidSpawnBlock.State;

import java.util.List;

public class RaidSpawn extends MaxTile
{
    public static TileEntityType<RaidSpawn> TYPE;

    private static final List<BeamSegment> empty = ImmutableList.of();

    private final List<BeamSegment> normal = Lists.newArrayList();
    private final List<BeamSegment> rare   = Lists.newArrayList();

    public RaidSpawn()
    {
        super(RaidSpawn.TYPE);
        float[] colours = { 0.83f, 0.0f, 0.0f, 1 };
        BeamSegment seg = new BeamSegment(colours);
        this.normal.add(seg);
        colours = new float[] { 0.98f, 0.74f, 0.14f, 1 };
        seg = new BeamSegment(colours);
        this.rare.add(seg);
    }

    public RaidSpawn(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
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
    public AxisAlignedBB getRenderBoundingBox()
    {
        return IForgeTileEntity.INFINITE_EXTENT_AABB;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public double getViewDistance()
    {
        return 65536.0D;
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
