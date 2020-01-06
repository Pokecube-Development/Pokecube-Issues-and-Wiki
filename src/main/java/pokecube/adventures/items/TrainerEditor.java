package pokecube.adventures.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class TrainerEditor extends Item
{

    public TrainerEditor(final Properties properties)
    {
        super(properties);
    }

    @Override
    public boolean itemInteractionForEntity(final ItemStack stack, final PlayerEntity playerIn,
            final LivingEntity target, final Hand hand)
    {
        // TODO Auto-generated method stub
        return super.itemInteractionForEntity(stack, playerIn, target, hand);
    }
}
