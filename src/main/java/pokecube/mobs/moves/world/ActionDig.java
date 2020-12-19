package pokecube.mobs.moves.world;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.abilities.Ability;
import pokecube.core.handlers.events.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
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
        if (!MoveEventsHandler.canEffectBlock(user, location)) return false;
        count = (int) Math.max(1, Math.ceil(this.digHole(user, location, true) * hungerValue * Math.pow((100 - level)
                / 100d, 3)));
        if (count > 0)
        {
            this.digHole(user, location, false);
            used = true;
            user.setHungerTime(user.getHungerTime() + count);
        }
        return used;
    }

    private int digHole(final IPokemob digger, final Vector3 v, final boolean count)
    {
        int ret = 0;

        final LivingEntity owner = digger.getOwner();
        PlayerEntity player = null;
        if (owner instanceof PlayerEntity)
        {
            player = (PlayerEntity) owner;

            final BreakEvent evt = new BreakEvent(player.getEntityWorld(), v.getPos(), v.getBlockState(player
                    .getEntityWorld()), player);

            MinecraftForge.EVENT_BUS.post(evt);
            if (evt.isCanceled()) return 0;
        }

        final boolean silky = Move_Basic.shouldSilk(digger) && player != null;
        final boolean dropAll = this.shouldDropAll(digger);
        final double uselessDrop = Math.pow((100 - digger.getLevel()) / 100d, 3);
        final Vector3 temp = Vector3.getNewVector();
        final World world = digger.getEntity().getEntityWorld();
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
