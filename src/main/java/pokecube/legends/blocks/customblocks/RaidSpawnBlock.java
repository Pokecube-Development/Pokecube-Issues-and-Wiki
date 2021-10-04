package pokecube.legends.blocks.customblocks;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.blocks.InteractableHorizontalBlock;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.init.function.MaxRaidFunction;
import pokecube.legends.tileentity.RaidSpawn;

public class RaidSpawnBlock extends InteractableHorizontalBlock implements SimpleWaterloggedBlock
{
    protected static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<State> ACTIVE = EnumProperty.create("state", State.class);

    public static enum State implements StringRepresentable
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
    private static final VoxelShape RAID_SPOT = Shapes.or(
            Block.box(2, 0, 2, 14, 3, 14),
            Block.box(3, 3, 3, 13, 9, 13)).optimize();
    
    public RaidSpawnBlock(final Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(RaidSpawnBlock.ACTIVE, State.EMPTY)
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH).setValue(RaidSpawnBlock.WATERLOGGED, false));
    }

    // Precise selection box
    @Override
    public VoxelShape getShape(final BlockState state, final BlockGetter worldIn, final BlockPos pos,
                               final CollisionContext context)
    {
        return RAID_SPOT;
    }

    @Override
    public boolean isRandomlyTicking(final BlockState state)
    {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(RaidSpawnBlock.ACTIVE, RaidSpawnBlock.WATERLOGGED);
    }

    // Waterlogging on placement
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context)
    {
        final FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return Objects.requireNonNull(super.getStateForPlacement(context)).setValue(HorizontalDirectionalBlock.FACING, context
                .getHorizontalDirection().getOpposite()).setValue(RaidSpawnBlock.WATERLOGGED, ifluidstate.is(
                FluidTags.WATER) && ifluidstate.getAmount() == 8);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(final BlockState state, final Direction facing, final BlockState facingState, final LevelAccessor world, final BlockPos currentPos,
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
    public BlockEntity createTileEntity(final BlockState state, final BlockGetter world)
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
    public void appendHoverText(final ItemStack stack, final BlockGetter worldIn, final List<Component> tooltip,
            final TooltipFlag flagIn)
    {
        String message;
        if (Screen.hasShiftDown()) message = I18n.get("legendblock." + this.infoname + ".tooltip");
        else message = I18n.get("pokecube.tooltip.advanced");
        tooltip.add(new TranslatableComponent(message));
    }

    @Override
    public InteractionResult use(final BlockState state, final Level worldIn, final BlockPos pos,
            final Player entity, final InteractionHand hand, final BlockHitResult hit)
    {
        if (worldIn instanceof ServerLevel)
        {
            final boolean active = state.getValue(RaidSpawnBlock.ACTIVE).active();
            if (active)
            {
                MaxRaidFunction.executeProcedure(pos, state, (ServerLevel) worldIn);
                worldIn.setBlockAndUpdate(pos, state.setValue(RaidSpawnBlock.ACTIVE, State.EMPTY));
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void randomTick(final BlockState state, final ServerLevel worldIn, final BlockPos pos, final Random random)
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
    public void animateTick(final BlockState state, final Level world, final BlockPos pos, final Random random)
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