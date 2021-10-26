package thut.api;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import thut.api.LinkableCaps.ILinkStorage;
import thut.api.LinkableCaps.ILinkable;
import thut.api.entity.IAnimated;
import thut.api.entity.IBreedingMob;
import thut.api.entity.IMobColourable;
import thut.api.terrain.CapabilityTerrain.ITerrainProvider;

public class ThutCaps
{

    @CapabilityInject(ILinkable.class)
    public static final Capability<ILinkable>    LINK  = null;
    @CapabilityInject(ILinkStorage.class)
    public static final Capability<ILinkStorage> STORE = null;

    @CapabilityInject(ITerrainProvider.class)
    public static final Capability<ITerrainProvider> TERRAIN_CAP = null;

    @CapabilityInject(IOwnable.class)
    public static final Capability<IOwnable> OWNABLE_CAP = null;

    @CapabilityInject(IMobColourable.class)
    public static final Capability<IMobColourable> COLOURABLE = null;

    @CapabilityInject(IBreedingMob.class)
    public static final Capability<IBreedingMob> BREEDS = null;

    @CapabilityInject(IAnimated.class)
    public static final Capability<IAnimated> ANIMATED = null;

}
