package pokecube.legends.conditions.custom;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.stats.CaptureStats;
import pokecube.core.database.Database;
import pokecube.legends.conditions.AbstractCondition;
import pokecube.mobs.moves.world.ActionTeleport;
import thut.lib.TComponent;

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
        return TComponent.translatable("pokecube_legends.mew.badges", this.getEntry().getTranslatedName());
    }

    @Override
    protected void onCapureFail(final IPokemob pokemob)
    {
        ActionTeleport.teleportRandomly(pokemob.getEntity());
    }

}
