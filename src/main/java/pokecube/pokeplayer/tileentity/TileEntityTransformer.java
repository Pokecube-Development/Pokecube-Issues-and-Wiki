package pokecube.pokeplayer.tileentity;

import java.util.List;
import java.util.Random;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IClearable;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.Tools;
import pokecube.pokeplayer.EventsHandler;
import pokecube.pokeplayer.PokeInfo;
import pokecube.pokeplayer.Pokeplayer;
import pokecube.pokeplayer.Reference;
import pokecube.pokeplayer.block.PokeTransformContainer;
import pokecube.pokeplayer.init.TileEntityInit;
import pokecube.pokeplayer.network.PacketTransform;
import thut.core.common.handlers.PlayerDataHandler;

public class TileEntityTransformer extends LockableLootTileEntity implements IClearable, INamedContainerProvider //ISidedInventory //extends InteractableTile //implements ITickable, IInventory
{		
	protected NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
	
	ItemStack stack    = ItemStack.EMPTY;
    int[]     nums     = {};
    int       lvl      = 5;
    boolean   random   = false;
    boolean   pubby    = false;
    boolean	  edit	   = false;
    int       stepTick = 20;
	
	public TileEntityTransformer(TileEntityType<?> tileEntityType) {
		super(tileEntityType);
	}
	
	public TileEntityTransformer() {
		this(TileEntityInit.TRANSFORM_TILE.get());
	}
	

	@Override
	public void read(BlockState state, CompoundNBT nbt) {
		super.read(state, nbt);
		if (nbt.contains("stack"))
        {
            CompoundNBT tag = nbt.getCompound("stack");
            stack.setTag(tag);
        }
		this.items = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
		if (nbt.contains("nums")) nums = nbt.getIntArray("nums");
	    if (nbt.contains("lvl")) lvl = nbt.getInt("lvl");
	    stepTick = nbt.getInt("stepTick");
	    random = nbt.getBoolean("random");
	    pubby = nbt.getBoolean("public");
		ItemStackHelper.loadAllItems(nbt, this.items);
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		ItemStackHelper.saveAllItems(compound, this.items);
		if (stack.isEmpty())
        {
			CompoundNBT tag = new CompoundNBT();
            stack.write(tag);
            compound.put("stack", tag);
        }
		if (nums != null) compound.putIntArray("nums", nums);
		compound.putInt("lvl", lvl);
		compound.putInt("stepTick", stepTick);
		compound.putBoolean("random", random);
		compound.putBoolean("public", pubby);
		return compound;
	}
	
	@Override
	public NonNullList<ItemStack> getItems() {
		return this.items;
	}
	
	@Override
	protected void setItems(NonNullList<ItemStack> itemsIn) {
		this.items = itemsIn;
	}
	
	@Override
	public void markDirty() {
		super.markDirty();
		this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), net.minecraftforge.common.util.Constants.BlockFlags.BLOCK_UPDATE);
	}
	
	@Override
	protected ITextComponent getDefaultName() {
		return new TranslationTextComponent("container." + Reference.ID + ".transform");
	}
	
	@Override
	protected Container createMenu(int id, PlayerInventory player) {
		return new PokeTransformContainer(id, player, this);
	}
	
	@Override
	public int getSizeInventory() {
		return this.items.size();
	}
	
	@Override
	public boolean isEmpty() {
		for (ItemStack stack: this.items) {
			if(!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public ItemStack getStackInSlot(int index) {
		return this.items.get(index);
	}
	
	@Override
	public ItemStack decrStackSize(int index, int count) {
		return ItemStackHelper.getAndSplit(this.items, index, count);
	}
	
	@Override
	public ItemStack removeStackFromSlot(int index) {
		return ItemStackHelper.getAndRemove(this.items, index);
	}
	
	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		ItemStack itemStack = this.items.get(index);
		boolean flag = !stack.isEmpty() && stack.isItemEqual(itemStack)
						&& ItemStack.areItemStackTagsEqual(stack, itemStack);
		this.items.set(index, stack);
		if(stack.getCount() > this.getInventoryStackLimit()) {
			stack.setCount(this.getInventoryStackLimit());
		}
		
		if(!flag) {
			this.markDirty();
		}
	}
	
	@Override
	public boolean isUsableByPlayer(PlayerEntity player) {
		if(this.world.getTileEntity(pos) != this) {
			return false;
		}
		else {
			return player.getDistanceSq((double) this.pos.getX() + 0.5D, (double) this.getPos().getY() + 0.5D,
								(double) this.pos.getZ() + 0.5D) <= 64.0D;
		}
	}
	
	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return !stack.isDamaged();
	}
	
	@Override
	public void clear() {
		super.clear();
		this.items.clear();
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT nbt = new CompoundNBT();
		this.write(nbt);
		
		return new SUpdateTileEntityPacket(this.getPos(), 1, nbt);
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		BlockState blockState = world.getBlockState(pos);
		this.read(blockState, pkt.getNbtCompound());
	}
	
	@Override
	public CompoundNBT getUpdateTag() {
		return this.write(new CompoundNBT());
	}
	
	@Override
	public void handleUpdateTag(BlockState state, CompoundNBT tag) {
		this.read(state, tag);
	}
    
	public ItemStack getStack(ItemStack stack)
    {
        return stack;
    }

    public void onInteract(PlayerEntity player)
    {
        if (getWorld().isRemote || random) return;
        if (canEdit(player) || pubby)
        {
            if (!stack.isEmpty() && PokecubeManager.isFilled(player.getHeldItemMainhand()))
            {
                getStack(player.getHeldItemMainhand());
                player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
                Pokeplayer.LOGGER.debug("Slot");
            }
            else
            {
                Tools.giveItem(player, stack);
                stack = ItemStack.EMPTY;
            }
        }
    }
	
	private boolean canEdit(PlayerEntity player) {
		return player.isAllowEdit();
	}

	public void onWalkedOn(Entity entityIn)
    {
        if (getWorld().isRemote || stepTick > 0) return;
        PlayerEntity player = (PlayerEntity) entityIn.getEntity();
        PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
        boolean isPokemob = info.getPokemob(world) != null;
        if ((stack.isEmpty() || random) && !isPokemob)
        {
            IPokemob pokemob = getPokemob();
            if (pokemob != null) {
            	Pokeplayer.proxyProxy.setPokemob(player, pokemob);
            }
            if (pokemob != null)
            {
            	Pokeplayer.LOGGER.debug("Test");
            	stack = ItemStack.EMPTY;
                stepTick = 50;
            }
            EventsHandler.sendUpdate(player);
            ServerWorld worldIn = (ServerWorld) player.getEntityWorld();
            for (PlayerEntity player2 : worldIn.getPlayers())
            {
            	PacketTransform.sendPacket(player, (ServerPlayerEntity) player2);
            }
            return;
        }
        else if (!stack.isEmpty() && !random && isPokemob)
        {
            stepTick = 50;
            IPokemob poke = Pokeplayer.proxyProxy.getPokemob(player);
            CompoundNBT tag = poke.getEntity().serializeNBT();
            poke.setPokemonNickname(tag.getString("oldName"));
            tag.remove("oldName");
            tag.remove("is_a_Player");
            tag.remove("playerID");
            ItemStack pokemob = PokecubeManager.pokemobToItem(poke);
            if (player.abilities.allowFlying)
            {
                player.abilities.allowFlying = false;
                player.sendPlayerAbilities();
            }
            Pokeplayer.proxyProxy.setPokemob(player, null);
            stack = pokemob;
            EventsHandler.sendUpdate(player);
            ServerWorld worldIn = (ServerWorld) player.getEntityWorld();
            for (PlayerEntity player2 : worldIn.getPlayers())
            {
               PacketTransform.sendPacket(player, (ServerPlayerEntity) player2);
            }
            return;
        }
        else if (random && isPokemob)
        {
            stepTick = 50;
            IPokemob poke = Pokeplayer.proxyProxy.getPokemob(player);
            CompoundNBT tag = poke.getEntity().getPersistentData();
            poke.setPokemonNickname(tag.getString("oldName"));
            tag.remove("oldName");
            tag.remove("is_a_Player");
            tag.remove("playerID");
            player.abilities.allowFlying = false;
            player.sendPlayerAbilities();
            Pokeplayer.proxyProxy.setPokemob(player, null);
            stack = ItemStack.EMPTY;
            EventsHandler.sendUpdate(player);
            ServerWorld worldIn = (ServerWorld) player.getEntityWorld();
            for (PlayerEntity player2 : worldIn.getPlayers())
            {
                PacketTransform.sendPacket(player, (ServerPlayerEntity) player2);
            }
            return;
        }
    }

    private IPokemob getPokemob()
    {
        if (random)
        {
            int num = 0;
            if (nums != null && nums.length > 0)
            {
                num = nums[new Random().nextInt(nums.length)];
            }
            else
            {
                List<Integer> numbers = Lists.newArrayList(Database.data.keySet());
                num = numbers.get(getWorld().rand.nextInt(numbers.size()));
            }
            Entity entity = PokecubeCore.createPokemob(Database.getEntry(num), getWorld());
            IPokemob pokemob = CapabilityPokemob.getPokemobFor(entity);
            if (entity != null)
            {
                pokemob.setForSpawn(Tools.levelToXp(pokemob.getPokedexEntry().getEvolutionMode(), lvl), false);
                pokemob.spawnInit();
            }
            return pokemob;
        }
        IPokemob pokemob = PokecubeManager.itemToPokemob(stack, getWorld());
        return pokemob;
    }
//
//    @Override
//	public void read(BlockState state, CompoundNBT nbt) 
//    {
//        super.read(state, nbt);
//        if (nbt.contains("stack"))
//        {
//            CompoundNBT tag = nbt.getCompound("stack");
//            stack.setTag(tag);
//        }
//        if (nbt.contains("nums")) nums = nbt.getIntArray("nums");
//        if (nbt.contains("lvl")) lvl = nbt.getInt("lvl");
//        stepTick = nbt.getInt("stepTick");
//        random = nbt.getBoolean("random");
//        pubby = nbt.getBoolean("public");
//        
//        if (!this.checkLootAndRead(nbt)) {
//		this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
//		}
//		ItemStackHelper.loadAllItems(nbt, this.stacks);
//
//    }
//
//    public void setStack(ItemStack stack)
//    {
//        this.stack = stack;
//    }
//
//    @Override
//    public CompoundNBT write(CompoundNBT tagCompound)
//    {
//        super.write(tagCompound);
//        if (stack.isEmpty())
//        {
//            CompoundNBT tag = new CompoundNBT();
//            stack.write(tag);
//            tagCompound.contains("stack");
//        }
//        if (nums != null) tagCompound.putIntArray("nums", nums);
//        tagCompound.putInt("lvl", lvl);
//        tagCompound.putInt("stepTick", stepTick);
//        tagCompound.putBoolean("random", random);
//        tagCompound.putBoolean("public", pubby);
//		
//        if (!this.checkLootAndWrite(tagCompound)) {
//			ItemStackHelper.saveAllItems(tagCompound, this.stacks);
//		}
//
//        return tagCompound;
//    }
//
//    @Override
//	public SUpdateTileEntityPacket getUpdatePacket() {
//		return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
//	}
//    
//    private final LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.values());
//	@Override
//	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
//		if (!this.removed && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
//			return handlers[facing.ordinal()].cast();
//		return super.getCapability(capability, facing);
//	}
//
//	@Override
//	public void remove() {
//		super.remove();
//		for (LazyOptional<? extends IItemHandler> handler : handlers)
//			handler.invalidate();
//	}
//
//
//	@Override
//	public int getSizeInventory() {
//		return stacks.size();
//	}
//
//	@Override
//	public CompoundNBT getUpdateTag() {
//		return this.write(new CompoundNBT());
//	}
//	
//	@Override
//	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
//		this.read(null, pkt.getNbtCompound());
//	}
//
//	@Override
//	public boolean isEmpty() {
//		for (ItemStack itemstack : this.stacks)
//			if (!itemstack.isEmpty())
//				return false;
//		return true;
//	}
//	
//	@Override
//	public int getInventoryStackLimit() {
//		return 1;
//	}
//
//	@Override
//	public int[] getSlotsForFace(Direction side) {
//		return IntStream.range(0, this.getSizeInventory()).toArray();
//	}
//
//	@Override
//	public boolean canInsertItem(int index, ItemStack stack, @Nullable Direction direction) {
//		return this.isItemValidForSlot(index, stack);
//	}
//
//	@Override
//	public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
//		return true;
//	}
//
//	@Override
//	protected NonNullList<ItemStack> getItems() {
//		return this.stacks;
//	}
//
//	@Override
//	protected void setItems(NonNullList<ItemStack> stacks) {
//		this.stacks = stacks;
//	}
//
//	@Override
//	public boolean isItemValidForSlot(int index, ItemStack stack) {
//		return true;
//	}
//
//	@Override
//	public ITextComponent getDefaultName() {
//		return new StringTextComponent("A");
//	}
//
//	@Override
//	public Container createMenu(int id, PlayerInventory player) {
//		return ChestContainer.createGeneric9X3(id, player, this);
//	}
}