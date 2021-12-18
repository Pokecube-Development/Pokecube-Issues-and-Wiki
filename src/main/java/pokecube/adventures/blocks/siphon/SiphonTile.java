package pokecube.adventures.blocks.siphon;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.blocks.InteractableTile;
import thut.api.LinkableCaps.ILinkStorage;
import thut.api.block.ITickTile;
import thut.api.entity.ThutTeleporter;

public class SiphonTile extends InteractableTile implements ITickTile
{
    public static class EnergyStore implements IEnergyStorage, ICapabilitySerializable<CompoundTag>
    {
        private final LazyOptional<IEnergyStorage> holder = LazyOptional.of(() -> this);
        public int currentOutput;
        public int theoreticalOutput;

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return CapabilityEnergy.ENERGY.orEmpty(cap, this.holder);
        }

        @Override
        public CompoundTag serializeNBT()
        {
            final CompoundTag tag = new CompoundTag();
            tag.putInt("cO", this.currentOutput);
            tag.putInt("tO", this.theoreticalOutput);
            return tag;
        }

        @Override
        public void deserializeNBT(final CompoundTag nbt)
        {
            this.currentOutput = nbt.getInt("cO");
            this.theoreticalOutput = nbt.getInt("yO");
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
    }

    @Override
    public InteractionResult onInteract(final BlockPos pos, final Player player, final InteractionHand hand,
            final BlockHitResult hit)
    {
        if (hand == InteractionHand.MAIN_HAND && this.energy != null && player instanceof ServerPlayer)
        {
            Component message = null;
            message = new TranslatableComponent("block.rfsiphon.info",
                    this.energy.theoreticalOutput - this.energy.currentOutput, this.energy.theoreticalOutput);
            player.displayClientMessage(message, true);
        }
        return super.onInteract(pos, player, hand, hit);
    }

    @Override
    public void tick()
    {
        if (!this.level.isClientSide) MinecraftForge.EVENT_BUS.post(new SiphonTickEvent(this));
    }

    @Override
    public void load(final CompoundTag compound)
    {
        this.wirelessLinks.clear();
        final CompoundTag wireless = compound.getCompound("links");
        final int n = wireless.getInt("n");
        for (int i = 0; i < n; i++)
        {
            final Tag tag = wireless.get("" + i);
            this.wirelessLinks.add(GlobalPos.CODEC.decode(NbtOps.INSTANCE, tag).result().get().getFirst());
        }
        super.load(compound);
    }

    @Override
    public void saveAdditional(final CompoundTag compound)
    {
        final CompoundTag wireless = new CompoundTag();
        wireless.putInt("n", this.wirelessLinks.size());
        int n = 0;
        for (final GlobalPos pos : this.wirelessLinks)
        {
            final Tag tag = GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, pos).get().left().get();
            wireless.put("" + n++, tag);
        }
        compound.put("links", wireless);
        super.saveAdditional(compound);
    }

    public boolean tryLink(final ILinkStorage link, final Entity user)
    {
        if (!PokecubeAdv.config.wirelessSiphons) return false;
        final GlobalPos pos = link.getLinkedPos(user);
        if (pos != null)
        {
            if (this.wirelessLinks.remove(pos))
            {
                if (user != null && user instanceof ServerPlayer)
                {
                    final Player player = (Player) user;
                    player.displayClientMessage(new TranslatableComponent("block.pokecube_adventures.siphon.unlink",
                            new ThutTeleporter.TeleDest().setPos(pos).getInfoName()), true);
                }
                return true;
            }
            this.wirelessLinks.add(pos);
            if (user != null && user instanceof ServerPlayer)
            {
                final Player player = (Player) user;
                player.displayClientMessage(new TranslatableComponent("block.pokecube_adventures.siphon.link",
                        new ThutTeleporter.TeleDest().setPos(pos).getInfoName()), true);
            }
            return true;
        }
        return false;
    }
}
