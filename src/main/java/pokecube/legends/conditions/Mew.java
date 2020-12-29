package pokecube.legends.conditions;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.mobs.moves.world.ActionTeleport;

public class Mew extends AbstractCondition
{
    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("Mew");
    }

    @Override
    boolean hasRequirements(final Entity trainer)
    {
        final int caught = CaptureStats.getNumberUniqueCaughtBy(trainer.getUniqueID());
        if (caught < Database.spawnables.size() - 1) return false;
        return true;
    }

    @Override
    void sendFailureMessage(final Entity trainer)
    {
        if (trainer instanceof PlayerEntity) ((PlayerEntity) trainer).sendMessage(new TranslationTextComponent(
                "pokecube_legends.mew.badges"), Util.DUMMY_UUID);
    }

    @Override
    protected void onCapureFail(final IPokemob pokemob)
    {
        ActionTeleport.teleportRandomly(pokemob.getEntity());
    }

}
