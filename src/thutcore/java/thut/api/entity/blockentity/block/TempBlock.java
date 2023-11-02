package thut.api.entity.blockentity.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import thut.api.block.ITickTile;
import thut.core.common.ThutCore;

public class TempBlock extends AirBlock implements EntityBlock
{
    public static final IntegerProperty LIGHTLEVEL = IntegerProperty.create("light", 0, 15);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static TempBlock make()
    {
        return new TempBlock(BlockBehaviour.Properties.of(Material.STRUCTURAL_AIR).noDrops()
                .isRedstoneConductor(TempBlock::solidCheck).dynamicShape().noOcclusion()
                .lightLevel(s -> s.getValue(TempBlock.LIGHTLEVEL)));
    }

    private static boolean solidCheck(final BlockState state, final BlockGetter reader, final BlockPos pos)
    {
        BlockEntity be = reader.getBlockEntity(pos);
        if (be instanceof TempTile temp && temp.getEffectiveState() != null
                && temp.getEffectiveState().getBlock() != state.getBlock())
            return temp.getEffectiveState().isRedstoneConductor(reader, pos);
        return false;
    }

    public TempBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(TempBlock.LIGHTLEVEL, 0).setValue(TempBlock.WATERLOGGED, false));
        ThutCore.FORGE_BUS.addListener(EventPriority.LOWEST, this::onPlayerInteract);
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state)
    {
        return new TempTile(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level world, final BlockState state,
            final BlockEntityType<T> type)
    {
        return ITickTile.getTicker(world, state, type);
    }

    private void onPlayerInteract(final PlayerInteractEvent.RightClickBlock event)
    {
        final BlockHitResult trace = event.getHitVec();
        if (trace == null || !event.getEntity().isShiftKeyDown()) return;
        final Level world = event.getEntity().getLevel();
        final BlockEntity tile = world.getBlockEntity(event.getPos());
        if (tile instanceof TempTile temp)
        {
            Player player = event.getPlayer();
            BlockPos pos = event.getPos();
            BlockState state = world.getBlockState(pos);
            InteractionHand hand = event.getHand();
            InteractionResult result = temp.use(state, world, pos, player, hand, trace);
            if (result != InteractionResult.PASS)
            {
                event.setCanceled(true);
                event.setUseBlock(Result.ALLOW);
                event.setUseItem(Result.ALLOW);
            }
        }
    }

    /**
     * The type of render function called. MODEL for mixed tesr and static
     * model, MODELBLOCK_ANIMATED for TESR-only, LIQUID for vanilla liquids,
     * INVISIBLE to skip all rendering
     *
     * @deprecated call via {@link BlockState#getRenderType()} whenever
     *             possible. Implementing/overriding is fine.
     */
    @Deprecated
    @Override
    public RenderShape getRenderShape(final BlockState state)
    {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public InteractionResult use(final BlockState state, final Level world, final BlockPos pos, final Player player,
            final InteractionHand hand, final BlockHitResult hit)
    {
        final BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TempTile temp) return temp.use(state, world, pos, player, hand, hit);
        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(TempBlock.LIGHTLEVEL, TempBlock.WATERLOGGED);
    }

    @Override
    public void entityInside(final BlockState state, final Level level, final BlockPos pos, final Entity entity)
    {
        final BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TempTile temp && temp.getEffectiveState() != null
                && temp.getEffectiveState().getBlock() != state.getBlock())
            temp.getEffectiveState().entityInside(level, pos, entity);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float distance)
    {
        final BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof TempTile tile)
        {
            distance = tile.onVerticalCollide(entity, distance);
            if (tile.getEffectiveState() != null && tile.getEffectiveState().getBlock() != state.getBlock())
            {
                tile.getEffectiveState().getBlock().stepOn(level, pos, state, entity);
                return;
            }
        }
        super.fallOn(level, state, pos, entity, distance);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity)
    {
        final BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof TempTile tile)
        {
            tile.onVerticalCollide(entity, 0);
            if (tile.getEffectiveState() != null && tile.getEffectiveState().getBlock() != state.getBlock())
                tile.getEffectiveState().getBlock().stepOn(level, pos, state, entity);
        }
    }

    @Override
    public float getFriction(BlockState state, LevelReader level, BlockPos pos, Entity entity)
    {
        final BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof TempTile tile && tile.getEffectiveState() != null
                && tile.getEffectiveState().getBlock() != state.getBlock())
            return tile.getEffectiveState().getBlock().getFriction(state, level, pos, entity);
        return super.getFriction(state, level, pos, entity);
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        final BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof TempTile temp)
        {
            // Default to true for collision, for fast moving things that have
            // not collided with us yet.
            boolean forCollision = true;

            // Now check if it should be modified.
            if (context instanceof EntityCollisionContext c && c.getEntity() != null)
            {
                boolean collider = false;

                forCollision = temp.blockEntity == null
                        || (collider = temp.blockEntity.recentCollides.containsKey(c.getEntity()));

                // If it has already collided, we need to check if it
                // intersects our bounds, if not, we are not colliding, so
                // report full box for interaction purposes.
                if (collider)
                {
                    VoxelShape base = temp.getShape(false);
                    if (!base.isEmpty())
                    {
                        forCollision = base.bounds().intersects(c.getEntity().getBoundingBox());
                    }
                }
            }
            return temp.getShape(forCollision);
        }
        return Shapes.empty();
    }
}
