package pokecube.adventures.utils.trade_presets;

import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrade;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrades;
import pokecube.adventures.utils.TradeEntryLoader;
import pokecube.adventures.utils.TradeEntryLoader.Trade;
import pokecube.adventures.utils.TradeEntryLoader.TradePreset;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import thut.api.attachments.CopyMob;

import java.util.Map;
import java.util.Optional;

@TradePresetAn(key = "sellRandomStatue")
public class SellRandomStatue implements TradePreset
{
    private void addTrade(final ItemStack statue, final Trade trade, final TrainerTrades trades)
    {
        Map<String, String> values;
        TrainerTrade recipe;
        ItemStack buy1;
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
        var _buy2 = Optional.ofNullable(buy2.isEmpty()
                ? null
                : new ItemCost(buy1.getItemHolder(), buy1.getCount(),
                        DataComponentPredicate.allOf(buy2.getComponents())));
        recipe = new TrainerTrade(cost, _buy2, statue, trade);
        values = trade.values;
        if (values.containsKey(TradeEntryLoader.CHANCE))
            recipe.chance = Float.parseFloat(values.get(TradeEntryLoader.CHANCE));
        if (values.containsKey(TradeEntryLoader.MIN)) recipe.min = Integer.parseInt(values.get(TradeEntryLoader.MIN));
        if (values.containsKey(TradeEntryLoader.MAX)) recipe.max = Integer.parseInt(values.get(TradeEntryLoader.MAX));
        trades.tradesList.add(recipe);
    }

    @Override
    public void apply(final Trade trade, final TrainerTrades trades)
    {
        for (final PokedexEntry e : Database.getSortedFormes())
        {
            ItemStack statue = new ItemStack(PokecubeAdv.STATUE.get());
            try
            {
                statue.set(CopyMob.COPY_STORE, CopyMob.CopyInfo.copyOf(e.getEntityType()));
                addTrade(statue, trade, trades);
            }
            catch (Exception ex)
            {
                PokecubeAPI.LOGGER.error("Error creating statue for {}", e, ex);
            }
        }
    }

}
