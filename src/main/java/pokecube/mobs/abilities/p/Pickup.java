package pokecube.mobs.abilities.p;

import java.util.Collections;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import pokecube.core.PokecubeItems;
import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.items.vitamins.ItemCandy;

public class Pickup extends Ability
{
    public static ResourceLocation lootTable    = new ResourceLocation("pokecube_mobs", "abilities/pickup");
    public static boolean          useLootTable = true;

    @Override
    public void onUpdate(final IPokemob mob)
    {
        final LivingEntity poke = mob.getEntity();
        // Staying in one place, nothing to find.
        if (mob.getGeneralState(GeneralStates.STAYING)) return;
        // Only works if your pokemob is following you.
        if (mob.getLogicState(LogicStates.SITTING)) return;

        if (poke.tickCount % 200 != 0 || Math.random() > 0.05) return;
        if (!mob.getHeldItem().isEmpty()) return;

        if (Pickup.lootTable != null && Pickup.useLootTable)
        {
            final LootTable loottable = mob.getEntity().getCommandSenderWorld().getServer().getLootTables()
                    .get(Pickup.lootTable);
            final LootContext.Builder lootcontext$builder = new LootContext.Builder((ServerLevel) mob.getEntity()
                    .getCommandSenderWorld()).withRandom(poke.getRandom());
            // Generate the loot list.
            final List<ItemStack> list = loottable.getRandomItems(lootcontext$builder.create(loottable.getParamSet()));
            // Shuffle the list.
            if (!list.isEmpty()) Collections.shuffle(list);
            for (final ItemStack itemstack : list)
                // Pick first valid item in it.
                if (!itemstack.isEmpty())
                {
                    ItemStack stack = itemstack.copy();
                    if (stack.getItem() instanceof ItemCandy) stack = PokecubeItems.makeCandyStack();
                    mob.setHeldItem(stack);
                    return;
                }
        }

    }

}
