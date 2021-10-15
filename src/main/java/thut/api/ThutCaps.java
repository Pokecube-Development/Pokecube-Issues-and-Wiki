package thut.api;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import thut.api.LinkableCaps.ILinkStorage;
import thut.api.LinkableCaps.ILinkable;
import thut.api.entity.IAnimated;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.IBreedingMob;
import thut.api.entity.ICopyMob;
import thut.api.entity.IMobColourable;
import thut.api.entity.IMobTexturable;
import thut.api.entity.IShearable;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.terrain.ITerrainAffected;
import thut.api.terrain.CapabilityTerrain.ITerrainProvider;
import thut.api.world.mobs.data.DataSync;

public class ThutCaps
{

    public static final Capability<ILinkable> LINK = CapabilityManager.get(new CapabilityToken<>(){});

    public static final Capability<ILinkStorage> STORE = CapabilityManager.get(new CapabilityToken<>(){});

    public static final Capability<ITerrainProvider> TERRAIN_PROVIDER = CapabilityManager.get(new CapabilityToken<>(){});

    public static final Capability<IOwnable> OWNABLE_CAP = CapabilityManager.get(new CapabilityToken<>(){});

    public static final Capability<IMobColourable> COLOURABLE = CapabilityManager.get(new CapabilityToken<>(){});

    public static final Capability<IBreedingMob> BREEDS = CapabilityManager.get(new CapabilityToken<>(){});

    public static final Capability<IAnimated> ANIMATED = CapabilityManager.get(new CapabilityToken<>(){});

    public static final Capability<IShearable> SHEARABLE = CapabilityManager.get(new CapabilityToken<>(){});

    public static final Capability<DataSync> DATASYNC = CapabilityManager.get(new CapabilityToken<>(){});

    public static final Capability<ICopyMob> COPYMOB = CapabilityManager.get(new CapabilityToken<>(){});

    public static final Capability<IAnimationHolder> ANIMCAP = CapabilityManager.get(new CapabilityToken<>(){});

    public static final Capability<ITerrainAffected> TERRAIN_AFFECTED = CapabilityManager.get(new CapabilityToken<>(){});

    public static final Capability<IMobGenetics> GENETICS_CAP = CapabilityManager.get(new CapabilityToken<>(){});

    public static final Capability<IMobTexturable> MOBTEX_CAP = CapabilityManager.get(new CapabilityToken<>(){});

    public static void registerCapabilities(final RegisterCapabilitiesEvent event)
    {
        event.register(ILinkable.class);
        event.register(ILinkStorage.class);
        event.register(ITerrainProvider.class);
        event.register(IOwnable.class);
        event.register(ICopyMob.class);
        event.register(IAnimationHolder.class);
        event.register(IMobColourable.class);
        event.register(IBreedingMob.class);
        event.register(IAnimated.class);
        event.register(IShearable.class);
        event.register(DataSync.class);
        event.register(ITerrainAffected.class);
        event.register(IMobGenetics.class);
        event.register(IMobTexturable.class);
    }
}
