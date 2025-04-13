package pokecube.legends.items;

import java.util.List;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import thut.lib.TComponent;

public class LegendsSword extends SwordItem
{
	String tooltip_id;
	boolean hasTooltip = true;
	boolean hasShiny = false;
    Item isRepairItem;
    int tooltipLineAmt = 0;

    public LegendsSword(final Tier material, final Properties properties)
    {
        super(material, properties);
    }

    public LegendsSword setTooltipName(final String tooltipname)
    {
        this.tooltip_id = tooltipname;
        return this;
    }

    public LegendsSword setTooltipExtraLine(final int tooltipExtraLineAmt)
    {
        this.tooltipLineAmt = tooltipExtraLineAmt;
        return this;
    }

    public LegendsSword setShiny(){
    	this.hasShiny = true;
    	return this;
    }

    public LegendsSword noTooltop()
    {
        this.hasTooltip = false;
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
            TooltipFlag tooltipFlag)
    {
        if (!this.hasTooltip) return;
        if (Screen.hasShiftDown())
        {
            tooltipComponents.add(TComponent.translatable("legends." + this.tooltip_id + ".tooltip"));
            for (int lineAmt = 1; lineAmt <= tooltipLineAmt;)
            {
                tooltipComponents.add(TComponent.translatable("legends." + this.tooltip_id + ".tooltip.line" + lineAmt));
                lineAmt++;
            }
        }
        else tooltipComponents.add(TComponent.translatable("pokecube.tooltip.advanced"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isFoil(final ItemStack itemstack)
    {
        return this.hasShiny;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairItem) {
        return repairItem.is(isRepairItem) || super.isValidRepairItem(stack, repairItem);
    }
}
