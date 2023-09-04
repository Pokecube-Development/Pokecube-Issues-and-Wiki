package pokecube.legends.conditions.custom;

import java.util.ArrayList;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.pokemobs.SpawnEvent.SpawnContext;
import pokecube.legends.conditions.AbstractCondition;
import pokecube.legends.conditions.AbstractEntriedCondition;
import pokecube.legends.init.BlockInit;
import thut.api.maths.Vector3;

public class Regigigas extends AbstractEntriedCondition
{
    public Regigigas()
    {
        super("regigigas", "regice", "registeel", "regirock", "regieleki", "regidrago");
        final Object[] blocks = { BlockInit.GOLEM_STONE.get(), BlockInit.REGIGIGA_CORE.get(), Blocks.END_STONE_BRICKS };
        for (final Object block : blocks)
            this.setRelevant(block);
    }

    @Override
    public void onSpawn(IPokemob mob)
    {
        mob = mob.setForSpawn(54500);
        final Vector3 location = new Vector3().set(mob.getEntity()).add(0, -1, 0);
        final ArrayList<Vector3> locations = new ArrayList<>();
        final Level world = mob.getEntity().level();
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
    public CanSpawn canSpawn(SpawnContext context, final boolean message)
    {
        final CanSpawn test = super.canSpawn(context, message);
        if (!test.test()) return test;

        final ArrayList<Vector3> locations = new ArrayList<>();
        boolean check = false;
        final Level world = context.level();
        Vector3 location = context.location();

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
            check = AbstractCondition.isBlock(world, locations, Blocks.END_STONE_BRICKS);
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
                check = AbstractCondition.isBlock(world, locations, Blocks.END_STONE_BRICKS);
            }
        }
        if (!check)
        {
            if (message) this.sendLegendBuild(context.player(), "Regigigas");
            return CanSpawn.NO;
        }
        return CanSpawn.YES;
    }
}
