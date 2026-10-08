package pokecube.mobs.abilities.simple;

import net.minecraft.world.entity.Mob;
import pokecube.api.data.abilities.Ability;
import pokecube.api.data.abilities.AbilityProvider;
import pokecube.api.entity.pokemob.IPokemob;

@AbilityProvider(name = "regenerator")
public class Regenerator extends Ability
{
    @Override
    public void onRecall(final IPokemob mob) // Heal 1/3rd health
    {
        Mob entity = mob.getEntity();
        entity.heal(Math.min(entity.getMaxHealth() / 3.0f, entity.getMaxHealth() - entity.getHealth()));
    }
}
