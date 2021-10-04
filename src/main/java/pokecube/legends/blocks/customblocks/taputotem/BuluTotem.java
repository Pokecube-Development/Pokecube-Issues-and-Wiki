package pokecube.legends.blocks.customblocks.taputotem;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.customblocks.TapuBuluCore;
import thut.api.item.ItemList;

public class BuluTotem extends TapuBuluCore{

	public BuluTotem(final Properties props) {
		super(props);
	}
	
	@Override
	public InteractionResult use(final BlockState stack, final Level world, final BlockPos pos, final Player entity, final InteractionHand hand,
			final BlockHitResult hit)
	{
		if (ItemList.is(PokecubeLegends.FUELTAG, entity.getMainHandItem()))
		{
			BuluTotem.addEffectTotem(entity);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}
	
	public static void addEffectTotem(final Player entity)
	{
		if (ItemList.is(PokecubeLegends.FUELTAG, entity.getMainHandItem()))
		{
			entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1));
			final ItemStack _stktoremove = entity instanceof LivingEntity ? ((LivingEntity) entity).getMainHandItem() : ItemStack.EMPTY;
			if (!entity.isCreative()) entity.getInventory().clearOrCountMatchingItems(p -> _stktoremove.getItem() == p.getItem(), 1,
            	entity.inventoryMenu.getCraftSlots());
		}
	}
}
