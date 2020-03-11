package pokecube.legends.blocks;

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.init.function.PortalActiveFunction;

public class PortalWarp extends Rotates
{

    String  infoname;
    boolean hasTextInfo = true;

    public PortalWarp(final String name, final Properties props)
    {
        super(name, props.tickRandomly());
    }

    // time for spawn
    //@Override
    //public int tickRate(final IWorldReader world)
    //{
    //    return 600;
    //}

    @Override
    public BlockBase setInfoBlockName(final String infoname)
    {
        this.infoname = infoname;
        return this;
    }

    @Override
    public BlockBase noInfoBlock()
    {
        this.hasTextInfo = false;
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(final ItemStack stack, final IBlockReader worldIn, final List<ITextComponent> tooltip,
            final ITooltipFlag flagIn)
    {
        if (!this.hasTextInfo) return;
        String message;
        if (Screen.hasShiftDown()) message = I18n.format("legendblock." + this.infoname + ".tooltip");
        else message = I18n.format("pokecube.tooltip.advanced");
        tooltip.add(new TranslationTextComponent(message));
    }

    @Override
    public boolean onBlockActivated(final BlockState state,final World worldIn,final BlockPos pos,final PlayerEntity entity,final Hand hand,final BlockRayTraceResult hit)
    {
    	boolean retval = super.onBlockActivated(state, worldIn, pos, entity, hand, hit);
    	Direction direction = hit.getFace();
		{
	        final int x = pos.getX();
	        final int y = pos.getY();
	        final int z = pos.getZ();
	        if (worldIn instanceof ServerWorld) PortalActiveFunction.executeProcedure(x, y, z, (ServerWorld) worldIn);
		}
		return true;
    }

    @OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState state, World world, BlockPos pos, Random random) {
		super.animateTick(state, world, pos, random);
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		if (true)
			for (int l = 0; l < 4; ++l) {
				double d0 = (x + random.nextFloat());
				double d1 = (y + random.nextFloat());
				double d2 = (z + random.nextFloat());
				double d3 = (random.nextFloat() - 0.5D) * 0.6000000014901161D;
				double d4 = (random.nextFloat() - 0.5D) * 0.6000000014901161D;
				double d5 = (random.nextFloat() - 0.5D) * 0.6000000014901161D;
				world.addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
			}
	}
}