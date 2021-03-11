package pokecube.core.blocks.repel;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.handlers.events.SpawnHandler.ForbidReason;
import pokecube.core.items.ItemPokedex;
import pokecube.core.items.berries.ItemBerry;

public class RepelTile extends InteractableTile
{
    public static int NESTSPAWNTYPES = 1;

    public int     range   = PokecubeCore.getConfig().repelRadius;
    public boolean enabled = true;

    public RepelTile()
    {
        super(PokecubeItems.REPEL_TYPE.get());
    }

    public RepelTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    public boolean addForbiddenSpawningCoord()
    {
        if (this.getLevel() == null || this.getLevel().isClientSide || !this.enabled) return false;
        final BlockPos pos = this.getBlockPos();
        return SpawnHandler.addForbiddenSpawningCoord(pos, this.level, this.range, ForbidReason.REPEL);
    }

    @Override
    public ActionResultType onInteract(final BlockPos pos, final PlayerEntity player, final Hand hand,
            final BlockRayTraceResult hit)
    {
        final ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof ItemBerry)
        {
            final ItemBerry berry = (ItemBerry) stack.getItem();
            final int old = this.range;
            this.removeForbiddenSpawningCoord();
            this.range = Math.max(1, berry.type.index);
            this.addForbiddenSpawningCoord();
            if (!player.isCreative() && old != this.range) stack.split(1);
            if (!this.getLevel().isClientSide) player.sendMessage(new TranslationTextComponent("repel.info.setrange",
                    this.range, this.enabled), Util.NIL_UUID);
            return ActionResultType.SUCCESS;
        }
        else if (stack.getItem() instanceof ItemPokedex)
        {
            if (!this.getLevel().isClientSide) player.sendMessage(new TranslationTextComponent("repel.info.getrange",
                    this.range, this.enabled), Util.NIL_UUID);
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void load(final BlockState state, final CompoundNBT nbt)
    {
        super.load(state, nbt);
        this.removeForbiddenSpawningCoord();
        this.range = nbt.getInt("range");
        this.addForbiddenSpawningCoord();
        this.enabled = nbt.getBoolean("enabled");
    }

    @Override
    public void setRemoved()
    {
        super.setRemoved();
        this.removeForbiddenSpawningCoord();
    }

    public boolean removeForbiddenSpawningCoord()
    {
        if (this.getLevel() == null || this.getLevel().isClientSide) return false;
        return SpawnHandler.removeForbiddenSpawningCoord(this.getBlockPos(), this.level);
    }

    @Override
    public void clearRemoved()
    {
        super.clearRemoved();
        this.addForbiddenSpawningCoord();
    }

    /**
     * Writes a tile entity to NBT.
     *
     * @return
     */
    @Override
    public CompoundNBT save(final CompoundNBT nbt)
    {
        super.save(nbt);
        nbt.putInt("range", this.range);
        nbt.putBoolean("enabled", true);
        return nbt;
    }
}
