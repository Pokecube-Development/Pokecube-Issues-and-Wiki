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
import pokecube.core.database.Database;
import thut.api.attachments.CopyMob;

import java.util.Optional;

@TradePresetAn(key = "sellRandomStatue")
public class SellRandomStatue implements TradePreset
{
    private void addTrade(final ItemStack statue, final Trade trade, final TrainerTrades trades)
    {
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
        var _buy2 = Optional.ofNullable(buy2.isEmpty()
                ? null
                : new ItemCost(buy1.getItemHolder(), buy1.getCount(),
                        DataComponentPredicate.allOf(buy2.getComponents())));
        recipe = new TrainerTrade(cost, _buy2, statue, trade);
        var values = trade.values;
        if (values.containsKey(TradeEntryLoader.CHANCE)) recipe.chance = values.get(TradeEntryLoader.CHANCE).getAsFloat();
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
                var info = CopyMob.CopyInfo.copyOf(e.getEntityType());
                var tag = info.tag();
                tag.putString("statue:over_tex", "minecraft:stone");
                info = new CopyMob.CopyInfo(tag);
                statue.set(CopyMob.COPY_STORE, info);
                addTrade(statue, trade, trades);
            }
            catch (Exception ex)
            {
                PokecubeAPI.LOGGER.error("Error creating statue for {}", e, ex);
            }
        }
    }

}
