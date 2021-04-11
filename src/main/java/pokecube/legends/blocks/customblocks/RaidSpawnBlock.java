package pokecube.legends.blocks.customblocks;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.blocks.InteractableHorizontalBlock;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.init.function.MaxRaidFunction;
import pokecube.legends.tileentity.RaidSpawn;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class RaidSpawnBlock extends InteractableHorizontalBlock implements IWaterLoggable
{
    protected static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<State> ACTIVE = EnumProperty.create("state", State.class);

    public static enum State implements IStringSerializable
    {
        EMPTY("empty"), NORMAL("normal"), RARE("rare");

        private final String name;

        private State(final String name)
        {
            this.name = name;
        }

        @Override
        public String getSerializedName()
        {
            return this.name;
        }

        public boolean active()
        {
            return this != EMPTY;
        }

    }

    String  infoname;
    boolean hasTextInfo = true;

    // Precise selection box
    private static final VoxelShape RAID_SPOT = VoxelShapes.or(
            Block.box(2, 0, 2, 14, 3, 14),
            Block.box(3, 3, 3, 13, 9, 13)).optimize();
    
    public RaidSpawnBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(RaidSpawnBlock.ACTIVE, State.EMPTY)
                .setValue(HorizontalBlock.FACING, Direction.NORTH).setValue(RaidSpawnBlock.WATERLOGGED, false));
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos,
                               final ISelectionContext context)
    {
        return RAID_SPOT;
    }

    @Override
    public boolean isRandomlyTicking(final BlockState state)
    {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(final StateContainer.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(RaidSpawnBlock.ACTIVE, RaidSpawnBlock.WATERLOGGED);
    }

    // Waterlogging on placement
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return Objects.requireNonNull(super.getStateForPlacement(context)).setValue(HorizontalBlock.FACING, context
                .getHorizontalDirection().getOpposite()).setValue(RaidSpawnBlock.WATERLOGGED, ifluidstate.is(
                FluidTags.WATER) && ifluidstate.getAmount() == 8);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState, final IWorld world, final BlockPos currentPos,
                                  final BlockPos facingPos)
    {
        if (state.getValue(RaidSpawnBlock.WATERLOGGED)) world.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        return super.updateShape(state, facing, facingState, world, currentPos, facingPos);
    }

    // Adds Waterlogging State
    @SuppressWarnings("deprecation")
    @Override
    public FluidState getFluidState(final BlockState state)
    {
        return state.getValue(RaidSpawnBlock.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new RaidSpawn();
    }

    @Override
    public boolean hasTileEntity(final BlockState state)
    {
        return true;
    }

    public RaidSpawnBlock setInfoBlockName(final String infoname)
    {
        this.infoname = infoname;
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(final ItemStack stack, final IBlockReader worldIn, final List<ITextComponent> tooltip,
            final ITooltipFlag flagIn)
    {
        String message;
        if (Screen.hasShiftDown()) message = I18n.get("legendblock." + this.infoname + ".tooltip");
        else message = I18n.get("pokecube.tooltip.advanced");
        tooltip.add(new TranslationTextComponent(message));
    }

    @Override
    public ActionResultType use(final BlockState state, final World worldIn, final BlockPos pos,
            final PlayerEntity entity, final Hand hand, final BlockRayTraceResult hit)
    {
        if (worldIn instanceof ServerWorld)
        {
            final boolean active = state.getValue(RaidSpawnBlock.ACTIVE).active();
            if (active)
            {
                MaxRaidFunction.executeProcedure(pos, state, (ServerWorld) worldIn);
                worldIn.setBlockAndUpdate(pos, state.setValue(RaidSpawnBlock.ACTIVE, State.EMPTY));
            }
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public void randomTick(final BlockState state, final ServerWorld worldIn, final BlockPos pos, final Random random)
    {
        final boolean active = state.getValue(RaidSpawnBlock.ACTIVE).active();
        if (active) return;
        final double rng = random.nextDouble();
        final boolean reset = rng < PokecubeLegends.config.raidResetChance;
        if (!reset) return;
        worldIn.setBlockAndUpdate(pos, state.setValue(RaidSpawnBlock.ACTIVE, random
                .nextDouble() > PokecubeLegends.config.rareRaidChance ? State.NORMAL : State.RARE));

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(final BlockState state, final World world, final BlockPos pos, final Random random)
    {
        if (!state.getValue(RaidSpawnBlock.ACTIVE).active()) return;

        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();
        for (int l = 0; l < 4; ++l)
        {
            final double d0 = x + random.nextFloat();
            final double d1 = y + random.nextFloat();
            final double d2 = z + random.nextFloat();
            final double d3 = (random.nextFloat() - 0.5D) * 0.6;
            final double d4 = (random.nextFloat() - 0.5D) * 0.6;
            final double d5 = (random.nextFloat() - 0.5D) * 0.6;
            world.addParticle(ParticleTypes.FLAME, d0, d1, d2, d3, d4, d5);
        }
    }
}