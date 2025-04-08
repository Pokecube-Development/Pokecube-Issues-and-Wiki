package pokecube.adventures.capabilities;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import pokecube.api.entity.trainers.IHasNPCAIStates;
import pokecube.api.entity.trainers.IHasNPCAIStates.AIState;
import pokecube.api.entity.trainers.IHasTrades;
import pokecube.api.entity.trainers.TrainerCaps;
import pokecube.api.events.npcs.NpcTradesEvent;
import thut.api.data.HolderProvider;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class CapabilityHasTrades
{
    public static class DefaultTrades implements IHasTrades
    {
        public Consumer<ItemStack> onTraded = t -> {};
        @Nullable
        private Player customer;
        @Nullable
        protected MerchantOffers offers;

        @Override
        public CompoundTag serializeNBT(Provider provider)
        {
            return new CompoundTag();
        }

        @Override
        public void deserializeNBT(Provider provider, final CompoundTag nbt)
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
        public void verify(final ItemStack stack)
        {
            this.onTraded.accept(stack);
        }

    }

    @SubscribeEvent
    public static void setup(final NpcTradesEvent event)
    {
        IHasNPCAIStates aiStates = TrainerCaps.getNPCAIStates(event.getEntity());
        if (aiStates == null) return;
        // If we don't trade items, clear the offers
        if (!aiStates.getAIState(AIState.TRADES_ITEMS)) event.offers.clear();
    }
    
    private static final HolderProvider<IHasTrades> _REGISTRY = new HolderProvider<>(ResourceLocation.parse("pokecube_adventure:trainer_trades"));

    public static void registerProvider(HolderProvider.Provider<IHasTrades> reg)
    {
        _REGISTRY.register(reg);
    }

    public static IHasTrades make(IAttachmentHolder holder)
    {
        return _REGISTRY.make(holder);
    }
}
