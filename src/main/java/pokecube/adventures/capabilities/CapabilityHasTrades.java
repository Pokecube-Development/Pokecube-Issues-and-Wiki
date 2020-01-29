package pokecube.adventures.capabilities;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class CapabilityHasTrades
{
    public static class DefaultTrades implements IHasTrades, ICapabilitySerializable<CompoundNBT>
    {
        private final LazyOptional<IHasTrades> cap_holder = LazyOptional.of(() -> this);
        public Consumer<ItemStack>             onTraded   = t ->
                                                          {
                                                          };
        @Nullable
        private PlayerEntity                   customer;
        @Nullable
        protected MerchantOffers               offers;

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return CapabilityHasTrades.CAPABILITY.orEmpty(cap, this.cap_holder);
        }

        @Override
        public CompoundNBT serializeNBT()
        {
            return this.getOffers().func_222199_a();
        }

        @Override
        public void deserializeNBT(final CompoundNBT nbt)
        {
            if (!nbt.isEmpty())
            {
                this.offers = new MerchantOffers(nbt);
                if (this.offers.isEmpty()) this.offers = null;
            }
        }

        @Override
        public void applyTrade(final MerchantOffer trade)
        {
            // TODO vanilla gives exp here.
        }

        @Override
        public void setCustomer(final PlayerEntity player)
        {
            this.customer = player;
        }

        @Override
        public PlayerEntity getCustomer()
        {
            return this.customer;
        }

        @Override
        public MerchantOffers getOffers()
        {
            if (this.offers == null)
            {
                this.offers = new MerchantOffers();
                this.initTrades();
            }
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
            // We do nothing here, ideally someone who owns us called setOffers.
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

    public static class Storage implements Capability.IStorage<IHasTrades>
    {

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void readNBT(final Capability<IHasTrades> capability, final IHasTrades instance, final Direction side,
                final INBT base)
        {
            if (instance instanceof INBTSerializable<?>) ((INBTSerializable) instance).deserializeNBT(base);
        }

        @Override
        public INBT writeNBT(final Capability<IHasTrades> capability, final IHasTrades instance, final Direction side)
        {
            if (instance instanceof INBTSerializable<?>) return ((INBTSerializable<?>) instance).serializeNBT();
            return null;
        }

    }

    @CapabilityInject(IHasTrades.class)
    public static final Capability<IHasTrades> CAPABILITY = null;

    public static Storage storage;

    public static interface IHasTrades
    {
        void applyTrade(MerchantOffer trade);

        void setCustomer(PlayerEntity player);

        PlayerEntity getCustomer();

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

    public static IHasTrades getHasTrades(final ICapabilityProvider entityIn)
    {
        if (entityIn == null) return null;
        final IHasTrades holder = entityIn.getCapability(CapabilityHasTrades.CAPABILITY, null).orElse(null);
        if (holder == null && entityIn instanceof IHasTrades) return (IHasTrades) entityIn;
        return holder;
    }
}
