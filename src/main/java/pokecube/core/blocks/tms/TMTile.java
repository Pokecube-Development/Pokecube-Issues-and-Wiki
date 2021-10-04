package pokecube.core.blocks.tms;

import java.util.Arrays;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.BlockHitResult;
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

    public TMTile(final BlockEntityType<?> tileEntityTypeIn)
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
    public InteractionResult onInteract(final BlockPos pos, final Player player, final InteractionHand hand,
            final BlockHitResult hit)
    {
        player.openMenu(new SimpleMenuProvider((id, playerInventory, playerIn) -> new TMContainer(id,
                playerInventory, ContainerLevelAccess.create(this.getLevel(), pos)), player.getDisplayName()));
        return InteractionResult.SUCCESS;
    }
}
