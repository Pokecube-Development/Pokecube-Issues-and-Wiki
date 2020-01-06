package pokecube.core.blocks.trade;

import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.inventory.trade.TradeContainer;

public class TraderTile extends InteractableTile
{
    public static final TileEntityType<? extends TileEntity> TYPE = TileEntityType.Builder.create(TraderTile::new,
            PokecubeItems.TRADER).build(null);

    public final boolean[] confirmed = new boolean[2];
    public final Set<UUID> users     = Sets.newHashSet();

    public TraderTile()
    {
        this(TraderTile.TYPE);
    }

    public TraderTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    @Override
    public boolean onInteract(final BlockPos pos, final PlayerEntity player, final Hand hand,
            final BlockRayTraceResult hit)
    {
        player.openContainer(new SimpleNamedContainerProvider((id, playerInventory, playerIn) -> new TradeContainer(id,
                playerInventory, IWorldPosCallable.of(this.getWorld(), pos)), player.getDisplayName()));
        return true;
    }
}
