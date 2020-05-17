package pokecube.core.items.pokecubes;

import java.util.UUID;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import pokecube.core.items.pokecubes.helper.CaptureManager;
import pokecube.core.items.pokecubes.helper.SendOutManager;
import pokecube.core.utils.TagNames;
import thut.api.maths.Vector3;
import thut.core.common.network.EntityUpdate;

public abstract class EntityPokecubeBase extends LivingEntity implements IProjectile
{
    public static final String CUBETIMETAG = "lastCubeTime";

    public static SoundEvent POKECUBESOUND;

    static final DataParameter<Integer> ENTITYID = EntityDataManager.<Integer> createKey(EntityPokecubeBase.class,
            DataSerializers.VARINT);

    private static final DataParameter<ItemStack> ITEM      = EntityDataManager.<ItemStack> createKey(
            EntityPokecubeBase.class, DataSerializers.ITEMSTACK);
    static final DataParameter<Boolean>           RELEASING = EntityDataManager.<Boolean> createKey(
            EntityPokecubeBase.class, DataSerializers.BOOLEAN);
    static final DataParameter<Integer>           TIME      = EntityDataManager.<Integer> createKey(
            EntityPokecubeBase.class, DataSerializers.VARINT);
    public static boolean                         SEEKING   = true;

    public static boolean canCaptureBasedOnConfigs(final IPokemob pokemob)
    {
        if (PokecubeCore.getConfig().captureDelayTillAttack) return !pokemob.getCombatState(CombatStates.NOITEMUSE);
        final long lastAttempt = pokemob.getEntity().getPersistentData().getLong(EntityPokecubeBase.CUBETIMETAG);
        final boolean capture = lastAttempt <= pokemob.getEntity().getEntityWorld().getGameTime();
        if (capture) pokemob.getEntity().getPersistentData().remove(EntityPokecubeBase.CUBETIMETAG);
        return capture;
    }

    public static void setNoCaptureBasedOnConfigs(final IPokemob pokemob)
    {

        if (PokecubeCore.getConfig().captureDelayTillAttack) pokemob.setCombatState(CombatStates.NOITEMUSE, true);
        else pokemob.getEntity().getPersistentData().putLong(EntityPokecubeBase.CUBETIMETAG, pokemob.getEntity()
                .getEntityWorld().getGameTime() + PokecubeCore.getConfig().captureDelayTicks);
    }

    public boolean canBePickedUp = true;
    /**
     * This gets decremented each tick, and will auto release if it hits 0, ie
     * will not auto release if below 0 to start with.
     */
    public int     autoRelease   = -1;
    public boolean isLoot        = false;

    public boolean isCapturing = false;

    public ResourceLocation lootTable = null;
    protected int           inData;
    protected boolean       inGround;
    public UUID             shooter;
    public LivingEntity     shootingEntity;

    public double       speed          = 2;
    public LivingEntity targetEntity;
    public Vector3      targetLocation = Vector3.getNewVector();

    protected Block    tile;
    protected BlockPos tilePos;
    private int        tilt = -1;
    public Vector3     v0   = Vector3.getNewVector();
    protected Vector3  v1   = Vector3.getNewVector();

    private int    xTile   = -1;
    private int    yTile   = -1;
    private int    zTile   = -1;
    private Entity ignoreEntity;
    private int    ignoreTime;
    public boolean seeking = EntityPokecubeBase.SEEKING;

    NonNullList<ItemStack> stuff = NonNullList.create();

    public EntityPokecubeBase(final EntityType<? extends EntityPokecubeBase> type, final World worldIn)
    {
        super(type, worldIn);
        this.noClip = false;
    }

    /** Called when the entity is attacked. */
    @Override
    public boolean attackEntityFrom(final DamageSource source, final float damage)
    {
        if (this.isLoot || this.isReleasing() || !this.canBePickedUp) return false;
        if (source.getImmediateSource() instanceof ServerPlayerEntity && (this.tilt <= 0 || ((PlayerEntity) source
                .getImmediateSource()).abilities.isCreativeMode))
        {
            final ServerPlayerEntity player = (ServerPlayerEntity) source.getImmediateSource();
            this.processInitialInteract(player, Hand.MAIN_HAND);
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
        final IPokemob mob = CapabilityPokemob.getPokemobFor(SendOutManager.sendOut(this, true));
        if (mob != null && mob.getOwnerId() != null) mob.onRecall();
    }

    /** Called when this EntityThrowable hits a block or entity. */
    protected void onImpact(final RayTraceResult result)
    {
        if (!this.isAlive()) return;

        final boolean serverSide = this.getEntityWorld() instanceof ServerWorld;
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
            final IPokemob hitMob = CapabilityPokemob.getPokemobFor(hit.getEntity());

            final boolean invalidStick = hit.getEntity() instanceof PlayerEntity || !capturing || hitMob == null
                    || hitMob.getOwnerId() != null;

            // Set us to the location, but not stick to players.
            if (!invalidStick) this.setPosition(result.getHitVec().x, result.getHitVec().y, result.getHitVec().z);

            // Capturing or on client, break early.
            if (!serverSide || capturing || releasing) break;
            // Send out or try to capture.
            if (PokecubeManager.isFilled(this.getItem()))
            {
                final LivingEntity sent = SendOutManager.sendOut(this, true);
                if (sent instanceof MobEntity && hit.getEntity() instanceof LivingEntity) ((MobEntity) sent)
                        .setAttackTarget((LivingEntity) hit.getEntity());
            }
            else CaptureManager.captureAttempt(this, this.rand, hit.getEntity());
            break;
        case MISS:
            break;
        default:
            break;
        }
    }

    public void checkCollision()
    {
        final AxisAlignedBB axisalignedbb = this.getBoundingBox().expand(this.getMotion()).grow(.2D);
        if (this.shootingEntity != null) this.ignoreEntity = this.shootingEntity;

        final Predicate<Entity> valid = (mob) ->
        {
            return !mob.isSpectator() && mob.canBeCollidedWith() && !(mob instanceof EntityPokecubeBase)
                    && mob instanceof LivingEntity && mob != this.ignoreEntity && mob != this && this.getDistanceSq(
                            mob) < 4;
        };

        if (!this.isReleasing()) for (final Entity entity : this.world.getEntitiesInAABBexcluding(this, axisalignedbb,
                valid))
        {
            if (entity == this.ignoreEntity)
            {
                ++this.ignoreTime;
                break;
            }

            if (this.shootingEntity != null && this.ticksExisted < 2 && this.ignoreEntity == null)
            {
                this.ignoreEntity = entity;
                this.ignoreTime = 10;
                break;
            }
            final EntityRayTraceResult hit = new EntityRayTraceResult(entity);
            if (hit.getType() == Type.ENTITY) this.onImpact(hit);
        }

        final RayTraceResult raytraceresult = ProjectileHelper.rayTrace(this, axisalignedbb, valid,
                RayTraceContext.BlockMode.COLLIDER, true);
        if (this.ignoreEntity != null && this.ignoreTime-- <= 0) this.ignoreEntity = null;

        trace:
        if (raytraceresult.getType() != RayTraceResult.Type.MISS)
        {
            if (raytraceresult.getType() == RayTraceResult.Type.BLOCK && this.world.getBlockState(
                    ((BlockRayTraceResult) raytraceresult).getPos()).getBlock() == Blocks.NETHER_PORTAL) this.setPortal(
                            ((BlockRayTraceResult) raytraceresult).getPos());
            if (raytraceresult instanceof BlockRayTraceResult)
            {
                final BlockRayTraceResult result = (BlockRayTraceResult) raytraceresult;
                final BlockState hit = this.getEntityWorld().getBlockState(result.getPos());
                final VoxelShape shape = hit.getCollisionShape(this.getEntityWorld(), result.getPos());
                if (!shape.isEmpty() && !shape.getBoundingBox().offset(result.getPos()).intersects(axisalignedbb))
                    break trace;
            }
            if (!net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) this.onImpact(
                    raytraceresult);
        }
        else if (this.shootingEntity != null && this.tilt < 0 && this.getMotion().lengthSquared() == 0 && this
                .isServerWorld() && PokecubeManager.isFilled(this.getItem())) SendOutManager.sendOut(this, true);
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
                dir.x += this.targetEntity.getMotion().x;
                dir.y += this.targetEntity.getMotion().y;
                dir.z += this.targetEntity.getMotion().z;
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

    private void validateDirection(final Vec3d vec3d)
    {
        final float f = MathHelper.sqrt(Entity.horizontalMag(vec3d));
        if (f > 0.5)
        {
            this.rotationYaw = (float) (-MathHelper.atan2(vec3d.x, vec3d.z) * (180F / (float) Math.PI));
            for (this.rotationPitch = (float) (MathHelper.atan2(vec3d.y, f) * (180F
                    / (float) Math.PI)); this.rotationPitch
                            - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
                ;
        }
        else this.rotationPitch = 0;
        while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
            this.prevRotationPitch += 360.0F;
        while (this.rotationYaw - this.prevRotationYaw < -180.0F)
            this.prevRotationYaw -= 360.0F;
        while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
            this.prevRotationYaw += 360.0F;
        this.renderYawOffset = this.rotationYaw;
        this.prevRenderYawOffset = this.prevRotationYaw;
        this.rotationYawHead = this.rotationYaw;
        this.prevRotationYawHead = this.prevRotationYaw;
    }

    private void postValidateVelocity()
    {
        final Vec3d vec3d = this.getMotion();
        this.validateDirection(vec3d);
        float f1;
        if (this.isInWater())
        {
            for (int i = 0; i < 4; ++i)
                this.world.addParticle(ParticleTypes.BUBBLE, this.posX - vec3d.x * 0.25D, this.posY - vec3d.y * 0.25D,
                        this.posZ - vec3d.z * 0.25D, vec3d.x, vec3d.y, vec3d.z);
            f1 = 0.8F;
        }
        else f1 = 0.99F;

        this.setMotion(vec3d.scale(f1));
        final Vec3d motion = this.getMotion();
        if (motion.y == 0) this.setMotion(motion.x * 0.8, motion.y, motion.z * 0.8);
        if (!this.hasNoGravity())
        {
            final Vec3d vec3d1 = this.getMotion();
            this.setMotion(vec3d1.x, vec3d1.y - this.getGravityVelocity(), vec3d1.z);
        }

        this.move(MoverType.SELF, this.getMotion());

    }

    /** Called to update the entity's position/logic. */
    @Override
    public void tick()
    {
        this.lastTickPosX = this.posX;
        this.lastTickPosY = this.posY;
        this.lastTickPosZ = this.posZ;

        this.autoRelease--;
        if (this.autoRelease == 0) SendOutManager.sendOut(this, true);
        final boolean capturing = this.getTilt() >= 0;
        final boolean releasing = this.isReleasing();
        if (capturing || releasing) this.seeking = false;

        if (this.posY < -64.0D) this.outOfWorld();

        this.preValidateVelocity();
        this.checkCollision();
        this.postValidateVelocity();
    }

    @Override
    public void writeAdditional(final CompoundNBT compound)
    {
        super.writeAdditional(compound);
        compound.putInt("tilt", this.tilt);
        compound.putInt("time", this.getTime());
        compound.putBoolean("releasing", this.isReleasing());
        if (this.shooter != null) compound.putString("shooter", this.shooter.toString());
        if (this.getItem() != null) compound.put("Item", this.getItem().write(new CompoundNBT()));
        compound.putInt("xTile", this.xTile);
        compound.putInt("yTile", this.yTile);
        compound.putInt("zTile", this.zTile);
        compound.putByte("inGround", (byte) (this.inGround ? 1 : 0));
        compound.putInt("autorelease", this.autoRelease);
        if (this.shooter != null) compound.put("owner", NBTUtil.writeUniqueId(this.shooter));

    }

    /**
     * (abstract) Protected helper method to read subclass entity data from
     * NBT.
     */
    @Override
    public void readAdditional(final CompoundNBT compound)
    {
        super.readAdditional(compound);
        this.tilt = compound.getInt("tilt");
        this.setTime(compound.getInt("time"));
        this.setReleasing(compound.getBoolean("releasing"));
        final CompoundNBT CompoundNBT1 = compound.getCompound("Item");
        this.setItem(ItemStack.read(CompoundNBT1));
        if (compound.contains("shooter")) this.shooter = UUID.fromString(compound.getString("shooter"));
        this.xTile = compound.getInt("xTile");
        this.yTile = compound.getInt("yTile");
        this.zTile = compound.getInt("zTile");
        this.autoRelease = compound.getInt("autorelease");
        this.inGround = compound.getByte("inGround") == 1;
        if (compound.contains("owner", 10)) this.shooter = NBTUtil.readUniqueId(compound.getCompound("owner"));

    }

    @Override
    protected void registerData()
    {
        super.registerData();
        this.getDataManager().register(EntityPokecubeBase.RELEASING, false);
        this.getDataManager().register(EntityPokecubeBase.ENTITYID, -1);
        this.getDataManager().register(EntityPokecubeBase.ITEM, ItemStack.EMPTY);
        this.getDataManager().register(EntityPokecubeBase.TIME, 40);
    }

    /** Sets the ItemStack for this entity */
    public void setItem(final ItemStack stack)
    {
        this.getDataManager().set(EntityPokecubeBase.ITEM, stack);
    }

    // For compatiblity
    public void setItemEntityStack(final ItemStack stack)
    {
        this.setItem(stack);
    }

    @Override
    public void setItemStackToSlot(final EquipmentSlotType slotIn, final ItemStack stack)
    {
    }

    @Override
    public void setMotion(final Vec3d velocity)
    {
        super.setMotion(velocity);
        this.validateDirection(velocity);
    }

    public void setReleased(final Entity entity)
    {
        this.getDataManager().set(EntityPokecubeBase.ENTITYID, entity.getEntityId());
        this.setReleasing(true);
    }

    public void setReleasing(final boolean tag)
    {
        this.getDataManager().set(EntityPokecubeBase.RELEASING, tag);
    }

    public void setTime(final int time)
    {
        this.getDataManager().set(EntityPokecubeBase.TIME, time);
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
        EntityUpdate.sendEntityUpdate(this);
        this.seeking = false;
        this.isCapturing = true;
        this.canBePickedUp = false;
    }

    public void setTilt(final int n)
    {
        this.tilt = n;
    }

    @Override
    public Iterable<ItemStack> getArmorInventoryList()
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
        final ItemStack itemstack = this.getDataManager().get(EntityPokecubeBase.ITEM);
        return itemstack.isEmpty() ? new ItemStack(Blocks.STONE) : itemstack;
    }

    // For compatiblity.
    public ItemStack getItemEntity()
    {
        return this.getItem();
    }

    @Override
    public ItemStack getItemStackFromSlot(final EquipmentSlotType slotIn)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public HandSide getPrimaryHand()
    {
        return HandSide.LEFT;
    }

    public Entity getReleased()
    {
        final int id = this.getDataManager().get(EntityPokecubeBase.ENTITYID);
        final Entity ret = this.getEntityWorld().getEntityByID(id);
        return ret;
    }

    public int getTime()
    {
        return this.getDataManager().get(EntityPokecubeBase.TIME);
    }

    public boolean isReleasing()
    {
        return this.getDataManager().get(EntityPokecubeBase.RELEASING);
    }
}