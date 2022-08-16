package pokecube.core.blocks.maxspot;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import pokecube.core.eventhandlers.SpawnHandler;
import pokecube.core.eventhandlers.SpawnHandler.ForbidReason;
import pokecube.core.items.ItemPokedex;
import pokecube.core.items.berries.ItemBerry;
import thut.lib.TComponent;

public class MaxTile extends InteractableTile
{
    public static final ForbidReason MAXSPOT = new ForbidReason("pokecube:maxspot");

    public int range = PokecubeCore.getConfig().repelRadius;
    public boolean enabled = true;

    public MaxTile(final BlockPos pos, final BlockState state)
    {
        this(PokecubeItems.MAX_TYPE.get(), pos, state);
    }

    public MaxTile(final BlockEntityType<?> tileEntityTypeIn, final BlockPos pos, final BlockState state)
    {
        super(tileEntityTypeIn, pos, state);
    }

    public boolean addForbiddenSpawningCoord()
    {
        if (this.getLevel() == null || this.getLevel().isClientSide || !this.enabled) return false;
        final BlockPos pos = this.getBlockPos();
        return SpawnHandler.addForbiddenSpawningCoord(pos, this.level, this.range, MaxTile.MAXSPOT);
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
            this.range = Math.max(1, berry.type.index);
            if (!player.isCreative() && old != this.range) stack.split(1);
            if (!this.getLevel().isClientSide)
                player.displayClientMessage(TComponent.translatable("repel.info.setrange", this.range, this.enabled),
                        true);
            return InteractionResult.SUCCESS;
        }
        else if (stack.getItem() instanceof ItemPokedex)
        {
            if (!this.getLevel().isClientSide)
                player.displayClientMessage(TComponent.translatable("repel.info.getrange", this.range, this.enabled),
                        true);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void load(final CompoundTag nbt)
    {
        super.load(nbt);
        this.range = nbt.getInt("range");
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
    public void saveAdditional(final CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        nbt.putInt("range", this.range);
        nbt.putBoolean("enabled", true);
    }
}
