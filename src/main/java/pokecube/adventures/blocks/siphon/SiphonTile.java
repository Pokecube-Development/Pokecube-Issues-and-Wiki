package pokecube.adventures.blocks.siphon;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import pokecube.adventures.PokecubeAdv;
import pokecube.core.blocks.InteractableTile;
import thut.api.LinkableCaps.ILinkStorage;

public class SiphonTile extends InteractableTile implements ITickableTileEntity
{
    public static class EnergyStore implements IEnergyStorage, ICapabilitySerializable<CompoundNBT>
    {
        private final LazyOptional<IEnergyStorage> holder = LazyOptional.of(() -> this);
        public int                                 currentOutput;
        public int                                 theoreticalOutput;

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
        {
            return CapabilityEnergy.ENERGY.orEmpty(cap, this.holder);
        }

        @Override
        public CompoundNBT serializeNBT()
        {
            final CompoundNBT tag = new CompoundNBT();
            tag.putInt("cO", this.currentOutput);
            tag.putInt("tO", this.theoreticalOutput);
            return tag;
        }

        @Override
        public void deserializeNBT(final CompoundNBT nbt)
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

    public AxisAlignedBB box;

    public List<Entity> mobs = Lists.newArrayList();

    public long updateTime = -1;

    public EnergyStore energy;

    public List<GlobalPos> wirelessLinks = Lists.newArrayList();

    public SiphonTile()
    {
        super(PokecubeAdv.SIPHON_TYPE.get());
    }

    public SiphonTile(final TileEntityType<?> tileEntityTypeIn)
    {
        super(tileEntityTypeIn);
    }

    @Override
    public ActionResultType onInteract(final BlockPos pos, final PlayerEntity player, final Hand hand,
            final BlockRayTraceResult hit)
    {
        if (hand == Hand.MAIN_HAND && this.energy != null && player instanceof ServerPlayerEntity)
        {
            ITextComponent message = null;
            message = new TranslationTextComponent("block.rfsiphon.info", this.energy.theoreticalOutput
                    - this.energy.currentOutput, this.energy.theoreticalOutput);
            player.sendMessage(message, Util.NIL_UUID);
        }
        return super.onInteract(pos, player, hand, hit);
    }

    @Override
    public void tick()
    {
        if (!this.level.isClientSide) MinecraftForge.EVENT_BUS.post(new SiphonTickEvent(this));
    }

    @Override
    public void load(final BlockState stateIn, final CompoundNBT compound)
    {
        this.wirelessLinks.clear();
        final CompoundNBT wireless = compound.getCompound("links");
        final int n = wireless.getInt("n");
        for (int i = 0; i < n; i++)
        {
            final INBT tag = wireless.get("" + i);
            this.wirelessLinks.add(GlobalPos.CODEC.decode(NBTDynamicOps.INSTANCE, tag).result().get().getFirst());
        }
        super.load(stateIn, compound);
    }

    @Override
    public CompoundNBT save(final CompoundNBT compound)
    {
        final CompoundNBT wireless = new CompoundNBT();
        wireless.putInt("n", this.wirelessLinks.size());
        int n = 0;
        for (final GlobalPos pos : this.wirelessLinks)
        {
            final INBT tag = GlobalPos.CODEC.encodeStart(NBTDynamicOps.INSTANCE, pos).get().left().get();
            wireless.put("" + n++, tag);
        }
        compound.put("links", wireless);
        return super.save(compound);
    }

    public boolean tryLink(final ILinkStorage link, final Entity user)
    {
        if (!PokecubeAdv.config.wirelessSiphons) return false;
        final GlobalPos pos = link.getLinkedPos(user);
        if (pos != null)
        {
            if (this.wirelessLinks.remove(pos))
            {
                if (user != null && user instanceof ServerPlayerEntity) user.sendMessage(new TranslationTextComponent(
                        "block.pokecube_adventures.siphon.unlink", pos.pos().getX(), pos.pos().getY(), pos.pos().getZ(),
                        pos.dimension()), Util.NIL_UUID);
                return true;
            }
            this.wirelessLinks.add(pos);
            if (user != null && user instanceof ServerPlayerEntity) user.sendMessage(new TranslationTextComponent(
                    "block.pokecube_adventures.siphon.link", pos.pos().getX(), pos.pos().getY(), pos.pos().getZ(), pos
                            .dimension()), Util.NIL_UUID);
            return true;
        }
        return false;
    }
}
