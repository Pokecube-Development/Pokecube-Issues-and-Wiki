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
    public boolean canCapture(final Entity trainer, final IPokemob pokemon)
    {
        if (!super.canCapture(trainer, pokemon)) return false;
        final int caught = CaptureStats.getNumberUniqueCaughtBy(trainer.getUniqueID());
        if (caught < Database.spawnables.size() - 1)
        {
            if (trainer instanceof PlayerEntity) ((PlayerEntity) trainer).sendMessage(new TranslationTextComponent(
                    "pokecube_legends.mew.badges"), Util.DUMMY_UUID);
            ActionTeleport.teleportRandomly(pokemon.getEntity());
            return false;
        }
        return true;
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("Mew");
    }

}
