package pokecube.core.blocks.trade;

import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.inventory.trade.TradeContainer;
import pokecube.core.inventory.trade.TradeInventory;

public class TraderTile extends InteractableTile
{
    public final ContainerData syncValues = new ContainerData()
    {
        @Override
        public int getCount()
        {
            return 2;
        }

        @Override
        public void set(final int index, final int value)
        {
            TraderTile.this.confirmed[index] = value != 0;
        }

        @Override
        public int get(final int index)
        {
            return TraderTile.this.confirmed[index] ? 1 : 0;
        }
    };

    public final boolean[] confirmed = new boolean[2];
    public final Set<UUID> users = Sets.newHashSet();
    public TradeInventory inventory;

    public TraderTile(final BlockPos pos, final BlockState state)
    {
        this(PokecubeItems.TRADE_TYPE.get(), pos, state);
    }

    public TraderTile(final BlockEntityType<?> tileEntityTypeIn, final BlockPos pos, final BlockState state)
    {
        super(tileEntityTypeIn, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
    }

    @Override
    public InteractionResult useWithoutItem(final BlockPos pos, final Player player, final BlockHitResult hit)
    {
        if (this.users.size() < 2)
            player.openMenu(new SimpleMenuProvider((id, playerInventory, playerIn) -> new TradeContainer(id,
                    playerInventory, ContainerLevelAccess.create(this.getLevel(), pos)), player.getDisplayName()));
        return InteractionResult.SUCCESS;
    }
}
