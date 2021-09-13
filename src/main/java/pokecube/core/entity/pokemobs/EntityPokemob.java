/**
 *
 */
package pokecube.core.entity.pokemobs;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockState;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
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
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.Path;
import net.minecraft.potion.Effects;
import net.minecraft.tags.ITag;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.network.NetworkHooks;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.logic.LogicMountedControl;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.entity.pokemobs.helper.PokemobRidable;
import pokecube.core.events.pokemob.FaintEvent;
import pokecube.core.events.pokemob.SpawnEvent;
import pokecube.core.events.pokemob.SpawnEvent.Variance;
import pokecube.core.handlers.Config;
import pokecube.core.handlers.events.SpawnHandler;
import pokecube.core.handlers.playerdata.PlayerPokemobCache;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.items.pokemobeggs.ItemPokemobEgg;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.PokemobTracker;
import pokecube.core.utils.TagNames;
import pokecube.core.utils.Tools;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.api.world.mobs.data.Data;
import thut.core.common.world.mobs.data.DataSync_Impl;

public class EntityPokemob extends PokemobRidable
{
    static ResourceLocation WALL_CLIMBERS = new ResourceLocation(PokecubeMod.ID, "wall_climbing");

    private static final DataParameter<Byte> CLIMBING = EntityDataManager.defineId(EntityPokemob.class,
            DataSerializers.BYTE);

    public EntityPokemob(final EntityType<? extends ShoulderRidingEntity> type, final World world)
    {
        super(type, world);
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(EntityPokemob.CLIMBING, (byte) 0);
    }

    @Override
    public boolean setEntityOnShoulder(final ServerPlayerEntity p_213439_1_)
    {
        final CompoundNBT compoundnbt = new CompoundNBT();
        compoundnbt.putString("id", this.getEncodeId());
        compoundnbt.putInt("pokemob:uid", this.pokemobCap.getPokemonUID());
        this.saveWithoutId(compoundnbt);
        if (p_213439_1_.setEntityOnShoulder(compoundnbt))
        {
            this.remove(true);
            return true;
        }
        else return false;
    }

    @Override
    public AgeableEntity getBreedOffspring(final ServerWorld p_241840_1_, final AgeableEntity ageable)
    {
        final IPokemob other = CapabilityPokemob.getPokemobFor(ageable);
        if (other == null) return null;
        final EntityPokemobEgg egg = EntityPokemobEgg.TYPE.create(this.getCommandSenderWorld());
        egg.setStackByParents(this, other);
        return egg;
    }

    @Override
    public boolean isInWall()
    {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater()
    {
        return this.pokemobCap.swims() || this.pokemobCap.canUseDive() || this.pokemobCap.isType(PokeType.getType(
                "water"));
    }

    @Override
    public IPacket<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean causeFallDamage(final float distance, final float damageMultiplier)
    {
        // TODO maybe do something here?
        // Vanilla plays sound and does damage, but only plays the sound if
        // damage occurred, maybe we should just play the sound instead?
        return super.causeFallDamage(distance, damageMultiplier);
    }

    @Override
    protected void tickDeath()
    {
        ++this.deathTime;
        if (!(this.getCommandSenderWorld() instanceof ServerWorld)) return;

        if (this.isVehicle()) this.ejectPassengers();

        final boolean isTamed = this.pokemobCap.getOwnerId() != null;
        boolean despawn = isTamed ? PokecubeCore.getConfig().tameDeadDespawn : PokecubeCore.getConfig().wildDeadDespawn;
        this.setNoGravity(false);
        final boolean noPoof = this.getPersistentData().getBoolean(TagNames.NOPOOF);
        if (this.deathTime >= PokecubeCore.getConfig().deadDespawnTimer)
        {
            final FaintEvent event = new FaintEvent(this.pokemobCap);
            PokecubeCore.POKEMOB_BUS.post(event);
            final Result res = event.getResult();
            despawn = res == Result.DEFAULT ? despawn : res == Result.ALLOW;
            if (despawn && !noPoof) this.pokemobCap.onRecall(true);
            for (int k = 0; k < 20; ++k)
            {
                final double d2 = this.random.nextGaussian() * 0.02D;
                final double d0 = this.random.nextGaussian() * 0.02D;
                final double d1 = this.random.nextGaussian() * 0.02D;
                this.level.addParticle(ParticleTypes.POOF, this.getX() + this.random.nextFloat() * this.getBbWidth()
                        * 2.0F - this.getBbWidth(), this.getY() + this.random.nextFloat() * this.getBbHeight(), this
                                .getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(), d2,
                        d0, d1);
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
    public void die(final DamageSource cause)
    {
        super.die(cause);
        this.pokemobCap.setCombatState(CombatStates.FAINTED, true);
    }

    @Override
    public void travel(final Vector3d dr)
    {
        // If we are ridden on ground, do similar stuff to horses.
        if (this.isVehicle())
        {
            final LogicMountedControl controller = this.pokemobCap.getController();
            if (!controller.hasInput())
            {
                this.setZza(0);
                this.setXxa(0);
                this.setYya(0);
                this.jumpPower = 0.0f;
                this.setJumping(false);
                super.travel(dr);
                final Vector3d motion = this.getDeltaMovement();
                this.setDeltaMovement(motion.x * 0.5, motion.y, motion.z * 0.5);
                return;
            }
            final LivingEntity livingentity = (LivingEntity) this.getControllingPassenger();
            this.pokemobCap.setHeading(livingentity.yRot);
            this.yRotO = this.yRot;
            this.xRot = livingentity.xRot * 0.5F;
            this.setRot(this.yRot, this.xRot);
            this.yBodyRot = this.yRot;
            this.yHeadRot = this.yBodyRot;
            float strafe = livingentity.xxa * 0.5F;
            float forwards = livingentity.zza;
            if (forwards <= 0.0F) forwards *= 0.25F;

            if (!this.onGround && this.jumpPower == 0.0F)
            {
                strafe = 0.0F;
                forwards = 0.0F;
            }
            if (this.jumpPower > 0.0F && !this.jumping && this.onGround)
            {
                final double jumpStrength = 1.7;
                final double preBoostJump = jumpStrength * this.jumpPower * this.getBlockJumpFactor();
                double jumpAmount;
                if (this.hasEffect(Effects.JUMP)) jumpAmount = preBoostJump + (this.getEffect(Effects.JUMP)
                        .getAmplifier() + 1) * 0.1F;
                else jumpAmount = preBoostJump;

                final Vector3d vector3d = this.getDeltaMovement();
                this.setDeltaMovement(vector3d.x, jumpAmount, vector3d.z);
                this.setJumping(true);
                this.hasImpulse = true;
                net.minecraftforge.common.ForgeHooks.onLivingJump(this);
                if (forwards > 0.0F)
                {
                    final float sinYaw = MathHelper.sin(this.yRot * ((float) Math.PI / 180F));
                    final float cosYaw = MathHelper.cos(this.yRot * ((float) Math.PI / 180F));
                    this.setDeltaMovement(this.getDeltaMovement().add(-0.4F * sinYaw * this.jumpPower, 0.0D, 0.4F
                            * cosYaw * this.jumpPower));
                }
                this.jumpPower = 0.0F;
            }
            this.flyingSpeed = this.getSpeed() * 0.1F;
            if (this.isControlledByLocalInstance())
            {
                final double speed = controller.throttle;
                final float speedFactor = (float) (1 + Math.sqrt(this.pokemobCap.getPokedexEntry().getStatVIT()) / 10F);
                final Config config = PokecubeCore.getConfig();
                final float moveSpeed = (float) (config.groundSpeedFactor * speed * speedFactor);
                this.setSpeed(moveSpeed);
                super.travel(new Vector3d(strafe, dr.y, forwards));
            }
            else if (livingentity instanceof PlayerEntity) this.setDeltaMovement(Vector3d.ZERO);

            if (this.onGround)
            {
                this.jumpPower = 0.0F;
                this.setJumping(false);
            }
            this.calculateEntityAnimation(this, false);
            return;
        }
        this.flyingSpeed = 0.02f;
        // Swimming mobs get their own treatment while swimming
        if (this.isEffectiveAi() && this.isInWater() && this.pokemobCap.swims())
        {
            this.moveRelative(this.getSpeed(), dr);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8D));
        }
        // Otherwise just act like vanilla
        else super.travel(dr);
    }

    @Override
    @Nullable
    protected ResourceLocation getDefaultLootTable()
    {
        if (this.getPersistentData().getBoolean(TagNames.CLONED) && !PokecubeCore.getConfig().clonesDrop) return null;
        if (this.getPersistentData().getBoolean(TagNames.NODROP)) return null;
        if (PokecubeCore.getConfig().pokemobsDropItems) return this.pokemobCap.getPokedexEntry().lootTable;
        else return null;
    }

    @Override
    public ItemStack getPickedResult(final RayTraceResult target)
    {
        return ItemPokemobEgg.getEggStack(this.pokemobCap);
    }

    @Override
    public ILivingEntityData finalizeSpawn(final IServerWorld worldIn, final DifficultyInstance difficultyIn,
            final SpawnReason reason, final ILivingEntityData spawnDataIn, final CompoundNBT dataTag)
    {
        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(this);
        if (pokemob == null || !(worldIn instanceof World)) return super.finalizeSpawn(worldIn, difficultyIn, reason,
                spawnDataIn, dataTag);
        final PokedexEntry pokeEntry = pokemob.getPokedexEntry();
        final SpawnData entry = pokeEntry.getSpawnData();
        if (entry == null) return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        final Vector3 loc = Vector3.getNewVector().set(this);
        final SpawnBiomeMatcher matcher = entry.getMatcher(worldIn, loc);

        final int orig_override = entry.getLevel(matcher);
        int overrideLevel = orig_override;
        final Variance variance = entry.getVariance(matcher);
        if (variance != null) overrideLevel = variance.apply(overrideLevel);

        if (pokemob != null)
        {
            final long time = System.nanoTime();
            int maxXP = 10;
            int level = 1;
            if (orig_override == -1) level = SpawnHandler.getSpawnLevel(this.level, loc, pokemob.getPokedexEntry(),
                    variance, overrideLevel);
            else
            {
                final SpawnEvent.Level event = new SpawnEvent.Level(pokemob.getPokedexEntry(), loc, this.level,
                        overrideLevel, variance);
                PokecubeCore.POKEMOB_BUS.post(event);
                level = event.getLevel();
            }
            maxXP = Tools.levelToXp(pokemob.getPokedexEntry().getEvolutionMode(), level);
            pokemob.getEntity().getPersistentData().putInt("spawnExp", maxXP);
            final double dt = (System.nanoTime() - time) / 10e3D;
            if (PokecubeMod.debug && dt > 100)
            {
                final String toLog = "location: %1$s took: %2$s\u00B5s to spawn Init for %3$s";
                PokecubeCore.LOGGER.info(String.format(toLog, loc.getPos(), dt, pokemob.getDisplayName().getString()));
            }
        }
        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
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
            CompoundNBT tag = buffer.readNbt();
            final ListNBT list = (ListNBT) tag.get("g");
            final IMobGenetics genes = this.getCapability(GeneRegistry.GENETICS_CAP).orElse(this.pokemobCap.genes);
            GeneRegistry.GENETICS_CAP.readNBT(genes, null, list);
            this.pokemobCap.read(tag.getCompound("p"));
            this.pokemobCap.onGenesChanged();
            tag = buffer.readNbt();
            if (!tag.isEmpty()) this.getPersistentData().put("url_model", tag);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void checkFallDamage(final double y, final boolean onGroundIn, final BlockState state, final BlockPos pos)
    {
    }

    @Override
    protected void jumpInLiquid(final ITag<Fluid> fluidTag)
    {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.04F * this.getAttribute(
                net.minecraftforge.common.ForgeMod.SWIM_SPEED.get()).getValue(), 0.0D));
    }

    @Override
    public void handleInsidePortal(final BlockPos pos)
    {// Nope, no nether portal for us.
    }

    @Override
    protected SoundEvent getAmbientSound()
    {
        return this.pokemobCap.getSound();
    }

    @Override
    protected float getSoundVolume()
    {
        return (float) PokecubeCore.getConfig().idleSoundVolume;
    }

    @Override
    public SoundCategory getSoundSource()
    {
        return SoundCategory.HOSTILE;
    }

    @Override
    public int getAmbientSoundInterval()
    {
        return PokecubeCore.getConfig().idleSoundRate;
    }

    @Override
    public void onAddedToWorld()
    {
        if (!this.isAddedToWorld())
        {
            PokemobTracker.addPokemob(this.pokemobCap);
            if (this.pokemobCap.isPlayerOwned() && this.pokemobCap.getOwnerId() != null) PlayerPokemobCache.UpdateCache(
                    this.pokemobCap);
        }
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
        buffer.writeNbt(nbt);
        nbt = this.getPersistentData().getCompound("url_model");
        buffer.writeNbt(nbt);
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
    public boolean checkSpawnRules(final IWorld worldIn, final SpawnReason spawnReasonIn)
    {
        return true;
    }

    private int despawntimer = 0;

    @Override
    public boolean requiresCustomPersistence()
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
        final boolean noPoof = this.getPersistentData().getBoolean(TagNames.NOPOOF);
        if (noPoof) return false;
        if (PokecubeCore.getConfig().despawn)
        {
            this.despawntimer--;
            if (this.despawntimer < 0 || player) this.despawntimer = PokecubeCore.getConfig().despawnTimer;
            else if (this.despawntimer == 0) return true;
        }
        player = Tools.isAnyPlayerInRange(PokecubeCore.getConfig().cullDistance, this.getCommandSenderWorld()
                .getMaxBuildHeight(), this);
        if (PokecubeCore.getConfig().cull && !player) return true;
        return false;
    }

    @Override
    public boolean removeWhenFarAway(final double distanceToClosestPlayer)
    {
        return this.cullCheck(distanceToClosestPlayer);
    }

    @Override
    public void readAdditionalSaveData(final CompoundNBT compound)
    {
        super.readAdditionalSaveData(compound);
        if (compound.contains("OwnerUUID")) try
        {
            final UUID id = UUID.fromString(compound.getString("OwnerUUID"));
            if (id != null)
            {
                this.setOwnerUUID(id);
                this.setTame(true);
            }
        }
        catch (final Exception e)
        {
            PokecubeCore.LOGGER.error("Error recovering old owner!");
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick()
    {
        super.tick();
        if (!this.level.isClientSide)
        {
            boolean climb = this.horizontalCollision && this.getNavigation().isInProgress();
            if (climb)
            {
                final Path p = this.getNavigation().getPath();
                climb = p.getNextNodePos().getY() >= this.getY();
            }
            this.setBesideClimbableBlock(climb);
        }

    }

    /**
     * Returns true if this entity should move as if it were on a ladder (either
     * because it's actually on a ladder, or
     * for AI reasons)
     */
    @Override
    public boolean onClimbable()
    {
        return this.isBesideClimbableBlock();
    }

    /**
     * Returns true if the WatchableObject (Byte) is 0x01 otherwise returns
     * false. The WatchableObject is updated using
     * setBesideClimableBlock.
     */
    public boolean isBesideClimbableBlock()
    {
        return (this.entityData.get(EntityPokemob.CLIMBING) & 1) != 0;
    }

    /**
     * Updates the WatchableObject (Byte) created in entityInit(), setting it to
     * 0x01 if par1 is true or 0x00 if it is
     * false.
     */
    public void setBesideClimbableBlock(final boolean climbing)
    {
        // Only do this if tagged accordingly
        if (!ItemList.is(EntityPokemob.WALL_CLIMBERS, this)) return;

        byte b0 = this.entityData.get(EntityPokemob.CLIMBING);
        if (climbing) b0 = (byte) (b0 | 1);
        else b0 = (byte) (b0 & -2);

        this.entityData.set(EntityPokemob.CLIMBING, b0);
    }
}
