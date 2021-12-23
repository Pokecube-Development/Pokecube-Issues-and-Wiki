package pokecube.adventures.capabilities;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates.AIState;
import pokecube.core.events.npc.NpcTradesEvent;

public class CapabilityHasTrades
{
    public static class DefaultTrades implements IHasTrades, ICapabilitySerializable<CompoundTag>
    {
        private final LazyOptional<IHasTrades> cap_holder = LazyOptional.of(() -> this);
        public Consumer<ItemStack> onTraded = t -> {};
        @Nullable
        private Player customer;
        @Nullable
        protected MerchantOffers offers;

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return TrainerCaps.TRADES_CAP.orEmpty(cap, this.cap_holder);
        }

        @Override
        public CompoundTag serializeNBT()
        {
            return new CompoundTag();
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {

        }

        @Override
        public void applyTrade(final MerchantOffer trade)
        {
            // TODO vanilla gives exp here.
        }

        @Override
        public void setCustomer(final Player player)
        {
            this.customer = player;
        }

        @Override
        public Player getCustomer()
        {
            return this.customer;
        }

        @Override
        public MerchantOffers getOffers()
        {
            if (this.offers == null) this.offers = new MerchantOffers();
            return this.offers;
        }

        @Override
        public void setOffers(final MerchantOffers offers)
        {
            this.offers = offers;
        }

        @Override
        public void initTrades()
        {
            // We do nothing, as are intended as a holder for IMerchants.
        }

        @Override
        public void verify(final ItemStack stack)
        {
            this.onTraded.accept(stack);
        }

        @Override
        public void setValidator(final Consumer<ItemStack> validator)
        {
            this.onTraded = validator;
        }

    }

    public static interface IHasTrades
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

    @SubscribeEvent
    public static void setup(final NpcTradesEvent event)
    {
        IHasNPCAIStates aiStates = event.getEntity().getCapability(TrainerCaps.AISTATES_CAP).orElse(null);
        if (aiStates == null) return;
        // If we don't trade items, clear the offers
        if (!aiStates.getAIState(AIState.TRADES_ITEMS)) event.offers.clear();
    }
}
