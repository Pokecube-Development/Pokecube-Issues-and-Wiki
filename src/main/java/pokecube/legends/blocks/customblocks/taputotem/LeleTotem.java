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
import pokecube.legends.blocks.customblocks.TapuLeleCore;
import thut.api.item.ItemList;

public class LeleTotem extends TapuLeleCore{

	public LeleTotem(String name, Properties props) {
		super(name, props);
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState stack, World world, BlockPos pos, PlayerEntity entity, Hand hand,
			BlockRayTraceResult hit) {
		{
			addEffectTotem(entity);
		}
		return ActionResultType.SUCCESS;
	}
	
	public static void addEffectTotem(PlayerEntity entity) 
	{
		if (ItemList.is(PokecubeLegends.FUELTAG, entity.getHeldItemMainhand())) 
		{
			entity.addPotionEffect(new EffectInstance(Effects.HEALTH_BOOST, 400, 1));
			ItemStack _stktoremove = ((entity instanceof LivingEntity) ? ((LivingEntity) entity).getHeldItemMainhand() : ItemStack.EMPTY);
			entity.inventory.func_234564_a_(p -> _stktoremove.getItem() == p.getItem(), 1,
					entity.container.func_234641_j_());
		}
	}
}
