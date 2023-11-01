package thut.api;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
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
import thut.api.level.structures.CapabilityWorldStructures;
import thut.api.level.terrain.CapabilityTerrain.ITerrainProvider;
import thut.api.level.terrain.ITerrainAffected;
import thut.api.world.mobs.data.DataSync;

public class ThutCaps
{

    public static final Capability<ILinkable> LINK = CapabilityManager.get(new CapabilityToken<>(){});

    public static final Capability<ILinkStorage> STORE = CapabilityManager.get(new CapabilityToken<>(){});

    public static final Capability<ITerrainProvider> TERRAIN_PROVIDER = CapabilityManager.get(new CapabilityToken<>(){});

    public static final Capability<CapabilityWorldStructures> WORLD_STRUCTURES = CapabilityManager.get(new CapabilityToken<>(){});

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
    
    public static final Capability<IItemHandler> ITEM_HANDLER = CapabilityManager.get(new CapabilityToken<>(){});
    
    public static final Capability<IEnergyStorage> ENERGY = CapabilityManager.get(new CapabilityToken<>(){});

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
        event.register(CapabilityWorldStructures.class);
    }
    
    public static IEnergyStorage getEnergy(final ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(ThutCaps.ENERGY).orElse(null);
    }
    
    public static IEnergyStorage getEnergy(final ICapabilityProvider in, Direction side)
    {
        if (in == null) return null;
        return in.getCapability(ThutCaps.ENERGY).orElse(null);
    }
    
    public static IItemHandler getInventory(final ICapabilityProvider in, Direction side)
    {
        if (in == null) return null;
        return in.getCapability(ThutCaps.ITEM_HANDLER, side).orElse(null);
    }
    
    public static IItemHandler getInventory(final ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(ThutCaps.ITEM_HANDLER).orElse(null);
    }
    
    public static IMobTexturable getTexturable(final ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(ThutCaps.MOBTEX_CAP).orElse(null);
    }
    
    public static IMobGenetics getGenetics(final ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(ThutCaps.GENETICS_CAP).orElse(null);
    }
    
    public static ITerrainAffected getTerrainAffected(final ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(ThutCaps.TERRAIN_AFFECTED).orElse(null);
    }
    
    public static IAnimationHolder getAnimationHolder(final ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(ThutCaps.ANIMCAP).orElse(null);
    }
    
    public static ICopyMob getCopyMob(final ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(ThutCaps.COPYMOB).orElse(null);
    }
    
    public static DataSync getDataSync(final ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(ThutCaps.DATASYNC).orElse(null);
    }
    
    public static IShearable getShearable(final ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(ThutCaps.SHEARABLE).orElse(null);
    }
    
    public static IAnimated getAnimated(final ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(ThutCaps.ANIMATED).orElse(null);
    }
    
    public static IBreedingMob getBreedable(final ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(ThutCaps.BREEDS).orElse(null);
    }
    
    public static IMobColourable getColourable(final ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(ThutCaps.COLOURABLE).orElse(null);
    }
    
    public static IOwnable getOwnable(final ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(ThutCaps.OWNABLE_CAP).orElse(null);
    }

    public static CapabilityWorldStructures getWorldStructures(ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(WORLD_STRUCTURES).orElse(null);
    }

    public static ITerrainProvider getTerrainProvider(ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(TERRAIN_PROVIDER).orElse(null);
    }

    public static ILinkable getLinkable(ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(LINK).orElse(null);
    }

    public static ILinkable getLinkable(ICapabilityProvider in, Direction side)
    {
        if (in == null) return null;
        return in.getCapability(LINK, side).orElse(null);
    }

    public static ILinkStorage getLinkStorage(ICapabilityProvider in)
    {
        if (in == null) return null;
        return in.getCapability(STORE).orElse(null);
    }
}
