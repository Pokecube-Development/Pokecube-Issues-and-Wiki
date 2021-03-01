package pokecube.legends.items;

import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.items.tools.ToolSword;

public class KeldeoSword extends ToolSword
{
	String  tooltipname;
	boolean hasTooltip = true;
	    
    public KeldeoSword(final int bonusDamage, final int attackSpeed, final IItemTier material, final String name, final ItemGroup group)
    {
        super(bonusDamage, attackSpeed, material);
        this.setTooltipName(name);
    }
    
    public KeldeoSword setTooltipName(final String tooltipname)
    {
        this.tooltipname = tooltipname;
        return this;
    }

    public KeldeoSword noTooltop()
    {
        this.hasTooltip = false;
        return this;
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(final ItemStack stack, final World worldIn, final List<ITextComponent> tooltip,
            final ITooltipFlag flagIn)
    {
        if (!this.hasTooltip) return;
        String message;
        if (Screen.hasShiftDown()) message = I18n.format("legends." + this.tooltipname + ".tooltip", TextFormatting.GOLD, TextFormatting.BOLD, TextFormatting.RESET);
        else message = I18n.format("pokecube.tooltip.advanced");
        tooltip.add(new TranslationTextComponent(message));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean hasEffect(final ItemStack itemstack)
    {
        return true;
    }
}
