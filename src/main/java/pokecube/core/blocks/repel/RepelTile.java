package pokecube.core.blocks.repel;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
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

    public RepelTile(final BlockPos pos, final BlockState state)
    {
        this(PokecubeItems.REPEL_TYPE.get(), pos, state);
    }

    public RepelTile(final BlockEntityType<?> tileEntityTypeIn, final BlockPos pos, final BlockState state)
    {
        super(tileEntityTypeIn, pos, state);
    }

    public boolean addForbiddenSpawningCoord()
    {
        if (this.getLevel() == null || this.getLevel().isClientSide || !this.enabled) return false;
        final BlockPos pos = this.getBlockPos();
        return SpawnHandler.addForbiddenSpawningCoord(pos, this.level, this.range, ForbidReason.REPEL);
    }

    @Override
    public InteractionResult onInteract(final BlockPos pos, final Player player, final InteractionHand hand,
            final BlockHitResult hit)
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
            if (!this.getLevel().isClientSide) player.displayClientMessage(new TranslatableComponent("repel.info.setrange",
                    this.range, this.enabled), true);
            return InteractionResult.SUCCESS;
        }
        else if (stack.getItem() instanceof ItemPokedex)
        {
            if (!this.getLevel().isClientSide) player.displayClientMessage(new TranslatableComponent("repel.info.getrange",
                    this.range, this.enabled), true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void load(final CompoundTag nbt)
    {
        super.load(nbt);
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
    public CompoundTag save(final CompoundTag nbt)
    {
        super.save(nbt);
        nbt.putInt("range", this.range);
        nbt.putBoolean("enabled", true);
        return nbt;
    }
}
