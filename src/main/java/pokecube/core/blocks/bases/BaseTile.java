package pokecube.core.blocks.bases;

import java.util.UUID;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.world.dimension.SecretBaseDimension;
import thut.api.ThutCaps;
import thut.api.block.IOwnableTE;

public class BaseTile extends InteractableTile
{
    boolean any = false;
    public GlobalPos last_base = null;
    public BlockState original = Blocks.STONE.defaultBlockState();

    public BaseTile(final BlockPos pos, final BlockState state)
    {
        super(PokecubeItems.BASE_TYPE.get(), pos, state);
    }

    @Override
    public InteractionResult onInteract(final BlockPos pos, final Player player, final InteractionHand hand,
            final BlockHitResult hit)
    {
        if (!(player instanceof ServerPlayer)) return InteractionResult.SUCCESS;
        final MinecraftServer server = player.getServer();
        UUID targetBase = player.getUUID();
        if (!this.any)
        {
            final IOwnableTE tile = (IOwnableTE) this.getCapability(ThutCaps.OWNABLE_CAP).orElse(null);
            targetBase = tile.getOwnerId();
            if (targetBase == null) return InteractionResult.SUCCESS;
            GlobalPos exit_here;
            try
            {
                exit_here = SecretBaseDimension.getSecretBaseLoc(targetBase, server,
                        player.getCommandSenderWorld().dimension() == SecretBaseDimension.WORLD_KEY);
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error(e);
                return InteractionResult.FAIL;
            }
            if (this.last_base == null) this.last_base = exit_here;
            if (exit_here.pos().distSqr(this.last_base.pos().getX(), this.last_base.pos().getY(),
                    this.last_base.pos().getZ(), false) > 0.0)
            {
                // We need to remove the location.
                this.level.setBlockAndUpdate(pos, this.original);
                player.sendMessage(new TranslatableComponent("pokemob.removebase.stale"), Util.NIL_UUID);
                return InteractionResult.FAIL;
            }
        }
        final ResourceKey<Level> dim = player.getCommandSenderWorld().dimension();
        if (dim == SecretBaseDimension.WORLD_KEY) SecretBaseDimension.sendToExit((ServerPlayer) player, targetBase);
        else SecretBaseDimension.sendToBase((ServerPlayer) player, targetBase);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void load(final CompoundTag compound)
    {
        super.load(compound);
        this.any = compound.getBoolean("any_use");
        if (compound.contains("last_base")) this.last_base = GlobalPos.CODEC
                .decode(NbtOps.INSTANCE, compound.get("last_base")).result().get().getFirst();
        if (compound.contains("revert_to"))
        {
            final CompoundTag tag = compound.getCompound("revert_to");
            this.original = NbtUtils.readBlockState(tag);
        }
    }

    @Override
    public void saveAdditional(final CompoundTag compound)
    {
        compound.putBoolean("any_use", this.any);
        if (this.last_base != null)
            compound.put("last_base", GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, this.last_base).get().left().get());
        if (this.original != null)
        {
            final CompoundTag tag = NbtUtils.writeBlockState(this.original);
            compound.put("revert_to", tag);
        }
        super.saveAdditional(compound);
    }
}
