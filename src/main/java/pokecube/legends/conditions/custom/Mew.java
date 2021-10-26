package pokecube.legends.conditions.custom;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.legends.conditions.AbstractCondition;
import pokecube.mobs.moves.world.ActionTeleport;

public class Mew extends AbstractCondition
{
    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("Mew");
    }

    @Override
    protected boolean hasRequirements(final Entity trainer)
    {
        final int caught = CaptureStats.getNumberUniqueCaughtBy(trainer.getUUID());
        if (caught < Database.spawnables.size() - 1) return false;
        return true;
    }

    @Override
    public MutableComponent getFailureMessage(final Entity trainer)
    {
        return new TranslatableComponent("pokecube_legends.mew.badges", this.getEntry().getTranslatedName());
    }

    @Override
    protected void onCapureFail(final IPokemob pokemob)
    {
        ActionTeleport.teleportRandomly(pokemob.getEntity());
    }

}
