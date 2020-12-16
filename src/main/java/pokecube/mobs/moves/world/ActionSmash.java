package pokecube.mobs.moves.world;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.events.pokemob.combat.MoveUse;
import pokecube.core.handlers.events.MoveEventsHandler;
import pokecube.core.interfaces.IMoveAction;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.templates.Move_Basic;
import pokecube.core.world.terrain.PokecubeTerrainChecker;
import thut.api.maths.Vector3;

public class ActionSmash implements IMoveAction
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
        if (!MoveEventsHandler.canEffectBlock(user, location)) return false;
        level = Math.min(99, level);
        final int rocks = this.smashRock(user, location, true);
        count = (int) Math.max(1, Math.ceil(rocks * hungerValue * Math.pow((100 - level) / 100d, 3)));
        if (rocks > 0)
        {
            this.smashRock(user, location, false);
            used = true;
            user.setHungerTime(user.getHungerTime() + count);
        }
        if (!used)
        {
            final World world = user.getEntity().getEntityWorld();
            final List<ItemEntity> items = world.getEntitiesWithinAABB(ItemEntity.class, location.getAABB().grow(1));
            if (!items.isEmpty())
            {
                final Move_Base move = MovesUtils.getMoveFromName(this.getMoveName());
                return PokecubeCore.MOVE_BUS.post(new MoveUse.MoveWorldAction.AffectItem(move, user, location, items));
            }
        }
        return used;
    }

    private void doFortuneDrop(final BlockState state, final BlockPos pos, final World worldIn,
            final PlayerEntity player, final int fortune)
    {

        final ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        pickaxe.addEnchantment(Enchantments.FORTUNE, fortune);
        state.getBlock().harvestBlock(worldIn, player, pos, state, null, pickaxe);
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
        PlayerEntity player = null;
        if (owner instanceof PlayerEntity)
        {
            player = (PlayerEntity) owner;
            final BreakEvent evt = new BreakEvent(player.getEntityWorld(), v.getPos(), v.getBlockState(player
                    .getEntityWorld()), player);

            MinecraftForge.EVENT_BUS.post(evt);
            if (evt.isCanceled()) return 0;
        }
        final int fortune = digger.getLevel() / 30;
        final boolean silky = Move_Basic.shouldSilk(digger) && player != null;
        final World world = digger.getEntity().getEntityWorld();
        final Vector3 temp = Vector3.getNewVector();
        temp.set(v);
        final int range = 1;
        for (int i = -range; i <= range; i++)
            for (int j = -range; j <= range; j++)
                for (int k = -range; k <= range; k++)
                {
                    if (!(i == 0 || k == 0 || j == 0)) continue;
                    temp.set(v);
                    final BlockState state = temp.addTo(i, j, k).getBlockState(world);
                    if (PokecubeTerrainChecker.isRock(state))
                    {
                        if (!count) if (!silky) this.doFortuneDrop(state, temp.getPos(), world, player, fortune);
                        else
                        {
                            Move_Basic.silkHarvest(state, temp.getPos(), world, player);
                            temp.breakBlock(world, false);
                        }
                        ret++;
                    }
                }
        return ret;
    }
}
