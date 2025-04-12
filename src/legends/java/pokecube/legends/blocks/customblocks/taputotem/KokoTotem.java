package pokecube.legends.blocks.customblocks.taputotem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.customblocks.TapuKokoCore;
import thut.api.item.ItemList;

public class KokoTotem extends TapuKokoCore{

	public KokoTotem(final Properties props) {
		super(props);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		if (ItemList.is(PokecubeLegends.TOTEM_FUEL_TAG, stack))
		{
			addEffectTotem(player);
			return ItemInteractionResult.SUCCESS;
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
	}
	
	public static void addEffectTotem(final Player entity) 
	{
		if (ItemList.is(PokecubeLegends.TOTEM_FUEL_TAG, entity.getMainHandItem()))
		{
			entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 1));
			final ItemStack _stktoremove = entity.getMainHandItem();
			if (!entity.isCreative()) entity.getInventory().clearOrCountMatchingItems(p -> _stktoremove.getItem() == p.getItem(), 1,
            	entity.inventoryMenu.getCraftSlots());
		}
	}
}
