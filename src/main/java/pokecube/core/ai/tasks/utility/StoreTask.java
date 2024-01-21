package pokecube.core.ai.tasks.utility;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.AIRoutine;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.utils.BookInstructionsParser;
import pokecube.core.ai.tasks.idle.HungerTask;
import thut.api.ThutCaps;
import thut.api.entity.event.BreakTestEvent;
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
    public static final String KEY = "store_stuff";

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
    private BlockPos pathTarget = null;

    protected ItemStack instructionsCache = ItemStack.EMPTY;
    protected List<ResourceLocation> keys = Lists.newArrayList();

    private IItemHandlerModifiable taskInventory;
    private IItemHandlerModifiable pokeInventory;

    private final Set<BlockPos> knownValid = Sets.newHashSet();

    private boolean initialised = false;

    public StoreTask(final IPokemob entity)
    {
        super(entity);
    }

    private void checkHeldItem()
    {
        ItemStack stack = pokemob.getEntity().getOffhandItem();
        if (stack != this.instructionsCache)
        {
            this.instructionsCache = stack;
            keys.clear();
            List<String> instructions = BookInstructionsParser.getInstructions(stack, "item filters", false);
            for (String line : instructions)
            {
                try
                {
                    ResourceLocation res = new ResourceLocation(line);
                    keys.add(res);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    protected boolean checkValid(ItemStack item_or_block)
    {
        checkHeldItem();
        for (ResourceLocation l : keys) if (ItemList.is(l, item_or_block)) return true;
        return keys.isEmpty();
    }

    protected boolean checkValid(BlockState item_or_block)
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
        final IItemHandlerModifiable itemhandler = getPokeInventory();

        // Search inventory for free slots or berries.
        // We only use 5 of the slots, so starting at 2.
        for (int i = 2; i < Math.min(7, itemhandler.getSlots()); i++)
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

        // If the item after the berry slot has an inventory, we use that.
        ItemStack pastBerry = itemhandler.getStackInSlot(3);
        var inv = ThutCaps.getInventory(pastBerry);
        if (inv instanceof IItemHandlerModifiable mod) setTaskInventory(mod);
        else setTaskInventory(itemhandler);
    }

    private BlockPos checkDir(final ServerLevel world, final Direction dir, BlockPos centre, final Direction side)
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

    public boolean doBerryCheck(final IItemHandlerModifiable pokemobInv)
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
        var berries = this.getInventory(this.world, this.berryLoc, null);
        // No Storage at berryLoc.
        if (berries == null && !this.findBerryStorage(true))
        {
            this.berryLoc = null;
            return false;
        }
        // Second pass to find storage.
        if (berries == null) berries = this.getInventory(this.world, this.berryLoc, null);
        if (berries == null || berries.getFirst() == null) return false;
        // No Berries in storage.
        if (!this.hasItem(i -> ItemList.is(HungerTask.FOODTAG, i), berries.getFirst())) return false;
        if (this.pokemob.getEntity().blockPosition().distSqr(this.berryLoc) > 9)
        {
            this.setPathTarget(berryLoc);
            // We should be pathing to berries, so return true to stop other
            // storage tasks.
            return true;
        }
        for (int i = 0; i < Math.min(berries.getFirst().getSlots(), StoreTask.MAXSIZE); i++)
        {
            final ItemStack stack = berries.getFirst().getStackInSlot(i);
            if (ItemList.is(HungerTask.FOODTAG, stack))
            {
                berries.getFirst().setStackInSlot(i, ItemStack.EMPTY);
                pokemobInv.setStackInSlot(this.firstEmpty, pokemobInv.getStackInSlot(2));
                pokemobInv.setStackInSlot(2, stack);
                // Collected our berry, Can pass to storage now.
                return false;
            }
        }
        return false;
    }

    public boolean doEmptyCheck(final IItemHandlerModifiable taskInventory)
    {
        // Can only pick up item if we have a free slot for it.
        if (this.emptySlots == 0) return false;
        // Return true here to make the cooldown not 5x, this means we don't
        // have a setting for empty, so no need to run this AI.
        if (!this.findEmptyStorage(false)) return false;
        var inventory = this.getInventory(this.world, this.emptyInventory, this.emptyFace);
        // No inventory to empty
        if (inventory == null && !this.findEmptyStorage(true))
        {
            this.emptyInventory = null;
            return false;
        }
        // Second pass to find storage.
        if (inventory == null) inventory = this.getInventory(this.world, this.emptyInventory, this.emptyFace);
        if (inventory == null || inventory.getFirst() == null) return false;
        // No items to empty
        if (!this.hasItem(i -> checkValid(i), inventory.getFirst())) return false;

        // Path to the inventory.
        if (this.pokemob.getEntity().blockPosition().distSqr(this.emptyInventory) > 9)
        {
            this.setPathTarget(emptyInventory);
            // We should be pathing, so return true.
            return true;
        }

        boolean collected = false;
        int start = 0;
        if (inventory.getSecond() != null)
        {
            int endSlot = taskInventory.getSlots();
            if (taskInventory == getPokeInventory()) endSlot = 7;
            WorldlyContainer container = inventory.getSecond();
            for (int i : container.getSlotsForFace(emptyFace))
            {
                inv:
                for (int slot = this.firstEmpty; slot < endSlot; slot++)
                    if (taskInventory.getStackInSlot(slot).isEmpty())
                {
                    ItemStack stack = container.getItem(i);
                    if (container.canTakeItemThroughFace(i, stack, emptyFace) && !stack.isEmpty() && checkValid(stack))
                    {
                        container.setItem(i, ItemStack.EMPTY);
                        taskInventory.setStackInSlot(slot, stack);
                        // Collected our item successfully
                        collected = true;
                        start = i + 1;
                        continue inv;
                    }
                }
            }
        }
        else
        {
            int endSlot = taskInventory.getSlots();
            if (taskInventory == getPokeInventory()) endSlot = 7;
            int imax = Math.min(inventory.getFirst().getSlots(), StoreTask.MAXSIZE);
            inv:
            for (int slot = this.firstEmpty; slot < endSlot; slot++)
                if (taskInventory.getStackInSlot(slot).isEmpty()) for (int i = start; i < imax; i++)
            {
                final ItemStack stack = inventory.getFirst().getStackInSlot(i);
                if (!stack.isEmpty() && checkValid(stack))
                {
                    inventory.getFirst().setStackInSlot(i, ItemStack.EMPTY);
                    taskInventory.setStackInSlot(slot, stack);
                    // Collected our item successfully
                    collected = true;
                    start = i + 1;
                    if (start >= imax) break inv;
                    continue inv;
                }
            }
        }
        return collected;
    }

    public boolean doStorageCheck(final IItemHandlerModifiable taskInventory)
    {
        boolean isPoke = taskInventory == getPokeInventory();
        // Only dump inventory if no free slots.
        if (this.emptySlots > 1 && isPoke) return false;

        // Otherwise, dump if more than 1/4 fills
        if (!isPoke)
        {
            int size = taskInventory.getSlots();
            int threshold = 3 * size / 4;
            // Count the number of empty slots
            int count = 0;
            for (int i = 0; i < size; i++)
            {
                if (taskInventory.getStackInSlot(i).isEmpty()) count++;
                // If we have more empty than needed, return.
                if (count >= threshold) return false;
            }
        }

        // No ItemStorage
        if (!this.findItemStorage(false)) return false;
        // check if should path to storage.
        if (setPathTarget(storageLoc))
        {
            // We should be pathing to storage here, so return true.
            return true;
        }
        var storage = this.getInventory(this.world, this.storageLoc, this.storageFace);
        // if Somehow have no storage, should return false.
        if (storage == null && !this.findItemStorage(true)) return false;
        // Second pass to find storage.
        if (storage == null) storage = this.getInventory(this.world, this.storageLoc, this.storageFace);
        if (storage == null || storage.getFirst() == null) return false;

        int endSlot = taskInventory.getSlots();
        int startSlot = 0;
        if (taskInventory == getPokeInventory())
        {
            endSlot = 7;
            startSlot = 3;
        }

        // Store every item after berry slot except offhand
        for (int i = startSlot; i < endSlot; i++)
        {
            ItemStack stack = taskInventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            if (storage.getSecond() != null)
            {
                WorldlyContainer container = storage.getSecond();
                if (ItemStackTools.addItemStackToInventory(stack, container, 0, storageFace))
                {
                    if (stack.isEmpty()) stack = ItemStack.EMPTY;
                    taskInventory.setStackInSlot(i, stack);
                }
            }
            else
            {
                if (ItemStackTools.addItemStackToInventory(stack, storage.getFirst(), 0))
                {
                    if (stack.isEmpty()) stack = ItemStack.EMPTY;
                    taskInventory.setStackInSlot(i, stack);
                }
            }
        }
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

    public Pair<IItemHandlerModifiable, WorldlyContainer> getInventory(final ServerLevel world, final BlockPos pos,
            final Direction side)
    {
        if (pos == null) return null;
        if (!this.canBreak(world, pos)) return null;
        final BlockEntity tile = world.getBlockEntity(pos);
        if (tile == null) return null;

        WorldlyContainer container = tile instanceof WorldlyContainer cont ? cont : null;
        IItemHandlerModifiable inventory = null;
        if ((ThutCaps.getInventory(tile, side)) instanceof IItemHandlerModifiable inv) inventory = inv;
        if (inventory == null && container == null) return null;
        return Pair.of(inventory, container);
    }

    @SuppressWarnings("deprecation")
    public boolean canBreak(final ServerLevel world, final BlockPos pos)
    {
        if (!world.hasChunkAt(pos)) return false;
        if (!this.pokemob.isPlayerOwned()) return true;
        if (this.knownValid.contains(pos)) return true;
        // TODO decide on what to do here later, for now, only let this run if
        // owner is online.
        if (!(pokemob.getOwner() instanceof Player player)) return false;
        if (!BreakTestEvent.testBreak(player.getLevel(), pos, world.getBlockState(pos), player)) return false;
        this.knownValid.add(pos.immutable());
        return true;
    }

    private boolean hasItem(Predicate<ItemStack> valid, final IItemHandlerModifiable inventory)
    {
        for (int i = 0; i < Math.min(inventory.getSlots(), StoreTask.MAXSIZE); i++)
        {
            final ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (valid.test(stack)) return true;
        }
        return false;
    }

    public boolean setPathTarget(BlockPos pos)
    {
        // If already close enough, then don't try to path.
        if (pos != null && pos.distManhattan(entity.getOnPos()) < 2 + entity.getBbWidth()) pos = null;
        this.pathTarget = pos;
        this.pathing = pos != null;
        return pathing;
    }

    @Override
    public void reset()
    {
        this.pathing = false;
        this.pathTarget = null;
    }

    @Override
    public void run()
    {
        if (this.pathing)
        {
            if (pathTarget != null)
            {
                this.setWalkTo(pathTarget, 1.0, 0);
                // This will reset us if we are close enough.
                setPathTarget(pathTarget);
            }
            else this.pathing = false;
        }
    }

    @Override
    public String getIdentifier()
    {
        return KEY;
    }

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
        if (pathing && this.pokemob.getHome() != null) return true;
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
        if (this.pathing) return;
        if (!this.initialised)
        {
            this.initialised = true;
            if (pokemob.getInventory() instanceof SimpleContainer container)
            {
                container.addListener(this);
                // Initialize this.
                this.containerChanged(pokemob.getInventory());
            }
        }
        if (this.tameCheck()) return;
        boolean stuff = false;
        if (this.searchInventoryCooldown-- < 0)
        {
            this.searchInventoryCooldown = StoreTask.COOLDOWN;
            // Re-initialise this.
            this.containerChanged(pokemob.getInventory());
            this.findBerryStorage(true);
            stuff = this.findItemStorage(true);
            if (!stuff) this.searchInventoryCooldown = 50 * StoreTask.COOLDOWN;
        }
        // Berry check uses poke inventory, the others use task inventory
        if (this.doBerryCheck(getPokeInventory()) || this.doStorageCheck(getTaskInventory())
                || this.doEmptyCheck(getTaskInventory()))
            this.doStorageCooldown = 5;
        else this.doStorageCooldown = 10 * StoreTask.COOLDOWN;
    }

    public IItemHandlerModifiable getPokeInventory()
    {
        if (pokeInventory == null) this.pokeInventory = new InvWrapper(this.pokemob.getInventory());
        return pokeInventory;
    }

    public IItemHandlerModifiable getTaskInventory()
    {
        if (taskInventory == null) this.setTaskInventory(getPokeInventory());
        return taskInventory;
    }

    public void setTaskInventory(IItemHandlerModifiable taskInventory)
    {
        this.taskInventory = taskInventory;
    }
}
