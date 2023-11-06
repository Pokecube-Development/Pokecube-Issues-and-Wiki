package pokecube.mobs.moves.world;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.pokemobs.combat.MoveUse;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveWorldEffect;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.MoveEventsHandler;
import pokecube.core.moves.MovesUtils;
import pokecube.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;

public class ActionSmash implements IMoveWorldEffect
{
    public ActionSmash()
    {}

    @Override
    public boolean applyOutOfCombat(final IPokemob user, final Vector3 location)
    {
        boolean used = false;
        int count = 10;
        int level = user.getLevel();
        final int hungerValue = PokecubeCore.getConfig().pokemobLifeSpan / 8;
        if (!MoveEventsHandler.canAffectBlock(user, location, this.getMoveName())) return false;
        level = Math.min(99, level);
        final int rocks = this.smashRock(user, location, true);
        count = (int) Math.max(1, Math.ceil(rocks * hungerValue * Math.pow((100 - level) / 100d, 3)));
        if (rocks > 0)
        {
            this.smashRock(user, location, false);
            used = true;
            user.applyHunger(count);
        }
        if (!used)
        {
            final Level world = user.getEntity().getLevel();
            final List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class, location.getAABB().inflate(1));
            if (!items.isEmpty())
            {
                final MoveEntry move = MovesUtils.getMove(this.getMoveName());
                return PokecubeAPI.MOVE_BUS.post(new MoveUse.MoveWorldAction.AffectItem(move, user, location, items));
            }
        }
        return used;
    }

    @Override
    public String getMoveName()
    {
        return "rock-smash";
    }

    private int smashRock(final IPokemob digger, final Vector3 v, final boolean count)
    {
        int ret = 0;
        final LivingEntity owner = digger.getOwner();
        ServerPlayer player = null;
        if (owner instanceof ServerPlayer splayer) player = splayer;
        final Level world = digger.getEntity().getLevel();
        final Vector3 temp = new Vector3();
        ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        temp.set(v);
        final int range = 1;
        for (int i = -range; i <= range; i++)
            for (int j = -range; j <= range; j++) for (int k = -range; k <= range; k++)
        {
            if (!(i == 0 || k == 0 || j == 0)) continue;
            temp.set(v).addTo(i, j, k);
            final BlockState state = temp.getBlockState(world);
            if (PokecubeTerrainChecker.isRock(state))
            {
                if (!MoveEventsHandler.canAffectBlock(digger, temp, this.getMoveName(), false, true)) continue;
                if (!count) MovesUtils.harvestBlock(digger, pickaxe, state, temp.getPos(), world, player, true);
                ret++;
            }
        }
        return ret;
    }
}
