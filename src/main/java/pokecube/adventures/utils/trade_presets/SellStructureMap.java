package pokecube.adventures.utils.trade_presets;

import java.util.Locale;
import java.util.Map;

import javax.xml.namespace.QName;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrade;
import pokecube.adventures.capabilities.utils.TypeTrainer.TrainerTrades;
import pokecube.adventures.utils.TradeEntryLoader;
import pokecube.adventures.utils.TradeEntryLoader.Trade;
import pokecube.adventures.utils.TradeEntryLoader.TradePreset;
import pokecube.core.PokecubeCore;
import pokecube.core.utils.Tools;

@TradePresetAn(key = "sellExplorationMap")
public class SellStructureMap implements TradePreset
{
    public static final QName ID = new QName("id");
    public static final QName NEW_ONLY = new QName("new_only");

    @Override
    public void apply(final Trade trade, final TrainerTrades trades)
    {
        Map<QName, String> values;
        TrainerTrade recipe;
        final ItemStack sell = new ItemStack(Items.MAP);
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

        ResourceLocation loc = new ResourceLocation(trade.values.get(ID));

        boolean newOnly = Boolean.parseBoolean(trade.values.getOrDefault(NEW_ONLY, "false"));

        recipe.outputModifier = (entity, random) -> {
            if (!(entity.level instanceof ServerLevel serverlevel)) return ItemStack.EMPTY;
            ItemStack output = ItemStack.EMPTY;
            try
            {
                StructureFeature<?> feature = serverlevel.registryAccess()
                        .registryOrThrow(Registry.STRUCTURE_FEATURE_REGISTRY).get(loc);
                // Vanilla one uses 100 and true.
                BlockPos blockpos = serverlevel.findNearestMapFeature(feature, entity.blockPosition(), 100, newOnly);
                if (blockpos != null)
                {
                    ItemStack itemstack = MapItem.create(serverlevel, blockpos.getX(), blockpos.getZ(), (byte) 2, true,
                            true);
                    MapItem.renderBiomePreviewMap(serverlevel, itemstack);
                    MapItemSavedData.addTargetDecoration(itemstack, blockpos, "+", MapDecoration.Type.RED_X);
                    itemstack.setHoverName(new TranslatableComponent(
                            "filled_map." + feature.getFeatureName().toLowerCase(Locale.ROOT)));
                    return itemstack;
                }
            }
            catch (Exception e)
            {
                PokecubeCore.LOGGER.error("Error making a map trade for {}", loc);
                PokecubeCore.LOGGER.error(e);
                return ItemStack.EMPTY;
            }

            return output;
        };

        trades.tradesList.add(recipe);
    }
}
