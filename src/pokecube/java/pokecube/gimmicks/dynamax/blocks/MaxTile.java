package pokecube.gimmicks.dynamax.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import pokecube.core.PokecubeCore;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.eventhandlers.SpawnHandler;
import pokecube.core.eventhandlers.SpawnHandler.ForbidReason;
import pokecube.core.items.ItemPokedex;
import pokecube.core.items.berries.ItemBerry;

import static pokecube.gimmicks.dynamax.DynamaxHelper.MAX_TYPE;

public class MaxTile extends InteractableTile
{
    public static final ForbidReason MAXSPOT = new ForbidReason("pokecube:maxspot");

    public int range = PokecubeCore.getConfig().repelRadius;
    public boolean enabled = true;

    public MaxTile(final BlockPos pos, final BlockState state)
    {
        this(MAX_TYPE.get(), pos, state);
    }

    public MaxTile(final BlockEntityType<?> tileEntityTypeIn, final BlockPos pos, final BlockState state)
    {
        super(tileEntityTypeIn, pos, state);
    }

    public void addForbiddenSpawningCoord()
    {
        if (this.getLevel() == null || this.getLevel().isClientSide || !this.enabled) return;
        final BlockPos pos = this.getBlockPos();
        SpawnHandler.addForbiddenSpawningCoord(pos, this.level, this.range, MaxTile.MAXSPOT);
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hitResult)
    {
        if (stack.getItem() instanceof ItemBerry berry)
        {
            final int old = this.range;
            this.range = Math.max(1, berry.type.index);
            if (!player.isCreative() && old != this.range) stack.split(1);
            if (!this.getLevel().isClientSide)
                player.displayClientMessage(Component.translatableEscape("repel.info.setrange", this.range, this.enabled),
                        true);
            return ItemInteractionResult.SUCCESS;
        }
        else if (stack.getItem() instanceof ItemPokedex)
        {
            if (!this.getLevel().isClientSide)
                player.displayClientMessage(Component.translatableEscape("repel.info.getrange", this.range, this.enabled),
                        true);
            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, pos, player, hand, hitResult);
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void loadAdditional(final CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        this.range = nbt.getInt("range");
        this.enabled = nbt.getBoolean("enabled");
    }

    @Override
    public void setRemoved()
    {
        super.setRemoved();
        this.removeForbiddenSpawningCoord();
    }

    public void removeForbiddenSpawningCoord()
    {
        if (this.getLevel() == null || this.getLevel().isClientSide) return;
        SpawnHandler.removeForbiddenSpawningCoord(this.getBlockPos(), this.level);
    }

    @Override
    public void clearRemoved()
    {
        super.clearRemoved();
        this.addForbiddenSpawningCoord();
    }

    /**
     * Writes a tile entity to NBT.
     */
    @Override
    public void saveAdditional(final CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.saveAdditional(nbt, registries);
        nbt.putInt("range", this.range);
        nbt.putBoolean("enabled", true);
    }
}
