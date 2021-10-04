package pokecube.adventures.utils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.adventures.blocks.afa.AfaTile;
import pokecube.adventures.blocks.daycare.DaycareTile;
import pokecube.core.inventory.InvHelper.ItemCap;
import thut.api.OwnableCaps;

public class InventoryHandler
{
    private static final ResourceLocation CAPID = new ResourceLocation("pokecube_adventures:inventory");
    private static final ResourceLocation OWNID = new ResourceLocation("pokecube_adventures:ownable");

    private static final ResourceLocation DAYCARETAG = new ResourceLocation("pokecube_adventures:daycare_fuel");

    @SubscribeEvent
    public static void onTileCapabilityAttach(final AttachCapabilitiesEvent<BlockEntity> event)
    {
        if (event.getCapabilities().containsKey(InventoryHandler.CAPID)) return;
        if (event.getObject() instanceof DaycareTile) event.addCapability(InventoryHandler.CAPID, new ItemCap(1, 64,
                InventoryHandler.DAYCARETAG));
        // AFA is ownable and also stores an item
        if (event.getObject() instanceof AfaTile) event.addCapability(InventoryHandler.CAPID, new ItemCap(1, 1));
        if (event.getObject() instanceof AfaTile) event.addCapability(InventoryHandler.OWNID, new OwnableCaps.ImplTE());
    }

}
