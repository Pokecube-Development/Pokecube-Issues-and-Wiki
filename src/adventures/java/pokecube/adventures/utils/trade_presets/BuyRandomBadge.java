package pokecube.adventures.utils.trade_presets;

import java.util.Map;
import java.util.Optional;

import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrade;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrades;
import pokecube.adventures.utils.TradeEntryLoader;
import pokecube.adventures.utils.TradeEntryLoader.Trade;
import pokecube.adventures.utils.TradeEntryLoader.TradePreset;
import pokecube.api.utils.PokeType;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeItems;

@TradePresetAn(key = "buyRandomBadge")
public class BuyRandomBadge implements TradePreset
{

    @Override
    public void apply(final Trade trade, final TrainerTrades trades)
    {
        for (final PokeType type : PokeType.values()) if (type != PokeType.unknown)
        {
            final ItemStack buy = PokecubeItems.getStack("pokecube_adventures:badge_" + type);
            if (!buy.isEmpty())
            {
                Map<String, String> values = trade.sell.getValues();
                TrainerTrade recipe;
                final ItemStack sell = Tools.getStack(values);
                var cost = new ItemCost(buy.getItemHolder(), buy.getCount(),
                        DataComponentPredicate.allOf(buy.getComponents()));
                recipe = new TrainerTrade(cost, Optional.empty(), sell, trade);
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

}
