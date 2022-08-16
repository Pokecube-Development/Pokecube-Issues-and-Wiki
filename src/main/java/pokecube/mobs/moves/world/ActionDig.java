package pokecube.mobs.moves.world;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.api.data.abilities.Ability;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.IMoveAction;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;

public class ActionDig implements IMoveAction
{
    public ActionDig()
    {
    }

    @Override
    public boolean applyEffect(final IPokemob user, final Vector3 location)
    {
        if (user.inCombat()) return false;
        boolean used = false;
        int count = 10;
        final int level = user.getLevel();
        final int hungerValue = PokecubeCore.getConfig().pokemobLifeSpan / 8;
        if (!MoveEventsHandler.canAffectBlock(user, location, this.getMoveName())) return false;
        count = (int) Math.max(1, Math.ceil(this.digHole(user, location, true) * hungerValue * Math.pow((100 - level)
                / 100d, 3)));
        if (count > 0)
        {
            this.digHole(user, location, false);
            used = true;
            user.applyHunger(count);
        }
        return used;
    }

    private int digHole(final IPokemob digger, final Vector3 v, final boolean count)
    {
        int ret = 0;

        final LivingEntity owner = digger.getOwner();
        Player player = null;
        final Level world = digger.getEntity().getLevel();
        if (owner instanceof Player) player = (Player) owner;
        else player = PokecubeMod.getFakePlayer(world);
        final boolean silky = Move_Basic.shouldSilk(digger) && player != null;
        final boolean dropAll = this.shouldDropAll(digger);
        final double uselessDrop = Math.pow((100 - digger.getLevel()) / 100d, 3);
        final Vector3 temp = new Vector3();
        temp.set(v);
        final int range = 1;
        for (int i = -range; i <= range; i++)
            for (int j = -range; j <= range; j++)
                for (int k = -range; k <= range; k++)
                {
                    temp.set(v);
                    final BlockState state = temp.addTo(i, j, k).getBlockState(world);
                    if (PokecubeTerrainChecker.isTerrain(state))
                    {
                        if(!MoveEventsHandler.canAffectBlock(digger, temp, this.getMoveName(), false, true)) continue;
                        boolean drop = true;
                        if (!dropAll && !silky && uselessDrop < Math.random()) drop = false;
                        if (!count) if (!silky) temp.breakBlock(world, drop);
                        else
                        {
                            Move_Basic.silkHarvest(state, temp.getPos(), world, player);
                            temp.breakBlock(world, drop);
                        }
                        ret++;
                    }
                }
        return ret;
    }

    @Override
    public String getMoveName()
    {
        return "dig";
    }

    private boolean shouldDropAll(final IPokemob pokemob)
    {
        if (pokemob.getAbility() == null) return false;
        final Ability ability = pokemob.getAbility();
        return ability.toString().equalsIgnoreCase("arenatrap");
    }
}
