package pokecube.api.entity.trainers;

import java.util.function.Consumer;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public interface IHasTrades
{
    void applyTrade(MerchantOffer trade);

    void setCustomer(Player player);

    Player getCustomer();

    default boolean hasCustomer()
    {
        return this.getCustomer() != null;
    }

    MerchantOffers getOffers();

    void setOffers(MerchantOffers offers);

    void initTrades();

    void verify(ItemStack stack);

    void setValidator(Consumer<ItemStack> validator);
}