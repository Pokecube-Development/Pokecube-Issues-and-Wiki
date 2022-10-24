package pokecube.mobs.moves.world;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
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
    {
    }

    @Override
    public boolean applyEffect(final IPokemob user, final Vector3 location)
    {
        if (user.inCombat()) return false;
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

    private void doFortuneDrop(final BlockState state, final BlockPos pos, final Level worldIn,
            final Player player, final int fortune)
    {

        final ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        pickaxe.enchant(Enchantments.BLOCK_FORTUNE, fortune);
        state.getBlock().playerDestroy(worldIn, player, pos, state, null, pickaxe);
        worldIn.destroyBlock(pos, false);
    }

    @Override
    public String getMoveName()
    {
        return "rocksmash";
    }

    private int smashRock(final IPokemob digger, final Vector3 v, final boolean count)
    {
        int ret = 0;
        final LivingEntity owner = digger.getOwner();
        Player player = null;
        if (owner instanceof Player) player = (Player) owner;
        final int fortune = digger.getLevel() / 30;
        final boolean silky = MovesUtils.shouldSilk(digger) && player != null;
        final Level world = digger.getEntity().getLevel();
        final Vector3 temp = new Vector3();
        temp.set(v);
        final int range = 1;
        for (int i = -range; i <= range; i++)
            for (int j = -range; j <= range; j++)
                for (int k = -range; k <= range; k++)
                {
                    if (!(i == 0 || k == 0 || j == 0)) continue;
                    temp.set(v).addTo(i, j, k);
                    final BlockState state = temp.getBlockState(world);
                    if (PokecubeTerrainChecker.isRock(state))
                    {
                        if (!MoveEventsHandler.canAffectBlock(digger, temp, this.getMoveName(), false, true)) continue;
                        if (!count) if (!silky) this.doFortuneDrop(state, temp.getPos(), world, player, fortune);
                        else
                        {
                            MovesUtils.silkHarvest(state, temp.getPos(), world, player);
                            temp.breakBlock(world, false);
                        }
                        ret++;
                    }
                }
        return ret;
    }
}
