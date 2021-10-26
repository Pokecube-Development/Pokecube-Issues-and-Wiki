package pokecube.adventures.items.bag;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.adventures.PokecubeAdv;
import pokecube.adventures.network.PacketBag;
import thut.wearables.ThutWearables;

public class BagItem extends Item
{
    @SubscribeEvent
    public static void attachCaps(final AttachCapabilitiesEvent<ItemStack> event)
    {
        if (event.getCapabilities().containsKey(ThutWearables.WEARABLES_ITEM_TAG)) return;
        if (event.getObject().getItem() instanceof BagItem) event.addCapability(ThutWearables.WEARABLES_ITEM_TAG,
                PokecubeAdv.proxy.getWearable());
    }

    public BagItem(final Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level worldIn, final Player playerIn, final InteractionHand handIn)
    {
        if (!worldIn.isClientSide) PacketBag.sendOpenPacket(playerIn, playerIn.getUUID());
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getItemInHand(handIn));
    }
}
