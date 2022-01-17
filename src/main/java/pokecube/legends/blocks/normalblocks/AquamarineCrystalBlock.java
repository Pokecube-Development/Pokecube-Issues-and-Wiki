package pokecube.legends.blocks.normalblocks;

import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.legends.init.BlockInit;

public class AquamarineCrystalBlock extends PointedDripstoneBlock implements Fallable, SimpleWaterloggedBlock
{
    public static final VoxelShape TIP_MERGE_SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);
    public static final VoxelShape TIP_SHAPE_UP = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 15.0D, 12.0D);
    public static final VoxelShape TIP_SHAPE_DOWN = Block.box(4.0D, 1.0D, 4.0D, 12.0D, 15.0D, 12.0D);
    public static final VoxelShape FRUSTUM_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 16.0D, 13.0D);
    public static final VoxelShape MIDDLE_SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    public static final VoxelShape BASE_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
    
    public AquamarineCrystalBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TIP_DIRECTION, Direction.UP).setValue(THICKNESS, DripstoneThickness.TIP)
                .setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
    {
       return isValidAquamarineCrystalPlacement(world, pos, state.getValue(TIP_DIRECTION));
    }

    public void onProjectileHit(Level world, BlockState state, BlockHitResult block, Projectile projectile)
    {
       BlockPos blockpos = block.getBlockPos();
       if (!world.isClientSide)
       {
          BlockPos pos = block.getBlockPos();
          world.playSound((Player)null, pos, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 1.0F, 0.5F + world.random.nextFloat() * 1.2F);
          world.playSound((Player)null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 0.5F + world.random.nextFloat() * 1.2F);
       }
       
       if (!world.isClientSide && projectile.mayInteract(world, blockpos) && projectile instanceof ThrownTrident 
               && projectile.getDeltaMovement().length() > 0.6D) {
           world.destroyBlock(blockpos, true);
       }
    }

    public BlockState updateShape(BlockState state, Direction direction, BlockState state1, LevelAccessor world, BlockPos pos, BlockPos pos1)
    {
       if (state.getValue(WATERLOGGED))
       {
          world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
       }

       if (direction != Direction.UP && direction != Direction.DOWN)
       {
          return state;
       } else {
          Direction direction1 = state.getValue(TIP_DIRECTION);
          if (direction1 == Direction.DOWN && world.getBlockTicks().hasScheduledTick(pos, this))
          {
             return state;
          } else if (direction == direction1.getOpposite() && !this.canSurvive(state, world, pos))
          {
             if (direction1 == Direction.DOWN)
             {
                this.scheduleStalactiteFallTicks(state, world, pos);
             } else
             {
                world.scheduleTick(pos, this, 1);
             }
             return state;
          } else
          {
             boolean flag = state.getValue(THICKNESS) == DripstoneThickness.TIP_MERGE;
             DripstoneThickness thickness = calculateAquamarineThickness(world, pos, direction1, flag);
             return state.setValue(THICKNESS, thickness);
          }
       }
    }

    public void animateTick(BlockState state, Level world, BlockPos pos, Random random)
    {
       if (canDrip(state))
       {
          float f = random.nextFloat();
          if (!(f > 0.12F))
          {
             getFluidAboveStalactite(world, pos, state).filter((p_154031_) ->
             {
                return f < 0.02F || canFillCauldron(p_154031_);
             }).ifPresent((fluid) ->
             {
                spawnDripParticle(world, pos, state, fluid);
             });
          }
       }
    }

    public void tick(BlockState state, ServerLevel world, BlockPos pos, Random random)
    {
       if (isStalagmite(state) && !this.canSurvive(state, world, pos))
       {
          world.destroyBlock(pos, true);
       } else
       {
          spawnFallingStalactite(state, world, pos);
       }
    }

    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, Random random)
    {
       maybeFillCauldron(state, world, pos, random.nextFloat());
       if (random.nextFloat() < 0.011377778F && isStalactiteStartPos(state, world, pos))
       {
          growStalactiteOrStalagmiteIfPossible(state, world, pos, random);
       }
    }

    @VisibleForTesting
    public static void maybeFillCauldron(BlockState state, ServerLevel world, BlockPos pos, float f1)
    {
       if (!(f1 > 0.17578125F) || !(f1 > 0.05859375F))
       {
          if (isStalactiteStartPos(state, world, pos))
          {
             Fluid fluid = getCauldronFillFluidType(world, pos);
             float f;
             if (fluid == Fluids.WATER)
             {
                f = 0.17578125F;
             } else {
                if (fluid != Fluids.LAVA)
                {
                   return;
                }
                f = 0.05859375F;
             }

             if (!(f1 >= f))
             {
                BlockPos pos1 = findTip(state, world, pos, 11, false);
                if (pos1 != null)
                {
                   BlockPos pos2 = findFillableCauldronBelowStalactiteTip(world, pos1, fluid);
                   if (pos2 != null)
                   {
                      world.levelEvent(1504, pos1, 0);
                      int i = pos1.getY() - pos2.getY();
                      int j = 50 + i;
                      BlockState state1 = world.getBlockState(pos2);
                      world.scheduleTick(pos2, state1.getBlock(), j);
                   }
                }
             }
          }
       }
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
       LevelAccessor world = context.getLevel();
       BlockPos pos = context.getClickedPos();
       Direction direction = context.getNearestLookingVerticalDirection().getOpposite();
       Direction direction1 = calculateTipDirection(world, pos, direction);
       if (direction1 == null)
       {
          return null;
       } else
       {
          boolean flag = !context.isSecondaryUseActive();
          DripstoneThickness thickness = calculateAquamarineThickness(world, pos, direction1, flag);
          return thickness == null ? null : this.defaultBlockState().setValue(TIP_DIRECTION, direction1).setValue(THICKNESS, thickness)
                  .setValue(WATERLOGGED, Boolean.valueOf(world.getFluidState(pos).getType() == Fluids.WATER));
       }
    }

    public VoxelShape getShape(BlockState state, BlockGetter block, BlockPos pos, CollisionContext context)
    {
       DripstoneThickness thickness = state.getValue(THICKNESS);
       VoxelShape shape;
       if (thickness == DripstoneThickness.TIP_MERGE)
       {
          shape = TIP_MERGE_SHAPE;
       } else if (thickness == DripstoneThickness.TIP)
       {
          if (state.getValue(TIP_DIRECTION) == Direction.DOWN)
          {
             shape = TIP_SHAPE_DOWN;
          } else
          {
             shape = TIP_SHAPE_UP;
          }
       } else if (thickness == DripstoneThickness.FRUSTUM) 
       {
          shape = FRUSTUM_SHAPE;
       } else if (thickness == DripstoneThickness.MIDDLE)
       {
          shape = MIDDLE_SHAPE;
       } else
       {
          shape = BASE_SHAPE;
       }
       Vec3 vec3 = state.getOffset(block, pos);
       return shape.move(vec3.x, 0.0D, vec3.z);
    }

    public void scheduleStalactiteFallTicks(BlockState state, LevelAccessor world, BlockPos pos)
    {
       BlockPos pos1 = findTip(state, world, pos, Integer.MAX_VALUE, true);
       if (pos1 != null)
       {
          BlockPos.MutableBlockPos mutablePos = pos1.mutable();
          mutablePos.move(Direction.DOWN);
          BlockState state1 = world.getBlockState(mutablePos);
          if (state1.getCollisionShape(world, mutablePos, CollisionContext.empty()).max(Direction.Axis.Y) >= 1.0D || state1.is(Blocks.POWDER_SNOW))
          {
             world.destroyBlock(pos1, true);
             mutablePos.move(Direction.UP);
          }

          mutablePos.move(Direction.UP);

          while(isStalactite(world.getBlockState(mutablePos)))
          {
             world.scheduleTick(mutablePos, this, 2);
             mutablePos.move(Direction.UP);
          }
       }
    }

    public static int getStalactiteSizeFromTip(ServerLevel world, BlockPos pos, int j)
    {
       int i = 1;
       BlockPos.MutableBlockPos mutablePos = pos.mutable().move(Direction.UP);

       while(i < j && isStalactite(world.getBlockState(mutablePos)))
       {
          ++i;
          mutablePos.move(Direction.UP);
       }
       return i;
    }

    public static void spawnFallingStalactite(BlockState state, ServerLevel world, BlockPos pos)
    {
       Vec3 vec3 = Vec3.atBottomCenterOf(pos);
       FallingBlockEntity entity = new FallingBlockEntity(world, vec3.x, vec3.y, vec3.z, state);
       if (isTip(state, true))
       {
          int i = getStalactiteSizeFromTip(world, pos, 6);
          float f = 1.0F * (float)i;
          entity.setHurtsEntities(f, 40);
       }
       world.addFreshEntity(entity);
    }

    @VisibleForTesting
    public static void growStalactiteOrStalagmiteIfPossible(BlockState state, ServerLevel world, BlockPos pos, Random random)
    {
       BlockState state1 = world.getBlockState(pos.above(1));
       BlockState state2 = world.getBlockState(pos.above(2));
       if (canGrow(state1, state2))
       {
          BlockPos pos1 = findTip(state, world, pos, 7, false);
          if (pos1 != null)
          {
             BlockState state3 = world.getBlockState(pos1);
             if (canDrip(state3) && canTipGrow(state3, world, pos1))
             {
                if (random.nextBoolean())
                {
                    grow(world, pos1, Direction.DOWN);
                } else
                {
                   growStalagmiteBelow(world, pos1);
                }
             }
          }
       }
    }

    public static void growStalagmiteBelow(ServerLevel world, BlockPos pos)
    {
       BlockPos.MutableBlockPos mutablePos = pos.mutable();

       for(int i = 0; i < 10; ++i) {
          mutablePos.move(Direction.DOWN);
          BlockState state = world.getBlockState(mutablePos);
          if (!state.getFluidState().isEmpty())
          {
             return;
          }

          if (isUnmergedTipWithDirection(state, Direction.UP) && canTipGrow(state, world, mutablePos))
          {
             grow(world, mutablePos, Direction.UP);
             return;
          }

          if (isValidAquamarineCrystalPlacement(world, mutablePos, Direction.UP) && !world.isWaterAt(mutablePos.below()))
          {
             grow(world, mutablePos.below(), Direction.UP);
             return;
          }
       }
    }

    public static void grow(ServerLevel world, BlockPos pos, Direction direction)
    {
       BlockPos pos1 = pos.relative(direction);
       BlockState state = world.getBlockState(pos1);
       if (isUnmergedTipWithDirection(state, direction.getOpposite()))
       {
          createMergedTips(state, world, pos1);
       } else if (state.isAir() || state.is(Blocks.WATER))
       {
          createDripstone(world, pos1, direction, DripstoneThickness.TIP);
       }
    }

    public static void createDripstone(LevelAccessor world, BlockPos pos, Direction direction, DripstoneThickness thickness)
    {
       BlockState state = BlockInit.AQUAMARINE_CRYSTAL.get().defaultBlockState().setValue(TIP_DIRECTION, direction)
               .setValue(THICKNESS, thickness).setValue(WATERLOGGED, Boolean.valueOf(world.getFluidState(pos).getType() == Fluids.WATER));
       world.setBlock(pos, state, 3);
    }

    public static void createMergedTips(BlockState state, LevelAccessor world, BlockPos pos)
    {
       BlockPos pos1;
       BlockPos pos2;
       if (state.getValue(TIP_DIRECTION) == Direction.UP)
       {
          pos2 = pos;
          pos1 = pos.above();
       } else
       {
          pos1 = pos;
          pos2 = pos.below();
       }
       createDripstone(world, pos1, Direction.DOWN, DripstoneThickness.TIP_MERGE);
       createDripstone(world, pos2, Direction.UP, DripstoneThickness.TIP_MERGE);
    }

    public static void spawnDripParticle(Level world, BlockPos pos, BlockState state)
    {
       getFluidAboveStalactite(world, pos, state).ifPresent((p_154189_) ->
       {
          spawnDripParticle(world, pos, state, p_154189_);
       });
    }

    public static void spawnDripParticle(Level world, BlockPos pos, BlockState state, Fluid fluid)
    {
       Vec3 vec3 = state.getOffset(world, pos);
       double d0 = 0.0625D;
       double d1 = (double)pos.getX() + 0.5D + vec3.x;
       double d2 = (double)((float)(pos.getY() + 1) - 0.6875F) - 0.0D;
       double d3 = (double)pos.getZ() + 0.5D + vec3.z;
       Fluid fluid1 = getDripFluid(world, fluid);
       ParticleOptions particle = fluid1.is(FluidTags.LAVA) ? ParticleTypes.DRIPPING_DRIPSTONE_LAVA : ParticleTypes.DRIPPING_DRIPSTONE_WATER;
       world.addParticle(particle, d1, d2, d3, 0.0D, 0.0D, 0.0D);
    }

    @Nullable
    public static BlockPos findTip(BlockState state, LevelAccessor world, BlockPos pos, int i, boolean b)
    {
       if (isTip(state, b))
       {
          return pos;
       } else
       {
          Direction direction = state.getValue(TIP_DIRECTION);
          Predicate<BlockState> predicate = (block) ->
          {
             return block.is(BlockInit.AQUAMARINE_CRYSTAL.get()) && block.getValue(TIP_DIRECTION) == direction;
          };
          return findBlockVertical(world, pos, direction.getAxisDirection(), predicate, (block) ->
          {
             return isTip(block, b);
          }, i).orElse((BlockPos)null);
       }
    }

    @Nullable
    public static Direction calculateTipDirection(LevelReader world, BlockPos pos, Direction direction)
    {
       Direction direction1;
       if (isValidAquamarineCrystalPlacement(world, pos, direction))
       {
          direction1 = direction;
       } else {
          if (!isValidAquamarineCrystalPlacement(world, pos, direction.getOpposite()))
          {
             return null;
          }
          direction1 = direction.getOpposite();
       }
       return direction1;
    }

    public static DripstoneThickness calculateAquamarineThickness(LevelReader world, BlockPos pos, Direction direction, boolean b)
    {
       Direction direction1 = direction.getOpposite();
       BlockState state = world.getBlockState(pos.relative(direction));
       if (isAquamarineCrystalWithDirection(state, direction1))
       {
          return !b && state.getValue(THICKNESS) != DripstoneThickness.TIP_MERGE ? DripstoneThickness.TIP : DripstoneThickness.TIP_MERGE;
       } else if (!isAquamarineCrystalWithDirection(state, direction))
       {
          return DripstoneThickness.TIP;
       } else
       {
          DripstoneThickness thickness = state.getValue(THICKNESS);
          if (thickness != DripstoneThickness.TIP && thickness != DripstoneThickness.TIP_MERGE)
          {
             BlockState state2 = world.getBlockState(pos.relative(direction1));
             return !isAquamarineCrystalWithDirection(state2, direction) ? DripstoneThickness.BASE : DripstoneThickness.MIDDLE;
          } else
          {
             return DripstoneThickness.FRUSTUM;
          }
       }
    }

    public static boolean canDrip(BlockState state)
    {
       return isStalactite(state) && state.getValue(THICKNESS) == DripstoneThickness.TIP && !state.getValue(WATERLOGGED);
    }

    public static boolean canTipGrow(BlockState state, ServerLevel world, BlockPos pos)
    {
       Direction direction = state.getValue(TIP_DIRECTION);
       BlockPos pos1 = pos.relative(direction);
       BlockState state1 = world.getBlockState(pos1);
       if (!state1.getFluidState().isEmpty())
       {
          return false;
       } else
       {
          return state1.isAir() ? true : isUnmergedTipWithDirection(state1, direction.getOpposite());
       }
    }

    public static Optional<BlockPos> findRootBlock(Level world, BlockPos pos, BlockState state, int i)
    {
       Direction direction = state.getValue(TIP_DIRECTION);
       Predicate<BlockState> predicate = (block) ->
       {
          return block.is(BlockInit.AQUAMARINE_CRYSTAL.get()) && block.getValue(TIP_DIRECTION) == direction;
       };
       return findBlockVertical(world, pos, direction.getOpposite().getAxisDirection(), predicate, (block) ->
       {
          return !block.is(BlockInit.AQUAMARINE_CRYSTAL.get());
       }, i);
    }

    public static boolean isValidAquamarineCrystalPlacement(LevelReader world, BlockPos pos, Direction direction)
    {
       BlockPos pos1 = pos.relative(direction.getOpposite());
       BlockState state = world.getBlockState(pos1);
       return state.isFaceSturdy(world, pos1, direction) || isAquamarineCrystalWithDirection(state, direction)
               || state.is(BlockInit.CRYSTALLIZED_CACTUS.get());
    }

    public static boolean isTip(BlockState state, boolean b)
    {
       if (!state.is(BlockInit.AQUAMARINE_CRYSTAL.get()))
       {
          return false;
       } else
       {
          DripstoneThickness thickness = state.getValue(THICKNESS);
          return thickness == DripstoneThickness.TIP || b && thickness == DripstoneThickness.TIP_MERGE;
       }
    }

    public static boolean isUnmergedTipWithDirection(BlockState state, Direction direction)
    {
       return isTip(state, false) && state.getValue(TIP_DIRECTION) == direction;
    }

    public static boolean isStalactite(BlockState state)
    {
       return isAquamarineCrystalWithDirection(state, Direction.DOWN);
    }

    public static boolean isStalagmite(BlockState state)
    {
       return isAquamarineCrystalWithDirection(state, Direction.UP);
    }

    public static boolean isStalactiteStartPos(BlockState state, LevelReader world, BlockPos pos)
    {
       return isStalactite(state) && !world.getBlockState(pos.above()).is(BlockInit.AQUAMARINE_CRYSTAL.get());
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter block, BlockPos pos, PathComputationType type)
    {
       return false;
    }

    public static boolean isAquamarineCrystalWithDirection(BlockState state, Direction direction)
    {
       return state.is(BlockInit.AQUAMARINE_CRYSTAL.get()) && state.getValue(TIP_DIRECTION) == direction;
    }

    @Nullable
    public static BlockPos findFillableCauldronBelowStalactiteTip(Level world, BlockPos pos, Fluid fluid)
    {
       Predicate<BlockState> predicate = (state) ->
       {
          return state.getBlock() instanceof AbstractCauldronBlock && ((AbstractCauldronBlock)state.getBlock()).canReceiveStalactiteDrip(fluid);
       };
       return findBlockVertical(world, pos, Direction.DOWN.getAxisDirection(), BlockBehaviour.BlockStateBase::isAir, predicate, 11).orElse((BlockPos)null);
    }

    @Nullable
    public static BlockPos findStalactiteTipAboveCauldron(Level world, BlockPos pos)
    {
       return findBlockVertical(world, pos, Direction.UP.getAxisDirection(), BlockBehaviour.BlockStateBase::isAir, AquamarineCrystalBlock::canDrip, 11)
               .orElse((BlockPos)null);
    }

    public static Fluid getCauldronFillFluidType(Level world, BlockPos pos)
    {
       return getFluidAboveStalactite(world, pos, world.getBlockState(pos)).filter(AquamarineCrystalBlock::canFillCauldron).orElse(Fluids.EMPTY);
    }

    public static Optional<Fluid> getFluidAboveStalactite(Level world, BlockPos pos, BlockState state)
    {
       return !isStalactite(state) ? Optional.empty() : findRootBlock(world, pos, state, 11).map((rootPos) ->
       {
          return world.getFluidState(rootPos.above()).getType();
       });
    }

    public static boolean canFillCauldron(Fluid fluid)
    {
       return fluid == Fluids.LAVA || fluid == Fluids.WATER;
    }

    public static boolean canGrow(BlockState state, BlockState state1)
    {
       return state.is(BlockInit.AQUAMARINE_CRYSTAL.get()) && state1.is(Blocks.WATER) && state1.getFluidState().isSource();
    }

    public static Fluid getDripFluid(Level world, Fluid fluid)
    {
       if (fluid.isSame(Fluids.EMPTY))
       {
          return world.dimensionType().ultraWarm() ? Fluids.LAVA : Fluids.WATER;
       } else
       {
          return fluid;
       }
    }

    public static Optional<BlockPos> findBlockVertical(LevelAccessor world, BlockPos pos, Direction.AxisDirection axisDirection, 
            Predicate<BlockState> predicateState, Predicate<BlockState> predicateState1, int j)
    {
       Direction direction = Direction.get(axisDirection, Direction.Axis.Y);
       BlockPos.MutableBlockPos mutablePos = pos.mutable();

       for(int i = 1; i < j; ++i)
       {
          mutablePos.move(direction);
          BlockState state = world.getBlockState(mutablePos);
          if (predicateState1.test(state))
          {
             return Optional.of(mutablePos.immutable());
          }

          if (world.isOutsideBuildHeight(mutablePos.getY()) || !predicateState.test(state))
          {
             return Optional.empty();
          }
       }
       return Optional.empty();
    }
}
