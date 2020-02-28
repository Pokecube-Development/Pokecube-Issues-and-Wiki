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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.goal.SitGoal;
import net.minecraft.entity.passive.IFlyingAnimal;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.logic.LogicMiscUpdate;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.capabilities.DefaultPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.interfaces.pokemob.ai.GeneralStates;
import pokecube.core.interfaces.pokemob.ai.LogicStates;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.utils.Tools;
import thut.api.entity.IMobColourable;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.maths.Vector3;
import thut.api.maths.vecmath.Matrix3f;
import thut.api.maths.vecmath.Vector3f;
import thut.api.world.mobs.data.Data;
import thut.core.common.world.mobs.data.DataSync_Impl;

public class EntityPokemob extends ShoulderRidingEntity implements IEntityAdditionalSpawnData, IFlyingAnimal,
        IMobColourable
{
    public final DefaultPokemob pokemobCap;
    protected final EntitySize  size;

    public EntityPokemob(final EntityType<? extends ShoulderRidingEntity> type, final World world)
    {
        super(type, world);
        final DefaultPokemob cap = (DefaultPokemob) this.getCapability(CapabilityPokemob.POKEMOB_CAP, null)
                .orElse(null);
        this.pokemobCap = cap == null ? new DefaultPokemob(this) : cap;
        this.size = new EntitySize(cap.getPokedexEntry().width, cap.getPokedexEntry().height, true);
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
    public boolean canFitPassenger(final Entity passenger)
    {
        // TODO see thutcrafts for what to do here!
        return super.canFitPassenger(passenger);
    }

    @Override
    public boolean canPassengerSteer()
    {
        if (this.getPassengers().isEmpty()) return false;
        return this.getPassengers().get(0).getUniqueID().equals(this.pokemobCap.getOwnerId());
    }

    @Override
    public Entity getControllingPassenger()
    {
        final List<Entity> passengers = this.getPassengers();
        if (passengers.isEmpty()) return null;
        return this.getPassengers().get(0).getUniqueID().equals(this.pokemobCap.getOwnerId()) ? this.getPassengers()
                .get(0) : null;
    }

    @Override
    public void updatePassenger(final Entity passenger)
    {
        if (!this.isPassenger(passenger)) return;
        // TODO find passenger index.
        final int index = 0;
        final double[] offsets = this.pokemobCap.getPokedexEntry().passengerOffsets[index];
        float dx = 0, dy = this.getHeight(), dz = 0;
        final Vector3 sizes = this.pokemobCap.getMobSizes();
        dx = (float) (offsets[0] * sizes.x);
        dy = (float) (offsets[1] * sizes.y);
        dz = (float) (offsets[2] * sizes.z);
        Vector3f v = new Vector3f(dx, dy, dz);
        final float yaw = -this.rotationYaw * 0.017453292F;
        final float pitch = -this.rotationPitch * 0.017453292F;
        final float sinYaw = MathHelper.sin(yaw);
        final float cosYaw = MathHelper.cos(yaw);
        final float sinPitch = MathHelper.sin(pitch);
        final float cosPitch = MathHelper.cos(pitch);
        final Matrix3f matrixYaw = new Matrix3f(cosYaw, 0, sinYaw, 0, 1, 0, -sinYaw, 0, cosYaw);
        final Matrix3f matrixPitch = new Matrix3f(cosPitch, -sinPitch, 0, sinPitch, cosPitch, 0, 0, 0, 1);
        final Matrix3f transform = new Matrix3f();
        transform.mul(matrixYaw, matrixPitch);
        v = (Vector3f) v.clone();
        transform.transform(v);
        passenger.setPosition(this.posX + v.x, this.posY + v.y, this.posZ + v.z);
    }

    @Override
    public boolean canBeRiddenInWater()
    {
        return this.pokemobCap.canUseSurf() || this.pokemobCap.canUseDive();
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
    public SitGoal getAISit()
    {
        // TODO custom sitting ai?
        return super.getAISit();
    }

    @Override
    /** Get the experience points the entity currently has. */
    protected int getExperiencePoints(final PlayerEntity player)
    {
        final float scale = (float) PokecubeCore.getConfig().expFromDeathDropScale;
        final int exp = (int) Math.max(1,
                this.pokemobCap.getBaseXP() * scale * 0.01 * Math.sqrt(this.pokemobCap.getLevel()));
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
        this.ignoreFrustumCheck = false;
        if (this.pokemobCap.getCombatState(CombatStates.DYNAMAX))
        {
            // Since we don't change hitbox, we need toset this here.
            this.ignoreFrustumCheck = true;
            size = (float) (PokecubeCore.getConfig().dynamax_scale / this.pokemobCap.getMobSizes().y);
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
