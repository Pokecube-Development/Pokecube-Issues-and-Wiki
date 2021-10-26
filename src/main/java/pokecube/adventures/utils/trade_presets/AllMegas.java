package pokecube.adventures.utils.trade_presets;

import java.util.Map;

import javax.xml.namespace.QName;

import net.minecraft.world.item.ItemStack;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrade;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrades;
import pokecube.adventures.utils.TradeEntryLoader;
import pokecube.adventures.utils.TradeEntryLoader.Trade;
import pokecube.adventures.utils.TradeEntryLoader.TradePreset;
import pokecube.core.PokecubeItems;
import pokecube.core.handlers.ItemGenerator;
import pokecube.core.utils.Tools;

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
                Map<QName, String> values;
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
                recipe = new TrainerTrade(buy1, buy2, sell);
                values = trade.values;
                if (values.containsKey(TradeEntryLoader.CHANCE)) recipe.chance = Float.parseFloat(values.get(
                        TradeEntryLoader.CHANCE));
                if (values.containsKey(TradeEntryLoader.MIN)) recipe.min = Integer.parseInt(values.get(
                        TradeEntryLoader.MIN));
                if (values.containsKey(TradeEntryLoader.MAX)) recipe.max = Integer.parseInt(values.get(
                        TradeEntryLoader.MAX));
                trades.tradesList.add(recipe);
            }
    }

}
