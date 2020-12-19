package pokecube.pokeplayer.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;
import pokecube.core.entity.pokemobs.AnimalChest;
import pokecube.core.interfaces.IPokemob;
import pokecube.pokeplayer.PokeInfo;
import pokecube.pokeplayer.PokePlayer;

public class InventoryPlayerPokemob extends AnimalChest
{
    final PokeInfo info;

    public InventoryPlayerPokemob(PokeInfo info, World world)
    {
        super();
        for (int i = 0; i < info.getPokemob(world).getInventory().getSizeInventory(); i++)
        {
            this.setInventorySlotContents(i, info.getPokemob(world).getInventory().getStackInSlot(i));
        }
        this.info = info;
    }

    public InventoryPlayerPokemob(AnimalChest inventory)
    {
        super();
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            this.setInventorySlotContents(i, inventory.getStackInSlot(i));
        }
        this.info = null;
    }

    public void saveToPokemob(IPokemob pokemob, PlayerEntity player)
    {
        IInventory inventory = pokemob.getInventory();
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            inventory.setInventorySlotContents(i, this.getStackInSlot(i));
        }
        if (info != null)
        {
            info.save(player);
        }
    }

    public void syncFromPokemob(IPokemob pokemob)
    {
        IInventory inventory = pokemob.getInventory();
        for (int i = 0; i < inventory.getSizeInventory(); i++)
        {
            this.setInventorySlotContents(i, inventory.getStackInSlot(i));
        }
    }

    @Override
    public void openInventory(PlayerEntity player)
    {
    }

    @Override
    public void closeInventory(PlayerEntity player)
    {
        if (player.getEntityWorld().isRemote) return;
        IPokemob e = PokePlayer.proxyProxy.getPokemob(player);
        saveToPokemob(e, player);
    }
}
