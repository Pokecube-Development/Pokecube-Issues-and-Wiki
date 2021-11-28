package thut.api.entity.blockentity.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import thut.api.block.ITickTile;

public class TempBlock extends AirBlock implements EntityBlock
{
    public static final IntegerProperty LIGHTLEVEL  = IntegerProperty.create("light", 0, 15);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static TempBlock make()
    {
        return new TempBlock(BlockBehaviour.Properties.of(Material.AIR).noDrops().isRedstoneConductor(
                TempBlock::solidCheck).dynamicShape().noOcclusion().lightLevel(s -> s.getValue(TempBlock.LIGHTLEVEL)));
    }

    private static boolean solidCheck(final BlockState state, final BlockGetter reader, final BlockPos pos)
    {
        return false;
    }

    public TempBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(TempBlock.LIGHTLEVEL, 0).setValue(
                TempBlock.WATERLOGGED, false));
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::onPlayerInteract);
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
        if (trace == null) return;
        final Level world = event.getPlayer().getCommandSenderWorld();
        final BlockEntity tile = world.getBlockEntity(event.getPos());
        if (tile instanceof TempTile)
        {
            final TempTile temp = (TempTile) tile;
            final Player player = event.getPlayer();
            final InteractionHand hand = event.getHand();
            InteractionResult result = temp.blockEntity.interact(player, hand);
            // Otherwise forward the interaction to the block entity;
            if (result != InteractionResult.PASS && event.getPlayer().isShiftKeyDown()) result = temp.blockEntity
                    .interactAtFromTile(player, trace.getLocation(), hand);
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
     * model, MODELBLOCK_ANIMATED for TESR-only,
     * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
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
        if (tile instanceof TempTile)
        {
            final TempTile temp = (TempTile) tile;
            final BlockState eff = temp.getEffectiveState();
            if (eff != null)
            {
                final InteractionResult res = eff.use(world, player, hand, hit);
                if (res != InteractionResult.PASS) return res;
            }
            // Otherwise forward the interaction to the block entity;
            return temp.blockEntity.interactAtFromTile(player, hit.getLocation(), hand);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(TempBlock.LIGHTLEVEL, TempBlock.WATERLOGGED);
    }

    @Override
    public void entityInside(final BlockState state, final Level worldIn, final BlockPos pos, final Entity entityIn)
    {
//        final BlockEntity te = worldIn.getBlockEntity(pos);
//        if (te instanceof TempTile) ((TempTile) te).onEntityCollision(entityIn);
    }

    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
            final CollisionContext context)
    {
        final BlockEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof TempTile) return ((TempTile) te).getShape();
        return Shapes.empty();
    }
}
