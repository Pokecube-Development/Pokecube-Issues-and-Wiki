package pokecube.legends.conditions.custom;

import java.util.ArrayList;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import pokecube.core.interfaces.IPokemob;
import pokecube.legends.conditions.AbstractCondition;
import pokecube.legends.conditions.AbstractRegiCondition;
import pokecube.legends.init.BlockInit;
import thut.api.maths.Vector3;

public class Registeel extends AbstractRegiCondition
{
    public Registeel()
    {
        super("registeel", BlockInit.GOLEM_STONE.get(), BlockInit.REGISTEEL_CORE.get(), Blocks.IRON_BLOCK);
    }

    @Override
    public void onSpawn(IPokemob mob)
    {
        mob = mob.setForSpawn(54500);
        final Vector3 location = Vector3.getNewVector().set(mob.getEntity()).add(0, -1, 0);
        final ArrayList<Vector3> locations = new ArrayList<>();
        final Level world = mob.getEntity().getCommandSenderWorld();
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
        final Level world = trainer.getCommandSenderWorld();

        locations.add(location.add(0, -1, 0));
        locations.add(location.add(0, -2, 0));
        locations.add(location.add(-1, -1, 0));
        locations.add(location.add(1, -1, 0));
        check = AbstractCondition.isBlock(world, locations, BlockInit.GOLEM_STONE.get());
        if (check)
        {
            locations.clear();
            locations.add(location.add(-1, 0, 0));
            locations.add(location.add(1, 0, 0));
            check = AbstractCondition.isBlock(world, locations, Blocks.IRON_BLOCK);
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
                locations.clear();
                locations.add(location.add(0, 0, 1));
                locations.add(location.add(0, 0, -1));
                check = AbstractCondition.isBlock(world, locations, Blocks.IRON_BLOCK);
            }
        }
        if (!check)
        {
            if (message) this.sendLegendBuild(trainer, "Registeel");
            return CanSpawn.NO;
        }
        return CanSpawn.YES;
    }

}
