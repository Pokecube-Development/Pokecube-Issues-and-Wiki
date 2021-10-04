package pokecube.legends.items;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBase extends Item
{
    String  tooltipname;
    boolean hasTooltip = false;
    boolean hasShiny = false;

    //Info
    public ItemBase(final String name, final int num, final CreativeModeTab group)
    {
        super(new Properties().tab(group).stacksTo(num));
        this.hasTooltip = true;
        this.tooltipname = name;
    }
    
    //No Info
    public ItemBase(final int num, final CreativeModeTab group)
    {
        super(new Properties().tab(group).stacksTo(num));
    }
    
    public ItemBase(final String name, final int num, final CreativeModeTab group, final FoodProperties food)
    {
        super(new Properties().tab(group).stacksTo(num).food(food));
        this.tooltipname = name;
        this.hasTooltip = true;
    }
    
    public ItemBase(final int num, final CreativeModeTab group, final FoodProperties food)
    {
        super(new Properties().tab(group).stacksTo(num).food(food));
    }
    
    public ItemBase setShiny() {
    	this.hasShiny = true;
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
}