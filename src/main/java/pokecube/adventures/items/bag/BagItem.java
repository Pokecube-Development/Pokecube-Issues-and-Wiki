package pokecube.adventures.items.bag;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import pokecube.adventures.network.PacketBag;

public class BagItem extends Item
{

    public BagItem(final Properties properties)
    {
        super(properties);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World worldIn, final PlayerEntity playerIn, final Hand handIn)
    {
        if (!worldIn.isRemote) PacketBag.sendOpenPacket(playerIn, playerIn.getUniqueID());
        return new ActionResult<>(ActionResultType.SUCCESS, playerIn.getHeldItem(handIn));
    }
}
