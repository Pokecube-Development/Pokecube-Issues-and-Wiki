package pokecube.core.blocks.repel;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
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
import pokecube.api.effects.EffectPacketInfo;
import pokecube.api.effects.ParticleEffects;
import pokecube.api.effects.VectorPositionSource;
import pokecube.core.blocks.InteractableHorizontalBlock;
import pokecube.core.init.Sounds;
import thut.api.maths.Vector3;

public class RepelBlock extends InteractableHorizontalBlock implements EntityBlock
{
    public static final MapCodec<RepelBlock> CODEC = simpleCodec(RepelBlock::new);

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec()
    {
        return CODEC;
    }

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
        final boolean power = world.hasNeighborSignal(pos);
        final BlockEntity tile = world.getBlockEntity(pos);

        if (!(tile instanceof RepelTile repel)) return;
        if (power == state.getValue(POWERED))
        {
            if (power)
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
                Vector3 direction = new Vector3(world.getBlockState(pos).getValue(FACING));
                addParticles(world, x + 0.5 + direction.x * 1.1, y + 1.1, z + 0.5 + direction.z * 1.1, direction.x,
                        direction.y, direction.z);
            }
        }
    }

    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random)
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

        Vector3 direction = new Vector3(world.getBlockState(pos).getValue(FACING));
        addParticles(world, x + 0.5 + direction.x * 1.1, y + 1.1, z + 0.5 + direction.z * 1.1, direction.x,
                direction.y, direction.z);
        super.setPlacedBy(world, pos, state, entity, stack);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        return this.defaultBlockState()
                .setValue(POWERED, !context.getLevel().hasNeighborSignal(context.getClickedPos()))
                .setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
    }

    public static void addParticles(Level world, double x, double y, double z, double motionX, double motionY,
            double motionZ)
    {
        if (!world.isClientSide()) return;
        var effect_key = "block.repel";
        var effectFunction = ParticleEffects.getEffect(effect_key);
        var applied = effectFunction.get();
        Vector3 here = new Vector3(x, y, z);
        Vector3 there = here.add(motionX * 10, motionY * 10, motionZ * 10);
        var source = new VectorPositionSource(here.toVec3d());
        var target = new VectorPositionSource(there.toVec3d());
        var effect = new EffectPacketInfo(applied, world, source, target, 1, 1);
        ParticleEffects.ADD_FOR_RENDER.accept(effect);
    }
}
