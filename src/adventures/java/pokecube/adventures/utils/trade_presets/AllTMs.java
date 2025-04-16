package pokecube.adventures.utils.trade_presets;

import com.google.common.collect.Lists;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrade;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrades;
import pokecube.adventures.utils.TradeEntryLoader;
import pokecube.adventures.utils.TradeEntryLoader.Trade;
import pokecube.adventures.utils.TradeEntryLoader.TradePreset;
import pokecube.api.moves.MoveEntry;
import pokecube.api.utils.Tools;
import pokecube.core.items.ItemTM;
import pokecube.core.moves.MovesUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

@TradePresetAn(key = "allTMs")
public class AllTMs implements TradePreset
{

    @Override
    public void apply(final Trade trade, final TrainerTrades trades)
    {
        final ArrayList<String> moves = Lists.newArrayList(MovesUtils.getKnownMoveNames());
        Collections.sort(moves);
        for (int i = 0; i < moves.size(); i++)
        {
            final int index = i;
            final String name = moves.get(index);

            final MoveEntry move = MovesUtils.getMove(name);
            if (move == null) continue;

            final ItemStack sell = ItemTM.getTM(name);
            // If the move isn't valid for a TM, it ends up empty.
            if (sell.isEmpty()) continue;
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
