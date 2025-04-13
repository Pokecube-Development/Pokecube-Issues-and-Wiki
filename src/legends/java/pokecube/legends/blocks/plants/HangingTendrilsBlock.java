package pokecube.legends.blocks.plants;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.legends.init.PlantsInit;

import java.util.function.ToIntFunction;

public class HangingTendrilsBlock extends GrowingPlantHeadBlock implements BonemealableBlock
{
    public static final MapCodec<HangingTendrilsBlock> CODEC = simpleCodec(HangingTendrilsBlock::new);

    @Override
    protected MapCodec<? extends GrowingPlantHeadBlock> codec()
    {
        return CODEC;
    }

    public static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
    public static final BooleanProperty EYES = BooleanProperty.create("eyes");

    public HangingTendrilsBlock(BlockBehaviour.Properties properties)
    {
        super(properties, Direction.DOWN, SHAPE, false, 0.1D);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0).setValue(EYES, Boolean.FALSE));
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(EYES);
    }

    @Override
    public BlockState updateBodyAfterConvertedFromHead(BlockState state, BlockState state1)
    {
        return state1.setValue(EYES, state.getValue(EYES));
    }

    @Override
    public BlockState getGrowIntoState(BlockState state, RandomSource random)
    {
        return super.getGrowIntoState(state, random).setValue(EYES, random.nextFloat() < 0.11F);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult)
    {
        if (state.getValue(EYES))
        {
            float f = Mth.randomBetween(level.random, 0.8F, 1.2F);
            level.playSound((Player) null, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, f);
            level.setBlock(pos, state.setValue(EYES, Boolean.FALSE), 2);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        else
        {
            return InteractionResult.PASS;
        }
    }

    @Override
    public GrowingPlantHeadBlock getHeadBlock()
    {
        return this;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state)
    {
        return !state.getValue(EYES);
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state)
    {
        world.setBlock(pos, state.setValue(EYES, Boolean.TRUE), 2);
    }

    @Override
    public int getBlocksToGrowWhenBonemealed(RandomSource random)
    {
        return 1;
    }

    @Override
    public boolean canGrowInto(BlockState state)
    {
        return state.isAir();
    }

    @Override
    public Block getBodyBlock()
    {
        return PlantsInit.HANGING_TENDRILS_PLANT.get();
    }

    public static ToIntFunction<BlockState> emission(int lightLevel)
    {
        return (state) -> state.hasProperty(EYES) && state.getValue(EYES) ? lightLevel : 0;
    }
}
