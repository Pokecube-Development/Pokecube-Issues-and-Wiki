package pokecube.legends.blocks;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class FaceBlockBase extends BlockBase
{

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public FaceBlockBase(final String name, final Material material, final MaterialColor color, final float hardness,
            final float resistance, final SoundType sound, final boolean hasDrop)
    {
        super(name, material, color, hardness, resistance, sound, hasDrop);
        this.registerDefaultState(this.stateDefinition.any().setValue(FaceBlockBase.FACING, Direction.NORTH));
    }

    public FaceBlockBase(final Material material, final MaterialColor color, final float hardness,
            final float resistance, final SoundType sound, final boolean hasDrop)
    {
        super(material, color, hardness, resistance, sound, hasDrop);
        this.registerDefaultState(this.stateDefinition.any().setValue(FaceBlockBase.FACING, Direction.NORTH));
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
