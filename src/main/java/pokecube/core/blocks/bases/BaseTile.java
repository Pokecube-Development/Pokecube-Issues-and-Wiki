package pokecube.core.blocks.bases;

import java.util.UUID;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.InteractableTile;
import pokecube.core.world.dimension.SecretBaseDimension;
import thut.api.ThutCaps;
import thut.api.block.IOwnableTE;

public class BaseTile extends InteractableTile
{
    boolean           any       = false;
    public GlobalPos  last_base = null;
    public BlockState original  = Blocks.STONE.defaultBlockState();

    public BaseTile()
    {
        super(PokecubeItems.BASE_TYPE.get());
    }

    @Override
    public ActionResultType onInteract(final BlockPos pos, final PlayerEntity player, final Hand hand,
            final BlockRayTraceResult hit)
    {
        if (!(player instanceof ServerPlayerEntity)) return ActionResultType.SUCCESS;
        final MinecraftServer server = player.getServer();
        UUID targetBase = player.getUUID();
        if (!this.any)
        {
            final IOwnableTE tile = (IOwnableTE) this.getCapability(ThutCaps.OWNABLE_CAP).orElse(null);
            targetBase = tile.getOwnerId();
            if (targetBase == null) return ActionResultType.SUCCESS;
            GlobalPos exit_here;
            try
            {
                exit_here = SecretBaseDimension.getSecretBaseLoc(targetBase, server, player.getCommandSenderWorld()
                        .dimension() == SecretBaseDimension.WORLD_KEY);
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.error(e);
                return ActionResultType.FAIL;
            }
            if (this.last_base == null) this.last_base = exit_here;
            if (exit_here.pos().distSqr(this.last_base.pos().getX(), this.last_base.pos().getY(),
                    this.last_base.pos().getZ(), false) > 0.0)
            {
                // We need to remove the location.
                this.level.setBlockAndUpdate(pos, this.original);
                player.sendMessage(new TranslationTextComponent("pokemob.removebase.stale"), Util.NIL_UUID);
                return ActionResultType.FAIL;
            }
        }
        final RegistryKey<World> dim = player.getCommandSenderWorld().dimension();
        if (dim == SecretBaseDimension.WORLD_KEY) SecretBaseDimension.sendToExit((ServerPlayerEntity) player,
                targetBase);
        else SecretBaseDimension.sendToBase((ServerPlayerEntity) player, targetBase);
        return ActionResultType.SUCCESS;
    }

    @Override
    public void load(final BlockState stateIn, final CompoundNBT compound)
    {
        super.load(stateIn, compound);
        this.any = compound.getBoolean("any_use");
        if (compound.contains("last_base")) this.last_base = GlobalPos.CODEC.decode(NBTDynamicOps.INSTANCE, compound
                .get("last_base")).result().get().getFirst();
        if (compound.contains("revert_to"))
        {
            final CompoundNBT tag = compound.getCompound("revert_to");
            this.original = NBTUtil.readBlockState(tag);
        }
    }

    @Override
    public CompoundNBT save(final CompoundNBT compound)
    {
        compound.putBoolean("any_use", this.any);
        if (this.last_base != null) compound.put("last_base", GlobalPos.CODEC.encodeStart(NBTDynamicOps.INSTANCE,
                this.last_base).get().left().get());
        if (this.original != null)
        {
            final CompoundNBT tag = NBTUtil.writeBlockState(this.original);
            compound.put("revert_to", tag);
        }
        return super.save(compound);
    }
}
