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
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import pokecube.legends.init.BlockInit;
import pokecube.legends.init.ItemInit;
import pokecube.legends.init.PlantsInit;

public class AshLayerBlock extends SnowLayerBlock
{
    public AshLayerBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, Integer.valueOf(1)));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
       BlockState state1 = world.getBlockState(pos.below());
       if (!state1.is(Blocks.BARRIER))
       {
          if (!state1.is(Blocks.HONEY_BLOCK))
          {
             return Block.isFaceFull(state1.getCollisionShape(world, pos.below()), Direction.UP)
                     || state1.is(this) && state1.getValue(LAYERS) == 8;
          } else {
             return true;
          }
       } else {
          return false;
       }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, Random random)
    {}
}
