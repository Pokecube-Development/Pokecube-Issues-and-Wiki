package pokecube.mobs.moves.world;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.moves.utils.IMoveWorldEffect;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.moves.MovesUtils;
import pokecube.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;

public class ActionDig implements IMoveWorldEffect
{
    final ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);

    public ActionDig()
    {}

    @Override
    public boolean applyOutOfCombat(final IPokemob user, final Vector3 location)
    {
        boolean used = false;
        int count = 10;
        final int level = user.getLevel();
        final int hungerValue = PokecubeCore.getConfig().pokemobLifeSpan / 8;
        if (!MoveEventsHandler.canAffectBlock(user, location, this.getMoveName())) return false;
        count = (int) Math.max(1,
                Math.ceil(this.digHole(user, location, true) * hungerValue * Math.pow((100 - level) / 100d, 3)));
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
        final Level world = digger.getEntity().getLevel();
        ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        ServerPlayer player = null;
        if (owner instanceof ServerPlayer splayer) player = splayer;
        else player = PokecubeMod.getFakePlayer(world);
        final Vector3 temp = new Vector3();
        temp.set(v);
        final int range = 1;
        for (int i = -range; i <= range; i++)
            for (int j = -range; j <= range; j++) for (int k = -range; k <= range; k++)
        {
            temp.set(v);
            final BlockState state = temp.addTo(i, j, k).getBlockState(world);
            if (PokecubeTerrainChecker.isTerrain(state))
            {
                if (!MoveEventsHandler.canAffectBlock(digger, temp, this.getMoveName(), false, true)) continue;
                if (!count) MovesUtils.harvestBlock(digger, pickaxe, state, temp.getPos(), world, player, true);
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
}
