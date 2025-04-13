package pokecube.adventures.utils.trade_presets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrade;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrades;
import pokecube.adventures.utils.TradeEntryLoader;
import pokecube.adventures.utils.TradeEntryLoader.Trade;
import pokecube.adventures.utils.TradeEntryLoader.TradePreset;
import pokecube.api.PokecubeAPI;
import pokecube.api.utils.Tools;
import thut.api.util.JsonUtil;
import thut.lib.RegHelper;
import thut.lib.TComponent;

import java.util.Locale;
import java.util.Optional;

@TradePresetAn(key = "sellExplorationMap")
public class SellStructureMap implements TradePreset
{
    public static final String ID = "id";
    public static final String NEW_ONLY = "new_only";

    @Override
    public void apply(final Trade trade, final TrainerTrades trades)
    {
        TrainerTrade recipe;
        final ItemStack sell = new ItemStack(Items.MAP);
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
        recipe = new TrainerTrade(cost, _buy2, sell, trade);
        var values = trade.values;
        if (values.containsKey(TradeEntryLoader.CHANCE))
            recipe.chance = (float) values.get(TradeEntryLoader.CHANCE);

        ResourceLocation loc = ResourceLocation.parse((String) trade.values.get(ID));

        boolean newOnly = (boolean) trade.values.getOrDefault(NEW_ONLY, false);

        recipe.outputModifier = (entity, random) -> {
            if (!(entity.level instanceof ServerLevel serverlevel)) return ItemStack.EMPTY;
            ItemStack output = ItemStack.EMPTY;
            try
            {
                TagKey<Structure> key = TagKey.create(RegHelper.STRUCTURE_REGISTRY, loc);
                // Vanilla one uses 100 and true.
                BlockPos blockpos = serverlevel.findNearestMapStructure(key, entity.blockPosition(), 100, newOnly);
                if (blockpos != null)
                {
                    ItemStack itemstack = MapItem.create(serverlevel, blockpos.getX(), blockpos.getZ(), (byte) 2, true,
                            true);
                    MapItem.renderBiomePreviewMap(serverlevel, itemstack);
                    MapItemSavedData.addTargetDecoration(itemstack, blockpos, "+", MapDecorationTypes.RED_X);
                    itemstack.set(DataComponents.ITEM_NAME,
                            TComponent.translatable("filled_map." + loc.getPath().toLowerCase(Locale.ROOT)));
                    return itemstack;
                }
            }
            catch (Exception e)
            {
                PokecubeAPI.LOGGER.error("Error making a map trade for {}", loc);
                PokecubeAPI.LOGGER.error(e);
                return ItemStack.EMPTY;
            }

            return output;
        };

        recipe.debug_string = JsonUtil.gson.toJson(trade);

        trades.tradesList.add(recipe);
    }
}
