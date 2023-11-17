package pokecube.mobs.abilities.complex;

import java.util.Optional;
import java.util.Random;

import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.impl.PokecubeMod;
import pokecube.gimmicks.nests.tasks.bees.BeeTasks;
import thut.api.maths.Vector3;

@AbilityProvider(name = "honey-gather", singleton = false)
public class HoneyGather extends Ability
{
    int range = 4;

    @Override
    public Ability init(final Object... args)
    {
        if (args == null) return this;
        for (final Object arg : args) if (arg instanceof Integer)
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
        final Vector3 here = new Vector3().set(entity);
        final Random rand = entity.getRandom();

        final Optional<GlobalPos> pos_opt = BeeTasks.getFlower(entity);
        if (pos_opt.isPresent())
        {
            here.set(pos_opt.get().pos());
            final Player player = PokecubeMod.getFakePlayer(mob.getEntity().getLevel());
            player.setPos(here.getPos().getX(), here.getPos().getY(), here.getPos().getZ());
            player.getInventory().items.set(player.getInventory().selected, new ItemStack(Items.BONE_MEAL));
            final UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(new Vec3(0.5, 1, 0.5), Direction.UP, here.getPos(), false));
            // Attempt to plant it.
            Items.BONE_MEAL.useOn(context);
            return;
        }

        here.set(entity).addTo(this.range * (rand.nextDouble() - 0.5),
                Math.min(10, this.range) * (rand.nextDouble() - 0.5), this.range * (rand.nextDouble() - 0.5));

        final Player player = PokecubeMod.getFakePlayer(mob.getEntity().getLevel());
        player.setPos(here.getPos().getX(), here.getPos().getY(), here.getPos().getZ());
        player.getInventory().items.set(player.getInventory().selected, new ItemStack(Items.BONE_MEAL));
        final UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(new Vec3(0.5, 1, 0.5), Direction.UP, here.getPos(), false));
        // Attempt to plant it.
        Items.BONE_MEAL.useOn(context);
    }
}
