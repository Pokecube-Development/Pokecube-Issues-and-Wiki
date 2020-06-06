package pokecube.mobs.moves.world;

import java.util.List;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import thut.api.maths.Vector3;

public class ActionPayDay implements IMoveAction
{
    public static ResourceLocation lootTable = new ResourceLocation("pokecube_mobs", "moves/payday");

    public ActionPayDay()
    {
    }

    @Override
    public boolean applyEffect(final IPokemob user, final Vector3 location)
    {
        if (!user.inCombat()) return false;
        final LivingEntity poke = user.getEntity();
        final LootTable loottable = poke.getEntityWorld().getServer().getLootTableManager().getLootTableFromLocation(
                ActionPayDay.lootTable);
        final LootContext.Builder lootcontext$builder = new LootContext.Builder((ServerWorld) poke.getEntityWorld())
                .withRandom(poke.getRNG());
        // Generate the loot list.
        final List<ItemStack> list = loottable.generate(lootcontext$builder.build(loottable.getParameterSet()));
        int num = 0;
        for (final ItemStack itemstack : list)
            if (!itemstack.isEmpty())
            {
                final ItemStack stack = itemstack.copy();
                final ItemEntity item = poke.entityDropItem(stack);
                if (item != null)
                {
                    location.moveEntity(item);
                    num++;
                }
            }
        System.out.println("Dropped " + num);
        return num > 0;
    }

    @Override
    public String getMoveName()
    {
        return "payday";
    }
}
