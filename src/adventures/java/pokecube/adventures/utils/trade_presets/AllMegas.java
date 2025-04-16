package pokecube.adventures.utils.trade_presets;

import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrade;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrades;
import pokecube.adventures.utils.TradeEntryLoader;
import pokecube.adventures.utils.TradeEntryLoader.Trade;
import pokecube.adventures.utils.TradeEntryLoader.TradePreset;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeItems;
import pokecube.core.init.ItemGenerator;

import java.util.Optional;

@TradePresetAn(key = "allMegas")
public class AllMegas implements TradePreset
{

    @Override
    public void apply(final Trade trade, final TrainerTrades trades)
    {
        for (final String s : ItemGenerator.variants)
            // Only mega stones
            if (s.contains("mega") && !s.equals("megastone"))
        {
            final ItemStack sell = PokecubeItems.getStack(s);
            TrainerTrade recipe;
            ItemStack buy1;
            ItemStack buy2 = ItemStack.EMPTY;
            buy1 = Tools.getStack(trade.buys.get(0));
            if (trade.buys.size() > 1)
            {
                buy2 = Tools.getStack(trade.buys.get(1));
            }
            var cost = new ItemCost(buy1.getItemHolder(), buy1.getCount(),
                    DataComponentPredicate.allOf(buy1.getComponents()));
            var _buy2 = Optional.ofNullable(buy2.isEmpty() ? null
                    : new ItemCost(buy1.getItemHolder(), buy1.getCount(),
                            DataComponentPredicate.allOf(buy2.getComponents())));
            recipe = new TrainerTrade(cost, _buy2, sell, trade);
            var values = trade.values;
            if (values.containsKey(TradeEntryLoader.CHANCE))
                recipe.chance = values.get(TradeEntryLoader.CHANCE).getAsFloat();
            trades.tradesList.add(recipe);
        }
    }

}
