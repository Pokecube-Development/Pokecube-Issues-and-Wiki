package pokecube.legends.blocks;

import java.util.Collections;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.common.PlantType;

public class MushroomBase extends BushBlock
{
    public static final Block block = null;

    public MushroomBase(final BlockBehaviour.Properties properties)
    {
        super(properties);
    }

    @Override
	public boolean canBeReplaced(final BlockState state, final BlockPlaceContext useContext) {
		return true;
	}

    @SuppressWarnings("deprecation")
	@Override
	public List<ItemStack> getDrops(final BlockState state, final LootContext.Builder builder) {
		final List<ItemStack> dropsOriginal = super.getDrops(state, builder);
		if (!dropsOriginal.isEmpty())
			return dropsOriginal;
		return Collections.singletonList(new ItemStack(this, 1));
	}

    @Override
	public int getFlammability(final BlockState state, final BlockGetter world, final BlockPos pos, final Direction face) {
		return 2;
	}

    @Override
    public PlantType getPlantType(final BlockGetter world, final BlockPos pos)
    {
    	return PlantType.PLAINS;
    }
}
