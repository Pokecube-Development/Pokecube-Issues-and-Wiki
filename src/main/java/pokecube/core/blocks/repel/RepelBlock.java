package pokecube.core.blocks.repel;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import pokecube.core.blocks.InteractableHorizontalBlock;
import pokecube.core.init.Sounds;

public class RepelBlock extends InteractableHorizontalBlock implements EntityBlock
{
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public RepelBlock(final Properties properties)
    {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, POWERED);
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state)
    {
        return new RepelTile(pos, state);
    }

    @Override
    public void neighborChanged(final BlockState state, final Level world, final BlockPos pos, final Block block,
            final BlockPos fromPos, final boolean isMoving)
    {
        final int power = world.getBestNeighborSignal(pos);
        final BlockEntity tile = world.getBlockEntity(pos);

        if (!(tile instanceof RepelTile repel)) return;
        if (power != 0)
        {
            repel.enabled = false;
            repel.removeForbiddenSpawningCoord();
            world.scheduleTick(pos, this, 4);
            world.setBlock(pos, state.setValue(POWERED, Boolean.FALSE), 3);
        }
        else
        {
            repel.enabled = true;
            repel.addForbiddenSpawningCoord();
            world.setBlock(pos, state.setValue(POWERED, Boolean.TRUE), 3);
        }
    }

    @Override
    public void onBlockStateChange(LevelReader worldReader, BlockPos pos, BlockState oldState, BlockState newState)
    {
        super.onBlockStateChange(worldReader, pos, oldState, newState);

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        if (newState.getValue(POWERED))
        {
            if (worldReader instanceof Level world)
            {
                world.playSound(null, pos, Sounds.REPEL_SPRAYS.get(), SoundSource.BLOCKS, 1.0F, 1.0F);

                if (worldReader.getBlockState(pos).getValue(FACING) == Direction.NORTH)
                    addParticles(world, x + 0.5, y + 1.1, z - 0.1, 0.15D, 0.15D, -0.5D);
                else if (worldReader.getBlockState(pos).getValue(FACING) == Direction.SOUTH)
                    addParticles(world, x + 0.5, y + 1.1, z + 1.1, 0.15D, 0.15D, 0.5D);
                else if (worldReader.getBlockState(pos).getValue(FACING) == Direction.EAST)
                    addParticles(world, x + 1.1, y + 1.1, z + 0.5, 0.5D, 0.15D, 0.15D);
                else if (worldReader.getBlockState(pos).getValue(FACING) == Direction.WEST)
                    addParticles(world, x - 0.1, y + 1.1, z + 0.5, -0.5D, 0.15D, 0.15D);
            }
        }
    }

    public void tick(BlockState state, ServerLevel world, BlockPos pos, Random random)
    {
        if (!state.getValue(POWERED) && !world.hasNeighborSignal(pos))
        {
            world.setBlock(pos, state.cycle(POWERED), 2);
        }
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack)
    {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        world.playSound(null, pos, Sounds.REPEL_SPRAYS.get(), SoundSource.BLOCKS, 1.0F, 1.0F);

        if (world.getBlockState(pos).getValue(FACING) == Direction.NORTH)
            addParticles(world, x + 0.5, y + 1.1, z - 0.1, 0.15D, 0.15D, -0.5D);
        else if (world.getBlockState(pos).getValue(FACING) == Direction.SOUTH)
            addParticles(world, x + 0.5, y + 1.1, z + 1.1, 0.15D, 0.15D, 0.5D);
        else if (world.getBlockState(pos).getValue(FACING) == Direction.EAST)
            addParticles(world, x + 1.1, y + 1.1, z + 0.5, 0.5D, 0.15D, 0.15D);
        else if (world.getBlockState(pos).getValue(FACING) == Direction.WEST)
            addParticles(world, x - 0.1, y + 1.1, z + 0.5, -0.5D, 0.15D, 0.15D);
        super.setPlacedBy(world, pos, state, entity, stack);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        return this.defaultBlockState().setValue(POWERED, !context.getLevel().hasNeighborSignal(context.getClickedPos()))
                .setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
    }

    public static void addParticles(Level world, double x, double y, double z, double motionX, double motionY, double motionZ)
    {
        Random random = world.getRandom();

        for (int i = 0; i < 50; ++i) {
            world.addParticle(ParticleTypes.CLOUD, x, y, z,
                    (random.nextDouble() - 0.0D) * motionX, -random.nextDouble() * motionY,
                    (random.nextDouble() - 0.0D) * motionZ);
        }
    }
}
