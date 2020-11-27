package pokecube.legends.items;

import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.PokecubeLegends;

public class DistortedMirror extends ItemBase
{

    public DistortedMirror(final String name, final int num)
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
	public ActionResult<ItemStack> onItemRightClick(final World world, final PlayerEntity entity, final Hand hand) {
		final ActionResult<ItemStack> ar = super.onItemRightClick(world, entity, hand);
//		final DimensionType dim = entity.dimension;
//		double x = entity.getPosX();
//		double y = entity.getPosY();
//		double z = entity.getPosZ();
//
//		if (dim == DimensionType.OVERWORLD)
//        {
//			if (entity instanceof ServerPlayerEntity) DistortedModDimension.sentToDistorted(
//                    (ServerPlayerEntity) entity);
//
//			if (entity instanceof PlayerEntity)
//				((PlayerEntity) entity).getCooldownTracker().setCooldown(ItemInit.GIRATINA_MIRROR.get(), PokecubeLegends.config.mirrorCooldown);
//
//			world.getWorld().playSound(x, y, z,
//					(net.minecraft.util.SoundEvent) ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.end_gateway.spawn")),
//					SoundCategory.NEUTRAL, (float) 1, (float) 1, false);
//
//            return ar;
//        }
//        else if (dim == ModDimensions.DIMENSION_TYPE_DW)
//        {
//			if (entity instanceof ServerPlayerEntity) DistortedModDimension.sendToOverworld(
//                    (ServerPlayerEntity) entity);
//
//			if (entity instanceof PlayerEntity)
//				((PlayerEntity) entity).getCooldownTracker().setCooldown(ItemInit.GIRATINA_MIRROR.get(), PokecubeLegends.config.mirrorCooldown);
//
//			world.getWorld().playSound(x, y, z,
//					(net.minecraft.util.SoundEvent) ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("block.end_gateway.spawn")),
//					SoundCategory.NEUTRAL, (float) 1, (float) 1, false);
//
//            return ar;
//        }
        return ar;
	}
}
