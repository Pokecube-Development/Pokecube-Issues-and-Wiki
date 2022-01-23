package pokecube.legends.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SandBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;

public class FallingDirtBlockBase extends SandBlock
{
    private final int dustColor;

    public FallingDirtBlockBase(final int color, final BlockBehaviour.Properties properties)
    {
        super(color, properties);
        this.dustColor = color;
    }

    @Override
    public int getDustColor(final BlockState state, final BlockGetter block, final BlockPos pos)
    {
        return this.dustColor;
    }

    @Override
    public boolean canSustainPlant(final BlockState state, final BlockGetter block, final BlockPos pos, final Direction direction, final IPlantable plantable)
    {
        final BlockPos plantPos = new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ());
        final PlantType plantType = plantable.getPlantType(block, plantPos);

        if (plantType == PlantType.PLAINS)
            return true;
        else if (plantType == PlantType.WATER)
            return block.getBlockState(pos).getMaterial() == Material.WATER && block.getBlockState(pos) == this.defaultBlockState();
        else if (plantType == PlantType.BEACH)
            return ((block.getBlockState(pos.east()).getBlock() == Blocks.WATER || block.getBlockState(pos.east()).hasProperty(BlockStateProperties.WATERLOGGED))
                    || (block.getBlockState(pos.west()).getBlock() == Blocks.WATER || block.getBlockState(pos.west()).hasProperty(BlockStateProperties.WATERLOGGED))
                    || (block.getBlockState(pos.north()).getBlock() == Blocks.WATER || block.getBlockState(pos.north()).hasProperty(BlockStateProperties.WATERLOGGED))
                    || (block.getBlockState(pos.south()).getBlock() == Blocks.WATER || block.getBlockState(pos.south()).hasProperty(BlockStateProperties.WATERLOGGED)));
        else
            return super.canSustainPlant(state, block, pos, direction, plantable);
    }
}
