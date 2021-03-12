package pokecube.legends.conditions;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.legends.Reference;
import pokecube.legends.init.BlockInit;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;

public class Regice extends AbstractRegiCondition
{
    private static final ResourceLocation VALID = new ResourceLocation(Reference.ID, "regice");

    public Regice()
    {
        super(BlockInit.GOLEM_STONE.get(), BlockInit.REGICE_CORE.get(), Regice.VALID);
    }

    @Override
    public void onSpawn(IPokemob mob)
    {
    	mob = mob.setForSpawn(54500);
        final Vector3 location = Vector3.getNewVector().set(mob.getEntity()).add(0, -1, 0);
        final ArrayList<Vector3> locations = new ArrayList<>();
        final World world = mob.getEntity().getCommandSenderWorld();
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
        final World world = trainer.getCommandSenderWorld();

        locations.add(location.add(0, -1, 0));
        locations.add(location.add(0, -2, 0));
        locations.add(location.add(-1, -1, 0));
        locations.add(location.add(1, -1, 0));
        check = AbstractCondition.isBlock(world, locations, BlockInit.GOLEM_STONE.get());
        if (check)
        {
            check = AbstractCondition.isBlock(world, locations, Regice.VALID);
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
            check = AbstractCondition.isBlock(world, locations, BlockInit.GOLEM_STONE.get());
            if (check)
            {
                check = AbstractCondition.isBlock(world, locations, Regice.VALID);
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
