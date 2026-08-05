package thut.api;

import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.registries.DeferredRegister;
import thut.api.attachments.AnimatedCaps;
import thut.api.attachments.Breedable;
import thut.api.attachments.CopyMob;
import thut.api.attachments.Energy;
import thut.api.attachments.IOwnable;
import thut.api.attachments.Inventory;
import thut.api.attachments.Linkable;
import thut.api.attachments.Linkable.ILinkable;
import thut.api.attachments.Linkable.LinkHolder;
import thut.api.attachments.Ownable;
import thut.api.attachments.PositionTracker;
import thut.api.attachments.Shearable;
import thut.api.entity.IAnimated;
import thut.api.entity.IAnimated.IAnimationHolder;
import thut.api.entity.IBreedingMob;
import thut.api.entity.ICopyMob;
import thut.api.entity.IMobColourable;
import thut.api.entity.IMobTexturable;
import thut.api.entity.IShearable;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.level.structures.CapabilityWorldStructures;
import thut.api.level.terrain.CapabilityTerrain;
import thut.api.level.terrain.CapabilityTerrain.ITerrainProvider;
import thut.api.level.terrain.ITerrainAffected;
import thut.api.world.mobs.data.DataSync;
import thut.core.common.genetics.DefaultGenetics;
import thut.core.common.mobs.DefaultColourable;
import thut.core.common.network.SyncAttachments;
import thut.core.common.terrain.CapabilityTerrainAffected;
import thut.core.common.world.mobs.data.DataSync_Impl;

public class ThutCaps
{
    public static void registerAttachments(DeferredRegister<AttachmentType<?>> registry)
    {
        Ownable.registerAttachment(registry);
        Shearable.registerAttachment(registry);
        CopyMob.registerAttachment(registry);
        CapabilityTerrain.registerAttachment(registry);
        CapabilityTerrainAffected.registerAttachment(registry);
        CapabilityWorldStructures.registerAttachment(registry);
        Linkable.registerAttachment(registry);
        DefaultGenetics.registerAttachment(registry);
        Inventory.registerAttachment(registry);
        DefaultColourable.registerAttachment(registry);
        Breedable.registerAttachment(registry);
        AnimatedCaps.registerAttachment(registry);
        DataSync_Impl.registerAttachment(registry);
        Energy.registerAttachment(registry);
        PositionTracker.registerAttachment(registry);

        IMobTexturable.Defaults.registerAttachment(registry);

        SyncAttachments.SYNCED.add(CopyMob.LOC);
        SyncAttachments.SYNCED.add(CopyMob.ANIM);
        SyncAttachments.SYNCED.add(DefaultGenetics.KEY);
    }

    public static void registerItemData(DeferredRegister<DataComponentType<?>> registry)
    {
        CopyMob.registerItemData(registry);
        Linkable.registerItemData(registry);
        DefaultGenetics.registerItemData(registry);
        Inventory.registerItemData(registry);
    }

    public static IMobTexturable getTexturable(final IAttachmentHolder in)
    {
        if (in == null) return null;
        return IMobTexturable.Defaults.get(in);
    }

    public static IMobGenetics getGenetics(final IAttachmentHolder in)
    {
        if (in == null) return null;
        return DefaultGenetics.get(in);
    }

    public static ITerrainAffected getTerrainAffected(final IAttachmentHolder in)
    {
        if (in == null) return null;
        return CapabilityTerrainAffected.get(in);
    }

    public static IAnimationHolder getAnimationHolder(final IAttachmentHolder in)
    {
        if (in == null) return null;
        return CopyMob.getAnimHolder(in);
    }

    public static ICopyMob getCopyMob(final IAttachmentHolder in)
    {
        if (in == null) return null;
        return CopyMob.getCopyHolder(in);
    }

    public static DataSync getDataSync(final IAttachmentHolder in)
    {
        if (in == null) return null;
        return DataSync_Impl.get(in);
    }

    public static IShearable getShearable(final IAttachmentHolder in)
    {
        if (in == null) return null;
        return Shearable.get(in);
    }

    public static IAnimated getAnimated(final IAttachmentHolder in)
    {
        if (in == null) return null;
        return AnimatedCaps.get(in);
    }

    public static IBreedingMob getBreedable(final IAttachmentHolder in)
    {
        if (in == null) return null;
        return Breedable.get(in);
    }

    public static IMobColourable getColourable(final IAttachmentHolder in)
    {
        if (in == null) return null;
        return DefaultColourable.get(in);
    }

    public static IOwnable getOwnable(final IAttachmentHolder in)
    {
        if (in == null) return null;
        return Ownable.get(in);
    }

    public static CapabilityWorldStructures getWorldStructures(IAttachmentHolder in)
    {
        if (in == null) return null;
        return CapabilityWorldStructures.get(in);
    }

    public static ITerrainProvider getTerrainProvider(IAttachmentHolder in)
    {
        if (in == null) return null;
        return CapabilityTerrain.get(in);
    }

    public static ILinkable getLinkable(IAttachmentHolder in)
    {
        return getLinkable(in, Direction.DOWN);
    }

    public static LinkHolder getLinkStorage(ItemStack stack)
    {
        return stack.get(Linkable.LINK_STORE);
    }

    public static ILinkable getLinkable(IAttachmentHolder in, Direction side)
    {
        if (in == null) return null;
        return Linkable.get(in, side);
    }

    public static EnergyStorage getEnergy(IAttachmentHolder in)
    {
        return getEnergy(in, Direction.DOWN);
    }

    public static EnergyStorage getEnergy(IAttachmentHolder in, Direction side)
    {
        if (in == null) return null;
        return Energy.get(in, side);
    }

    public static IItemHandler getInventory(ItemStack item)
    {
        return item.getCapability(Capabilities.ItemHandler.ITEM);
    }

    public static IItemHandler getInventory(BlockEntity tile, Direction side)
    {
        return tile.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, tile.getBlockPos(), side);
    }

    public static IItemHandler getInventory(BlockEntity tile)
    {
        return getInventory(tile, null);
    }

    public static PositionTracker.ITrackedPosition getPositionTracker(Entity entity)
    {
        return entity.getData(PositionTracker.TYPE);
    }
}
