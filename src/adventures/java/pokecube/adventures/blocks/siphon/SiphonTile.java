package pokecube.adventures.blocks.siphon;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.energy.EnergyStorage;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.blocks.InteractableTile;
import thut.api.attachments.Energy;
import thut.api.attachments.Linkable.ILinkStorage;
import thut.api.block.ITickTile;
import thut.api.entity.teleporting.TeleDest;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

import java.util.List;

public class SiphonTile extends InteractableTile implements ITickTile
{
    public static class EnergyStore extends EnergyStorage
    {
        public int currentOutput;
        public int theoreticalOutput;

        public EnergyStore()
        {
            super(0, 0, Integer.MAX_VALUE);
        }

        @Override
        public int receiveEnergy(final int maxReceive, final boolean simulate)
        {
            return 0;
        }

        @Override
        public int extractEnergy(final int maxExtract, final boolean simulate)
        {
            final int output = Math.min(maxExtract, this.currentOutput);
            if (!simulate) this.currentOutput -= output;
            return output;
        }

        @Override
        public int getEnergyStored()
        {
            return this.currentOutput;
        }

        @Override
        public int getMaxEnergyStored()
        {
            return this.theoreticalOutput;
        }

        @Override
        public boolean canExtract()
        {
            return true;
        }

        @Override
        public boolean canReceive()
        {
            return false;
        }

    }

    public AABB box;

    public List<Entity> mobs = Lists.newArrayList();

    public long updateTime = -1;

    public EnergyStore energy;

    public List<GlobalPos> wirelessLinks = Lists.newArrayList();

    public SiphonTile(final BlockPos pos, final BlockState state)
    {
        this(PokecubeAdv.SIPHON_TYPE.get(), pos, state);
    }

    public SiphonTile(final BlockEntityType<?> tileEntityTypeIn, final BlockPos pos, final BlockState state)
    {
        super(tileEntityTypeIn, pos, state);
        energy = (EnergyStore) Energy.get(this);
    }

    @Override
    public InteractionResult useWithoutItem(BlockPos pos, Player player, BlockHitResult hit)
    {
        Component message = null;
        message = TComponent.translatable("block.rfsiphon.info",
                this.energy.theoreticalOutput - this.energy.currentOutput, this.energy.theoreticalOutput);
        player.displayClientMessage(message, true);
        return super.useWithoutItem(pos, player, hit);
    }

    @Override
    public void tick()
    {
        if (!this.level.isClientSide) ThutCore.FORGE_BUS.post(new SiphonTickEvent(this));
    }

    @Override
    public void loadAdditional(final CompoundTag compound, Provider provider)
    {
        this.wirelessLinks.clear();
        final CompoundTag wireless = compound.getCompound("links");
        final int n = wireless.getInt("n");
        for (int i = 0; i < n; i++)
        {
            final Tag tag = wireless.get("" + i);
            this.wirelessLinks.add(GlobalPos.CODEC.decode(NbtOps.INSTANCE, tag).result().get().getFirst());
        }
        super.loadAdditional(compound, provider);
        energy = (EnergyStore) Energy.get(this);
    }

    @Override
    public void saveAdditional(final CompoundTag compound, Provider provider)
    {
        final CompoundTag wireless = new CompoundTag();
        wireless.putInt("n", this.wirelessLinks.size());
        int n = 0;
        for (final GlobalPos pos : this.wirelessLinks)
        {
            final Tag tag = GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).getOrThrow();
            wireless.put("" + n++, tag);
        }
        compound.put("links", wireless);
        super.saveAdditional(compound, provider);
    }

    public boolean tryLink(final ILinkStorage link, final Entity user)
    {
        if (!PokecubeAdv.config.wirelessSiphons) return false;
        final GlobalPos pos = link.getLinkedPos(user);
        if (pos != null)
        {
            if (this.wirelessLinks.remove(pos))
            {
                if (user != null && user instanceof ServerPlayer player)
                {
                    player.displayClientMessage(TComponent.translatable("block.pokecube_adventures.siphon.unlink",
                            new TeleDest().setPos(pos).getInfoName()), true);
                }
                return true;
            }
            this.wirelessLinks.add(pos);
            if (user != null && user instanceof ServerPlayer player)
            {
                player.displayClientMessage(TComponent.translatable("block.pokecube_adventures.siphon.link",
                        new TeleDest().setPos(pos).getInfoName()), true);
            }
            return true;
        }
        return false;
    }
}
