package pokecube.legends.items;

import java.util.List;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.lib.TComponent;

public class LegendsSword extends SwordItem
{
	String tooltip_id;
	boolean hasTooltip = true;
	boolean hasShiny = false;
    int tooltipLineAmt = 0;

    public LegendsSword(final Tier material, final int bonusDamage, final float attackSpeed, final Properties properties, final CreativeModeTab group)
    {
        super(material, bonusDamage, attackSpeed, properties.tab(group));
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
    public void appendHoverText(final ItemStack stack, final Level worldIn, final List<Component> tooltip,
            final TooltipFlag flagIn)
    {
        if (!this.hasTooltip) return;
        if (Screen.hasShiftDown())
        {
            tooltip.add(TComponent.translatable("legends." + this.tooltip_id + ".tooltip"));
            for (int lineAmt = 1; lineAmt <= tooltipLineAmt;)
            {
                tooltip.add(TComponent.translatable("legends." + this.tooltip_id + ".tooltip.line" + lineAmt));
                lineAmt++;
            }
        }
        else tooltip.add(TComponent.translatable("pokecube.tooltip.advanced"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isFoil(final ItemStack itemstack)
    {
        return this.hasShiny;
    }
}
