package pokecube.adventures.items.bag;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import pokecube.adventures.network.PacketBag;

public class BagItem extends Item
{

    public BagItem(final Properties properties)
    {
        super(properties);
        // TODO default colours and whatnot for bag
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level worldIn, final Player playerIn,
            final InteractionHand handIn)
    {
        if (!worldIn.isClientSide) PacketBag.sendOpenPacket(playerIn, playerIn.getUUID());
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getItemInHand(handIn));
    }

//    @Override
//    public int getColor(ItemStack stack)
//    {
//        CompoundTag compoundtag = stack.getTagElement("display");
//        return compoundtag != null && compoundtag.contains("color", 99) ? compoundtag.getInt("color") : 0xFFB02E26;
//    }
}
