package pokecube.legends.blocks;

import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.common.PlantType;

public class PlantBase extends FlowerBlock
{
    public static final Block block = null;

    public PlantBase(final Material material, final float hardness, final float resistance, final SoundType sound)
    {
        super(Effects.SATURATION, 0, Block.Properties.create(material).hardnessAndResistance(hardness, resistance)
                .doesNotBlockMovement().sound(sound).lightValue(2));
    }
    
    @Override
	public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
		return true;
	}
    
    @SuppressWarnings("deprecation")
	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		List<ItemStack> dropsOriginal = super.getDrops(state, builder);
		if (!dropsOriginal.isEmpty())
			return dropsOriginal;
		return Collections.singletonList(new ItemStack(this, 1));
	}
    
    @Override
	public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		return 2;
	}

    @Override
    public PlantType getPlantType(final IBlockReader world, final BlockPos pos)
    {
        return PlantType.Plains;
    }
}
