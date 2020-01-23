/**
 *
 */
package pokecube.core.entity.pokemobs;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockState;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.logic.LogicMiscUpdate;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.capabilities.DefaultPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import thut.api.entity.IMobColourable;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.world.mobs.data.Data;
import thut.core.common.world.mobs.data.DataSync_Impl;

public class EntityPokemob extends TameableEntity implements IEntityAdditionalSpawnData, IFlyingAnimal, IMobColourable
{
    protected final DefaultPokemob pokemobCap;
    protected final EntitySize     size;

    public EntityPokemob(final EntityType<? extends TameableEntity> type, final World world)
    {
        super(type, world);
        final DefaultPokemob cap = (DefaultPokemob) this.getCapability(CapabilityPokemob.POKEMOB_CAP, null).orElse(
                null);
        this.pokemobCap = cap == null ? new DefaultPokemob(this) : cap;
        this.size = new EntitySize(cap.getPokedexEntry().width, cap.getPokedexEntry().height, true);
    }

    @Override
    public boolean canFitPassenger(final Entity passenger)
    {
        // TODO Auto-generated method stub
        return super.canFitPassenger(passenger);
    }

    @Override
    public AgeableEntity createChild(final AgeableEntity ageable)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void fall(final float distance, final float damageMultiplier)
    {
    }

    @Override
    public SitGoal getAISit()
    {
        // TODO custom sitting ai.
        return super.getAISit();
    }

    @Override
    /** Get the experience points the entity currently has. */
    protected int getExperiencePoints(final PlayerEntity player)
    {
        final float scale = (float) PokecubeCore.getConfig().expFromDeathDropScale;
        final int exp = (int) Math.max(1, this.pokemobCap.getBaseXP() * scale * 0.01 * Math.sqrt(this.pokemobCap
                .getLevel()));
        return exp;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable()
    {
        if (this.getPersistentData().getBoolean("cloned")) return null;
        if (PokecubeCore.getConfig().pokemobsDropItems) return this.pokemobCap.getPokedexEntry().lootTable;
        else return null;
    }

    @Override
    public ItemStack getPickedResult(final RayTraceResult target)
    {
        return ItemPokemobEgg.getEggStack(this.pokemobCap);
    }

    @Override
    public float getRenderScale()
    {
        float size = (float) (this.pokemobCap.getSize() * PokecubeCore.getConfig().scalefactor);
        if (this.pokemobCap.getGeneralState(GeneralStates.EXITINGCUBE))
        {
            float scale = 1;
            scale = Math.min(1, (this.ticksExisted + 1) / (float) LogicMiscUpdate.EXITCUBEDURATION);
            size = Math.max(0.1f, scale);
        }
        return size;
    }

    @Override
    public EntitySize getSize(final Pose poseIn)
    {
        return this.size.scale(this.getRenderScale());
    }

    @Override
    public boolean isSitting()
    {
        return this.pokemobCap.getLogicState(LogicStates.SITTING);
    }

    @Override
    public void readSpawnData(final PacketBuffer data)
    {
        // Read the datasync stuff
        final List<Data<?>> data_list = Lists.newArrayList();
        final byte num = data.readByte();
        if (num > 0)
        {
            for (int i = 0; i < num; i++)
            {
                final int uid = data.readInt();
                try
                {
                    final Data<?> val = DataSync_Impl.makeData(uid);
                    val.read(data);
                    data_list.add(val);
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }
            }
            this.pokemobCap.dataSync().update(data_list);
        }

        final PacketBuffer buffer = new PacketBuffer(data);
        try
        {
            CompoundNBT tag = buffer.readCompoundTag();
            final ListNBT list = (ListNBT) tag.get("g");
            final IMobGenetics genes = this.getCapability(GeneRegistry.GENETICS_CAP, null).orElse(null);
            GeneRegistry.GENETICS_CAP.readNBT(genes, null, list);
            this.pokemobCap.read(tag.getCompound("p"));
            this.pokemobCap.onGenesChanged();
            tag = buffer.readCompoundTag();
            if (!tag.isEmpty()) this.getPersistentData().put("url_model", tag);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void setSitting(final boolean sitting)
    {
        this.pokemobCap.setLogicState(LogicStates.SITTING, sitting);
        super.setSitting(sitting);
    }

    @Override
    protected void updateFallState(final double y, final boolean onGroundIn, final BlockState state, final BlockPos pos)
    {
    }

    @Override
    public void remove(final boolean keepData)
    {
        if (!keepData && this.addedToChunk) this.pokemobCap.onRecall();
        super.remove(keepData);
    }

    @Override
    public void writeSpawnData(final PacketBuffer data)
    {
        // Write the dataSync stuff
        final List<Data<?>> data_list = this.pokemobCap.dataSync().getAll();
        final byte num = (byte) data_list.size();
        data.writeByte(num);
        for (int i = 0; i < num; i++)
        {
            final Data<?> val = data_list.get(i);
            data.writeInt(val.getUID());
            val.write(data);
        }
        this.pokemobCap.updateHealth();
        this.pokemobCap.onGenesChanged();
        final IMobGenetics genes = this.getCapability(GeneRegistry.GENETICS_CAP, null).orElse(null);
        final PacketBuffer buffer = new PacketBuffer(data);
        final ListNBT list = (ListNBT) GeneRegistry.GENETICS_CAP.writeNBT(genes, null);
        CompoundNBT nbt = new CompoundNBT();
        nbt.put("p", this.pokemobCap.write());
        nbt.put("g", list);
        buffer.writeCompoundTag(nbt);
        nbt = this.getPersistentData().getCompound("url_model");
        buffer.writeCompoundTag(nbt);
    }

    // Methods for IMobColourable
    @Override
    public int getDyeColour()
    {
        return this.pokemobCap.getDyeColour();
    }

    @Override
    public int[] getRGBA()
    {
        return this.pokemobCap.getRGBA();
    }

    @Override
    public void setDyeColour(final int colour)
    {
        this.pokemobCap.setDyeColour(colour);
    }

    @Override
    public void setRGBA(final int... colours)
    {
        this.pokemobCap.setRGBA(colours);
    }
}
