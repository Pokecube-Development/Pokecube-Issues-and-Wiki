package pokecube.legends.blocks;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
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
import pokecube.core.blocks.maxspot.MaxBlock;
import pokecube.legends.init.function.MaxRaidFunction;
import pokecube.legends.tileentity.RaidSpawn;

public class RaidSpawnBlock extends MaxBlock
{
    public static enum State implements IStringSerializable
    {
        EMPTY("empty"), NORMAL("normal"), RARE("rare");

        private final String name;

        private State(final String name)
        {
            this.name = name;
        }

        @Override
        public String getName()
        {
            return this.name;
        }

        public boolean active()
        {
            return this != EMPTY;
        }

    }

    public static final EnumProperty<State> ACTIVE = EnumProperty.create("state", State.class);

    String  infoname;
    boolean hasTextInfo = true;

    public RaidSpawnBlock(final Material material)
    {
        super(Properties.create(material).sound(SoundType.METAL).hardnessAndResistance(2000, 2000));
        this.setDefaultState(this.stateContainer.getBaseState().with(RaidSpawnBlock.ACTIVE, State.EMPTY));
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder)
    {
        super.fillStateContainer(builder);
        builder.add(RaidSpawnBlock.ACTIVE);
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world)
    {
        return new RaidSpawn();
    }

    @Override
    public int tickRate(final IWorldReader world)
    {
        return 3000;
    }

    public RaidSpawnBlock setInfoBlockName(final String infoname)
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
    public boolean onBlockActivated(final BlockState state, final World worldIn, final BlockPos pos,
            final PlayerEntity entity, final Hand hand, final BlockRayTraceResult hit)
    {
        if (worldIn instanceof ServerWorld)
        {
            final boolean active = state.get(RaidSpawnBlock.ACTIVE).active();
            if (active)
            {
                MaxRaidFunction.executeProcedure(pos, state, (ServerWorld) worldIn);
                worldIn.setBlockState(pos, state.with(RaidSpawnBlock.ACTIVE, State.EMPTY));
            }
        }
        return true;
    }

    @Override
    public void randomTick(final BlockState state, final World worldIn, final BlockPos pos, final Random random)
    {
        final boolean active = state.get(RaidSpawnBlock.ACTIVE).active();
        if (active) return;
        // TODO decide on random chance of going rare spawn instead
        worldIn.setBlockState(pos, state.with(RaidSpawnBlock.ACTIVE, State.NORMAL));

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(final BlockState state, final World world, final BlockPos pos, final Random random)
    {
        if (!state.get(RaidSpawnBlock.ACTIVE).active()) return;

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