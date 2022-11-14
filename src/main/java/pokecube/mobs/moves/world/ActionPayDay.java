package pokecube.mobs.moves.world;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveWorldEffect;
import thut.api.maths.Vector3;

public class ActionPayDay implements IMoveWorldEffect
{
    public static ResourceLocation lootTable = new ResourceLocation("pokecube_mobs", "moves/payday");

    public ActionPayDay()
    {}

    @Override
    public boolean applyInCombat(final IPokemob user, final Vector3 location)
    {
        final LivingEntity poke = user.getEntity();
        final LootTable loottable = poke.getLevel().getServer().getLootTables().get(ActionPayDay.lootTable);
        final LootContext.Builder lootcontext$builder = new LootContext.Builder((ServerLevel) poke.getLevel())
                .withRandom(poke.getRandom());
        // Generate the loot list.
        final List<ItemStack> list = loottable.getRandomItems(lootcontext$builder.create(loottable.getParamSet()));
        int num = 0;
        for (final ItemStack itemstack : list) if (!itemstack.isEmpty())
        {
            final ItemStack stack = itemstack.copy();
            final ItemEntity item = poke.spawnAtLocation(stack);
            if (item != null)
            {
                location.moveEntity(item);
                num++;
            }
        }
        return num > 0;
    }

    @Override
    public String getMoveName()
    {
        return "pay-day";
    }
}
