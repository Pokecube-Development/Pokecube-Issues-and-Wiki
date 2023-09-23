package pokecube.mobs.abilities.simple;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;

@AbilityProvider(name = "pickup")
public class Pickup extends Ability
{
    public static ResourceLocation lootTable = new ResourceLocation("pokecube_mobs", "abilities/pickup");
    public static boolean useLootTable = true;

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
            final LootTable loottable = mob.getEntity().level().getServer().getLootData()
                    .getLootTable(Pickup.lootTable);
            LootParams params = new LootParams.Builder((ServerLevel) poke.level()).create(loottable.getParamSet());
            // Generate the loot list.
            final List<ItemStack> list = loottable.getRandomItems(params);
            for (final ItemStack itemstack : list) if (!itemstack.isEmpty())
            {
                mob.setHeldItem(itemstack.copy());
                return;
            }
        }

    }

}
