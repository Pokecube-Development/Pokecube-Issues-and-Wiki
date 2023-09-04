package pokecube.legends.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class FaceBlockBase extends BlockBase
{

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public FaceBlockBase(final String name, final MapColor color, final Direction defaultDirection, final SoundType sound, final NoteBlockInstrument instrument,
                         final boolean requiresCorrectToolForDrops, final float destroyTime, final float blastResistance)
    {
        super(BlockBehaviour.Properties.of().strength(destroyTime, blastResistance).mapColor(color).sound(sound).instrument(instrument).requiresCorrectToolForDrops());
        this.registerDefaultState(this.stateDefinition.any().setValue(FaceBlockBase.FACING, defaultDirection));
        this.hasRequiredCorrectToolForDrops(requiresCorrectToolForDrops);
        this.hasTextInfo = true;
        this.infoname = name;
    }

    public FaceBlockBase(final MapColor color, final Direction defaultDirection, final SoundType sound, final NoteBlockInstrument instrument,
                         final boolean requiresCorrectToolForDrops, final float destroyTime, final float blastResistance)
    {
        super(BlockBehaviour.Properties.of().strength(destroyTime, blastResistance).mapColor(color).sound(sound).instrument(instrument).requiresCorrectToolForDrops());
        this.registerDefaultState(this.stateDefinition.any().setValue(FaceBlockBase.FACING, defaultDirection));
        this.hasRequiredCorrectToolForDrops(requiresCorrectToolForDrops);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FaceBlockBase.FACING);
    }

    @Override
    public BlockState rotate(final BlockState state, final Rotation rot)
    {
        return state.setValue(FaceBlockBase.FACING, rot.rotate(state.getValue(FaceBlockBase.FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(final BlockState state, final Mirror mirrorIn)
    {
        return state.rotate(mirrorIn.getRotation(state.getValue(FaceBlockBase.FACING)));
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        return this.defaultBlockState().setValue(FaceBlockBase.FACING, context.getHorizontalDirection().getOpposite());
    }
}
