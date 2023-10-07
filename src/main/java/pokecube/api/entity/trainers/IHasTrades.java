package pokecube.api.entity.trainers;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

/**
 * Generic trading mob interface, mostly just a wrapper for vanilla's Merchant
 * interface used for villagers.
 *
 */
public interface IHasTrades
{
    /**
     * Called when the trade is applied
     * 
     * @param trade
     */
    void applyTrade(MerchantOffer trade);

    /**
     * Called when the gui to access trades is opened.
     * 
     * @param player
     */
    void setCustomer(Player player);

    /**
     * 
     * @return the player set in {@link #setCustomer(Player)}
     */
    Player getCustomer();

    /**
     * 
     * @return Whether we have an existing customer at present.
     */
    default boolean hasCustomer()
    {
        return this.getCustomer() != null;
    }

    /**
     * 
     * @return the MerchantOffers containing our trades
     */
    MerchantOffers getOffers();

    /**
     * 
     * @param offers - the MerchantOffers containing our trades.
     */
    void setOffers(MerchantOffers offers);

    /**
     * Called when a trade is updated, usually after the trade is about to be
     * completed.
     * 
     * @param stack
     */
    void verify(ItemStack stack);
}