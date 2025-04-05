package thut.bling.bag.large;

import thut.api.inventory.big.Manager;

public class LargeManager extends Manager<LargeInventory>
{
    public static LargeManager INSTANCE = new LargeManager();

    public LargeManager()
    {
        super(s -> LargeContainer.isItemValid(s), LargeInventory::new, LargeInventory::new);
    }

    @Override
    public String fileName()
    {
        return "LargeEnderBag";
    }

    @Override
    public String tagID()
    {
        return "Bag";
    }

}
