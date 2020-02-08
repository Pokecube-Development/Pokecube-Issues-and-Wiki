package pokecube.legends.blocks;

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.worldgen.dimension.ModDimensions;
import pokecube.legends.worldgen.dimension.UltraSpaceModDimension;

public class UltraSpacePortal extends Rotates
{
    String  infoname;
    boolean hasTextInfo = true;

    public UltraSpacePortal(final String name, final Properties props)
    {
        super(name, props.tickRandomly());
    }

    // Time for Despawn
    @Override
    public int tickRate(final IWorldReader world)
    {
        return 700;
    }

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
    public void randomTick(final BlockState state, final World worldIn, final BlockPos pos, final Random random)
    {

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void animateTick(final BlockState stateIn, final World world, final BlockPos pos, final Random random)
    {
        if (random.nextInt(100) == 0) world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                SoundEvents.AMBIENT_CAVE, SoundCategory.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F, false);
    }

    @Override
    public void onEntityCollision(final BlockState state, final World worldIn, final BlockPos pos, final Entity entity)
    {
        if (!(entity instanceof ServerPlayerEntity)) return;
        if (entity.dimension == DimensionType.OVERWORLD) UltraSpaceModDimension.sendToBase((ServerPlayerEntity) entity);
        else if (entity.dimension == ModDimensions.DIMENSION_TYPE) UltraSpaceModDimension.sendToExit(
                (ServerPlayerEntity) entity);
    }

    @Override
    public boolean onBlockActivated(final BlockState state, final World world, final BlockPos pos,
            final PlayerEntity entity, final Hand hand, final BlockRayTraceResult hit)
    {
        final DimensionType dim = entity.dimension;
        if (!(entity instanceof ServerPlayerEntity)) return dim == DimensionType.OVERWORLD
                || dim == ModDimensions.DIMENSION_TYPE;
        if (dim == DimensionType.OVERWORLD)
        {
            UltraSpaceModDimension.sendToBase((ServerPlayerEntity) entity);
            return true;
        }
        else if (dim == ModDimensions.DIMENSION_TYPE)
        {
            UltraSpaceModDimension.sendToExit((ServerPlayerEntity) entity);
            return true;
        }
        return false;
    }
}