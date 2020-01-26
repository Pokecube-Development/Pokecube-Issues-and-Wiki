package pokecube.adventures.utils;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import pokecube.adventures.blocks.afa.AfaTile;
import pokecube.adventures.blocks.daycare.DaycareTile;

@Mod.EventBusSubscriber
public class InventoryHandler
{
    private static final ResourceLocation CAPID = new ResourceLocation("pokecube_adventures:inventory");

    public static class ItemCap extends ItemStackHandler implements ICapabilitySerializable<CompoundNBT>
    {
        private final int                        stackSize;
        private final LazyOptional<IItemHandler> holder = LazyOptional.of(() -> this);

        public ItemCap(final int slotCount, final int stackSize)
        {
            super(slotCount);
            this.stackSize = stackSize;
        }

        @Override
        public int getSlotLimit(final int slot)
        {
            return this.stackSize;
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing)
        {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(capability, this.holder);
        }
    }

    @SubscribeEvent
    public static void onTileCapabilityAttach(final AttachCapabilitiesEvent<TileEntity> event)
    {
        if (event.getCapabilities().containsKey(InventoryHandler.CAPID)) return;
        if (event.getObject() instanceof DaycareTile) event.addCapability(InventoryHandler.CAPID, new ItemCap(1, 64));
        if (event.getObject() instanceof AfaTile) event.addCapability(InventoryHandler.CAPID, new ItemCap(1, 1));
    }

}
