package pokecube.adventures.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import pokecube.adventures.network.PacketTrainer;

public class TrainerEditor extends Item
{

    public TrainerEditor(final Properties properties)
    {
        super(properties);
    }

    @Override
    public ActionResultType itemInteractionForEntity(final ItemStack stack, final PlayerEntity playerIn,
            final LivingEntity target, final Hand hand)
    {
        if (playerIn instanceof ServerPlayerEntity) PacketTrainer.sendEditOpenPacket(target,
                (ServerPlayerEntity) playerIn);
        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResultType onItemUse(final ItemUseContext context)
    {
        final PlayerEntity playerIn = context.getPlayer();
        if (playerIn instanceof ServerPlayerEntity) PacketTrainer.sendEditOpenPacket(null,
                (ServerPlayerEntity) playerIn);
        return ActionResultType.SUCCESS;
    }
}
