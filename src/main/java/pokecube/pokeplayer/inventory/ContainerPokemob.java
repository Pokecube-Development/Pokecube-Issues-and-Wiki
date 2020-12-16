package pokecube.pokeplayer.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokemob;
import pokecube.pokeplayer.PokeInfo;
import pokecube.pokeplayer.PokePlayer;
import thut.core.common.handlers.PlayerDataHandler;

public class ContainerPokemob extends Container
{
	private IInventory	pokemobInv;
	private PlayerEntity playerEntity;
	public ContainerPokemob(ContainerType<?> type, int id)
	{
		super(type, id);
		PlayerEntity player = playerEntity;
	    final IPokemob e = PokePlayer.proxyProxy.getPokemob(player);
	    final IInventory pokeInv;
        PokeInfo info = PlayerDataHandler.getInstance().getPlayerData(player).getData(PokeInfo.class);
        pokeInv = info.pokeInventory;
	    IInventory playerInv = player.inventory;
		this.pokemobInv = pokeInv;
		byte b0 = 3;
		pokeInv.openInventory(null);
		int i = (b0 - 4) * 18;
		int slot = 0;
		this.addSlot(new Slot(pokeInv, slot++, 8, 18)
		{
			/** Check if the stack is a valid item for this slot. Always true
			 * beside for the armor slots. */
			@Override
			public boolean isItemValid(ItemStack stack)
			{
				return super.isItemValid(stack) && stack.getItem() == Items.SADDLE && !this.getHasStack();
			}
		});
		this.addSlot(new Slot(pokeInv, slot++, 8, 36)
		{
            
            /** Returns the maximum stack size for a given slot (usually the
			 * same as getInventoryStackLimit(), but 1 in the case of armor
			 * slots) */
			@Override
			public int getSlotStackLimit()
			{
				return 1;
			}

            /** Check if the stack is a valid item for this slot. Always true
			 * beside for the armor slots. */
			@Override
			public boolean isItemValid(ItemStack stack)
			{
				return PokecubeItems.isValidHeldItem(stack);
			}
		    
            @Override
            public ItemStack onTake(PlayerEntity playerIn, ItemStack stack)
            {
                ItemStack old = getStack();
//                if(FMLCommonHandler.instance().getEffectiveSide() == Dist.DEDICATED_SERVER)
//                {
                    e.getPokedexEntry().onHeldItemChange(stack, old, e);
//                }
                return super.onTake(playerIn, stack);
            }

			/**
             * Helper method to put a stack in the slot.
             */
            @Override
            public void putStack(ItemStack stack)
            {
                super.putStack(stack);
//                if(FMLCommonHandler.instance().getEffectiveSide() == Dist.DEDICATED_SERVER)
//                {
                    e.setHeldItem(stack);
//                }
            }
		});
		int j;
		int k;

		for (j = 0; j < 1; ++j)
		{
			for (k = 0; k < 5; ++k)
			{
				this.addSlot(new Slot(pokeInv, slot++, 80 + k * 18, 18 + j * 18)
				{
					/** Check if the stack is a valid item for this slot. Always
					 * true beside for the armor slots. */
					@Override
					public boolean isItemValid(ItemStack stack)
					{
						return PokecubeItems.isValidHeldItem(stack);
					}
				});
			}
		}
		slot = 0;
        for (j = 0; j < 9; ++j)
        {
            this.addSlot(new Slot(playerInv, slot++, 8 + j * 18, 160 + i));
        }

		for (j = 0; j < 3; ++j)
		{
			for (k = 0; k < 9; ++k)
			{
				this.addSlot(new Slot(playerInv, slot++, 8 + k * 18, 102 + j * 18 + i));
			}
		}
	}

	@Override
	public boolean canInteractWith(PlayerEntity player)
	{
		return true;
	}

	/** Called when the container is closed. */
	@Override
	public void onContainerClosed(PlayerEntity player)
	{
		super.onContainerClosed(player);
		this.pokemobInv.closeInventory(player);
	}

	/** Called when a player shift-clicks on a slot. You must override this or
	 * you will crash when someone does that. */
	@Override
	public ItemStack transferStackInSlot(PlayerEntity player, int slotId)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(slotId);

		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (slotId < this.pokemobInv.getSizeInventory())
			{
				if (!this.mergeItemStack(itemstack1, this.pokemobInv.getSizeInventory(), this.inventorySlots.size(),
						true)) { return ItemStack.EMPTY; }
			}
			else if (this.getSlot(1).isItemValid(itemstack1) && !this.getSlot(1).getHasStack())
			{
			    this.getSlot(1).putStack(slot.getStack().split(1));
			}
			else if (this.getSlot(0).isItemValid(itemstack1))
			{
				if (!this.mergeItemStack(itemstack1, 0, 1, false)) { return ItemStack.EMPTY; }
			}
			else if (this.pokemobInv.getSizeInventory() <= 2
					|| !this.mergeItemStack(itemstack1, 2, this.pokemobInv.getSizeInventory(), false)) { return ItemStack.EMPTY; }

            if (!itemstack1.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}
		}
		return itemstack;
	}
}
