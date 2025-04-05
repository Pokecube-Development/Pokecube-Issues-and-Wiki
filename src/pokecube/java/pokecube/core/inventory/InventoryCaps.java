package pokecube.core.inventory;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.nests.NestTile;
import pokecube.core.blocks.pc.PCTile;
import pokecube.core.blocks.tms.TMTile;
import pokecube.core.blocks.trade.TraderTile;
import pokecube.core.inventory.pc.PCWrapper;
import thut.api.attachments.Inventory;
import thut.api.data.HolderProvider;
import thut.api.inventory.InvHelper.ItemCap;

public class InventoryCaps
{
    static class TMContainerProvider implements ICapabilityProvider<TMTile, Direction, IItemHandler>
    {
        @Override
        public @Nullable IItemHandler getCapability(TMTile object, Direction context)
        {
            // Only 1 inventory, so mark it as down here.
            context = Direction.DOWN;
            return Inventory.get(object, context);
        }
    }

    static class TradeContainerProvider implements ICapabilityProvider<TraderTile, Direction, IItemHandler>
    {
        @Override
        public @Nullable IItemHandler getCapability(TraderTile object, Direction context)
        {
            // Only 1 inventory, so mark it as down here.
            context = Direction.DOWN;
            return Inventory.get(object, context);
        }
    }

    static class PCContainerProvider implements ICapabilityProvider<PCTile, Direction, IItemHandler>
    {
        @Override
        public @Nullable IItemHandler getCapability(PCTile object, Direction context)
        {
            // Only 1 inventory, so mark it as down here.
            context = Direction.DOWN;
            return Inventory.get(object, context);
        }
    }

    static class NestProvider implements ICapabilityProvider<NestTile, Direction, IItemHandler>
    {
        @Override
        public @Nullable IItemHandler getCapability(NestTile object, Direction context)
        {
            // Only 1 inventory, so mark it as down here.
            context = Direction.DOWN;
            return Inventory.get(object, context);
        }
    }

    public static void AttachCaps(final RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PokecubeItems.TM_TYPE.get(),
                new TMContainerProvider());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PokecubeItems.TRADE_TYPE.get(),
                new TradeContainerProvider());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PokecubeItems.PC_TYPE.get(),
                new PCContainerProvider());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PokecubeItems.NEST_TYPE.get(), new NestProvider());

        // Register our providers for custom item attachments
        Inventory.DEFAULT().register(new HolderProvider.Provider<ItemCap>()
        {
            ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("pokecube", "tms");

            @Override
            public ItemCap apply(IAttachmentHolder t)
            {
                if (t instanceof TMTile) return new ItemCap(2, 1);
                return null;
            }

            @Override
            protected ResourceLocation key()
            {
                return ID;
            }
        });

        Inventory.DEFAULT().register(new HolderProvider.Provider<ItemCap>()
        {
            ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("pokecube", "trade");

            @Override
            public ItemCap apply(IAttachmentHolder t)
            {
                if (t instanceof TraderTile) return new ItemCap(2, 1);
                return null;
            }

            @Override
            protected ResourceLocation key()
            {
                return ID;
            }
        });

        Inventory.DEFAULT().register(new HolderProvider.Provider<ItemCap>()
        {
            ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("pokecube", "pc");

            @Override
            public ItemCap apply(IAttachmentHolder t)
            {
                if (t instanceof PCTile tile) return new PCWrapper(tile);
                return null;
            }

            @Override
            protected ResourceLocation key()
            {
                return ID;
            }
        });

        Inventory.DEFAULT().register(new HolderProvider.Provider<ItemCap>()
        {
            ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("pokecube", "nest");

            @Override
            public ItemCap apply(IAttachmentHolder t)
            {
                if (t instanceof NestTile) return new ItemCap(54);
                return null;
            }

            @Override
            protected ResourceLocation key()
            {
                return ID;
            }
        });
    }
}
