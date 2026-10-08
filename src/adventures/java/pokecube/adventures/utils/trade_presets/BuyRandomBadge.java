package pokecube.adventures.utils.trade_presets;

import net.minecraft.world.item.ItemStack;
import pokecube.api.data.trainers.TradeData.*;
import pokecube.api.data.trainers.TypeTrainer.*;
import pokecube.adventures.utils.TradeEntryLoader;
import pokecube.adventures.utils.TradeEntryLoader.TradePreset;
import pokecube.api.utils.PokeType;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeItems;

import java.util.Optional;

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
                TrainerTrade recipe;
                final ItemStack sell = Tools.getStack(trade.sell);
                var cost = TradeHelper.getCost(buy);
                recipe = new TrainerTrade(cost, Optional.empty(), sell, trade);
                var values = trade.values;
                if (values.containsKey(TradeEntryLoader.CHANCE))
                    recipe.chance = values.get(TradeEntryLoader.CHANCE).getAsFloat();
                trades.tradesList.add(recipe);
            }
        }
    }

}
