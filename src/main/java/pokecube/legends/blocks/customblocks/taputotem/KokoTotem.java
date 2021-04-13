package pokecube.legends.blocks.customblocks.taputotem;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.customblocks.TapuKokoCore;
import thut.api.item.ItemList;

public class KokoTotem extends TapuKokoCore{

	public KokoTotem(Properties props) {
		super(props);
	}
	
	@Override
	public ActionResultType use(BlockState stack, World world, BlockPos pos, PlayerEntity entity, Hand hand,
			BlockRayTraceResult hit)
	{
		if (ItemList.is(PokecubeLegends.FUELTAG, entity.getMainHandItem()))
		{
			addEffectTotem(entity);
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}
	
	public static void addEffectTotem(PlayerEntity entity) 
	{
		if (ItemList.is(PokecubeLegends.FUELTAG, entity.getMainHandItem())) 
		{
			entity.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED, 400, 1));
			ItemStack _stktoremove = ((entity instanceof LivingEntity) ? ((LivingEntity) entity).getMainHandItem() : ItemStack.EMPTY);
			if (!entity.isCreative())
			{
				entity.inventory.clearOrCountMatchingItems(p -> _stktoremove.getItem() == p.getItem(), 1,
					entity.inventoryMenu.getCraftSlots());
			}
		}
	}
}
