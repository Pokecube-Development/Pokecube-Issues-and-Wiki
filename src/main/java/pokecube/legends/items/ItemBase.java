package pokecube.legends.items;

import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBase extends Item
{
    String  tooltipname;
    boolean hasTooltip = false;
    boolean hasShiny = false;

    //Info
    public ItemBase(final String name, final int num, final ItemGroup group)
    {
        super(new Properties().tab(group).stacksTo(num));
        this.hasTooltip = true;
        this.tooltipname = name;
    }
    
    //No Info
    public ItemBase(final int num, final ItemGroup group)
    {
        super(new Properties().tab(group).stacksTo(num));
    }
    
    public ItemBase(final String name, final int num, final ItemGroup group, final Food food)
    {
        super(new Properties().tab(group).stacksTo(num).food(food));
        this.tooltipname = name;
        this.hasTooltip = true;
    }
    
    public ItemBase(final int num, final ItemGroup group, final Food food)
    {
        super(new Properties().tab(group).stacksTo(num).food(food));
    }
    
    public ItemBase setShiny() {
    	this.hasShiny = true;
    	return this;
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