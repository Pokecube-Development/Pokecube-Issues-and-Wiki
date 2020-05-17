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
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.ShoulderRidingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.Tag;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.network.NetworkHooks;
import pokecube.core.PokecubeCore;
import pokecube.core.entity.pokemobs.helper.PokemobHasParts;
import pokecube.core.events.pokemob.FaintEvent;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.utils.PokemobTracker;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.world.mobs.data.Data;
import thut.core.common.world.mobs.data.DataSync_Impl;

public class EntityPokemob extends PokemobHasParts
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
    public void livingTick()
    {
        if (this.getEntityWorld() instanceof ServerWorld)
        {
            if (this.pokemobCap.getOwnerId() != null) this.enablePersistence();
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
        super.livingTick();
    }

    @Override
    protected void onDeathUpdate()
    {
        final boolean isTamed = this.pokemobCap.getOwnerId() != null;
        boolean despawn = isTamed ? PokecubeCore.getConfig().tameDeadDespawn : PokecubeCore.getConfig().wildDeadDespawn;
        ++this.deathTime;
        if (this.deathTime == PokecubeCore.getConfig().deadDespawnTimer)
        {
            final FaintEvent event = new FaintEvent(this.pokemobCap);
            PokecubeCore.POKEMOB_BUS.post(event);
            final Result res = event.getResult();
            despawn = res == Result.DEFAULT ? despawn : res == Result.ALLOW;
            if (this.getPersistentData().contains(TagNames.NOPOOF)) despawn = false;
            if (despawn) this.pokemobCap.onRecall(true);
            for (int k = 0; k < 20; ++k)
            {
                final double d2 = this.rand.nextGaussian() * 0.02D;
                final double d0 = this.rand.nextGaussian() * 0.02D;
                final double d1 = this.rand.nextGaussian() * 0.02D;
                this.world.addParticle(ParticleTypes.POOF, this.posX + this.rand.nextFloat() * this.getWidth() * 2.0F
                        - this.getWidth(), this.posY + this.rand.nextFloat() * this.getHeight(), this.posZ + this.rand
                                .nextFloat() * this.getWidth() * 2.0F - this.getWidth(), d2, d0, d1);
            }
        }
        if (this.deathTime >= PokecubeCore.getConfig().deadReviveTimer)
        {
            this.pokemobCap.revive();
            // If we revive naturally, we remove this tag, it only applies for
            // forced revivals
            this.getPersistentData().remove(TagNames.REVIVED);
        }
    }

    @Override
    public void onDeath(final DamageSource cause)
    {
        super.onDeath(cause);
        this.pokemobCap.setLogicState(LogicStates.FAINTED, true);
    }

    @Override
    protected float getWaterSlowDown()
    {
        return 0.8f;
    }

    @Override
    public void travel(final Vec3d dr)
    {
        if (this.isServerWorld() && this.isInWater() && this.pokemobCap.swims())
        {
            this.moveRelative(this.getAIMoveSpeed(), dr);
            this.move(MoverType.SELF, this.getMotion());
            this.setMotion(this.getMotion().scale(0.8D));
        }
        else super.travel(dr);
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
    public void setPortal(final BlockPos pos)
    {// Nope, no nether portal for us.
    }

    @Override
    public void onAddedToWorld()
    {
        PokemobTracker.addPokemob(this.pokemobCap);
        if (this.pokemobCap.isPlayerOwned() && this.pokemobCap.getOwnerId() != null) PlayerPokemobCache.UpdateCache(
                this.pokemobCap);
        super.onAddedToWorld();
    }

    @Override
    public void onRemovedFromWorld()
    {
        PokemobTracker.removePokemob(this.pokemobCap);
        if (this.pokemobCap.isPlayerOwned() && this.pokemobCap.getOwnerId() != null) PlayerPokemobCache.UpdateCache(
                this.pokemobCap);
        super.onRemovedFromWorld();
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

    @Override
    public boolean preventDespawn()
    {
        final boolean despawns = PokecubeCore.getConfig().despawn;
        final boolean culls = PokecubeCore.getConfig().cull;
        final boolean owned = this.pokemobCap.getOwnerId() != null;
        if (owned) return true;
        if (this.getPersistentData().contains(TagNames.NOPOOF)) return true;
        return !(despawns || culls);
    }

    private boolean cullCheck(final double distanceToClosestPlayer)
    {
        if (this.pokemobCap.getOwnerId() != null) return false;
        boolean player = distanceToClosestPlayer < PokecubeCore.getConfig().cullDistance;
        if (PokecubeCore.getConfig().despawn)
        {
            this.despawntimer--;
            if (this.despawntimer < 0 || player) this.despawntimer = PokecubeCore.getConfig().despawnTimer;
            else if (this.despawntimer == 0) return true;
        }
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
