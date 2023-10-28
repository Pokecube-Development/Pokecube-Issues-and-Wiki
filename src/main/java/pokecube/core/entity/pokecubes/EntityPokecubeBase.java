package pokecube.core.entity.pokecubes;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.moves.Battle;
import pokecube.api.utils.TagNames;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.logic.LogicMiscUpdate;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.items.pokecubes.helper.CaptureManager;
import pokecube.core.items.pokecubes.helper.SendOutManager;
import pokecube.core.utils.AITools;
import pokecube.core.utils.EntityTools;
import pokecube.core.utils.PokemobTracker;
import thut.api.Tracker;
import thut.api.entity.ICopyMob;
import thut.api.maths.Vector3;
import thut.core.common.network.EntityUpdate;
import thut.lib.RegHelper;

public abstract class EntityPokecubeBase extends LivingEntity
{
    public static final String CUBETIMETAG = "lastCubeTime";

    static final EntityDataAccessor<Integer> ENTITYID;
    static final EntityDataAccessor<ItemStack> ITEM;
    static final EntityDataAccessor<Boolean> RELEASING;
    static final EntityDataAccessor<Boolean> CAPTURING;
    static final EntityDataAccessor<Boolean> SEEKING;
    static final EntityDataAccessor<Integer> TIME;

    public static boolean CUBES_SEEK = true;

    public static Object2FloatOpenHashMap<ResourceLocation> CUBE_SIZES = new Object2FloatOpenHashMap<>();

    static
    {
        ENTITYID = SynchedEntityData.<Integer>defineId(EntityPokecubeBase.class, EntityDataSerializers.INT);
        ITEM = SynchedEntityData.<ItemStack>defineId(EntityPokecubeBase.class, EntityDataSerializers.ITEM_STACK);
        RELEASING = SynchedEntityData.<Boolean>defineId(EntityPokecubeBase.class, EntityDataSerializers.BOOLEAN);
        TIME = SynchedEntityData.<Integer>defineId(EntityPokecubeBase.class, EntityDataSerializers.INT);
        CAPTURING = SynchedEntityData.<Boolean>defineId(EntityPokecubeBase.class, EntityDataSerializers.BOOLEAN);
        SEEKING = SynchedEntityData.<Boolean>defineId(EntityPokecubeBase.class, EntityDataSerializers.BOOLEAN);
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
        else pokemob.getEntity().getPersistentData().putLong(EntityPokecubeBase.CUBETIMETAG,
                Tracker.instance().getTick() + PokecubeCore.getConfig().captureDelayTicks);
    }

    public boolean canBePickedUp = true;
    /**
     * This gets decremented each tick, and will auto release if it hits 0, ie
     * will not auto release if below 0 to start with.
     */
    public int autoRelease = -1;
    public boolean isLoot = false;

    private boolean checkCube = false;

    public ResourceLocation lootTable = null;

    protected int inData;
    protected boolean inGround;
    public UUID shooter;
    public LivingEntity shootingEntity;

    public double speed = 2;
    public LivingEntity targetEntity;
    public LivingEntity _capturingEntity;
    public Vector3 targetLocation = new Vector3();

    protected Block tile;
    protected BlockPos tilePos;
    private int tilt = -1;
    public Vector3 v0 = new Vector3();
    protected Vector3 v1 = new Vector3();

    public Vector3 capturePos = new Vector3();

    private Entity ignoreEntity;
    private int ignoreTime;

    NonNullList<ItemStack> stuff = NonNullList.create();

    public IPokemob containedMob = null;

    public EntityPokecubeBase(final EntityType<? extends EntityPokecubeBase> type, final Level worldIn)
    {
        super(type, worldIn);
        this.noPhysics = false;
    }

    /** Called when the entity is attacked. */
    @Override
    public boolean hurt(final DamageSource source, final float damage)
    {
        if (this.isLoot || this.isReleasing() || !this.canBePickedUp) return false;
        if (source.getDirectEntity() instanceof ServerPlayer player
                && (this.tilt <= 0 || player.getAbilities().instabuild))
        {
            this.interact(player, InteractionHand.MAIN_HAND);
            return false;
        }
        if (source == DamageSource.OUT_OF_WORLD)
        {
            if (PokecubeManager.isFilled(this.getItem()))
            {
                final IPokemob mob = PokemobCaps.getPokemobFor(SendOutManager.sendOut(this, true));
                if (mob != null) mob.onRecall();
            }
            this.discard();
        }
        return false;
    }

    @Override
    protected void outOfWorld()
    {
        final IPokemob mob = PokemobCaps.getPokemobFor(SendOutManager.sendOut(this, true, false));
        if (mob != null && mob.getOwnerId() != null) mob.onRecall();
    }

    /** Called when this EntityThrowable hits a block or entity. */
    protected void onImpact(final HitResult result)
    {
        if (!this.isAlive()) return;

        final boolean serverSide = this.getLevel() instanceof ServerLevel;
        final boolean capturing = this.getTilt() >= 0;
        final boolean releasing = this.isReleasing();

        switch (result.getType())
        {
        case BLOCK:
            this.setSeeking(null);
            this.targetLocation.clear();

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
            final EntityHitResult hit = (EntityHitResult) result;
            final Entity hitEntity = EntityTools.getCoreEntity(hit.getEntity());
            final IPokemob hitMob = PokemobCaps.getPokemobFor(hitEntity);

            final boolean invalidStick = hitEntity instanceof Player || !capturing || hitMob == null
                    || hitMob.getOwnerId() != null;

            // Set us to the location, but not stick to players.
            if (!invalidStick) this.setPos(result.getLocation().x, result.getLocation().y, result.getLocation().z);

            // Capturing or on client, break early.
            if (!serverSide || capturing || releasing) break;
            // Send out or try to capture.
            if (PokecubeManager.isFilled(this.getItem()))
            {
                final LivingEntity sent = SendOutManager.sendOut(this, true);
                if (sent instanceof Mob mob && hit.getEntity() instanceof LivingEntity living)
                    Battle.createOrAddToBattle(mob, living);
            }
            else CaptureManager.captureAttempt(this, hitEntity);
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
        return !this.isReleasing() && !isLoot;
    }

    @Override
    public boolean attackable()
    {
        return false;
    }

    public LivingEntity getCapturing()
    {
        if (!this.getEntityData().get(CAPTURING)) return null;
        ItemStack stack = getItem();
        if (_capturingEntity == null && PokecubeManager.isFilled(stack))
        {
            _capturingEntity = PokecubeManager.itemToMob(stack, level);
            if (_capturingEntity != null)
            {
                _capturingEntity.tickCount = 0;
                Vector3 loc = Vector3.readFromNBT(stack.getTag(), "_cap_pos_");
                this.capturePos.set(loc);
                ICopyMob.copyEntityTransforms(_capturingEntity, this);
            }
        }
        return _capturingEntity;
    }

    public void checkCollision()
    {
        final AABB axisalignedbb = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(.2D);
        if (this.shootingEntity != null) this.ignoreEntity = this.shootingEntity;

        final Predicate<Entity> valid = (mob) -> {
            final Entity e = EntityTools.getCoreEntity(mob);
            if (this.ignoreEntity != null
                    && (e.isPassengerOfSameVehicle(this.ignoreEntity) || e.hasPassenger(this.ignoreEntity)))
                return false;
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
            final EntityHitResult hit = new EntityHitResult(entity);
            if (hit.getType() == Type.ENTITY) this.onImpact(hit);
        }

        final HitResult raytraceresult = EntityPokecubeBase.rayTrace(this, axisalignedbb, valid,
                ClipContext.Block.COLLIDER, true);
        if (this.ignoreEntity != null && this.ignoreTime-- <= 0) this.ignoreEntity = null;

        trace:
        if (raytraceresult.getType() != HitResult.Type.MISS)
        {
            if (raytraceresult.getType() == HitResult.Type.BLOCK && this.level
                    .getBlockState(((BlockHitResult) raytraceresult).getBlockPos()).getBlock() == Blocks.NETHER_PORTAL)
                this.handleInsidePortal(((BlockHitResult) raytraceresult).getBlockPos());
            if (raytraceresult instanceof BlockHitResult result)
            {
                final BlockState hit = this.getLevel().getBlockState(result.getBlockPos());
                final VoxelShape shape = hit.getCollisionShape(this.getLevel(), result.getBlockPos());
                if (!shape.isEmpty() && !shape.bounds().move(result.getBlockPos()).intersects(axisalignedbb))
                    break trace;
            }
            // TODO maybe do an event here?
            this.onImpact(raytraceresult);
            return;
        }
        if (this.isReleasing()) return;
        if (this.shootingEntity != null && this.tilt < 0 && this.getDeltaMovement().lengthSqr() == 0
                && this.isEffectiveAi() && PokecubeManager.isFilled(this.getItem()))
            SendOutManager.sendOut(this, true);
        else if (!this.getNoCollisionRelease() && this.isInWater() && PokecubeManager.isFilled(this.getItem()))
            SendOutManager.sendOut(this, true);
    }

    public void preValidateVelocity()
    {
        // Calculate velocity if seeking
        if (this.tilt > 0 || this.targetEntity != null && !this.targetEntity.isAddedToWorld())
        {
            this.targetEntity = null;
            if (!this.targetLocation.equals(Vector3.secondAxisNeg)) this.targetLocation.clear();
        }

        final Vector3 target = new Vector3();
        if (this.targetEntity != null) target.set(this.targetEntity, true);
        else target.set(this.targetLocation);
        if (!target.isEmpty() && this.isSeeking())
        {
            final Vector3 here = new Vector3().set(this);
            final Vector3 dir = new Vector3().set(target);
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

    private void validateDirection(final Vec3 vec3d)
    {
        if (vec3d.lengthSqr() == 0) return;

        double dx = vec3d.x;
        double dy = vec3d.y;
        double dz = vec3d.z;
        double d3 = Math.sqrt(dx * dx + dz * dz);
        Optional<Float> getXRotD = !(Math.abs(dy) > 1.0E-5F) && !(Math.abs(d3) > 1.0E-5F)
                ? Optional.empty()
                : Optional.of((float) (-(Mth.atan2(dy, d3) * (180F / (float) Math.PI))));

        Optional<Float> getYRotD = !(Math.abs(dz) > 1.0E-5F) && !(Math.abs(dx) > 1.0E-5F)
                ? Optional.empty()
                : Optional.of((float) (Mth.atan2(dz, dx) * (180F / (float) Math.PI)) - 90.0F);

        if (getYRotD.isPresent()) this.yRot = this.yHeadRot = this.yBodyRot = getYRotD.get();
        if (getXRotD.isPresent()) this.xRot = getXRotD.get();
    }

    private void postValidateVelocity()
    {
        final Vec3 vec3d = this.getDeltaMovement();
        this.validateDirection(vec3d);
        float f1;
        if (this.isInWater())
        {
            for (int i = 0; i < 4; ++i) this.level.addParticle(ParticleTypes.BUBBLE, this.getX() - vec3d.x * 0.25D,
                    this.getY() - vec3d.y * 0.25D, this.getZ() - vec3d.z * 0.25D, vec3d.x, vec3d.y, vec3d.z);
            f1 = 0.8F;
        }
        else f1 = 0.99F;

        this.setDeltaMovement(vec3d.scale(f1));
        final Vec3 motion = this.getDeltaMovement();
        if (motion.y == 0) this.setDeltaMovement(motion.x * 0.8, motion.y, motion.z * 0.8);
        if (!this.isNoGravity())
        {
            final Vec3 vec3d1 = this.getDeltaMovement();
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
        this.animStepO = this.animStep;
        this.yBodyRotO = this.yBodyRot;
        this.yHeadRotO = this.yHeadRot;
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();

        this.autoRelease--;
        if (this.autoRelease == 0) SendOutManager.sendOut(this, true);
        final boolean capturing = this.getTilt() >= 0;
        final boolean releasing = this.isReleasing();
        if (capturing || releasing) this.setSeeking(null);

        if (this.getY() < -64.0D) this.outOfWorld();

        ItemStack item = this.getItem();
        float size = CUBE_SIZES.getOrDefault(RegHelper.getKey(item), 0.25f);
        if (size != this.dimensions.width) this.dimensions = EntityDimensions.fixed(size, size);
        if (this.checkCube)
        {
            this.checkCube = false;
            PokemobTracker.removePokecube(this);
            this.containedMob = PokecubeManager.itemToPokemob(item, this.getLevel());
            if (this.containedMob != null && this.shooter == null)
            {
                this.shootingEntity = this.containedMob.getOwner();
                if (this.shootingEntity != null) this.shooter = this.shootingEntity.getUUID();
            }
            PokemobTracker.addPokecube(this);
        }
        if (_capturingEntity != null) _capturingEntity.tickCount++;
        this.preValidateVelocity();
        this.checkCollision();
        this.postValidateVelocity();
        if (this.isReleasing() || _capturingEntity != null)
        {
            var test = _capturingEntity != null ? this._capturingEntity : this.getReleased();
            if (test != null) this.capturePos.set(test);
            if (this.isReleasing() && test == null && this.getTime() < LogicMiscUpdate.EXITCUBEDURATION - 2)
                this.discard();
            double dh = 1;
            if (test != null) dh = test.getBbWidth();

            double dx = this.capturePos.x - this.getX();
            double dy = this.capturePos.y - this.getEyeY();
            double dz = this.capturePos.z - this.getZ();
            double d3 = Math.sqrt(dx * dx + dz * dz);
            Optional<Float> getXRotD = !(Math.abs(dy) > 1.0E-5F) && !(Math.abs(d3) > 1.0E-5F)
                    ? Optional.empty()
                    : Optional.of((float) (-(Mth.atan2(dy, d3) * (180F / (float) Math.PI))));

            Optional<Float> getYRotD = !(Math.abs(dz) > 1.0E-5F) && !(Math.abs(dx) > 1.0E-5F)
                    ? Optional.empty()
                    : Optional.of((float) (Mth.atan2(dz, dx) * (180F / (float) Math.PI)) - 90.0F);

            if (getYRotD.isPresent()) this.yRot = this.yHeadRot = this.yBodyRot = getYRotD.get();
            if (getXRotD.isPresent()) this.xRot = getXRotD.get();

            Vector3 dr = new Vector3(dx, dy, dz);
            if (dr.mag() < dh) dr.normalize().scalarMultBy(0.005).reverse().addVelocities(this);
        }
    }

    @Override
    public void addAdditionalSaveData(final CompoundTag compound)
    {
        super.addAdditionalSaveData(compound);
        compound.putInt("tilt", this.tilt);
        compound.putInt("time", this.getTime());
        compound.putBoolean("releasing", this.isReleasing());
        if (this.shooter != null) compound.putString("shooter", this.shooter.toString());
        if (this.getItem() != null) compound.put("Item", this.getItem().save(new CompoundTag()));
        if (this.getEntityData().get(CAPTURING)) this.capturePos.writeToNBT(compound, "capt_");
        compound.putByte("inGround", (byte) (this.inGround ? 1 : 0));
        compound.putInt("autorelease", this.autoRelease);
        if (this.shooter != null) compound.put("owner", NbtUtils.createUUID(this.shooter));

    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    public void readAdditionalSaveData(final CompoundTag compound)
    {
        super.readAdditionalSaveData(compound);
        this.tilt = compound.getInt("tilt");
        this.setTime(compound.getInt("time"));
        this.setReleasing(compound.getBoolean("releasing"));
        final CompoundTag CompoundNBT1 = compound.getCompound("Item");
        this.setItem(ItemStack.of(CompoundNBT1));
        if (compound.contains("shooter")) this.shooter = UUID.fromString(compound.getString("shooter"));
        if (compound.contains("capt_x")) this.capturePos = Vector3.readFromNBT(compound, "capt_");
        this.autoRelease = compound.getInt("autorelease");
        this.inGround = compound.getByte("inGround") == 1;
        if (compound.contains("owner", 10)) this.shooter = NbtUtils.loadUUID(compound.getCompound("owner"));

    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.getEntityData().define(EntityPokecubeBase.RELEASING, false);
        this.getEntityData().define(EntityPokecubeBase.ENTITYID, -1);
        this.getEntityData().define(EntityPokecubeBase.ITEM, ItemStack.EMPTY);
        this.getEntityData().define(EntityPokecubeBase.TIME, 40);
        this.getEntityData().define(EntityPokecubeBase.CAPTURING, false);
        this.getEntityData().define(EntityPokecubeBase.SEEKING, false);
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
    public void setItemSlot(final EquipmentSlot slotIn, final ItemStack stack)
    {}

    @Override
    public void setDeltaMovement(final Vec3 velocity)
    {
        super.setDeltaMovement(velocity);
    }

    public void setReleased(final Entity entity)
    {
        this.getEntityData().set(EntityPokecubeBase.ENTITYID, entity.getId());
        this.setDeltaMovement(0, 0, 0);
        this.setTime(LogicMiscUpdate.EXITCUBEDURATION);
        this.setReleasing(true);
        this.canBePickedUp = false;
        this.capturePos.set(entity);
        this.setSeeking(null);
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
        this.setSeeking(null);
        this.canBePickedUp = false;

        ItemStack stack = this.getItem();
        this.capturePos.writeToNBT(stack.getTag(), "_cap_pos_");
        this.setItem(stack);

        final IPokemob pokemob = PokemobCaps.getPokemobFor(mob);
        if (pokemob != null && pokemob.getBossInfo() != null)
        {
            pokemob.getBossInfo().removeAllPlayers();
            pokemob.getBossInfo().setVisible(false);
        }
        this.getEntityData().set(CAPTURING, true);
    }

    public boolean isCapturing()
    {
        return this.getEntityData().get(CAPTURING);
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
     * Gets the amount of gravity to apply to the thrown entity with each tick.
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
    public ItemStack getItemBySlot(final EquipmentSlot slotIn)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public HumanoidArm getMainArm()
    {
        return HumanoidArm.LEFT;
    }

    public Entity getReleased()
    {
        final int id = this.getEntityData().get(EntityPokecubeBase.ENTITYID);
        final Entity ret = this.getLevel().getEntity(id);
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

    public boolean isSeeking()
    {
        return this.getEntityData().get(EntityPokecubeBase.SEEKING);
    }

    public void setSeeking(LivingEntity target)
    {
        if (target != null) this.getEntityData().set(EntityPokecubeBase.SEEKING, true);
        else this.getEntityData().set(EntityPokecubeBase.SEEKING, false);
        this.targetEntity = target;
    }

    public static HitResult rayTrace(final Entity projectile, final boolean checkEntityCollision,
            final boolean includeShooter, @Nullable final Entity shooter, final ClipContext.Block blockModeIn)
    {
        final Predicate<Entity> valid = (target) -> {
            return !target.isSpectator() && target.isPickable() && (includeShooter || !target.is(shooter))
                    && !target.noPhysics;
        };
        return ProjectileUtil.getHitResult(projectile, valid);
    }

    public static HitResult rayTrace(final Entity projectile, final AABB boundingBox, final Predicate<Entity> filter,
            final ClipContext.Block blockModeIn, final boolean checkEntityCollision)
    {
        return ProjectileUtil.getHitResult(projectile, filter);
    }

    /**
     * Gets the EntityRayTraceResult representing the entity hit
     */
    @Nullable
    public static EntityHitResult rayTraceEntities(final Level worldIn, final Entity projectile, final Vec3 startVec,
            final Vec3 endVec, final AABB boundingBox, final Predicate<Entity> filter)
    {
        return EntityPokecubeBase.rayTraceEntities(worldIn, projectile, startVec, endVec, boundingBox, filter,
                Double.MAX_VALUE);
    }

    /**
     * Gets the EntityRayTraceResult representing the entity hit
     */
    @Nullable
    public static EntityHitResult rayTraceEntities(final Level worldIn, final Entity projectile, final Vec3 startVec,
            final Vec3 endVec, final AABB boundingBox, final Predicate<Entity> filter, final double distance)
    {
        double d0 = distance;
        Entity entity = null;

        for (final Entity entity1 : worldIn.getEntities(projectile, boundingBox, filter))
        {
            final AABB axisalignedbb = entity1.getBoundingBox().inflate(0.3F);
            final Optional<Vec3> optional = axisalignedbb.clip(startVec, endVec);
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

        return entity == null ? null : new EntityHitResult(entity);
    }

    public abstract EntityPokecubeBase copy();
}