package pokecube.legends.items.tools;

import java.util.List;

import net.minecraft.block.DispenserBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.UseAction;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ZamazentaShieldItem extends ShieldItem {

	String  tooltipname;
    boolean hasTooltip = false;
    
	public ZamazentaShieldItem(String name, Properties properties) {
		super(properties);
		DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
		this.hasTooltip = true;
        this.tooltipname = name;
	}
	
	public String getDescriptionId(ItemStack stack) {
	      return stack.getTagElement("BlockEntityTag") != null ? this.getDescriptionId() + '.' + getColor(stack).getName() : super.getDescriptionId(stack);
   }

   public UseAction getUseAnimation(ItemStack stack) {
      return UseAction.BLOCK;
   }

   public ActionResult<ItemStack> use(World world, PlayerEntity playerIn, Hand hand) {
      ItemStack itemstack = playerIn.getItemInHand(hand);
      playerIn.startUsingItem(hand);
      return ActionResult.consume(itemstack);
   }

   public boolean isValidRepairItem(ItemStack itemStack, ItemStack repair) {
      return ItemTags.STONE_BRICKS.contains(repair.getItem()) || super.isValidRepairItem(itemStack, repair);
   }
   
   @Override
   public boolean isShield(ItemStack stack, LivingEntity entity) {
	   return true;
   }
   
   @Override
   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(final ItemStack stack, final World worldIn, final List<ITextComponent> tooltip,
           final ITooltipFlag flagIn)
   {
       if (!this.hasTooltip) return;
       String message;
       if (Screen.hasShiftDown()) message = I18n.get("legends." + this.tooltipname + ".tooltip", TextFormatting.GOLD, TextFormatting.BOLD, TextFormatting.RESET);
       else message = I18n.get("pokecube.tooltip.advanced");
       tooltip.add(new TranslationTextComponent(message));
   }
}
