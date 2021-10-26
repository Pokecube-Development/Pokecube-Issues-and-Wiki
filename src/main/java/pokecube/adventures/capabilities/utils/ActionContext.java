package pokecube.adventures.capabilities.utils;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ActionContext
{
    // The player or similar doing the interaction
    public final LivingEntity target;
    // The holder of the IHasMessages
    public final LivingEntity holder;

    // If this is done via interact event, this might not be empty.
    public ItemStack playerStack = ItemStack.EMPTY;

    // If this is not null, it is the damage the player did to the holder
    public DamageSource damage = null;

    public ActionContext(final LivingEntity target, final LivingEntity holder)
    {
        this.target = target;
        this.holder = holder;
    }

    public ActionContext(final LivingEntity target, final LivingEntity holder, final ItemStack itemStack)
    {
        this(target, holder);
        this.playerStack = itemStack;
    }

    public ActionContext(final LivingEntity target, final LivingEntity holder, final DamageSource damage)
    {
        this(target, holder);
        this.damage = damage;
    }
}
