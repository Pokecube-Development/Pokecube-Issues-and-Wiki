package pokecube.legends.blocks;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.init.function.PortalActiveFunction;

public class PortalWarp extends Rotates
{
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    String infoname;

    public PortalWarp(final String name, final Properties props)
    {
        super(name, props.tickRandomly());
        this.setDefaultState(this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH)
                .with(PortalWarp.ACTIVE, true));
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(HorizontalBlock.HORIZONTAL_FACING);
        builder.add(PortalWarp.ACTIVE);
    }

    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context)
    {
        return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, context.getPlacementHorizontalFacing()
                .getOpposite()).with(PortalWarp.ACTIVE, true);
    }

    // time for spawn
    @Override
    public int tickRate(final IWorldReader world)
    {
        return PokecubeLegends.config.ticksPerMirageSpawn;
    }

    @Override
    public BlockBase setInfoBlockName(final String infoname)
    {
        this.infoname = infoname;
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(final ItemStack stack, final IBlockReader worldIn, final List<ITextComponent> tooltip,
            final ITooltipFlag flagIn)
    {
        String message;
        if (Screen.hasShiftDown()) message = I18n.format("legendblock." + this.infoname + ".tooltip");
        else message = I18n.format("pokecube.tooltip.advanced");
        tooltip.add(new TranslationTextComponent(message));
    }

    @Override
    public void randomTick(final BlockState state, final World worldIn, final BlockPos pos, final Random random)
    {
        if (state.get(PortalWarp.ACTIVE)) return;
        worldIn.setBlockState(pos, state.with(PortalWarp.ACTIVE, true));
    }

    @Override
    public boolean onBlockActivated(final BlockState state, final World worldIn, final BlockPos pos,
            final PlayerEntity entity, final Hand hand, final BlockRayTraceResult hit)
    {
        if (!state.get(PortalWarp.ACTIVE)) return false;
        if (worldIn instanceof ServerWorld) PortalActiveFunction.executeProcedure(pos, state, (ServerWorld) worldIn);
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(final BlockState state, final World world, final BlockPos pos, final Random random)
    {
        super.animateTick(state, world, pos, random);
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
            world.addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
        }
    }

    @Override
    public boolean canRenderInLayer(final BlockState state, final BlockRenderLayer layer)
    {
        return layer == BlockRenderLayer.CUTOUT || layer == BlockRenderLayer.TRANSLUCENT;
    }
}