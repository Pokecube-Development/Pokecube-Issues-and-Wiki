package pokecube.legends.worldgen.utils;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import pokecube.legends.Reference;
import pokecube.legends.init.BlockInit;

public class AquamarineUtils
{
     // Block Tag
     public static final Tag.Named<Block> AQUAMARINE_REPLACEABLE = BlockTags.createOptional(new ResourceLocation(Reference.ID, "aquamarine_replaceable_blocks"));
     
     public static double getAquamarineHeight(double i, double j, double k, double l)
     {
        if (i < l)
        {
           i = l;
        }
        double d0 = 0.384D;
        double d1 = i / j * 0.384D;
        double d2 = 0.75D * Math.pow(d1, 1.3333333333333333D);
        double d3 = Math.pow(d1, 0.6666666666666666D);
        double d4 = 0.3333333333333333D * Math.log(d1);
        double d5 = k * (d2 - d3 - d4);
        d5 = Math.max(d5, 0.0D);
        return d5 / 0.384D * j;
     }

     public static boolean isCircleMostlyEmbeddedInStone(WorldGenLevel world, BlockPos pos, int x)
     {
        if (isEmptyOrWaterOrLava(world, pos))
        {
           return false;
        } else
        {
           float f = 6.0F;
           float f1 = 6.0F / (float)x;

           for(float f2 = 0.0F; f2 < ((float)Math.PI * 2F); f2 += f1)
           {
              int i = (int)(Mth.cos(f2) * (float)x);
              int j = (int)(Mth.sin(f2) * (float)x);
              if (isEmptyOrWaterOrLava(world, pos.offset(i, 0, j)))
              {
                 return false;
              }
           }
           return true;
        }
     }

     public static boolean isEmptyOrWater(LevelAccessor world, BlockPos pos)
     {
        return world.isStateAtPosition(pos, AquamarineUtils::isEmptyOrWater);
     }

     public static boolean isEmptyOrWaterOrLava(LevelAccessor world, BlockPos pos)
     {
        return world.isStateAtPosition(pos, AquamarineUtils::isEmptyOrWaterOrLava);
     }

     public static void buildBaseToTipColumn(Direction direction, int x, boolean b, Consumer<BlockState> consumerState)
     {
        if (x >= 3)
        {
           consumerState.accept(createAquamarineCrystal(direction, DripstoneThickness.BASE));
           
           for(int i = 0; i < x - 3; ++i)
           {
              consumerState.accept(createAquamarineCrystal(direction, DripstoneThickness.MIDDLE));
           }
        }

        if (x >= 2)
        {
           consumerState.accept(createAquamarineCrystal(direction, DripstoneThickness.FRUSTUM));
        }

        if (x >= 1)
        {
           consumerState.accept(createAquamarineCrystal(direction, b ? DripstoneThickness.TIP_MERGE : DripstoneThickness.TIP));
        }

     }

     public static void growAquamarineCrystal(LevelAccessor world, BlockPos pos, Direction direction, int x, boolean b)
     {
        if (isAquamarineBase(world.getBlockState(pos.relative(direction.getOpposite()))))
        {
           BlockPos.MutableBlockPos mutablePos = pos.mutable();
           buildBaseToTipColumn(direction, x, b, (block) ->
           {
              if (block.is(Blocks.POINTED_DRIPSTONE))
              {
                 block = block.setValue(PointedDripstoneBlock.WATERLOGGED, Boolean.valueOf(world.isWaterAt(mutablePos)));
              }
              world.setBlock(mutablePos, block, 2);
              mutablePos.move(direction);
           });
        }
     }

     public static boolean placeAquamarineBlockIfPossible(LevelAccessor world, BlockPos pos) 
     {
        BlockState state = world.getBlockState(pos);
        if (state.is(AQUAMARINE_REPLACEABLE))
        {
           world.setBlock(pos, BlockInit.UNREFINED_AQUAMARINE.get().defaultBlockState(), 2);
           return true;
        } else
        {
           return false;
        }
     }

     public static BlockState createAquamarineCrystal(Direction direction, DripstoneThickness thickness)
     {
        return Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(PointedDripstoneBlock.TIP_DIRECTION, direction)
                .setValue(PointedDripstoneBlock.THICKNESS, thickness);
     }

     public static boolean isAquamarineBaseOrLava(BlockState state)
     {
        return isAquamarineBase(state) || state.is(Blocks.LAVA);
     }

     public static boolean isAquamarineBase(BlockState state)
     {
        return state.is(BlockInit.UNREFINED_AQUAMARINE.get()) || state.is(AQUAMARINE_REPLACEABLE);
     }

     public static boolean isEmptyOrWater(BlockState state)
     {
        return state.isAir() || state.is(Blocks.WATER);
     }

     public static boolean isEmptyOrWaterOrLava(BlockState state)
     {
        return state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.LAVA);
     }

}
