package pokecube.core.moves;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.world.level.Level;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;

public class TreeRemover
{
    Level    world;
    IPokemob user;
    Vector3  centre;
    String   move;

    List<Vector3> blocks  = new LinkedList<>();
    List<Vector3> checked = new LinkedList<>();

    public TreeRemover(final Level world, final IPokemob user, final String move, final Vector3 pos)
    {
        this.world = world;
        this.centre = pos;
        this.user = user;
        this.move = move;
    }

    public void clear()
    {
        this.blocks.clear();
        this.checked.clear();
    }

    public int cut(final boolean count)
    {
        if (!count && this.cutTree(true) == 0) this.cutGrass();
        return this.cutTree(count);
    }

    public void cutGrass()
    {
        final Vector3 temp = new Vector3();
        for (int i = -4; i < 5; i++)
            for (int j = -4; j < 5; j++)
                for (int k = -1; k < 6; k++)
                {
                    temp.set(this.centre).addTo(i, k, j);
                    if (PokecubeTerrainChecker.isCutablePlant(temp.getBlockState(this.world))) temp.breakBlock(
                            this.world, true);
                }
    }

    private int cutPoints(final boolean count)
    {
        int ret = 0;
        for (final Vector3 v : this.blocks)
        {
            if (!count && MoveEventsHandler.canAffectBlock(this.user, v,
                    this.move, false, true)) v.breakBlock(this.world, true);
            ret++;
        }
        return ret;
    }

    public int cutTree(final boolean count)
    {
        if (this.blocks.size() > 0 && count) this.clear();
        else if (this.blocks.size() > 0) return this.cutPoints(count);
        final Vector3 base = this.findTreeBase();
        int ret = 0;
        if (!base.isEmpty())
        {
            this.populateList(base);
            ret = this.cutPoints(count);
        }
        return ret;
    }

    private Vector3 findTreeBase()
    {
        final Vector3 base = new Vector3();
        int k = -1;
        final Vector3 temp = new Vector3();

        if (PokecubeTerrainChecker.isWood(temp.set(this.centre).getBlockState(this.world)))
        {
            boolean valid = false;
            while (this.centre.intY() + k > 0)
            {
                if (PokecubeTerrainChecker.isWood(temp.set(this.centre).addTo(0, k, 0).getBlockState(this.world)))
                {
                }
                else if (PokecubeTerrainChecker.isGround(temp.set(this.centre).addTo(0, k, 0).getBlockState(
                        this.world))) valid = true;
                else break;
                if (valid) break;
                k--;
            }
            if (valid) base.set(temp).set(this.centre).addTo(0, k + 1, 0);
        }
        return base;
    }

    private boolean nextPoint(final Vector3 prev, final List<Vector3> tempList)
    {
        boolean ret = false;

        final Vector3 temp = new Vector3();
        for (int i = -1; i <= 1; i++)
            for (int j = -1; j <= 1; j++)
                for (int k = -1; k <= 1; k++)
                {
                    temp.set(prev).addTo(i, k, j);
                    if (PokecubeTerrainChecker.isWood(temp.getBlockState(this.world)))
                    {
                        tempList.add(temp.copy());
                        ret = true;
                    }
                }
        this.checked.add(prev);
        return ret;
    }

    private void populateList(final Vector3 base)
    {
        this.blocks.add(base);
        while (this.checked.size() < this.blocks.size())
        {
            final List<Vector3> toAdd = new ArrayList<>();
            for (final Vector3 v : this.blocks)
                if (!this.checked.contains(v)) this.nextPoint(v, toAdd);
            for (final Vector3 v : toAdd)
                if (!this.blocks.contains(v)) this.blocks.add(v);
        }
    }
}
