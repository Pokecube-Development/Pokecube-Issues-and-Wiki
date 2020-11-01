package pokecube.legends.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.init.ItemInit;
import pokecube.legends.worldgen.dimension.ModDimensions;
import pokecube.legends.worldgen.dimension.UltraSpaceModDimension;

public class UltraKey extends ItemBase {

	public UltraKey(final String name, final int num)
    {
        super(name, num, PokecubeLegends.TAB);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(final ItemStack stack, final World worldIn, final List<ITextComponent> tooltip,
            final ITooltipFlag flagIn)
    {
        String message;
        if (Screen.hasShiftDown()) message = I18n.format("legends." + this.tooltipname + ".tooltip");
        else message = I18n.format("pokecube.tooltip.advanced");
        tooltip.add(new TranslationTextComponent(message));
    }
    
    @Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity entity, Hand hand) {
		ActionResult<ItemStack> ar = super.onItemRightClick(world, entity, hand);
		ItemStack itemstack = ar.getResult();
		double x = entity.getPosX();
		double y = entity.getPosY();
		double z = entity.getPosZ();
		{
			Map<String, Object> $_dependencies = new HashMap<>();
			$_dependencies.put("entity", entity);
			$_dependencies.put("itemstack", itemstack);
			$_dependencies.put("x", x);
			$_dependencies.put("y", y);
			$_dependencies.put("z", z);
			$_dependencies.put("world", world);
			dimensionTP($_dependencies);
		}
        return ar;
	}
    
    public static void dimensionTP(Map<String, Object> dependencies) {
		Entity entity = (Entity) dependencies.get("entity");
		double x = dependencies.get("x") instanceof Integer ? (int) dependencies.get("x") : (double) dependencies.get("x");
		double y = dependencies.get("y") instanceof Integer ? (int) dependencies.get("y") : (double) dependencies.get("y");
		double z = dependencies.get("z") instanceof Integer ? (int) dependencies.get("z") : (double) dependencies.get("z");
		IWorld world = (IWorld) dependencies.get("world");
		final DimensionType dim = entity.dimension;
		
		//Comsume Item Enabled
		if(PokecubeLegends.config.enabledkeyusecombustible == true) {
			if (dim == DimensionType.OVERWORLD) {
				if ((((entity instanceof ServerPlayerEntity) ? ((PlayerEntity) entity).inventory.hasItemStack(
					new ItemStack(ItemInit.COSMIC_DUST.get())) : true) && ((PlayerEntity) entity).inventory.count(ItemInit.COSMIC_DUST.get()) >= 5)) {
					
					world.getWorld().playSound(x, y, z,
							(net.minecraft.util.SoundEvent) ForgeRegistries.SOUND_EVENTS
									.getValue(new ResourceLocation("block.end_portal_frame.fill")),
							SoundCategory.NEUTRAL, (float) 1, (float) 1, false);
					
					if (entity instanceof PlayerEntity)
					((PlayerEntity) entity).inventory.clearMatchingItems(p -> new ItemStack(ItemInit.COSMIC_DUST.get(), 1).getItem() == p.getItem(), PokecubeLegends.config.itemCombustiveStack);
					
					if (entity instanceof ServerPlayerEntity) UltraSpaceModDimension.sentToUltraspace(
		                    (ServerPlayerEntity) entity);
					
					if (entity instanceof PlayerEntity)
						((PlayerEntity) entity).getCooldownTracker().setCooldown(ItemInit.ULTRAKEY.get(), 200);
				}
				else {
					world.getWorld().playSound(x, y, z,
							(net.minecraft.util.SoundEvent) ForgeRegistries.SOUND_EVENTS
									.getValue(new ResourceLocation("block.shulker_box.close")),
							SoundCategory.NEUTRAL, (float) 1, (float) 1, false);
				}
			}
			else if (dim == ModDimensions.DIMENSION_TYPE_US)
	        {
				if ((((entity instanceof ServerPlayerEntity) ? ((PlayerEntity) entity).inventory.hasItemStack(
					new ItemStack(ItemInit.COSMIC_DUST.get(), 1)) : true) && ((PlayerEntity) entity).inventory.count(ItemInit.COSMIC_DUST.get()) >= 5)) {
					
					world.getWorld().playSound(x, y, z,
							(net.minecraft.util.SoundEvent) ForgeRegistries.SOUND_EVENTS
									.getValue(new ResourceLocation("block.end_portal_frame.fill")),
							SoundCategory.NEUTRAL, (float) 1, (float) 1, false);
					
					if (entity instanceof PlayerEntity)
						((PlayerEntity) entity).inventory
								.clearMatchingItems(p -> new ItemStack(ItemInit.COSMIC_DUST.get(), 1).getItem() == p.getItem(), PokecubeLegends.config.itemCombustiveStack);
					
					if (entity instanceof ServerPlayerEntity) UltraSpaceModDimension.sendToOverworld(
		                    (ServerPlayerEntity) entity);
					
					if (entity instanceof PlayerEntity)
						((PlayerEntity) entity).getCooldownTracker().setCooldown(ItemInit.ULTRAKEY.get(), 200);
				}
				else {
					world.getWorld().playSound(x, y, z,
							(net.minecraft.util.SoundEvent) ForgeRegistries.SOUND_EVENTS
									.getValue(new ResourceLocation("block.shulker_box.close")),
							SoundCategory.NEUTRAL, (float) 1, (float) 1, false);
				}
	        }
		}
		
		//Comsume Item Disable
		if(PokecubeLegends.config.enabledkeyusecombustible == false) {
			if (dim == DimensionType.OVERWORLD) {			
					world.getWorld().playSound(x, y, z,
							(net.minecraft.util.SoundEvent) ForgeRegistries.SOUND_EVENTS
									.getValue(new ResourceLocation("block.end_portal_frame.fill")),
							SoundCategory.NEUTRAL, (float) 1, (float) 1, false);

					if (entity instanceof ServerPlayerEntity) UltraSpaceModDimension.sentToUltraspace(
		                    (ServerPlayerEntity) entity);
					
					if (entity instanceof PlayerEntity)
						((PlayerEntity) entity).getCooldownTracker().setCooldown(ItemInit.ULTRAKEY.get(), 200);
			}
			else if (dim == ModDimensions.DIMENSION_TYPE_US)
	        {
					world.getWorld().playSound(x, y, z,
							(net.minecraft.util.SoundEvent) ForgeRegistries.SOUND_EVENTS
									.getValue(new ResourceLocation("block.end_portal_frame.fill")),
							SoundCategory.NEUTRAL, (float) 1, (float) 1, false);
					
					if (entity instanceof ServerPlayerEntity) UltraSpaceModDimension.sendToOverworld(
		                    (ServerPlayerEntity) entity);
					
					if (entity instanceof PlayerEntity)
						((PlayerEntity) entity).getCooldownTracker().setCooldown(ItemInit.ULTRAKEY.get(), 200);
    		}
		}
    }
}