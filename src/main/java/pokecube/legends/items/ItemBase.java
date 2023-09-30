package pokecube.legends.items;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import thut.lib.TComponent;

public class ItemBase extends Item
{
    String tooltip_block_id;
    boolean hasTooltip = false;
    boolean hasShiny = false;
    int tooltipLineAmt = 0;

    // Info
    public ItemBase(final String tooltipName, final CreativeModeTab tab, final int maxStackSize)
    {
        super(new Properties().tab(tab).stacksTo(maxStackSize));
        this.hasTooltip = true;
        this.tooltip_block_id = tooltipName;
    }

    public ItemBase(final String tooltipName, final int tooltipExtraLineAmt, final CreativeModeTab tab, final int maxStackSize)
    {
        super(new Properties().tab(tab).stacksTo(maxStackSize));
        this.hasTooltip = true;
        this.tooltip_block_id = tooltipName;
        this.tooltipLineAmt = tooltipExtraLineAmt;
    }

    public ItemBase(final String tooltipName, final CreativeModeTab tab, final Rarity rarity, final FoodProperties food,
                    final int maxStackSize)
    {
        super(new Properties().tab(tab).stacksTo(maxStackSize).rarity(rarity).food(food));
        this.tooltip_block_id = tooltipName;
        this.hasTooltip = true;
    }

    public ItemBase(final String tooltipName, final int tooltipExtraLineAmt, final CreativeModeTab tab, final Rarity rarity, final FoodProperties food,
                    final int maxStackSize)
    {
        super(new Properties().tab(tab).stacksTo(maxStackSize).rarity(rarity).food(food));
        this.tooltip_block_id = tooltipName;
        this.hasTooltip = true;
        this.tooltipLineAmt = tooltipExtraLineAmt;
    }

    // No Info
    public ItemBase(final CreativeModeTab tab, final int maxStackSize)
    {
        super(new Properties().tab(tab).stacksTo(maxStackSize));
    }

    public ItemBase(final CreativeModeTab tab, final int maxStackSize, final FoodProperties food)
    {
        super(new Properties().tab(tab).stacksTo(maxStackSize).food(food));
    }

    public ItemBase setShiny()
    {
        this.hasShiny = true;
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isFoil(final ItemStack itemstack)
    {
        if (!hasShiny) return super.isFoil(itemstack);
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(final ItemStack stack, final Level worldIn, final List<Component> tooltip,
            final TooltipFlag flagIn)
    {
        if (!this.hasTooltip) return;
        if (Screen.hasShiftDown())
        {
            tooltip.add(TComponent.translatable("legends." + this.tooltip_block_id + ".tooltip"));
            for (int lineAmt = 1; lineAmt <= tooltipLineAmt;)
            {
                tooltip.add(TComponent.translatable("legends." + this.tooltip_block_id + ".tooltip.line" + lineAmt));
                lineAmt++;
            }
        }
        else tooltip.add(TComponent.translatable("pokecube.tooltip.advanced"));
    }
}