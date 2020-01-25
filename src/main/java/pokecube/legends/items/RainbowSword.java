package pokecube.legends.items;

import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.legends.items.tools.ToolSword;

public class RainbowSword extends ToolSword
{

    public RainbowSword(final String name, final int maxDamage, final int attackDamage, final IItemTier material)
    {
        super(name, maxDamage, attackDamage, material);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(final ItemStack stack, final World worldIn, final List<ITextComponent> tooltip,
            final ITooltipFlag flagIn)
    {
        String message;
        if (Screen.hasShiftDown()) message = I18n.format("legends.rainsword.tooltip");
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
