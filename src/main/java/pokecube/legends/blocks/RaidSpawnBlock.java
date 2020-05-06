package pokecube.legends.blocks;

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
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
import pokecube.legends.init.function.MaxRaidFunction;

public class RaidSpawnBlock extends BlockBase
{
    String infoname;

    public RaidSpawnBlock(final String name, final Material material)
    {
        super(name, Properties.create(material).sound(SoundType.METAL).hardnessAndResistance(2000, 2000));
    }
    
    @Override
    public int tickRate(final IWorldReader world)
    {
        return 3000;
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
    public boolean onBlockActivated(final BlockState state, final World worldIn, final BlockPos pos,
            final PlayerEntity entity, final Hand hand, final BlockRayTraceResult hit)
    {
        if (worldIn instanceof ServerWorld)
        {
            MaxRaidFunction.executeProcedure(pos, state, (ServerWorld) worldIn);
        }
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
            world.addParticle(ParticleTypes.FLAME, d0, d1, d2, d3, d4, d5);
        }
    }
}