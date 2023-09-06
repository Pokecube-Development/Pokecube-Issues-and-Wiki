package pokecube.core.entity.boats;

import javax.annotation.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import pokecube.core.init.EntityTypes;

public class GenericChestBoat extends GenericBoat implements HasCustomInventoryScreen, ContainerEntity
{
    private static final int CONTAINER_SIZE = 27;
    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
    @Nullable
    private ResourceLocation lootTable;
    private long lootTableSeed;

    public GenericChestBoat(EntityType<? extends Boat> boatType, Level world)
    {
        super(boatType, world);
    }

    public GenericChestBoat(Level world, double x, double y, double z)
    {
        this(EntityTypes.getChestBoat(), world);
        this.setPos(x, y, z);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    public Item getDropItem()
    {
        return this.getGenericBoatType().chestBoat().get();
    }

    @Override
    protected float getSinglePassengerXOffset()
    {
        return 0.15F;
    }

    @Override
    protected int getMaxPassengers()
    {
        return 1;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag)
    {
        super.addAdditionalSaveData(tag);
        this.addChestVehicleSaveData(tag);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag)
    {
        super.readAdditionalSaveData(tag);
        this.readChestVehicleSaveData(tag);
    }

    @Override
    public void destroy(DamageSource source)
    {
        super.destroy(source);
        this.chestVehicleDestroyed(source, this.level, this);
    }

    @Override
    public void remove(Entity.RemovalReason reason)
    {
        if (!this.level.isClientSide && reason.shouldDestroy())
        {
            Containers.dropContents(this.level, this, this);
        }

        super.remove(reason);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand)
    {
        if (this.canAddPassenger(player) && !player.isSecondaryUseActive()) {
            return super.interact(player, hand);
        } else {
            InteractionResult interactionresult = this.interactWithContainerVehicle(player);
            if (interactionresult.consumesAction()) {
                this.gameEvent(GameEvent.CONTAINER_OPEN, player);
                PiglinAi.angerNearbyPiglins(player, true);
            }

            return interactionresult;
        }
    }

    @Override
    public void openCustomInventoryScreen(Player p_219906_)
    {
        p_219906_.openMenu(this);
        if (!p_219906_.level.isClientSide)
        {
            this.gameEvent(GameEvent.CONTAINER_OPEN, p_219906_);
            PiglinAi.angerNearbyPiglins(p_219906_, true);
        }

    }

    @Override
    public void clearContent()
    {
        this.clearChestVehicleContent();
    }

    @Override
    public int getContainerSize()
    {
        return CONTAINER_SIZE;
    }

    @Override
    public ItemStack getItem(int p_219880_)
    {
        return this.getChestVehicleItem(p_219880_);
    }

    @Override
    public ItemStack removeItem(int i, int j)
    {
        return this.removeChestVehicleItem(i, j);
    }

    @Override
    public ItemStack removeItemNoUpdate(int i)
    {
        return this.removeChestVehicleItemNoUpdate(i);
    }

    @Override
    public void setItem(int i, ItemStack stack)
    {
        this.setChestVehicleItem(i, stack);
    }

    @Override
    public SlotAccess getSlot(int slot)
    {
        return this.getChestVehicleSlot(slot);
    }

    @Override
    public void setChanged()
    {}

    @Override
    public boolean stillValid(Player player)
    {
        return this.isChestVehicleStillValid(player);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player)
    {
        if (this.lootTable != null && player.isSpectator())
        {
            return null;
        }
        else
        {
            this.unpackLootTable(inventory.player);
            return ChestMenu.threeRows(i, inventory, this);
        }
    }

    public void unpackLootTable(@Nullable Player player)
    {
        this.unpackChestVehicleLootTable(player);
    }

    @Nullable
    @Override
    public ResourceLocation getLootTable()
    {
        return this.lootTable;
    }

    @Override
    public void setLootTable(@Nullable ResourceLocation location)
    {
        this.lootTable = location;
    }

    @Override
    public long getLootTableSeed()
    {
        return this.lootTableSeed;
    }

    @Override
    public void setLootTableSeed(long l)
    {
        this.lootTableSeed = l;
    }

    @Override
    public NonNullList<ItemStack> getItemStacks()
    {
        return this.itemStacks;
    }

    @Override
    public void clearItemStacks()
    {
        this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    }

    // Forge Start
    private net.minecraftforge.common.util.LazyOptional<?> itemHandler = net.minecraftforge.common.util.LazyOptional
            .of(() -> new net.minecraftforge.items.wrapper.InvWrapper(this));

    @Override
    public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(
            net.minecraftforge.common.capabilities.Capability<T> capability,
            @Nullable net.minecraft.core.Direction facing)
    {
        if (this.isAlive() && capability == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER)
            return itemHandler.cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps()
    {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public void reviveCaps()
    {
        super.reviveCaps();
        itemHandler = net.minecraftforge.common.util.LazyOptional
                .of(() -> new net.minecraftforge.items.wrapper.InvWrapper(this));
    }
}
