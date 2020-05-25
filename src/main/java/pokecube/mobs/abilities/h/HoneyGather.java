package pokecube.mobs.abilities.h;

import java.util.Random;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import thut.api.maths.Vector3;

public class HoneyGather extends Ability
{
    int range = 4;

    @Override
    public Ability init(final Object... args)
    {
        if (args == null) return this;
        for (final Object arg : args)
            if (arg instanceof Integer)
            {
                this.range = (int) arg;
                return this;
            }
        return this;
    }

    @Override
    public void onUpdate(final IPokemob mob)
    {
        double diff = 0.002 * this.range * this.range;
        diff = Math.min(0.5, diff);
        if (Math.random() < 1 - diff) return;

        final LivingEntity entity = mob.getEntity();
        final Vector3 here = Vector3.getNewVector().set(entity);
        final Random rand = entity.getRNG();

        here.set(entity).addTo(this.range * (rand.nextDouble() - 0.5), Math.min(10, this.range) * (rand.nextDouble()
                - 0.5), this.range * (rand.nextDouble() - 0.5));

        final PlayerEntity player = PokecubeMod.getFakePlayer(mob.getEntity().getEntityWorld());
        player.setPosition(here.getPos().getX(), here.getPos().getY(), here.getPos().getZ());
        player.inventory.mainInventory.set(player.inventory.currentItem, new ItemStack(Items.BONE_MEAL));
        final ItemUseContext context = new ItemUseContext(player, Hand.MAIN_HAND, new BlockRayTraceResult(new Vec3d(0.5,
                1, 0.5), Direction.UP, here.getPos(), false));
        // Attempt to plant it.
        Items.BONE_MEAL.onItemUse(context);
    }
}
