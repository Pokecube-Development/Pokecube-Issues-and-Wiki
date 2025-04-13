package pokecube.core.blocks.tms;

import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import pokecube.api.data.PokedexEntry;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.inventory.tms.TMContainer;
import pokecube.core.items.ItemTM;
import thut.api.attachments.Inventory;

import java.util.Arrays;
import java.util.Set;

public class TMTile extends InteractableTile
{
    public TMTile(final BlockPos pos, final BlockState state)
    {
        this(PokecubeItems.TM_TYPE.get(), pos, state);
    }

    public TMTile(final BlockEntityType<?> tileEntityTypeIn, final BlockPos pos, final BlockState state)
    {
        super(tileEntityTypeIn, pos, state);
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
        String[] moves;
        final Set<String> set = Sets.newHashSet();
        for (final String s : mob.getMoves()) if (s != null) set.add(s);
        for (final String s : entry.getMovesForLevel(mob.getLevel())) if (s != null) set.add(s);
        set.removeIf(String::isEmpty);
        if (set.isEmpty()) return new String[] {};
        moves = set.toArray(new String[0]);
        Arrays.sort(moves);
        return moves;
    }

    @Override
    public InteractionResult useWithoutItem(final BlockPos pos, final Player player, final BlockHitResult hit)
    {
        player.openMenu(new SimpleMenuProvider((id, playerInventory, playerIn) -> new TMContainer(id, playerInventory,
                ContainerLevelAccess.create(this.getLevel(), pos)), player.getDisplayName()));
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, Provider registries)
    {
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, Provider registries)
    {
        super.loadAdditional(tag, registries);
    }
}
