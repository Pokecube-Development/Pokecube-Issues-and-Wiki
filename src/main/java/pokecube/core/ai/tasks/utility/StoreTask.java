package pokecube.core.ai.tasks.utility;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import pokecube.core.ai.tasks.idle.HungerTask;
import pokecube.core.interfaces.IMoveConstants.AIRoutine;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.items.berries.ItemBerry;
import thut.api.item.ItemList;
import thut.lib.ItemStackTools;

/**
 * This IAIRunnable will result in the mob occasionally emptying its inventory
 * into an inventory near its home location. This, along with AIGatherStuff
 * allows using pokemobs for automatic harvesting and storage of berries and
 * dropped items.
 */
public class StoreTask extends UtilTask implements INBTSerializable<CompoundTag>, ContainerListener
{
    public static int COOLDOWN = 10;
    public static int MAXSIZE = 100;

    // Inventory to store stuff in.
    public BlockPos storageLoc = null;
    // Inventory to check for berries
    public BlockPos berryLoc = null;
    // Inventory to pull stuff out of
    public BlockPos emptyInventory = null;
    // Side to store stuff in.
    public Direction storageFace = Direction.UP;
    // Side to emtpy things from.
    public Direction emptyFace = Direction.UP;

    int searchInventoryCooldown = 0;
    int doStorageCooldown = 0;

    int berrySlotIndex;

    boolean hasBerries = false;

    public int filledSlots = 0;
    public int emptySlots = 0;
    public int firstEmpty = 0;

    public boolean pathing = false;

    protected ItemStack heldItem = ItemStack.EMPTY;
    protected List<ResourceLocation> keys = Lists.newArrayList();

    private final Set<BlockPos> knownValid = Sets.newHashSet();

    public StoreTask(final IPokemob entity)
    {
        super(entity);
        if (entity.getInventory() instanceof SimpleContainer)
        {
            ((SimpleContainer) entity.getInventory()).addListener(this);
            // Initialize this.
            this.containerChanged(entity.getInventory());
        }
    }

    private void checkHeldItem()
    {
        ItemStack stack = pokemob.getHeldItem();
        if (stack != this.heldItem)
        {
            this.heldItem = stack;
            keys.clear();
            if (stack.hasTag() && stack.getTag().contains("pages") && stack.getTag().get("pages") instanceof ListTag)
            {
                final ListTag pages = (ListTag) stack.getTag().get("pages");
                try
                {
                    final Component comp = Component.Serializer.fromJson(pages.getString(0));
                    boolean isFilter = false;
                    for (final String line : comp.getString().split("\n"))
                    {
                        if (line.toLowerCase(Locale.ROOT).contains("item filters"))
                        {
                            isFilter = true;
                            continue;
                        }
                        if (isFilter)
                        {
                            ResourceLocation res = new ResourceLocation(line);
                            keys.add(res);
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    protected boolean checkValid(Object item_or_block)
    {
        checkHeldItem();
        for (ResourceLocation l : keys) if (ItemList.is(l, item_or_block)) return true;
        return keys.isEmpty();
    }

    @Override
    public void containerChanged(final Container invBasic)
    {
        ItemStack stack;
        this.berrySlotIndex = -1;
        this.hasBerries = false;
        this.filledSlots = 0;
        this.emptySlots = 0;
        this.firstEmpty = -1;
        // Has a berry already, can pass through to storage check.
        final IItemHandlerModifiable itemhandler = new InvWrapper(this.pokemob.getInventory());
        // Search inventory for free slots or berries.
        for (int i = 2; i < itemhandler.getSlots(); i++)
        {
            final boolean test = (stack = itemhandler.getStackInSlot(i)).isEmpty();
            if (!test)
            {
                this.filledSlots++;
                if (!this.hasBerries)
                {
                    this.hasBerries = ItemList.is(HungerTask.FOODTAG, stack);
                    if (this.hasBerries) this.berrySlotIndex = i;
                }
            }
            else
            {
                if (this.emptySlots == 0) this.firstEmpty = i;
                this.emptySlots++;
            }
        }
    }

    private BlockPos checkDir(final BlockGetter world, final Direction dir, BlockPos centre, final Direction side)
    {
        if (centre == null) return null;
        if (dir != null) centre = centre.relative(dir);
        if (this.getInventory(world, centre, side) != null) return centre;
        return null;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        final CompoundTag berry = nbt.getCompound("b");
        final CompoundTag storage = nbt.getCompound("s");
        final CompoundTag empty = nbt.getCompound("e");
        if (!berry.isEmpty()) this.berryLoc = new BlockPos(berry.getInt("x"), berry.getInt("y"), berry.getInt("z"));
        else this.berryLoc = null;
        if (!storage.isEmpty())
        {
            this.storageLoc = new BlockPos(storage.getInt("x"), storage.getInt("y"), storage.getInt("z"));
            this.storageFace = Direction.values()[storage.getByte("f")];
        }
        else this.storageLoc = null;
        if (!empty.isEmpty())
        {
            this.emptyInventory = new BlockPos(empty.getInt("x"), empty.getInt("y"), empty.getInt("z"));
            this.emptyFace = Direction.values()[empty.getByte("f")];
        }
        else this.emptyInventory = null;
    }

    private boolean doBerryCheck(final IItemHandlerModifiable pokemobInv)
    {
        // If you have a berry stack elsewhere, swap it into first slot in
        // inventory.
        if (this.berrySlotIndex != -1 && this.berrySlotIndex != 2 && this.hasBerries)
        {
            final ItemStack stack1 = pokemobInv.getStackInSlot(this.berrySlotIndex);
            pokemobInv.setStackInSlot(this.berrySlotIndex, pokemobInv.getStackInSlot(2));
            pokemobInv.setStackInSlot(2, stack1);
            this.berrySlotIndex = 2;
            // Retrun false to allow storage check.
            return false;
        }
        // Has a berry already, can pass through to storage check.
        if (this.hasBerries) return false;
        // No room to pick up a berry if it wanted to, so can pass through
        // to
        // storage check.
        if (this.emptySlots == 0) return false;
        // No Berry Storage, so move to normal storage checks.
        if (!this.findBerryStorage(false)) return false;
        IItemHandlerModifiable berries = this.getInventory(this.world, this.berryLoc, null);
        // No Storage at berryLoc.
        if (berries == null && !this.findBerryStorage(true))
        {
            this.berryLoc = null;
            return false;
        }
        // Second pass to find storage.
        if (berries == null) berries = this.getInventory(this.world, this.berryLoc, null);
        if (berries == null) return false;
        // No Berries in storage.
        if (!this.hasItem(HungerTask.FOODTAG, berries)) return false;
        if (this.pokemob.getEntity().blockPosition().distSqr(this.berryLoc) > 9)
        {
            this.pathing = true;
            final double speed = 1;
            this.setWalkTo(this.berryLoc, speed, 0);
            // We should be pathing to berries, so return true to stop other
            // storage tasks.
            // PokecubeCore.LOGGER.debug(this.pokemob.getDisplayName().getUnformattedComponentText()
            // + " Pathing to Berries at " + this.berryLoc);
            return true;
        }
        for (int i = 0; i < Math.min(berries.getSlots(), StoreTask.MAXSIZE); i++)
        {
            final ItemStack stack = berries.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBerry)
            {
                berries.setStackInSlot(i, ItemStack.EMPTY);
                pokemobInv.setStackInSlot(this.firstEmpty, pokemobInv.getStackInSlot(2));
                pokemobInv.setStackInSlot(2, stack);
                // Collected our berry, Can pass to storage now.
                return false;
            }
        }
        this.pathing = false;
        return false;
    }

    private boolean doEmptyCheck(final IItemHandlerModifiable pokemobInv)
    {
        // Can only pick up item if we have a free slot for it.
        if (this.emptySlots == 0) return false;
        // Return true here to make the cooldown not 5x, this means we don't
        // have a setting for empty, so no need to run this AI.
        if (!this.findEmptyStorage(false)) return false;
        IItemHandlerModifiable inventory = this.getInventory(this.world, this.emptyInventory, this.emptyFace);
        // No inventory to empty
        if (inventory == null && !this.findEmptyStorage(true))
        {
            this.emptyInventory = null;
            return false;
        }
        // Second pass to find storage.
        if (inventory == null) inventory = this.getInventory(this.world, this.emptyInventory, this.emptyFace);
        if (inventory == null) return false;
        // No items to empty
        if (!this.hasItem(null, inventory)) return false;
        // Path to the inventory.
        if (this.pokemob.getEntity().blockPosition().distSqr(this.emptyInventory) > 9)
        {
            this.pathing = true;
            final double speed = 1;
            this.setWalkTo(this.emptyInventory, speed, 0);
            // We should be pathing, so return true.
            return true;
        }
        boolean collected = false;
        int start = 0;
        inv:
        for (int slot = this.firstEmpty; slot < pokemobInv.getSlots(); slot++)
            if (pokemobInv.getStackInSlot(slot).isEmpty())
                for (int i = start; i < Math.min(inventory.getSlots(), StoreTask.MAXSIZE); i++)
        {
            final ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && checkValid(stack))
            {
                inventory.setStackInSlot(i, ItemStack.EMPTY);
                pokemobInv.setStackInSlot(slot, stack);
                // Collected our item successfully
                collected = true;
                start = i + 1;
                continue inv;
            }
        }
        this.pathing = false;
        return collected;
    }

    private boolean doStorageCheck(final IItemHandlerModifiable pokemobInv)
    {
        // Only dump inventory if no free slots.
        if (this.emptySlots > 2) return false;
        // No ItemStorage
        if (!this.findItemStorage(false)) return false;
        // check if should path to storage.
        if (this.pokemob.getEntity().blockPosition().distSqr(this.storageLoc) > 9)
        {
            this.pathing = true;
            final double speed = 1;
            this.setWalkTo(this.storageLoc, speed, 0);
            // We should be pathing to storage here, so return true.
            return true;
        }
        IItemHandlerModifiable storage = this.getInventory(this.world, this.storageLoc, this.storageFace);
        // if Somehow have no storage, should return false.
        if (storage == null && !this.findItemStorage(true)) return false;
        // Second pass to find storage.
        if (storage == null) storage = this.getInventory(this.world, this.storageLoc, this.storageFace);
        if (storage == null) return false;
        // Store every item after berry slot
        for (int i = 3; i < pokemobInv.getSlots(); i++)
        {
            ItemStack stack = pokemobInv.getStackInSlot(i);
            // final ItemStack prev = stack.copy();
            if (ItemStackTools.addItemStackToInventory(stack, storage, 0))
            {
                if (stack.isEmpty()) stack = ItemStack.EMPTY;
                pokemobInv.setStackInSlot(i, stack);
            }
        }
        this.pathing = false;
        return true;
    }

    private boolean findBerryStorage(final boolean refresh)
    {
        if (!refresh && this.berryLoc != null && this.pokemob.getGeneralState(GeneralStates.TAMED)) return true;
        if (this.berryLoc != null && refresh)
        {
            BlockPos found = this.checkDir(this.world, null, this.berryLoc, null);
            if (found == null) for (final Direction dir : Direction.Plane.HORIZONTAL)
            {
                found = this.checkDir(this.world, dir, this.berryLoc, null);
                if (found != null) break;
            }
            if (found == null) found = this.checkDir(this.world, Direction.DOWN, this.berryLoc, null);
            if (found == null) found = this.checkDir(this.world, Direction.UP, this.berryLoc, null);
            if (found != null) this.berryLoc = found;
        }
        return this.berryLoc != null;
    }

    private boolean findEmptyStorage(final boolean refresh)
    {
        if (this.emptyInventory != null && refresh)
        {
            BlockPos found = this.checkDir(this.world, null, this.emptyInventory, this.emptyFace);
            if (found == null) for (final Direction dir : Direction.Plane.HORIZONTAL)
            {
                found = this.checkDir(this.world, dir, this.emptyInventory, this.emptyFace);
                if (found != null) break;
            }
            if (found == null) found = this.checkDir(this.world, Direction.DOWN, this.emptyInventory, this.emptyFace);
            if (found == null) found = this.checkDir(this.world, Direction.UP, this.emptyInventory, this.emptyFace);
            if (found != null) this.emptyInventory = found;
        }
        return this.emptyInventory != null && this.emptyInventory.distSqr(this.pokemob.getHome()) < 256;
    }

    private boolean findItemStorage(final boolean refresh)
    {
        if (!refresh && this.storageLoc != null && this.pokemob.getGeneralState(GeneralStates.TAMED)) return true;
        if (this.storageLoc != null && refresh)
        {
            BlockPos found = this.checkDir(this.world, null, this.storageLoc, this.storageFace);
            if (found == null) for (final Direction dir : Direction.Plane.HORIZONTAL)
            {
                found = this.checkDir(this.world, dir, this.storageLoc, this.storageFace);
                if (found != null) break;
            }
            if (found == null) found = this.checkDir(this.world, Direction.DOWN, this.storageLoc, this.storageFace);
            if (found == null) found = this.checkDir(this.world, Direction.UP, this.storageLoc, this.storageFace);
            if (found != null) this.storageLoc = found;
        }
        return this.storageLoc != null;
    }

    @Override
    public String getIdentifier()
    {
        return "store_stuff";
    }

    public IItemHandlerModifiable getInventory(final BlockGetter world, final BlockPos pos, final Direction side)
    {
        if (pos == null) return null;
        if (!this.canBreak(world, pos)) return null;
        final BlockEntity tile = world.getBlockEntity(pos);
        if (tile == null) return null;
        IItemHandler handler;
        if ((handler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)
                .orElse(null)) instanceof IItemHandlerModifiable)
            return (IItemHandlerModifiable) handler;
        return null;
    }

    private boolean canBreak(final BlockGetter world, final BlockPos pos)
    {
        if (!this.pokemob.isPlayerOwned()) return true;
        if (this.knownValid.contains(pos)) return true;
        // TODO decide on what to do here later, for now, only let this run if
        // owner is online.
        if (this.pokemob.getOwner() == null) return false;
        final Player player = (Player) this.pokemob.getOwner();
        final BreakEvent evt = new BreakEvent(player.getLevel(), pos, world.getBlockState(pos), player);
        MinecraftForge.EVENT_BUS.post(evt);
        if (evt.isCanceled()) return false;
        this.knownValid.add(pos.immutable());
        return true;
    }

    private boolean hasItem(final ResourceLocation tag, final IItemHandlerModifiable inventory)
    {
        for (int i = 0; i < Math.min(inventory.getSlots(), StoreTask.MAXSIZE); i++)
        {
            final ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (tag == null) return true;
            if (ItemList.is(tag, stack)) return true;
        }
        return false;
    }

    @Override
    public void reset()
    {
        this.pathing = false;
    }

    @Override
    public void run()
    {}

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag tag = new CompoundTag();
        final CompoundTag berry = new CompoundTag();
        final CompoundTag storage = new CompoundTag();
        final CompoundTag empty = new CompoundTag();
        if (this.berryLoc != null)
        {
            berry.putInt("x", this.berryLoc.getX());
            berry.putInt("y", this.berryLoc.getY());
            berry.putInt("z", this.berryLoc.getZ());
        }
        if (this.storageLoc != null)
        {
            storage.putInt("x", this.storageLoc.getX());
            storage.putInt("y", this.storageLoc.getY());
            storage.putInt("z", this.storageLoc.getZ());
            storage.putByte("f", (byte) this.storageFace.ordinal());
        }
        if (this.emptyInventory != null)
        {
            empty.putInt("x", this.emptyInventory.getX());
            empty.putInt("y", this.emptyInventory.getY());
            empty.putInt("z", this.emptyInventory.getZ());
            empty.putByte("f", (byte) this.emptyFace.ordinal());
        }
        tag.put("b", berry);
        tag.put("s", storage);
        tag.put("e", empty);
        return tag;
    }

    @Override
    public boolean shouldRun()
    {
        if (!this.pokemob.isRoutineEnabled(AIRoutine.STORE) || this.pokemob.getHome() == null) return false;
        return true;
    }

    @Override
    public boolean sync()
    {
        return true;
    }

    /**
     * Only tame pokemobs set to "stay" should run this AI.
     *
     * @return
     */
    private boolean tameCheck()
    {
        return this.pokemob.getGeneralState(GeneralStates.TAMED)
                && !this.pokemob.getGeneralState(GeneralStates.STAYING);
    }

    @Override
    public void tick()
    {
        if (this.tameCheck()) return;
        boolean stuff = false;
        if (this.searchInventoryCooldown-- < 0)
        {
            this.searchInventoryCooldown = StoreTask.COOLDOWN;
            this.findBerryStorage(true);
            stuff = this.findItemStorage(true);
            if (!stuff) this.searchInventoryCooldown = 50 * StoreTask.COOLDOWN;
        }
        final IItemHandlerModifiable itemhandler = new InvWrapper(this.pokemob.getInventory());
        if (this.doBerryCheck(itemhandler) || this.doStorageCheck(itemhandler) || this.doEmptyCheck(itemhandler))
            this.doStorageCooldown = 5;
        else this.doStorageCooldown = 10 * StoreTask.COOLDOWN;
    }
}
