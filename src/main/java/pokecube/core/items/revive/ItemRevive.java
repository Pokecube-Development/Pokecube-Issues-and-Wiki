package pokecube.core.items.revive;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.TagNames;

public class ItemRevive extends Item
{
    public ItemRevive(final Properties props)
    {
        super(props);
    }

    @Override
    public ActionResultType itemInteractionForEntity(final ItemStack stack, final PlayerEntity playerIn,
            final LivingEntity target, final Hand hand)
    {
        if (target.deathTime > 0)
        {
            PokecubeManager.heal(target);
            target.getPersistentData().putBoolean(TagNames.REVIVED, true);
            stack.grow(-1);
            return ActionResultType.CONSUME;
        }
        return super.itemInteractionForEntity(stack, playerIn, target, hand);
    }
}
