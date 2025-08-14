package pokecube.adventures.utils.trade_presets;

import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;

public class TradeHelper
{
    public static ItemCost getCost(ItemStack buy){
        var base = new ItemStack((buy.getItem()));
        base.setCount(buy.getCount());
        if(ItemStack.isSameItemSameComponents(buy, base))
            return new ItemCost(buy.getItem(), buy.getCount());
        return new ItemCost(buy.getItemHolder(), buy.getCount(),
                DataComponentPredicate.allOf(buy.getComponents()));
    }
}
