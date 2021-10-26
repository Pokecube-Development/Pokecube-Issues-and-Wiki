package thut.api.entity.blockentity.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;

public class TempBlock extends AirBlock
{
    public static final IntegerProperty LIGHTLEVEL  = IntegerProperty.create("light", 0, 15);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static TempBlock make()
    {
        return new TempBlock(AbstractBlock.Properties.of(Material.AIR).noDrops().isRedstoneConductor(TempBlock::solidCheck)
                .dynamicShape().noOcclusion().lightLevel(s -> s.getValue(TempBlock.LIGHTLEVEL)));
    }

    private static boolean solidCheck(final BlockState state, final IBlockReader reader, final BlockPos pos)
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

    private void onPlayerInteract(final PlayerInteractEvent.RightClickBlock event)
    {
        final BlockRayTraceResult trace = event.getHitVec();
        if (trace == null) return;
        final World world = event.getPlayer().getCommandSenderWorld();
        final TileEntity tile = world.getBlockEntity(event.getPos());
        if (tile instanceof TempTile)
        {
            final TempTile temp = (TempTile) tile;
            final PlayerEntity player = event.getPlayer();
            final Hand hand = event.getHand();
            ActionResultType result = temp.blockEntity.interact(player, hand);
            // Otherwise forward the interaction to the block entity;
            if (result != ActionResultType.PASS && event.getPlayer().isShiftKeyDown()) result = temp.blockEntity
                    .interactAtFromTile(player, trace.getLocation(), hand);
            if (result != ActionResultType.PASS)
            {
                event.setCanceled(true);
                event.setUseBlock(Result.ALLOW);
                event.setUseItem(Result.ALLOW);
            }
        }
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new TempTile();
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
    public BlockRenderType getRenderShape(final BlockState state)
    {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public ActionResultType use(final BlockState state, final World world, final BlockPos pos,
            final PlayerEntity player, final Hand hand, final BlockRayTraceResult hit)
    {
        final TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TempTile)
        {
            final TempTile temp = (TempTile) tile;
            final BlockState eff = temp.getEffectiveState();
            if (eff != null)
            {
                final ActionResultType res = eff.use(world, player, hand, hit);
                if (res != ActionResultType.PASS) return res;
            }
            // Otherwise forward the interaction to the block entity;
            return temp.blockEntity.interactAt(player, hit.getLocation(), hand);
        }
        return ActionResultType.PASS;
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(TempBlock.LIGHTLEVEL, TempBlock.WATERLOGGED);
    }

    @Override
    public void entityInside(final BlockState state, final World worldIn, final BlockPos pos,
            final Entity entityIn)
    {
        final TileEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof TempTile) ((TempTile) te).onEntityCollision(entityIn);
    }

    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
            final ISelectionContext context)
    {
        final TileEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof TempTile) return ((TempTile) te).getShape();
        return VoxelShapes.empty();
    }
}
