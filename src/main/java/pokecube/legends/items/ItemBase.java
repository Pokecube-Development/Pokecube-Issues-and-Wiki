package pokecube.legends.items;

import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import pokecube.core.PokecubeItems;
import pokecube.legends.Reference;
import pokecube.legends.init.ItemInit;

public class ItemBase extends Item
{
    String tooltipname;

    public ItemBase(final String name, final int num)
    {
        super(new Properties().group(PokecubeItems.POKECUBEITEMS).maxStackSize(num));
        this.setRegistryName(Reference.ID, name);
        this.setTooltipName(name);
        ItemInit.ITEMS.add(this);
    }

    public ItemBase setTooltipName(final String tooltipname)
    {
        this.tooltipname = tooltipname;
        return this;
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
}