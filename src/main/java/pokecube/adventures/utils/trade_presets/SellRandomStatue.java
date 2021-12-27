package pokecube.adventures.utils.trade_presets;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrade;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrades;
import pokecube.adventures.utils.TradeEntryLoader;
import pokecube.adventures.utils.TradeEntryLoader.Trade;
import pokecube.adventures.utils.TradeEntryLoader.TradePreset;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob.FormeHolder;
import pokecube.core.utils.Tools;

@TradePresetAn(key = "sellRandomStatue")
public class SellRandomStatue implements TradePreset
{
    private void addTrade(final ItemStack statue, final Trade trade, final TrainerTrades trades)
    {
        Map<String, String> values;
        TrainerTrade recipe;
        final ItemStack sell = statue;
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
        if (values.containsKey(TradeEntryLoader.MIN)) recipe.min = Integer.parseInt(values.get(TradeEntryLoader.MIN));
        if (values.containsKey(TradeEntryLoader.MAX)) recipe.max = Integer.parseInt(values.get(TradeEntryLoader.MAX));
        trades.tradesList.add(recipe);
    }

    @Override
    public void apply(final Trade trade, final TrainerTrades trades)
    {
        for (final PokedexEntry e : Database.getSortedFormes())
        {
            List<FormeHolder> variants = Database.customModels.getOrDefault(e, Collections.emptyList());
            if (variants.isEmpty())
            {
                ItemStack statue = new ItemStack(PokecubeAdv.STATUE.get());
                CompoundTag modelTag = new CompoundTag();

                modelTag.putString("id", e.getEntityType().getRegistryName().toString());
                modelTag.putString("over_tex", "minecraft:textures/block/stone.png");
                statue.getOrCreateTagElement("BlockEntityTag").put("custom_model", modelTag);

                addTrade(statue, trade, trades);
            }
            else
            {
                ItemStack statue = new ItemStack(PokecubeAdv.STATUE.get());
                CompoundTag modelTag = new CompoundTag();

                for (FormeHolder variant : variants)
                {
                    modelTag.putString("variant", variant.key.toString());
                }

                modelTag.putString("id", e.getEntityType().getRegistryName().toString());
                modelTag.putString("over_tex", "minecraft:textures/block/stone.png");
                statue.getOrCreateTagElement("BlockEntityTag").put("custom_model", modelTag);

                addTrade(statue, trade, trades);
            }
        }
    }

}
