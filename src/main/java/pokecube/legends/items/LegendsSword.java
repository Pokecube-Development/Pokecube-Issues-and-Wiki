package pokecube.legends.items;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LegendsSword extends SwordItem
{
	String  tooltipname;
	boolean hasTooltip = true;
	boolean hasShiny = false;

    public LegendsSword(final Tier material, final int bonusDamage, final float attackSpeed, final Properties properties, final CreativeModeTab group)
    {
        super(material, bonusDamage, attackSpeed, properties.tab(group));
    }

    public LegendsSword setTooltipName(final String tooltipname)
    {
        this.tooltipname = tooltipname;
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
        String message;
        if (Screen.hasShiftDown()) message = I18n.get("legends." + this.tooltipname + ".tooltip", ChatFormatting.GOLD, ChatFormatting.BOLD, ChatFormatting.RESET);
        else message = I18n.get("pokecube.tooltip.advanced");
        tooltip.add(new TranslatableComponent(message));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isFoil(final ItemStack itemstack)
    {
        return this.hasShiny;
    }
}
