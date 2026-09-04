package pokecube.adventures.utils;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.blocks.afa.AfaTile;
import pokecube.adventures.blocks.daycare.DaycareTile;
import thut.api.attachments.Inventory;
import thut.api.attachments.Ownable;
import thut.api.attachments.Ownable.IOwnableSerializable;
import thut.api.data.HolderProvider;
import thut.api.inventory.InvHelper.ItemCap;

@EventBusSubscriber(modid = PokecubeAdv.MODID)
public class InventoryHandler
{
    static class DaycareTileInventory implements ICapabilityProvider<DaycareTile, Direction, IItemHandler>
    {
        @Override
        public @Nullable IItemHandler getCapability(DaycareTile object, Direction context)
        {
            // Only 1 inventory, so mark it as down here.
            return Inventory.get(object);
        }
    }

    static class AFATileInventory implements ICapabilityProvider<AfaTile, Direction, IItemHandler>
    {
        @Override
        public @Nullable IItemHandler getCapability(AfaTile object, Direction context)
        {
            // Only 1 inventory, so mark it as down here.
            return Inventory.get(object);
        }
    }

    public static final ResourceLocation DAYCARETAG = ResourceLocation.parse("pokecube_adventures:daycare_fuel");

    @SubscribeEvent
    public static void AttachCaps(final RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PokecubeAdv.AFA_TYPE.get(), new AFATileInventory());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PokecubeAdv.DAYCARE_TYPE.get(),
                new DaycareTileInventory());

        Inventory.REGISTRY.register(new HolderProvider.Provider<>()
        {
            final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("pokecube_adventures", "afa");

            @Override
            public ItemCap apply(IAttachmentHolder t)
            {
                if (t instanceof AfaTile) return new ItemCap(1, 1);
                return null;
            }

            @Override
            protected ResourceLocation key()
            {
                return ID;
            }
        });

        Inventory.REGISTRY.register(new HolderProvider.Provider<>()
        {
            final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("pokecube_adventures", "daycare");

            @Override
            public ItemCap apply(IAttachmentHolder t)
            {
                if (t instanceof DaycareTile) return new ItemCap(1, 128, DAYCARETAG);
                return null;
            }

            @Override
            protected ResourceLocation key()
            {
                return ID;
            }
        });

        Ownable._REGISTRY.register(new HolderProvider.Provider<>()
        {
            final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("pokecube_adventures", "afa");

            @Override
            public IOwnableSerializable apply(IAttachmentHolder t)
            {
                if (t instanceof AfaTile afa) return new Ownable.ImplTE(afa);
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
