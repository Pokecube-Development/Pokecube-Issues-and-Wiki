/**
 *
 */
package pokecube.core.entity.pokemobs;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockState;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.passive.ShoulderRidingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;
import pokecube.core.PokecubeCore;
import pokecube.core.entity.pokemobs.helper.PokemobCombat;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.utils.Tools;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.world.mobs.data.Data;
import thut.core.common.world.mobs.data.DataSync_Impl;

public class EntityPokemob extends PokemobCombat
{
    public EntityPokemob(final EntityType<? extends ShoulderRidingEntity> type, final World world)
    {
        super(type, world);
    }

    @Override
    public boolean func_213439_d(final ServerPlayerEntity p_213439_1_)
    {
        final CompoundNBT compoundnbt = new CompoundNBT();
        compoundnbt.putString("id", this.getEntityString());
        compoundnbt.putInt("pokemob:uid", this.pokemobCap.getPokemonUID());
        this.writeWithoutTypeId(compoundnbt);
        if (p_213439_1_.addShoulderEntity(compoundnbt))
        {
            this.remove(true);
            return true;
        }
        else return false;
    }

    @Override
    public AgeableEntity createChild(final AgeableEntity ageable)
    {
        final IPokemob other = CapabilityPokemob.getPokemobFor(ageable);
        if (other == null) return null;
        final EntityPokemobEgg egg = EntityPokemobEgg.TYPE.create(this.getEntityWorld());
        egg.setStackByParents(this, other);
        return egg;
    }

    @Override
    public boolean canBreatheUnderwater()
    {
        return this.pokemobCap.swims() || this.pokemobCap.canUseDive();
    }

    @Override
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean onLivingFall(final float distance, final float damageMultiplier)
    {
        // TODO maybe do something here?
        // Vanilla plays sound and does damage, but only plays the sound if
        // damage occurred, maybe we should just play the sound instead?
        return super.onLivingFall(distance, damageMultiplier);
    }

    @Override
    public void tick()
    {
        if (this.getEntityWorld() instanceof ServerWorld)
        {
            final PlayerEntity near = this.getEntityWorld().getClosestPlayer(this, -1);
            if (near != null && this.getOwnerId() == null)
            {
                final float dist = near.getDistance(this);
                if (PokecubeCore.getConfig().cull && dist > PokecubeCore.getConfig().cullDistance)
                {
                    this.pokemobCap.onRecall();
                    return;
                }
                if (dist > PokecubeCore.getConfig().aiDisableDistance) return;
            }
        }
        super.tick();
    }

    @Override
    public SitGoal getAISit()
    {
        // TODO custom sitting ai?
        return super.getAISit();
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
        this.seatCount = data.readInt();
        final PacketBuffer buffer = new PacketBuffer(data);
        try
        {
            CompoundNBT tag = buffer.readCompoundTag();
            final ListNBT list = (ListNBT) tag.get("g");
            final IMobGenetics genes = this.getCapability(GeneRegistry.GENETICS_CAP).orElse(this.pokemobCap.genes);
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
    protected void handleFluidJump(final Tag<Fluid> fluidTag)
    {
        this.setMotion(this.getMotion().add(0.0D, 0.04F * this.getAttribute(LivingEntity.SWIM_SPEED).getValue(), 0.0D));
    }

    @Override
    public void remove(final boolean keepData)
    {
        boolean shouldRecall = this.addedToChunk;
        final boolean remote = !(this.getEntityWorld() instanceof ServerWorld);
        boolean remoteRecall = remote && PokecubeCore.getConfig().autoRecallPokemobs;
        remoteRecall = remoteRecall && PokecubeCore.proxy.getPlayer().getUniqueID().equals(this.pokemobCap.getOwnerId())
                && !this.pokemobCap.getGeneralState(GeneralStates.STAYING);
        shouldRecall = shouldRecall && (!remote || remoteRecall);
        if (!keepData && shouldRecall) this.pokemobCap.onRecall();
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
        this.initSeats();
        data.writeInt(this.seatCount);
        this.pokemobCap.updateHealth();
        this.pokemobCap.onGenesChanged();
        final IMobGenetics genes = this.getCapability(GeneRegistry.GENETICS_CAP).orElse(this.pokemobCap.genes);
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

    @Override
    public float getBlockPathWeight(final BlockPos pos, final IWorldReader worldIn)
    {
        return super.getBlockPathWeight(pos, worldIn);
    }

    @Override
    public boolean canSpawn(final IWorld worldIn, final SpawnReason spawnReasonIn)
    {
        return true;
    }

    private int despawntimer = 0;

    private boolean cullCheck(final double distanceToClosestPlayer)
    {
        boolean player = distanceToClosestPlayer < PokecubeCore.getConfig().cullDistance;
        this.despawntimer--;
        if (PokecubeCore.getConfig().despawn)
            if (this.despawntimer < 0 || player) this.despawntimer = PokecubeCore.getConfig().despawnTimer;
            else if (this.despawntimer == 0) return true;
        player = Tools.isAnyPlayerInRange(PokecubeCore.getConfig().cullDistance, this.getEntityWorld().getHeight(),
                this);
        if (PokecubeCore.getConfig().cull && !player) return true;
        return false;
    }

    @Override
    public boolean canDespawn(final double distanceToClosestPlayer)
    {
        return this.cullCheck(distanceToClosestPlayer);
    }
}
