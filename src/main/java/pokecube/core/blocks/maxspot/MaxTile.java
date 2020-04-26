package pokecube.core.blocks.maxspot;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.handlers.events.SpawnHandler.ForbidReason;
import pokecube.core.items.ItemPokedex;
import pokecube.core.items.berries.ItemBerry;

public class MaxTile extends InteractableTile
{
    public static TileEntityType<? extends TileEntity> TYPE;

    public static final ForbidReason MAXSPOT = new ForbidReason("pokecube:maxspot");

    public static int NESTSPAWNTYPES = 1;

    public int     range   = PokecubeCore.getConfig().repelRadius;
    public boolean enabled = true;

    public MaxTile()
    {
        super(MaxTile.TYPE);
    }

    public MaxTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    public boolean addForbiddenSpawningCoord()
    {
        if (this.getWorld() == null || this.getWorld().isRemote || !this.enabled) return false;
        final BlockPos pos = this.getPos();
        return SpawnHandler.addForbiddenSpawningCoord(pos.getX(), pos.getY(), pos.getZ(), this.world.getDimension()
                .getType().getId(), this.range, MaxTile.MAXSPOT);
    }

    @Override
    public ActionResultType onInteract(final BlockPos pos, final PlayerEntity player, final Hand hand,
            final BlockRayTraceResult hit)
    {
        final ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() instanceof ItemBerry)
        {
            final ItemBerry berry = (ItemBerry) stack.getItem();
            final int old = this.range;
            this.range = Math.max(1, berry.type.index);
            if (!player.isCreative() && old != this.range) stack.split(1);
            if (!this.getWorld().isRemote) player.sendMessage(new TranslationTextComponent("repel.info.setrange",
                    this.range, this.enabled));
            return ActionResultType.SUCCESS;
        }
        else if (stack.getItem() instanceof ItemPokedex)
        {
            if (!this.getWorld().isRemote) player.sendMessage(new TranslationTextComponent("repel.info.getrange",
                    this.range, this.enabled));
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void read(final CompoundNBT nbt)
    {
        super.read(nbt);
        this.range = nbt.getInt("range");
        this.enabled = nbt.getBoolean("enabled");
    }

    @Override
    public void remove()
    {
        super.remove();
        this.removeForbiddenSpawningCoord();
    }

    public boolean removeForbiddenSpawningCoord()
    {
        if (this.getWorld() == null || this.getWorld().isRemote) return false;
        return SpawnHandler.removeForbiddenSpawningCoord(this.getPos(), this.world.getDimension().getType().getId());
    }

    @Override
    public void validate()
    {
        super.validate();
        this.addForbiddenSpawningCoord();
    }

    /**
     * Writes a tile entity to NBT.
     *
     * @return
     */
    @Override
    public CompoundNBT write(final CompoundNBT nbt)
    {
        super.write(nbt);
        nbt.putInt("range", this.range);
        nbt.putBoolean("enabled", true);
        return nbt;
    }
}
