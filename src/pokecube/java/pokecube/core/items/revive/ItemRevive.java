package pokecube.core.items.revive;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.utils.TagNames;
import pokecube.core.items.pokecubes.PokecubeManager;

public class ItemRevive extends Item
{
    public ItemRevive(final Properties props)
    {
        super(props);
    }

    @Override
    public InteractionResult interactLivingEntity(final ItemStack stack, final Player playerIn,
            final LivingEntity target, final InteractionHand hand)
    {
        revive:
        if (target.deathTime > 0)
        {
            IPokemob pokemob = PokemobCaps.getPokemobFor(target);
            if (pokemob.getOwnerId() != null) break revive;
            PokecubeManager.heal(target);
            target.getPersistentData().putBoolean(TagNames.REVIVED, true);
            stack.grow(-1);
            return InteractionResult.CONSUME;
        }
        return super.interactLivingEntity(stack, playerIn, target, hand);
    }
}
