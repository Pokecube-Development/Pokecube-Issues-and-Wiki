package pokecube.adventures.proxy;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.ai.tasks.Tasks;
import pokecube.adventures.blocks.afa.AfaTile;
import pokecube.adventures.blocks.warppad.WarppadTile;
import pokecube.adventures.capabilities.CapabilityHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.DefaultPokemobs;
import pokecube.adventures.capabilities.CapabilityHasPokemobs.IHasPokemobs;
import pokecube.adventures.capabilities.CapabilityHasRewards;
import pokecube.adventures.capabilities.CapabilityHasRewards.DefaultRewards;
import pokecube.adventures.capabilities.CapabilityHasRewards.IHasRewards;
import pokecube.adventures.capabilities.CapabilityHasTrades;
import pokecube.adventures.capabilities.CapabilityHasTrades.DefaultTrades;
import pokecube.adventures.capabilities.CapabilityHasTrades.IHasTrades;
import pokecube.adventures.capabilities.CapabilityNPCAIStates;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.DefaultAIStates;
import pokecube.adventures.capabilities.CapabilityNPCAIStates.IHasNPCAIStates;
import pokecube.adventures.capabilities.CapabilityNPCMessages;
import pokecube.adventures.capabilities.CapabilityNPCMessages.DefaultMessager;
import pokecube.adventures.capabilities.CapabilityNPCMessages.IHasMessages;
import pokecube.adventures.capabilities.utils.TypeTrainer;
import pokecube.adventures.events.CompatEvent;
import pokecube.adventures.network.PacketAFA;
import pokecube.adventures.network.PacketBag;
import pokecube.adventures.network.PacketCommander;
import pokecube.adventures.network.PacketTrainer;
import pokecube.compat.Compat;
import thut.api.OwnableCaps;
import thut.core.common.Proxy;
import thut.wearables.EnumWearable;
import thut.wearables.IActiveWearable;
import thut.wearables.ThutWearables;

public class CommonProxy implements Proxy
{
    protected static class Wearable extends IActiveWearable.Default implements ICapabilityProvider
    {
        private final LazyOptional<IActiveWearable> holder = LazyOptional.of(() -> this);

        @Override
        public EnumWearable getSlot(final ItemStack stack)
        {
            return EnumWearable.BACK;
        }

        @Override
        public boolean dyeable(final ItemStack stack)
        {
            return true;
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return ThutWearables.WEARABLE_CAP.orEmpty(cap, this.holder);
        }
    }

    public ResourceLocation getTrainerSkin(final LivingEntity mob, final TypeTrainer type, final byte gender)
    {
        return null;
    }

    public Wearable getWearable()
    {
        return new Wearable();
    }

    @Override
    public void setup(final FMLCommonSetupEvent event)
    {
        Proxy.super.setup(event);

        // Register capabilities
        CapabilityManager.INSTANCE.register(IHasPokemobs.class,
                CapabilityHasPokemobs.storage = new CapabilityHasPokemobs.Storage(), DefaultPokemobs::new);
        CapabilityManager.INSTANCE.register(IHasNPCAIStates.class,
                CapabilityNPCAIStates.storage = new CapabilityNPCAIStates.Storage(), DefaultAIStates::new);
        CapabilityManager.INSTANCE.register(IHasMessages.class,
                CapabilityNPCMessages.storage = new CapabilityNPCMessages.Storage(), DefaultMessager::new);
        CapabilityManager.INSTANCE.register(IHasRewards.class,
                CapabilityHasRewards.storage = new CapabilityHasRewards.Storage(), DefaultRewards::new);
        CapabilityManager.INSTANCE.register(IHasTrades.class,
                CapabilityHasTrades.storage = new CapabilityHasTrades.Storage(), DefaultTrades::new);

        // Register packets
        PokecubeAdv.packets.registerMessage(PacketBag.class, PacketBag::new);
        PokecubeAdv.packets.registerMessage(PacketTrainer.class, PacketTrainer::new);
        PokecubeAdv.packets.registerMessage(PacketCommander.class, PacketCommander::new);
        PokecubeAdv.packets.registerMessage(PacketAFA.class, PacketAFA::new);

        OwnableCaps.TILES.add(AfaTile.class);
        OwnableCaps.TILES.add(WarppadTile.class);

        PacketTrainer.register();
        Tasks.init();
    }

    @Override
    public void loaded(final FMLLoadCompleteEvent event)
    {
        Compat.BUS.post(new CompatEvent());
        PokecubeAdv.config.loaded = true;
        // Reload this here to initialze anything that needs to be done here.
        PokecubeAdv.config.onUpdated();
    }
}
