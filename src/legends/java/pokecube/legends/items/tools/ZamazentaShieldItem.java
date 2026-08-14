package pokecube.legends.items.tools;

import java.util.List;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ZamazentaShieldItem extends ShieldItem {

    public final Tier tier;
	String tooltip_id;
    boolean hasTooltip = false;
    int tooltipLineAmt = 0;

	public ZamazentaShieldItem(final Tier material, final String name, final Properties properties) {
		super(properties);
		DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
        this.tier = material;
		this.hasTooltip = true;
        this.tooltip_id = name;
	}

    public ZamazentaShieldItem setTooltipExtraLine(final int tooltipExtraLineAmt)
    {
        this.tooltipLineAmt = tooltipExtraLineAmt;
        return this;
    }

	@Override
    public String getDescriptionId(final ItemStack stack) {
        DyeColor dyecolor = stack.get(DataComponents.BASE_COLOR);
        return dyecolor != null ? this.getDescriptionId() + "." + dyecolor.getName() : super.getDescriptionId(stack);
	}

	@Override
    public UseAnim getUseAnimation(final ItemStack stack) {
      return UseAnim.BLOCK;
   }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level world, final Player playerIn, final InteractionHand hand) {
        final ItemStack itemstack = playerIn.getItemInHand(hand);
        playerIn.startUsingItem(hand);
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public boolean isValidRepairItem(final ItemStack item, final ItemStack repair) {
        return this.tier.getRepairIngredient().test(repair) || super.isValidRepairItem(item, repair);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, net.neoforged.neoforge.common.ItemAbility itemAbility) {
        return net.neoforged.neoforge.common.ItemAbilities.DEFAULT_SHIELD_ACTIONS.contains(itemAbility);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
            TooltipFlag tooltipFlag)
    {
        if (!this.hasTooltip) return;
        if (Screen.hasShiftDown())
        {
            tooltipComponents.add(Component.translatable("legends." + this.tooltip_id + ".tooltip"));
            for (int lineAmt = 1; lineAmt <= tooltipLineAmt;)
            {
                tooltipComponents.add(Component.translatable("legends." + this.tooltip_id + ".tooltip.line" + lineAmt));
                lineAmt++;
            }
        }
        else tooltipComponents.add(Component.translatable("pokecube.tooltip.advanced"));
    }
}
