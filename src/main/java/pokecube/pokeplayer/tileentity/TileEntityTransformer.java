package pokecube.pokeplayer.tileentity;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.Tools;
import pokecube.pokeplayer.EventsHandler;
import pokecube.pokeplayer.PokeInfo;
import pokecube.pokeplayer.PokePlayer;
import pokecube.pokeplayer.init.TileEntityInit;
import pokecube.pokeplayer.network.PacketTransform;
import thut.core.common.handlers.PlayerDataHandler;
import thut.core.common.network.Packet;

public class TileEntityTransformer extends LockableLootTileEntity implements ITickableTileEntity
{
	private NonNullList<ItemStack> pokeContens = NonNullList.<ItemStack>withSize(1, ItemStack.EMPTY);
	
    ItemStack stack    = ItemStack.EMPTY;
    int[]     nums     = {};
    int       lvl      = 5;
    boolean   random   = false;
    boolean   pubby    = false;
    boolean	  edit	   = false;
    int       stepTick = 20;

    protected TileEntityTransformer(TileEntityType<?> typeIn) {
		super(typeIn);
	}
    
    public TileEntityTransformer() {
		this(TileEntityInit.TRANSFORM_TILE.get());
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
                setStack(player.getHeldItemMainhand());
                player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
            }
            else
            {
                Tools.giveItem(player, stack);
                stack = ItemStack.EMPTY;
            }
        }
    }

    private boolean canEdit(PlayerEntity player) {
		return true;
	}

	public void onStepped(PlayerEntity player, World world)
    {
        if (getWorld().isRemote || stepTick > 0) return;
        PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
        boolean isPokemob = info.getPokemob(world) != null;
        if ((stack.isEmpty() || random) && !isPokemob)
        {
            IPokemob pokemob = getPokemob();
            if (pokemob != null) PokePlayer.proxyProxy.setPokemob(player, pokemob);
            if (pokemob != null)
            {
                stack = ItemStack.EMPTY;
                stepTick = 50;
            }
            EventsHandler.sendUpdate(player);
            //ServerWorld world = (ServerWorld) player.getEntityWorld();
//            for (PlayerEntity player2 : world.getEntityTracker().getTrackingPlayers(player))
//            {
//                PacketTransform.sendPacket(player, (ServerPlayerEntity) player2);
//            }
            return;
        }
        else if (!stack.isEmpty() && !random && isPokemob)
        {
            stepTick = 50;
            IPokemob poke = PokePlayer.proxyProxy.getPokemob(player);
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
            PokePlayer.proxyProxy.setPokemob(player, null);
            stack = pokemob;
            EventsHandler.sendUpdate(player);
            ServerWorld worldS = (ServerWorld) player.getEntityWorld();
            for (PlayerEntity player2 : worldS.getWorldServer().getPlayers())
            {
               // PacketTransform.sendPacket(player.pac, (ServerPlayerEntity) player2);
            }
            return;
        }
        else if (random && isPokemob)
        {
            stepTick = 50;
            IPokemob poke = PokePlayer.proxyProxy.getPokemob(player);
            CompoundNBT tag = poke.getEntity().getPersistentData();
            poke.setPokemonNickname(tag.getString("oldName"));
            tag.remove("oldName");
            tag.remove("isPlayer");
            tag.remove("playerID");
            player.abilities.allowFlying = false;
            player.sendPlayerAbilities();
            PokePlayer.proxyProxy.setPokemob(player, null);
            stack = ItemStack.EMPTY;
            EventsHandler.sendUpdate(player);
            //World world = (World) player.getEntityWorld();
//            for (PlayerEntity player2 : world.getEntityTracker().getTrackingPlayers(player))
//            {
//                PacketTransform.sendPacket(player, (ServerPlayerEntity) player2);
//            }
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

    @Override
	public void read(BlockState state, CompoundNBT nbt) 
    {
        super.read(state, nbt);
        if (nbt.contains("stack"))
        {
            CompoundNBT tag = nbt.getCompound("stack");
            stack.setTag(tag);
        }
        if (nbt.contains("nums")) nums = nbt.getIntArray("nums");
        if (nbt.contains("lvl")) lvl = nbt.getInt("lvl");
        stepTick = nbt.getInt("stepTick");
        random = nbt.getBoolean("random");
        pubby = nbt.getBoolean("public");
        
        if(!this.checkLootAndRead(nbt)) {
			this.pokeContens = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
		}
        ItemStackHelper.loadAllItems(nbt, this.pokeContens);
    }

    public void setStack(ItemStack stack)
    {
        this.stack = stack;
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound)
    {
        super.write(tagCompound);
        if (stack.isEmpty())
        {
            CompoundNBT tag = new CompoundNBT();
            stack.write(tag);
            tagCompound.contains("stack");
        }
        if (nums != null) tagCompound.putIntArray("nums", nums);
        tagCompound.putInt("lvl", lvl);
        tagCompound.putInt("stepTick", stepTick);
        tagCompound.putBoolean("random", random);
        tagCompound.putBoolean("public", pubby);
		if(!this.checkLootAndWrite(tagCompound)) {
			ItemStackHelper.saveAllItems(tagCompound, this.pokeContens);
		}
        return tagCompound;
    }

    public void tick()
    {
        stepTick--;
    }

    @Override
	public boolean isEmpty() {
		for(ItemStack itemStack : this.pokeContens)
			if(!itemStack.isEmpty())
				return false;
		return true;
	}

    @Override
	public int getSizeInventory() {
		return pokeContens.size();
	}
    
	@Override
	protected ITextComponent getDefaultName() {
		return new StringTextComponent("AAAA");
	}
	
	@Override
	public int getInventoryStackLimit() {
		return 1;
	}
	
	@Override
	protected Container createMenu(int id, PlayerInventory player) {
		return ChestContainer.createGeneric9X3(id, player, this);
	}
	
	@Override
	public ITextComponent getDisplayName() {
		return new StringTextComponent("AAAA");
	}
	
	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.pokeContens;
	}
	
	@Override
	protected void setItems(NonNullList<ItemStack> itemsIn) {
		this.pokeContens = itemsIn;
	}
	
	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
	}
	
//	@Override
//	public int[] getSlotsForFace(Direction side) {
//		return IntStream.range(0, this.getSizeInventory()).toArray();
//	}
//	
//	@Override
//	public boolean canInsertItem(int index, ItemStack itemStackIn, Direction direction) {
//		return this.isItemValidForSlot(index, itemStackIn);
//	}
//	
//	@Override
//	public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
//		return false;
//	}
	
//	private final LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.values());
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
}