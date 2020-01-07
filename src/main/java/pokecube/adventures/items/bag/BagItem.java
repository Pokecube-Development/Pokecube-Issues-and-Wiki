package pokecube.adventures.items.bag;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.network.PacketBag;
import thut.wearables.ThutWearables;

@Mod.EventBusSubscriber
public class BagItem extends Item
{
    @SubscribeEvent
    public static void attachCaps(final AttachCapabilitiesEvent<ItemStack> event)
    {
        if (event.getCapabilities().containsKey(ThutWearables.WEARABLES_ITEM_TAG)) return;
        if (event.getObject().getItem() instanceof BagItem)
        {
            event.addCapability(ThutWearables.WEARABLES_ITEM_TAG, PokecubeAdv.proxy.getWearable());
        }
    }

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
