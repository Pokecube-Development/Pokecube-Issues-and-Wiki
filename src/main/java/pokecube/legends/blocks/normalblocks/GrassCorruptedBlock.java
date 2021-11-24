package pokecube.legends.blocks.normalblocks;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.Features;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.NyliumBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.NetherForestVegetationFeature;
import net.minecraft.world.level.levelgen.feature.TwistingVinesFeature;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.FeaturesInit;
import pokecube.legends.init.ItemInit;

public class GrassCorruptedBlock extends NyliumBlock implements BonemealableBlock
{
    public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;

    public GrassCorruptedBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(GrassCorruptedBlock.SNOWY, false));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState updateShape(final BlockState state, final Direction direction, final BlockState state1,
            final LevelAccessor world, final BlockPos pos, final BlockPos pos1)
    {
        return direction != Direction.UP ? super.updateShape(state, direction, state1, world, pos, pos1)
                : (BlockState) state.setValue(GrassCorruptedBlock.SNOWY, state1.is(Blocks.SNOW_BLOCK) || state1.is(
                        Blocks.SNOW));
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final BlockState state = context.getLevel().getBlockState(context.getClickedPos().above());
        return this.defaultBlockState().setValue(GrassCorruptedBlock.SNOWY, state.is(Blocks.SNOW_BLOCK) || state.is(
                Blocks.SNOW));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(GrassCorruptedBlock.SNOWY);
    }

    private static boolean canBeGrass(final BlockState state, final LevelReader world, final BlockPos pos)
    {
        final BlockPos blockpos = pos.above();
        final BlockState blockstate = world.getBlockState(blockpos);
        if (blockstate.is(Blocks.SNOW) && blockstate.getValue(SnowLayerBlock.LAYERS) >= 1) return true;
        else if (blockstate.getFluidState().getAmount() == 8) return false;
        else
        {
            final int light = LayerLightEngine.getLightBlockInto(world, state, pos, blockstate, blockpos, Direction.UP,
                    blockstate.getLightBlock(world, blockpos));
            return light < world.getMaxLightLevel();
        }
    }

    @Override
    public void randomTick(final BlockState state, final ServerLevel world, final BlockPos pos, final Random random)
    {
        if (!GrassCorruptedBlock.canBeGrass(state, world, pos)) world.setBlockAndUpdate(pos, BlockInit.CORRUPTED_DIRT
                .get().defaultBlockState());
    }

    @Override
    public void performBonemeal(final ServerLevel world, final Random random, final BlockPos pos,
            final BlockState state)
    {
        final BlockState blockstate = world.getBlockState(pos);
        final BlockPos blockpos = pos.above();
        if (blockstate.is(BlockInit.CORRUPTED_GRASS.get()))
        {
            NetherForestVegetationFeature.place(world, random, blockpos, FeaturesInit.Configs.TAINTED_BARRENS_CONFIG, 3, 1);
        }
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter block, BlockPos pos, Direction direction, IPlantable plantable)
    {
        final BlockPos plantPos = new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ());
        final PlantType plantType = plantable.getPlantType(block, plantPos);
        if (plantType == PlantType.PLAINS)
        {
            return true;
        } else if (plantType == PlantType.WATER)
        {
            return block.getBlockState(pos).getMaterial() == Material.WATER && block.getBlockState(pos) == defaultBlockState();
        } else if (plantType == PlantType.BEACH)
        {
            return ((block.getBlockState(pos.east()).getBlock() == Blocks.WATER || block.getBlockState(pos.east()).hasProperty(BlockStateProperties.WATERLOGGED))
                    || (block.getBlockState(pos.west()).getBlock() == Blocks.WATER || block.getBlockState(pos.west()).hasProperty(BlockStateProperties.WATERLOGGED))
                    || (block.getBlockState(pos.north()).getBlock() == Blocks.WATER || block.getBlockState(pos.north()).hasProperty(BlockStateProperties.WATERLOGGED))
                    || (block.getBlockState(pos.south()).getBlock() == Blocks.WATER || block.getBlockState(pos.south()).hasProperty(BlockStateProperties.WATERLOGGED)));
        } else
        {
            return false;
        }
    }

    @Override
    public void stepOn(final Level world, final BlockPos pos, final BlockState state, final Entity entity)
    {
        super.stepOn(world, pos, state, entity);

        GrassCorruptedBlock.executeProcedure(entity);
    }

    public static void executeProcedure(final Entity entity)
    {
        if (entity instanceof ServerPlayer) if (((Player) entity).getInventory().armor.get(3)
                .getItem() != new ItemStack(ItemInit.ULTRA_HELMET.get(), 1).getItem() || ((Player) entity)
                        .getInventory().armor.get(2).getItem() != new ItemStack(ItemInit.ULTRA_CHESTPLATE.get(), 1)
                                .getItem() || ((Player) entity).getInventory().armor.get(1).getItem() != new ItemStack(
                                        ItemInit.ULTRA_LEGGINGS.get(), 1).getItem() || ((Player) entity)
                                                .getInventory().armor.get(0).getItem() != new ItemStack(
                                                        ItemInit.ULTRA_BOOTS.get(), 1).getItem())
            ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 120, 1));
    }
}
