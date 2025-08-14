package pokecube.adventures.utils.trade_presets;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrade;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrades;
import pokecube.adventures.utils.TradeEntryLoader;
import pokecube.adventures.utils.TradeEntryLoader.Trade;
import pokecube.adventures.utils.TradeEntryLoader.TradePreset;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeItems;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.items.vitamins.ItemVitamin;

import java.util.Optional;

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
            TrainerTrade recipe;
            ItemStack buy1;
            ItemStack buy2 = ItemStack.EMPTY;
            buy1 = Tools.getStack(trade.buys.get(0));
            if (trade.buys.size() > 1)
            {
                buy2 = Tools.getStack(trade.buys.get(1));
            }
            var cost = TradeHelper.getCost(buy1);
            var _buy2 = Optional.ofNullable(buy2.isEmpty()
                    ? null
                    : TradeHelper.getCost(buy2));
            recipe = new TrainerTrade(cost, _buy2, sell, trade);
            var values = trade.values;
            if (values.containsKey(TradeEntryLoader.CHANCE))
                recipe.chance = values.get(TradeEntryLoader.CHANCE).getAsFloat();
            trades.tradesList.add(recipe);
        }
    }

}
