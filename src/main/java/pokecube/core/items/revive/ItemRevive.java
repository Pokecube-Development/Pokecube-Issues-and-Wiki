package pokecube.core.items.revive;

import net.minecraft.item.Item;

public class ItemRevive extends Item
{
    public ItemRevive(Properties props)
    {
        super(props);
    }

    @Override
    public boolean shouldSyncTag()
    {
        return true;
    }
}
