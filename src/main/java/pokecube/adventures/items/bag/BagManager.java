package pokecube.adventures.items.bag;

import thut.api.inventory.big.Manager;

public class BagManager extends Manager<BagInventory>
{
    public static BagManager INSTANCE = new BagManager();

    public BagManager()
    {
        super(s -> BagContainer.isItemValid(s), BagInventory::new, BagInventory::new);
    }

    @Override
    public String fileName()
    {
        return "BagInventory";
    }

    @Override
    public String tagID()
    {
        return "Bag";
    }

}
