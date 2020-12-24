package pokecube.core.blocks.tms;

import java.util.Arrays;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.inventory.tms.TMContainer;
import pokecube.core.items.ItemTM;

public class TMTile extends InteractableTile
{
    public TMTile()
    {
        this(PokecubeItems.TM_TYPE.get());
    }

    public TMTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    public ItemStack addMoveToTM(final String move, final ItemStack tmIn)
    {
        final ItemStack newTM = ItemTM.getTM(move);
        newTM.setCount(tmIn.getCount());
        return newTM;
    }

    public String[] getMoves(final IPokemob mob)
    {
        final PokedexEntry entry = mob.getPokedexEntry();
        String[] moves = null;
        final Set<String> set = Sets.newHashSet();
        for (final String s : mob.getMoves())
            if (s != null) set.add(s);
        for (final String s : entry.getMovesForLevel(mob.getLevel()))
            if (s != null) set.add(s);
        set.removeIf(s -> s.isEmpty());
        if (set.isEmpty()) return new String[] {};
        moves = set.toArray(new String[0]);
        Arrays.sort(moves);
        return moves;
    }

    @Override
    public ActionResultType onInteract(final BlockPos pos, final PlayerEntity player, final Hand hand,
            final BlockRayTraceResult hit)
    {
        player.openContainer(new SimpleNamedContainerProvider((id, playerInventory, playerIn) -> new TMContainer(id,
                playerInventory, IWorldPosCallable.of(this.getWorld(), pos)), player.getDisplayName()));
        return ActionResultType.SUCCESS;
    }
}
