package pokecube.adventures.utils.trade_presets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Lists;

import net.minecraft.world.item.ItemStack;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrade;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrades;
import pokecube.adventures.utils.TradeEntryLoader;
import pokecube.adventures.utils.TradeEntryLoader.Trade;
import pokecube.adventures.utils.TradeEntryLoader.TradePreset;
import pokecube.api.moves.MoveEntry;
import pokecube.api.utils.Tools;
import pokecube.core.items.ItemTM;
import pokecube.core.moves.MovesUtils;

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
            recipe = new TrainerTrade(buy1, buy2, sell, trade);
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
