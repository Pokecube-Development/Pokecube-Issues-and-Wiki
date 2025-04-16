package pokecube.adventures.utils.trade_presets;

import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.neoforged.neoforge.registries.DeferredItem;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrade;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrades;
import pokecube.adventures.utils.TradeEntryLoader;
import pokecube.adventures.utils.TradeEntryLoader.Trade;
import pokecube.adventures.utils.TradeEntryLoader.TradePreset;
import pokecube.api.utils.Tools;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.berries.ItemBerry;

import java.util.Optional;

@TradePresetAn(key = "allBerryBuy")
public class BuyRandomBerry implements TradePreset
{
    @Override
    public void apply(final Trade trade, final TrainerTrades trades)
    {
        for (DeferredItem<ItemBerry> berry : BerryManager.berryItems.values())
        {
            final ItemStack buy = new ItemStack(berry.get());
            if (!buy.isEmpty())
            {
                if (trade.count > 0) buy.setCount(trade.count);
                TrainerTrade recipe;
                final ItemStack sell = Tools.getStack(trade.sell);
                var cost = new ItemCost(buy.getItemHolder(), buy.getCount(),
                        DataComponentPredicate.allOf(buy.getComponents()));
                recipe = new TrainerTrade(cost, Optional.empty(), sell, trade);
                var values = trade.values;
                if (values.containsKey(TradeEntryLoader.CHANCE))
                    recipe.chance = values.get(TradeEntryLoader.CHANCE).getAsFloat();
                trades.tradesList.add(recipe);
            }
        }
    }

}
