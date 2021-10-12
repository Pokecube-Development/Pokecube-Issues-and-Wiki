package thut.wearables.inventory;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class WearableHandler
{
    public static final Capability<IWearableInventory> WEARABLES_CAP = CapabilityManager.get(new CapabilityToken<>(){});
}
