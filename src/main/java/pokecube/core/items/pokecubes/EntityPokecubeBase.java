package pokecube.core.items.pokecubes;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.items.pokecubes.helper.CaptureManager;
import pokecube.core.items.pokecubes.helper.SendOutManager;
import pokecube.core.utils.AITools;
import pokecube.core.utils.EntityTools;
import pokecube.core.utils.PokemobTracker;
import pokecube.core.utils.TagNames;
import thut.api.Tracker;
import thut.api.maths.Vector3;
import thut.core.common.network.EntityUpdate;

public abstract class EntityPokecubeBase extends LivingEntity
{
    public static final String CUBETIMETAG = "lastCubeTime";

    public static SoundEvent POKECUBESOUND;

    static final DataParameter<Integer>   ENTITYID;
    static final DataParameter<ItemStack> ITEM;
    static final DataParameter<Boolean>   RELEASING;
    static final DataParameter<Integer>   TIME;

    public static boolean SEEKING = true;

    static
    {
        ENTITYID = EntityDataManager.<Integer> defineId(EntityPokecubeBase.class, DataSerializers.INT);
        ITEM = EntityDataManager.<ItemStack> defineId(EntityPokecubeBase.class, DataSerializers.ITEM_STACK);
        RELEASING = EntityDataManager.<Boolean> defineId(EntityPokecubeBase.class, DataSerializers.BOOLEAN);
        TIME = EntityDataManager.<Integer> defineId(EntityPokecubeBase.class, DataSerializers.INT);
    }

    public static boolean canCaptureBasedOnConfigs(final IPokemob pokemob)
    {
        if (PokecubeCore.getConfig().captureDelayTillAttack) return !pokemob.getCombatState(CombatStates.NOITEMUSE);
        final long lastAttempt = pokemob.getEntity().getPersistentData().getLong(EntityPokecubeBase.CUBETIMETAG);
        final long now = Tracker.instance().getTick();
        final boolean capture = lastAttempt <= now;
        if (capture) pokemob.getEntity().getPersistentData().remove(EntityPokecubeBase.CUBETIMETAG);
        return capture;
    }

    public static void setNoCaptureBasedOnConfigs(final IPokemob pokemob)
    {
        if (PokecubeCore.getConfig().captureDelayTillAttack) pokemob.setCombatState(CombatStates.NOITEMUSE, true);
        else pokemob.getEntity().getPersistentData().putLong(EntityPokecubeBase.CUBETIMETAG, Tracker.instance()
                .getTick() + PokecubeCore.getConfig().captureDelayTicks);
    }

    public boolean canBePickedUp = true;
    /**
     * This gets decremented each tick, and will auto release if it hits 0, ie
     * will not auto release if below 0 to start with.
     */
    public int     autoRelease   = -1;
    public boolean isLoot        = false;

    public boolean isCapturing = false;

    private boolean checkCube = false;

    public ResourceLocation lootTable = null;

    protected int       inData;
    protected boolean   inGround;
    public UUID         shooter;
    public LivingEntity shootingEntity;

    public double       speed          = 2;
    public LivingEntity targetEntity;
    public Vector3      targetLocation = Vector3.getNewVector();

    protected Block    tile;
    protected BlockPos tilePos;
    private int        tilt = -1;
    public Vector3     v0   = Vector3.getNewVector();
    protected Vector3  v1   = Vector3.getNewVector();

    public Vector3 capturePos = Vector3.getNewVector();

    private Entity ignoreEntity;
    private int    ignoreTime;
    public boolean seeking = EntityPokecubeBase.SEEKING;

    NonNullList<ItemStack> stuff = NonNullList.create();

    public IPokemob containedMob = null;

    public EntityPokecubeBase(final EntityType<? extends EntityPokecubeBase> type, final World worldIn)
    {
        super(type, worldIn);
        this.noPhysics = false;
    }

    /** Called when the entity is attacked. */
    @Override
    public boolean hurt(final DamageSource source, final float damage)
    {
        if (this.isLoot || this.isReleasing() || !this.canBePickedUp) return false;
        if (source.getDirectEntity() instanceof ServerPlayerEntity && (this.tilt <= 0 || ((PlayerEntity) source
                .getDirectEntity()).abilities.instabuild))
        {
            final ServerPlayerEntity player = (ServerPlayerEntity) source.getDirectEntity();
            this.interact(player, Hand.MAIN_HAND);
            return false;
        }
        if (source == DamageSource.OUT_OF_WORLD)
        {
            if (PokecubeManager.isFilled(this.getItem()))
            {
                final IPokemob mob = CapabilityPokemob.getPokemobFor(SendOutManager.sendOut(this, true));
                if (mob != null) mob.onRecall();
            }
            this.remove();
        }
        return false;
    }

    @Override
    protected void outOfWorld()
    {
        final IPokemob mob = CapabilityPokemob.getPokemobFor(SendOutManager.sendOut(this, true, false));
        if (mob != null && mob.getOwnerId() != null) mob.onRecall();
    }

    /** Called when this EntityThrowable hits a block or entity. */
    protected void onImpact(final RayTraceResult result)
    {
        if (!this.isAlive()) return;

        final boolean serverSide = this.getCommandSenderWorld() instanceof ServerWorld;
        final boolean capturing = this.getTilt() >= 0;
        final boolean releasing = this.isReleasing();

        switch (result.getType())
        {
        case BLOCK:
            this.seeking = false;
            this.targetLocation.clear();
            this.targetEntity = null;

            // Only handle this on server, and if not capturing something
            if (serverSide)
            {
                boolean sendOut = PokecubeManager.isFilled(this.getItem()) && !this.getNoCollisionRelease();
                sendOut = sendOut && !(releasing || capturing);
                if (sendOut) SendOutManager.sendOut(this, true);
                EntityUpdate.sendEntityUpdate(this);
            }
            break;
        case ENTITY:
            final EntityRayTraceResult hit = (EntityRayTraceResult) result;
            final Entity hitEntity = EntityTools.getCoreEntity(hit.getEntity());
            final IPokemob hitMob = CapabilityPokemob.getPokemobFor(hitEntity);

            final boolean invalidStick = hitEntity instanceof PlayerEntity || !capturing || hitMob == null || hitMob
                    .getOwnerId() != null;

            // Set us to the location, but not stick to players.
            if (!invalidStick) this.setPos(result.getLocation().x, result.getLocation().y, result.getLocation().z);

            // Capturing or on client, break early.
            if (!serverSide || capturing || releasing) break;
            // Send out or try to capture.
            if (PokecubeManager.isFilled(this.getItem()))
            {
                final LivingEntity sent = SendOutManager.sendOut(this, true);
                if (sent instanceof MobEntity && hit.getEntity() instanceof LivingEntity) BrainUtils.initiateCombat(
                        (MobEntity) sent, (LivingEntity) hit.getEntity());
            }
            else CaptureManager.captureAttempt(this, this.random, hitEntity);
            break;
        case MISS:
            break;
        default:
            break;
        }
    }

    @Override
    public void onAddedToWorld()
    {
        PokemobTracker.addPokecube(this);
        super.onAddedToWorld();
    }

    @Override
    public void onRemovedFromWorld()
    {
        PokemobTracker.removePokecube(this);
        super.onRemovedFromWorld();
    }

    @Override
    public boolean isPickable()
    {
        return true;
    }

    @Override
    public boolean isPushable()
    {
        return !this.isReleasing();
    }

    @Override
    public boolean attackable()
    {
        return false;
    }

    public void checkCollision()
    {
        final AxisAlignedBB axisalignedbb = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(.2D);
        if (this.shootingEntity != null) this.ignoreEntity = this.shootingEntity;

        final Predicate<Entity> valid = (mob) ->
        {
            final Entity e = EntityTools.getCoreEntity(mob);
            if (this.ignoreEntity != null && (e.isPassengerOfSameVehicle(this.ignoreEntity) || e.hasPassenger(
                    this.ignoreEntity))) return false;
            if (!AITools.shouldBeAbleToAgro(this.shootingEntity, e)) return false;
            return !e.isSpectator() && mob.isPickable() && !(e instanceof EntityPokecubeBase)
                    && e instanceof LivingEntity && e != this.ignoreEntity && e != this;
        };

        if (!this.isReleasing()) for (final Entity entity : this.level.getEntities(this, axisalignedbb, valid))
        {
            if (entity == this.ignoreEntity)
            {
                ++this.ignoreTime;
                break;
            }

            if (this.shootingEntity != null && this.tickCount < 2 && this.ignoreEntity == null)
            {
                this.ignoreEntity = entity;
                this.ignoreTime = 10;
                break;
            }
            final EntityRayTraceResult hit = new EntityRayTraceResult(entity);
            if (hit.getType() == Type.ENTITY) this.onImpact(hit);
        }

        final RayTraceResult raytraceresult = EntityPokecubeBase.rayTrace(this, axisalignedbb, valid,
                RayTraceContext.BlockMode.COLLIDER, true);
        if (this.ignoreEntity != null && this.ignoreTime-- <= 0) this.ignoreEntity = null;

        trace:
        if (raytraceresult.getType() != RayTraceResult.Type.MISS)
        {
            if (raytraceresult.getType() == RayTraceResult.Type.BLOCK && this.level.getBlockState(
                    ((BlockRayTraceResult) raytraceresult).getBlockPos()).getBlock() == Blocks.NETHER_PORTAL) this
                            .handleInsidePortal(((BlockRayTraceResult) raytraceresult).getBlockPos());
            if (raytraceresult instanceof BlockRayTraceResult)
            {
                final BlockRayTraceResult result = (BlockRayTraceResult) raytraceresult;
                final BlockState hit = this.getCommandSenderWorld().getBlockState(result.getBlockPos());
                final VoxelShape shape = hit.getCollisionShape(this.getCommandSenderWorld(), result.getBlockPos());
                if (!shape.isEmpty() && !shape.bounds().move(result.getBlockPos()).intersects(axisalignedbb))
                    break trace;
            }
            if (!net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) this.onImpact(
                    raytraceresult);
            return;
        }
        if (this.isReleasing()) return;
        if (this.shootingEntity != null && this.tilt < 0 && this.getDeltaMovement().lengthSqr() == 0 && this
                .isEffectiveAi() && PokecubeManager.isFilled(this.getItem())) SendOutManager.sendOut(this, true);
        else if (!this.getNoCollisionRelease() && this.isInWater() && PokecubeManager.isFilled(this.getItem()))
            SendOutManager.sendOut(this, true);
    }

    public void preValidateVelocity()
    {
        // Calculate velocity if seeking
        if (this.tilt > 0 || this.targetEntity != null && !this.targetEntity.isAlive())
        {
            this.targetEntity = null;
            if (!this.targetLocation.equals(Vector3.secondAxisNeg)) this.targetLocation.clear();
        }

        final Vector3 target = Vector3.getNewVector();
        if (this.targetEntity != null) target.set(this.targetEntity);
        else target.set(this.targetLocation);
        if (!target.isEmpty() && this.seeking)
        {
            final Vector3 here = Vector3.getNewVector().set(this);
            final Vector3 dir = Vector3.getNewVector().set(target);
            if (this.targetEntity != null)
            {
                dir.x += this.targetEntity.getDeltaMovement().x;
                dir.y += this.targetEntity.getDeltaMovement().y;
                dir.z += this.targetEntity.getDeltaMovement().z;
            }
            final double dr = dir.distanceTo(here);
            double dist = dr / 10;
            dist = Math.min(2, dist);
            if (dr > 0.5) dist = Math.max(0.5, dist);
            dir.subtractFrom(here);
            dir.norm().scalarMultBy(dist);
            dir.setVelocities(this);
        }
    }

    private void validateDirection(final Vector3d vec3d)
    {
        final float f = MathHelper.sqrt(Entity.getHorizontalDistanceSqr(vec3d));
        if (f > 0.5)
        {
            this.yRot = (float) (-MathHelper.atan2(vec3d.x, vec3d.z) * (180F / (float) Math.PI));
            for (this.xRot = (float) (MathHelper.atan2(vec3d.y, f) * (180F / (float) Math.PI)); this.xRot
                    - this.xRotO < -180.0F; this.xRotO -= 360.0F)
                ;
        }
        else this.xRot = 0;
        while (this.xRot - this.xRotO >= 180.0F)
            this.xRotO += 360.0F;
        while (this.yRot - this.yRotO < -180.0F)
            this.yRotO -= 360.0F;
        while (this.yRot - this.yRotO >= 180.0F)
            this.yRotO += 360.0F;
        this.yBodyRot = this.yRot;
        this.yBodyRotO = this.yRotO;
        this.yHeadRot = this.yRot;
        this.yHeadRotO = this.yRotO;
    }

    private void postValidateVelocity()
    {
        final Vector3d vec3d = this.getDeltaMovement();
        this.validateDirection(vec3d);
        float f1;
        if (this.isInWater())
        {
            for (int i = 0; i < 4; ++i)
                this.level.addParticle(ParticleTypes.BUBBLE, this.getX() - vec3d.x * 0.25D, this.getY() - vec3d.y
                        * 0.25D, this.getZ() - vec3d.z * 0.25D, vec3d.x, vec3d.y, vec3d.z);
            f1 = 0.8F;
        }
        else f1 = 0.99F;

        this.setDeltaMovement(vec3d.scale(f1));
        final Vector3d motion = this.getDeltaMovement();
        if (motion.y == 0) this.setDeltaMovement(motion.x * 0.8, motion.y, motion.z * 0.8);
        if (!this.isNoGravity())
        {
            final Vector3d vec3d1 = this.getDeltaMovement();
            this.setDeltaMovement(vec3d1.x, vec3d1.y - this.getGravityVelocity(), vec3d1.z);
        }

        this.move(MoverType.SELF, this.getDeltaMovement());

    }

    /** Called to update the entity's position/logic. */
    @Override
    public void tick()
    {
        this.xOld = this.getX();
        this.yOld = this.getY();
        this.zOld = this.getZ();

        this.autoRelease--;
        if (this.autoRelease == 0) SendOutManager.sendOut(this, true);
        final boolean capturing = this.getTilt() >= 0;
        final boolean releasing = this.isReleasing();
        if (capturing || releasing) this.seeking = false;

        if (this.getY() < -64.0D) this.outOfWorld();

        if (this.checkCube)
        {
            this.checkCube = false;
            PokemobTracker.removePokecube(this);
            this.containedMob = PokecubeManager.itemToPokemob(this.getItem(), this.getCommandSenderWorld());
            if (this.containedMob != null && this.shooter == null)
            {
                this.shootingEntity = this.containedMob.getOwner();
                if (this.shootingEntity != null) this.shooter = this.shootingEntity.getUUID();
            }
            PokemobTracker.addPokecube(this);
        }

        this.preValidateVelocity();
        this.checkCollision();
        this.postValidateVelocity();
    }

    @Override
    public void addAdditionalSaveData(final CompoundNBT compound)
    {
        super.addAdditionalSaveData(compound);
        compound.putInt("tilt", this.tilt);
        compound.putInt("time", this.getTime());
        compound.putBoolean("releasing", this.isReleasing());
        if (this.shooter != null) compound.putString("shooter", this.shooter.toString());
        if (this.getItem() != null) compound.put("Item", this.getItem().save(new CompoundNBT()));
        if (this.isCapturing) this.capturePos.writeToNBT(compound, "capt_");
        compound.putByte("inGround", (byte) (this.inGround ? 1 : 0));
        compound.putInt("autorelease", this.autoRelease);
        if (this.shooter != null) compound.put("owner", NBTUtil.createUUID(this.shooter));

    }

    /**
     * (abstract) Protected helper method to read subclass entity data from
     * NBT.
     */
    @Override
    public void readAdditionalSaveData(final CompoundNBT compound)
    {
        super.readAdditionalSaveData(compound);
        this.tilt = compound.getInt("tilt");
        this.setTime(compound.getInt("time"));
        this.setReleasing(compound.getBoolean("releasing"));
        final CompoundNBT CompoundNBT1 = compound.getCompound("Item");
        this.setItem(ItemStack.of(CompoundNBT1));
        if (compound.contains("shooter")) this.shooter = UUID.fromString(compound.getString("shooter"));
        this.isCapturing = compound.contains("capt_x");
        if (this.isCapturing) this.capturePos = Vector3.readFromNBT(compound, "capt_");
        this.autoRelease = compound.getInt("autorelease");
        this.inGround = compound.getByte("inGround") == 1;
        if (compound.contains("owner", 10)) this.shooter = NBTUtil.loadUUID(compound.getCompound("owner"));

    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.getEntityData().define(EntityPokecubeBase.RELEASING, false);
        this.getEntityData().define(EntityPokecubeBase.ENTITYID, -1);
        this.getEntityData().define(EntityPokecubeBase.ITEM, ItemStack.EMPTY);
        this.getEntityData().define(EntityPokecubeBase.TIME, 40);
    }

    /** Sets the ItemStack for this entity */
    public void setItem(final ItemStack stack)
    {
        if (this.isAddedToWorld()) PokemobTracker.removePokecube(this);
        this.getEntityData().set(EntityPokecubeBase.ITEM, stack);
        this.checkCube = true;
        if (this.isAddedToWorld()) PokemobTracker.addPokecube(this);
    }

    @Override
    public void setItemSlot(final EquipmentSlotType slotIn, final ItemStack stack)
    {
    }

    @Override
    public void setDeltaMovement(final Vector3d velocity)
    {
        super.setDeltaMovement(velocity);
        this.validateDirection(velocity);
    }

    public void setReleased(final Entity entity)
    {
        this.getEntityData().set(EntityPokecubeBase.ENTITYID, entity.getId());
        this.setDeltaMovement(0, 0, 0);
        this.setTime(20);
        this.setReleasing(true);
        this.canBePickedUp = false;
        this.seeking = false;
        EntityUpdate.sendEntityUpdate(this);
    }

    public void setReleasing(final boolean tag)
    {
        this.getEntityData().set(EntityPokecubeBase.RELEASING, tag);
    }

    public void setTime(final int time)
    {
        this.getEntityData().set(EntityPokecubeBase.TIME, time);
    }

    public void setNoCollisionRelease()
    {
        this.tilt = -2;
    }

    public boolean getNoCollisionRelease()
    {
        return this.tilt == -2 || this.tilt >= 0;
    }

    public void setNotCapturing()
    {
        this.tilt = -1;
    }

    public int getTilt()
    {
        return this.tilt;
    }

    public void setCapturing(final LivingEntity mob)
    {
        mob.getPersistentData().putBoolean(TagNames.CAPTURING, true);
        this.setDeltaMovement(0, 0, 0);
        this.capturePos.set(mob);
        this.seeking = false;
        this.isCapturing = true;
        this.canBePickedUp = false;

        final IPokemob pokemob = CapabilityPokemob.getPokemobFor(mob);
        if (pokemob != null && pokemob.getBossInfo() != null)
        {
            pokemob.getBossInfo().removeAllPlayers();
            pokemob.getBossInfo().setVisible(false);
        }
        EntityUpdate.sendEntityUpdate(this);
    }

    public void setTilt(final int n)
    {
        this.tilt = n;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots()
    {
        return this.stuff;
    }

    /**
     * Gets the amount of gravity to apply to the thrown entity with each
     * tick.
     */
    protected float getGravityVelocity()
    {
        return 0.03F;
    }

    /**
     * Returns the ItemStack corresponding to the Entity (Note: if no item
     * exists, will log an error but still return an ItemStack containing
     * Block.stone)
     */
    public ItemStack getItem()
    {
        final ItemStack itemstack = this.getEntityData().get(EntityPokecubeBase.ITEM);
        return itemstack.isEmpty() ? new ItemStack(Blocks.STONE) : itemstack;
    }

    @Override
    public ItemStack getItemBySlot(final EquipmentSlotType slotIn)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public HandSide getMainArm()
    {
        return HandSide.LEFT;
    }

    public Entity getReleased()
    {
        final int id = this.getEntityData().get(EntityPokecubeBase.ENTITYID);
        final Entity ret = this.getCommandSenderWorld().getEntity(id);
        return ret;
    }

    public int getTime()
    {
        return this.getEntityData().get(EntityPokecubeBase.TIME);
    }

    public boolean isReleasing()
    {
        return this.getEntityData().get(EntityPokecubeBase.RELEASING);
    }

    public static RayTraceResult rayTrace(final Entity projectile, final boolean checkEntityCollision,
            final boolean includeShooter, @Nullable final Entity shooter, final RayTraceContext.BlockMode blockModeIn)
    {
        return EntityPokecubeBase.rayTrace(projectile, checkEntityCollision, includeShooter, shooter, blockModeIn, true,
                (p_221270_2_) ->
                {
                    return !p_221270_2_.isSpectator() && p_221270_2_.isPickable() && (includeShooter || !p_221270_2_.is(
                            shooter)) && !p_221270_2_.noPhysics;
                }, projectile.getBoundingBox().expandTowards(projectile.getDeltaMovement()).inflate(1.0D));
    }

    public static RayTraceResult rayTrace(final Entity projectile, final AxisAlignedBB boundingBox,
            final Predicate<Entity> filter, final RayTraceContext.BlockMode blockModeIn,
            final boolean checkEntityCollision)
    {
        return EntityPokecubeBase.rayTrace(projectile, checkEntityCollision, false, (Entity) null, blockModeIn, false,
                filter, boundingBox);
    }

    /**
     * Gets the EntityRayTraceResult representing the entity hit
     */
    @Nullable
    public static EntityRayTraceResult rayTraceEntities(final World worldIn, final Entity projectile,
            final Vector3d startVec, final Vector3d endVec, final AxisAlignedBB boundingBox,
            final Predicate<Entity> filter)
    {
        return EntityPokecubeBase.rayTraceEntities(worldIn, projectile, startVec, endVec, boundingBox, filter,
                Double.MAX_VALUE);
    }

    private static RayTraceResult rayTrace(final Entity projectile, final boolean checkEntityCollision,
            final boolean includeShooter, @Nullable final Entity shooter, final RayTraceContext.BlockMode blockModeIn,
            final boolean p_221268_5_, final Predicate<Entity> filter, final AxisAlignedBB boundingBox)
    {
        final Vector3d vec3d = projectile.getDeltaMovement();
        final World world = projectile.level;
        final Vector3d vec3d1 = projectile.position();
        if (p_221268_5_ && !world.noCollision(projectile, projectile.getBoundingBox(), e -> (!includeShooter
                && shooter != null ? EntityPokecubeBase.getEntityAndMount(shooter) : ImmutableSet.of()).contains(e)))
            return new BlockRayTraceResult(vec3d1, Direction.getNearest(vec3d.x, vec3d.y, vec3d.z), new BlockPos(
                    projectile.position()), false);
        else
        {
            Vector3d vec3d2 = vec3d1.add(vec3d);
            RayTraceResult raytraceresult = world.clip(new RayTraceContext(vec3d1, vec3d2, blockModeIn,
                    RayTraceContext.FluidMode.NONE, projectile));
            if (checkEntityCollision)
            {
                if (raytraceresult.getType() != RayTraceResult.Type.MISS) vec3d2 = raytraceresult.getLocation();

                final RayTraceResult raytraceresult1 = EntityPokecubeBase.rayTraceEntities(world, projectile, vec3d1,
                        vec3d2, boundingBox, filter);
                if (raytraceresult1 != null) raytraceresult = raytraceresult1;
            }

            return raytraceresult;
        }
    }

    /**
     * Gets the EntityRayTraceResult representing the entity hit
     */
    @Nullable
    public static EntityRayTraceResult rayTraceEntities(final World worldIn, final Entity projectile,
            final Vector3d startVec, final Vector3d endVec, final AxisAlignedBB boundingBox,
            final Predicate<Entity> filter, final double distance)
    {
        double d0 = distance;
        Entity entity = null;

        for (final Entity entity1 : worldIn.getEntities(projectile, boundingBox, filter))
        {
            final AxisAlignedBB axisalignedbb = entity1.getBoundingBox().inflate(0.3F);
            final Optional<Vector3d> optional = axisalignedbb.clip(startVec, endVec);
            if (optional.isPresent())
            {
                final double d1 = startVec.distanceToSqr(optional.get());
                if (d1 < d0)
                {
                    entity = entity1;
                    d0 = d1;
                }
            }
        }

        return entity == null ? null : new EntityRayTraceResult(entity);
    }

    private static Set<Entity> getEntityAndMount(final Entity rider)
    {
        final Entity entity = rider.getVehicle();
        return entity != null ? ImmutableSet.of(rider, entity) : ImmutableSet.of(rider);
    }
}