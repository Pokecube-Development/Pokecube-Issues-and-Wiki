package pokecube.adventures.utils.trade_presets;

import java.util.Map;
import java.util.Optional;

import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrade;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrades;
import pokecube.adventures.utils.TradeEntryLoader;
import pokecube.adventures.utils.TradeEntryLoader.Trade;
import pokecube.adventures.utils.TradeEntryLoader.TradePreset;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeItems;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.items.vitamins.ItemVitamin;

@TradePresetAn(key = "allVitamins")
public class AllVitamins implements TradePreset
{

    @Override
    public void apply(final Trade trade, final TrainerTrades trades)
    {
        for (final String s : ItemVitamin.vitamins)
        {
            ResourceLocation key = ResourceLocation.fromNamespaceAndPath(PokecubeMod.ID, "vitamin_" + s);
            final ItemStack sell = PokecubeItems.getStack(key);
            if (trade.count > 0) sell.setCount(trade.count);
            Map<String, String> values;
            TrainerTrade recipe;
            ItemStack buy1 = ItemStack.EMPTY;
            ItemStack buy2 = ItemStack.EMPTY;
            values = trade.buys.get(0).getValues();
            buy1 = Tools.getStack(values);
            if (trade.buys.size() > 1)
            {
                values = trade.buys.get(1).getValues();
                buy2 = Tools.getStack(values);
            }
            var cost = new ItemCost(buy1.getItemHolder(), buy1.getCount(),
                    DataComponentPredicate.allOf(buy1.getComponents()));
            var _buy2 = Optional.ofNullable(buy2.isEmpty() ? null
                    : new ItemCost(buy1.getItemHolder(), buy1.getCount(),
                            DataComponentPredicate.allOf(buy2.getComponents())));
            recipe = new TrainerTrade(cost, _buy2, sell, trade);
            values = trade.values;
            if (values.containsKey(TradeEntryLoader.CHANCE))
                recipe.chance = Float.parseFloat(values.get(TradeEntryLoader.CHANCE));
            if (values.containsKey(TradeEntryLoader.MIN))
                recipe.min = Integer.parseInt(values.get(TradeEntryLoader.MIN));
            if (values.containsKey(TradeEntryLoader.MAX))
                recipe.max = Integer.parseInt(values.get(TradeEntryLoader.MAX));
            trades.tradesList.add(recipe);
        }
    }

}
