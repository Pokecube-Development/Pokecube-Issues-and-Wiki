package pokecube.legends.blocks.normalblocks;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraftforge.common.IPlantable;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.ItemInit;

public class GrassJungleBlock extends GrassBlock implements BonemealableBlock
{
    public GrassJungleBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SnowyDirtBlock.SNOWY, false));
    }

    @Override
    public boolean canSustainPlant(final BlockState state, final BlockGetter world, final BlockPos pos,
            final Direction direction, final IPlantable plantable)
    {
        return true;
    }

    @Override
    public void randomTick(final BlockState state, final ServerLevel world, final BlockPos pos, final Random random)
    {
        if (!GrassJungleBlock.canBeGrass(state, world, pos))
        {
            if (!world.isAreaLoaded(pos, 3)) return;

            world.setBlockAndUpdate(pos, BlockInit.JUNGLE_DIRT.get().defaultBlockState());
        }
        else if (world.getMaxLocalRawBrightness(pos.above()) >= 9)
        {
            final BlockState blockstate = this.defaultBlockState();

            for (int i = 0; i < 4; ++i)
            {
                final BlockPos blockpos = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3)
                        - 1);
                if (world.getBlockState(blockpos).is(BlockInit.JUNGLE_DIRT.get()) && GrassJungleBlock.canPropagate(
                        blockstate, world, blockpos)) world.setBlockAndUpdate(blockpos, blockstate.setValue(
                                SnowyDirtBlock.SNOWY, world.getBlockState(blockpos.above()).is(Blocks.SNOW)));
            }
        }
    }

    public static boolean canPropagate(final BlockState state, final LevelReader world, final BlockPos pos)
    {
        final BlockPos blockpos = pos.above();
        return GrassJungleBlock.canBeGrass(state, world, pos) && !world.getFluidState(blockpos).is(FluidTags.WATER);
    }

    public static boolean canBeGrass(final BlockState state, final LevelReader world, final BlockPos pos)
    {
        final BlockPos blockpos = pos.above();
        final BlockState blockstate = world.getBlockState(blockpos);
        if (blockstate.is(Blocks.SNOW) && blockstate.getValue(SnowLayerBlock.LAYERS) >= 1) return true;
        else if (blockstate.getFluidState().getAmount() == 8) return false;
        else
        {
            final int i = LayerLightEngine.getLightBlockInto(world, state, pos, blockstate, blockpos, Direction.UP,
                    blockstate.getLightBlock(world, blockpos));
            return i < world.getMaxLightLevel();
        }
    }

    @Override
    public void stepOn(final Level world, final BlockPos pos, final BlockState state, final Entity entity)
    {
        super.stepOn(world, pos, state, entity);
        GrassJungleBlock.executeProcedure(entity);
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
            ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1));
    }
}
