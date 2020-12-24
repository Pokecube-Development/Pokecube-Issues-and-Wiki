package pokecube.core.blocks.trade;

import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.inventory.trade.TradeContainer;

public class TraderTile extends InteractableTile
{
    public final IIntArray syncValues = new IIntArray()
    {
        @Override
        public int size()
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
    public final Set<UUID> users     = Sets.newHashSet();

    public TraderTile()
    {
        this(PokecubeItems.TRADE_TYPE.get());
    }

    public TraderTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        final CompoundNBT tag = new CompoundNBT();
        return this.write(tag);
    }

    @Override
    public void handleUpdateTag(final BlockState state, final CompoundNBT tag)
    {
        this.read(state, tag);
    }

    @Override
    public ActionResultType onInteract(final BlockPos pos, final PlayerEntity player, final Hand hand,
            final BlockRayTraceResult hit)
    {
        if (this.users.size() < 2) player.openContainer(new SimpleNamedContainerProvider((id, playerInventory,
                playerIn) -> new TradeContainer(id, playerInventory, IWorldPosCallable.of(this.getWorld(), pos)), player
                        .getDisplayName()));
        return ActionResultType.SUCCESS;
    }
}
