package pokecube.legends.conditions;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.stats.CaptureStats;
import pokecube.core.interfaces.IPokemob;
import pokecube.legends.Reference;
import pokecube.legends.init.BlockInit;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;

public class Regice extends Condition
{
    private static final ResourceLocation VALID = new ResourceLocation(Reference.ID, "regice");

    @Override
    public boolean canCapture(final Entity trainer, final IPokemob pokemon)
    {
        if (!super.canCapture(trainer, pokemon)) return false;
        final boolean relicanth = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(), Database.getEntry(
                "relicanth")) > 0;
        final boolean wailord = CaptureStats.getTotalNumberOfPokemobCaughtBy(trainer.getUniqueID(), Database.getEntry(
                "wailord")) > 0;

        final String name = "Wailord, Relicanth";

        if (relicanth && wailord) return true;
        if (pokemon != null && !trainer.getEntityWorld().isRemote)
        {
            this.sendNoTrust(trainer);
            this.sendLegendExtra(trainer, name);
        }
        return false;
    }

    @Override
    public void onSpawn(IPokemob mob)
    {
    	mob = mob.setForSpawn(54500);
        final Vector3 location = Vector3.getNewVector().set(mob.getEntity()).add(0, -1, 0);
        final ArrayList<Vector3> locations = new ArrayList<>();
        final World world = mob.getEntity().getEntityWorld();
        locations.add(location.add(0, -1, 0));
        locations.add(location.add(0, -2, 0));
        locations.add(location.add(1, -1, 0));
        locations.add(location.add(-1, -1, 0));
        locations.add(location.add(0, -1, -1));
        locations.add(location.add(0, -1, 1));
        locations.add(location.add(0, 0, -1));
        locations.add(location.add(0, 0, 1));
        locations.add(location.add(1, 0, 0));
        locations.add(location.add(-1, 0, 0));
        for (final Vector3 v : locations)
            v.setAir(world);
        location.setAir(world);
    }

    @Override
    public CanSpawn canSpawn(final Entity trainer, final Vector3 location, final boolean message)
    {
        final CanSpawn test = super.canSpawn(trainer, location, message);
        if (!test.test()) return test;

        final ArrayList<Vector3> locations = new ArrayList<>();
        boolean check = false;
        final World world = trainer.getEntityWorld();

        locations.add(location.add(0, -1, 0));
        locations.add(location.add(0, -2, 0));
        locations.add(location.add(-1, -1, 0));
        locations.add(location.add(1, -1, 0));
        check = Condition.isBlock(world, locations, BlockInit.GOLEM_STONE.get());
        if (check)
        {
            check = Condition.isBlock(world, locations, Regice.VALID);
            Block b = location.add(1, 0, 0).getBlock(world);
            b = location.add(-1, 0, 0).getBlock(world);
            check = ItemList.is(Regice.VALID, b);
            if (!check)
            {
                b = location.add(1, 0, 0).getBlock(world);
                b = location.add(-1, 0, 0).getBlock(world);
                check = ItemList.is(Regice.VALID, b);
            }
        }
        else
        {
            locations.clear();
            locations.add(location.add(0, -1, 0));
            locations.add(location.add(0, -2, 0));
            locations.add(location.add(0, -1, 1));
            locations.add(location.add(0, -1, -1));
            check = Condition.isBlock(world, locations, BlockInit.GOLEM_STONE.get());
            if (check)
            {
                check = Condition.isBlock(world, locations, Regice.VALID);
                Block b = location.add(0, 0, 1).getBlock(world);
                b = location.add(0, 0, -1).getBlock(world);
                check = ItemList.is(Regice.VALID, b);
                if (!check)
                {
                    b = location.add(0, 0, 1).getBlock(world);
                    b = location.add(0, 0, -1).getBlock(world);
                    check = ItemList.is(Regice.VALID, b);
                }
            }
        }
        if (!check)
        {
            if (message) this.sendLegendBuild(trainer, "Regice");
            return CanSpawn.NO;
        }
        return CanSpawn.YES;
    }

    @Override
    public PokedexEntry getEntry()
    {
        return Database.getEntry("regice");
    }

}
