package pokecube.core.inventory.pc;

import java.util.Collection;

import thut.api.inventory.big.Manager;

public class PCManager extends Manager<PCInventory>
{
    public static PCManager INSTANCE = new PCManager();

    public PCManager()
    {
        super(s -> PCContainer.isItemValid(s), PCInventory::new, PCInventory::new);
    }

    public Collection<PCInventory> getPCs()
    {
        return this._map.values();
    }

    @Override
    public String fileName()
    {
        return "PCInventory";
    }

    @Override
    public String tagID()
    {
        return "PC";
    }

}
