package pokecube.core.moves.animations;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeTileEntity;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.fml.network.NetworkHooks;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IMoveAnimation.MovePacketInfo;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.EntityTools;
import thut.api.maths.Vector3;

public class EntityMoveUse extends ThrowableEntity
{
    public static EntityType<EntityMoveUse> TYPE;
    static
    {
        EntityMoveUse.TYPE = EntityType.Builder.of(EntityMoveUse::new, EntityClassification.MISC).noSummon()
                .fireImmune().setTrackingRange(64).setShouldReceiveVelocityUpdates(true).setUpdateInterval(1).sized(
                        0.5f, 0.5f).setCustomClientFactory((spawnEntity, world) ->
                        {
                            return EntityMoveUse.TYPE.create(world);
                        }).build("move_use");
    }

    public static class Builder
    {
        public static Builder make(final Entity user, final Move_Base move, final Vector3 start)
        {
            return new Builder(user, move, start);
        }

        EntityMoveUse toMake;

        protected Builder(final Entity user, final Move_Base move, final Vector3 start)
        {
            this.toMake = new EntityMoveUse(EntityMoveUse.TYPE, user.getCommandSenderWorld());
            this.toMake.setMove(move).setUser(user).setStart(start).setEnd(start);
        }

        public Builder setStartTick(final int tick)
        {
            this.toMake.setStartTick(tick);
            return this;
        }

        public Builder setTarget(final Entity target)
        {
            if (target != null) this.toMake.setTarget(target);
            return this;
        }

        public Builder setEnd(final Vector3 end)
        {
            if (end != null) this.toMake.setEnd(end);
            return this;
        }

        public EntityMoveUse build()
        {
            this.toMake.init();
            return this.toMake;
        }
    }

    static final DataParameter<String>  MOVENAME  = EntityDataManager.<String> defineId(EntityMoveUse.class,
            DataSerializers.STRING);
    static final DataParameter<Float>   ENDX      = EntityDataManager.<Float> defineId(EntityMoveUse.class,
            DataSerializers.FLOAT);
    static final DataParameter<Float>   ENDY      = EntityDataManager.<Float> defineId(EntityMoveUse.class,
            DataSerializers.FLOAT);
    static final DataParameter<Float>   ENDZ      = EntityDataManager.<Float> defineId(EntityMoveUse.class,
            DataSerializers.FLOAT);
    static final DataParameter<Float>   STARTX    = EntityDataManager.<Float> defineId(EntityMoveUse.class,
            DataSerializers.FLOAT);
    static final DataParameter<Float>   STARTY    = EntityDataManager.<Float> defineId(EntityMoveUse.class,
            DataSerializers.FLOAT);
    static final DataParameter<Float>   STARTZ    = EntityDataManager.<Float> defineId(EntityMoveUse.class,
            DataSerializers.FLOAT);
    static final DataParameter<Integer> USER      = EntityDataManager.<Integer> defineId(EntityMoveUse.class,
            DataSerializers.INT);
    static final DataParameter<Integer> TARGET    = EntityDataManager.<Integer> defineId(EntityMoveUse.class,
            DataSerializers.INT);
    static final DataParameter<Integer> TICK      = EntityDataManager.<Integer> defineId(EntityMoveUse.class,
            DataSerializers.INT);
    static final DataParameter<Integer> STARTTICK = EntityDataManager.<Integer> defineId(EntityMoveUse.class,
            DataSerializers.INT);
    static final DataParameter<Integer> APPLYTICK = EntityDataManager.<Integer> defineId(EntityMoveUse.class,
            DataSerializers.INT);

    Vector3 end   = Vector3.getNewVector();
    Vector3 start = Vector3.getNewVector();

    Vector3 here = Vector3.getNewVector();
    Vector3 prev = Vector3.getNewVector();

    Vector3 dir = Vector3.getNewVector();

    Entity user   = null;
    Entity target = null;

    Move_Base move = null;

    boolean applied = false;
    boolean onSelf  = false;
    boolean contact = false;
    boolean init    = false;

    int startAge = 1;

    double dist = 0;

    int initTimer = 10;

    final Set<UUID> alreadyHit = Sets.newHashSet();

    Predicate<Entity> valid = e ->
    {
        if (EntityTools.getCoreLiving(e) == null) return false;
        final UUID targetID = EntityTools.getCoreEntity(e).getUUID();
        return !this.alreadyHit.contains(targetID);
    };

    public EntityMoveUse(final EntityType<EntityMoveUse> type, final World worldIn)
    {
        super(type, worldIn);
        this.noCulling = true;
    }

    protected void init()
    {
        if (this.init) return;
        if (this.initTimer-- < 0)
        {
            // If we timed out, it means our user died before the move could
            // finish (ie it died on the same tick it used the attack), in this
            // case, we just remove the attack.
            this.remove();
            return;
        }

        // This should initialise these values on the client side correctly.
        this.getStart();
        this.getEnd();
        this.getMove();
        this.getUser();
        this.getTarget();

        if (this.getUser() == null) return;

        this.init = true;
        this.startAge = this.getDuration();
        if (!this.start.equals(this.end)) this.dir.set(this.end).subtractFrom(this.start).norm();
        else this.onSelf = true;
        this.dist = this.start.distanceTo(this.end);
        this.refreshDimensions();

        this.here.set(this);

        // Put us and our user in here by default.
        this.alreadyHit.add(this.getUUID());
        this.alreadyHit.add(this.user.getUUID());
    }

    @Override
    public EntitySize getDimensions(final Pose poseIn)
    {
        EntitySize size = super.getDimensions(poseIn);
        this.getMove();
        if (this.move == null) return size;
        this.contact = (this.move.move.attackCategory & IMoveConstants.CATEGORY_CONTACT) > 0;
        final boolean aoe = this.move.aoe;
        if (this.contact)
        {
            final float s = (float) Math.max(0.75, PokecubeCore.getConfig().contactAttackDistance);
            final float width = this.user.getBbWidth();
            final float height = this.user.getBbHeight();
            size = EntitySize.fixed(width + s, height + s);
        }
        else if (aoe)
        {
            final float s = 8;
            final float width = this.user.getBbWidth();
            final float height = this.user.getBbHeight();
            size = EntitySize.fixed(width + s, height + s);
        }
        return size;
    }

    @Override
    public IPacket<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void doMoveUse(final Entity target)
    {
        final Move_Base attack = this.getMove();
        final Entity user = this.getUser();
        final LivingEntity living = EntityTools.getCoreLiving(target);
        if (!this.valid.test(target) || living == null || !target.isPickable()) return;
        if (user == null || !this.isAlive() || !user.isAlive()) return;
        // We only want to hit living entities, but non-living ones can be
        // detected as parts of other mobs, like ender dragons.
        final UUID targetID = living.getUUID();
        // If it is null here, it means that the target was not a living entity,
        // or a part of one.
        if (targetID == null) return;

        final Entity targ = this.getTarget();
        final UUID targId = targ == null ? null : targ.getUUID();

        this.alreadyHit.add(targetID);

        // Only hit multipart entities once
        // Only can hit our valid target!
        if (targId != null && !attack.move.canHitNonTarget() && !targId.equals(targetID)) return;
        if (!this.getCommandSenderWorld().isClientSide)
        {
            final IPokemob userMob = CapabilityPokemob.getPokemobFor(user);
            MovesUtils.doAttack(attack.name, userMob, target);
            this.applied = true;
        }
    }

    public int getDuration()
    {
        return this.getEntityData().get(EntityMoveUse.TICK);
    }

    public int getApplicationTick()
    {
        return this.getEntityData().get(EntityMoveUse.APPLYTICK);
    }

    public Vector3 getEnd()
    {
        this.end.x = this.getEntityData().get(EntityMoveUse.ENDX);
        this.end.y = this.getEntityData().get(EntityMoveUse.ENDY);
        this.end.z = this.getEntityData().get(EntityMoveUse.ENDZ);
        return this.end;
    }

    public Move_Base getMove()
    {
        if (this.move != null) return this.move;
        final String name = this.getEntityData().get(EntityMoveUse.MOVENAME);
        if (name.isEmpty()) return null;
        return this.move = MovesUtils.getMoveFromName(name);
    }

    public MovePacketInfo getMoveInfo()
    {
        final MovePacketInfo info = new MovePacketInfo(this.getMove(), this.getUser(), this.getTarget(), this
                .getStart(), this.getEnd());
        final IPokemob userMob = CapabilityPokemob.getPokemobFor(info.attacker);
        info.currentTick = info.move.getAnimation(userMob).getDuration() - this.getDuration();
        return info;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public AxisAlignedBB getBoundingBoxForCulling()
    {
        return IForgeTileEntity.INFINITE_EXTENT_AABB;
    }

    public Vector3 getStart()
    {
        this.start.x = this.getEntityData().get(EntityMoveUse.STARTX);
        this.start.y = this.getEntityData().get(EntityMoveUse.STARTY);
        this.start.z = this.getEntityData().get(EntityMoveUse.STARTZ);
        return this.start;
    }

    public int getStartTick()
    {
        return this.getEntityData().get(EntityMoveUse.STARTTICK);
    }

    public Entity getTarget()
    {
        if (this.target != null) return this.target;
        return this.target = this.getCommandSenderWorld().getEntity(this.getEntityData().get(EntityMoveUse.TARGET));
    }

    public Entity getUser()
    {
        if (this.user != null) return this.user;
        return this.user = PokecubeCore.getEntityProvider().getEntity(this.getCommandSenderWorld(), this.getEntityData().get(
                EntityMoveUse.USER), true);
    }

    public boolean isDone()
    {
        return this.applied || !this.isAlive();
    }

    @Override
    protected void onHit(final RayTraceResult result)
    {
        // We don't do anything, as we are a "fake" projectile, damage is
        // handled by whatever threw us instead.
    }

    @Override
    public void readAdditionalSaveData(final CompoundNBT compound)
    {
        // Do nothing, if it needs to load/save, it should delete itself
        // instead.
    }

    @Override
    protected void defineSynchedData()
    {
        this.getEntityData().define(EntityMoveUse.MOVENAME, "");
        this.getEntityData().define(EntityMoveUse.ENDX, 0f);
        this.getEntityData().define(EntityMoveUse.ENDY, 0f);
        this.getEntityData().define(EntityMoveUse.ENDZ, 0f);
        this.getEntityData().define(EntityMoveUse.STARTX, 0f);
        this.getEntityData().define(EntityMoveUse.STARTY, 0f);
        this.getEntityData().define(EntityMoveUse.STARTZ, 0f);
        this.getEntityData().define(EntityMoveUse.USER, -1);
        this.getEntityData().define(EntityMoveUse.TARGET, -1);
        this.getEntityData().define(EntityMoveUse.TICK, 0);
        this.getEntityData().define(EntityMoveUse.APPLYTICK, 0);
        this.getEntityData().define(EntityMoveUse.STARTTICK, 0);
    }

    protected void setDuration(final int age)
    {
        this.getEntityData().set(EntityMoveUse.TICK, age);
    }

    protected void setApplicationTick(final int tick)
    {
        this.getEntityData().set(EntityMoveUse.APPLYTICK, tick);
    }

    protected void setStartTick(final int tick)
    {
        this.getEntityData().set(EntityMoveUse.STARTTICK, tick);
    }

    public EntityMoveUse setEnd(final Vector3 location)
    {
        this.end.set(location);
        this.getEntityData().set(EntityMoveUse.ENDX, (float) this.end.x);
        this.getEntityData().set(EntityMoveUse.ENDY, (float) this.end.y);
        this.getEntityData().set(EntityMoveUse.ENDZ, (float) this.end.z);
        return this;
    }

    public EntityMoveUse setMove(final Move_Base move)
    {
        this.move = move;
        String name = "";
        if (move != null) name = move.name;
        this.getEntityData().set(EntityMoveUse.MOVENAME, name);
        final IPokemob user = CapabilityPokemob.getPokemobFor(this.getUser());
        if (move.getAnimation(user) != null)
        {
            this.setDuration(move.getAnimation(user).getDuration() + 1);
            this.setApplicationTick(this.getDuration() - move.getAnimation(user).getApplicationTick());
        }
        else this.setDuration(1);
        return this;
    }

    public EntityMoveUse setMove(final Move_Base move, final int tickOffset)
    {
        this.setMove(move);
        this.getEntityData().set(EntityMoveUse.STARTTICK, tickOffset);
        return this;
    }

    public EntityMoveUse setStart(final Vector3 location)
    {
        this.start.set(location);
        this.start.moveEntity(this);
        this.getEntityData().set(EntityMoveUse.STARTX, (float) this.start.x);
        this.getEntityData().set(EntityMoveUse.STARTY, (float) this.start.y);
        this.getEntityData().set(EntityMoveUse.STARTZ, (float) this.start.z);
        return this;
    }

    public EntityMoveUse setTarget(final Entity target)
    {
        this.target = target;
        if (target != null) this.getEntityData().set(EntityMoveUse.TARGET, target.getId());
        else this.getEntityData().set(EntityMoveUse.TARGET, -1);
        return this;
    }

    public EntityMoveUse setUser(final Entity user)
    {
        this.user = user;
        this.getEntityData().set(EntityMoveUse.USER, user.getId());
        this.alreadyHit.add(this.user.getUUID());
        if (this.init) this.refreshDimensions();
        return this;
    }

    @Override
    public void tick()
    {
        this.init();
        if (!this.init) return;

        final int start = this.getStartTick() - 1;
        this.getEntityData().set(EntityMoveUse.STARTTICK, start);
        // Not ready to start yet
        if (start > 0) return;

        final int age = this.getDuration() - 1;
        this.setDuration(age);

        final Entity user = this.getUser();
        final IPokemob userMob = CapabilityPokemob.getPokemobFor(user);
        // Finished, or is invalid
        if (this.getMove() == null || user == null || age < 0 || !this.isAlive() || !user.isAlive())
        {
            this.remove();
            return;
        }

        this.prev.set(this.here);
        AxisAlignedBB testBox = this.getBoundingBox();
        final Move_Base attack = this.getMove();

        final List<AxisAlignedBB> hitboxes = Lists.newArrayList();

        if (attack.aoe)
        {
            // AOE moves are just a 8-radius box around us.
            final double frac = (this.startAge - this.getDuration()) / this.startAge;
            testBox = this.start.getAABB().inflate(8 * frac);
            hitboxes.add(testBox);
        }
        else if (this.onSelf || this.contact)
        {
            // Self or contact moves will stick to the user.
            this.start.set(user);
            this.end.set(this.start);
            EntityTools.copyPositions(this, user);
            final float s = (float) Math.max(0.75, PokecubeCore.getConfig().contactAttackDistance);
            testBox = user.getBoundingBox().inflate(s);
            if (user.isMultipartEntity())
            {
                testBox = null;
                for (final PartEntity<?> part : user.getParts())
                {
                    final AxisAlignedBB box = part.getBoundingBox().inflate(s);
                    if (testBox == null) testBox = box;
                    else testBox = box.minmax(testBox);
                    hitboxes.add(box);
                }
            }
            else hitboxes.add(testBox);

        }
        else
        {
            // Otherwise they fly in a straight line from the user to the target
            final double frac = this.dist * (this.startAge - this.getDuration()) / this.startAge;
            this.setDeltaMovement(this.dir.x * frac, this.dir.y * frac, this.dir.z * frac);
            this.setPos(this.start.x + this.dir.x * frac, this.start.y + this.dir.y * frac, this.start.z
                    + this.dir.z * frac);
            this.here.set(this);
            testBox = this.getBoundingBox();
            // Increase size near end to increase accuracy a bit
            if (this.end.distToSq(this.here) < 1) testBox = testBox.inflate(1);
            hitboxes.add(testBox);
        }

        if (this.getCommandSenderWorld().isClientSide && attack.getAnimation(userMob) != null) attack.getAnimation(userMob)
                .spawnClientEntities(this.getMoveInfo());

        // Not ready to apply yet
        if (this.getApplicationTick() < age) return;

        final Vector3d v = this.getDeltaMovement();
        testBox = testBox.expandTowards(v.x, v.y, v.z);
        final List<Entity> hits = this.getCommandSenderWorld().getEntities(this, testBox, this.valid);
        final AxisAlignedBB hitBox = testBox;
        hits.removeIf(e ->
        {
            boolean hit = hitboxes.size() > 1;
            if (!hit) for (final AxisAlignedBB box : hitboxes)
                if (box.intersects(e.getBoundingBox()))
                {
                    hit = true;
                    break;
                }
            if (!hit) return true;
            if (!e.isMultipartEntity()) return false;
            final PartEntity<?>[] parts = e.getParts();
            for (final PartEntity<?> part : parts)
                if (part.getBoundingBox().intersects(hitBox)) return false;
            return true;
        });

        for (final Entity e : hits)
            this.doMoveUse(e);

        if (this.getMove() != null && userMob != null && !this.applied && !this.getCommandSenderWorld().isClientSide)
        {
            boolean canApply = false;
            this.getEnd();
            if (this.contact)
            {
                final double range = userMob.inCombat() ? 0.5 : 4;
                final AxisAlignedBB endPos = new AxisAlignedBB(this.end.getPos()).inflate(range);
                canApply = endPos.intersects(this.getBoundingBox());
            }
            else
            {
                final EntityRayTraceResult hit = ProjectileHelper.getEntityHitResult(this.getCommandSenderWorld(), this,
                        this.here.toVec3d(), this.end.toVec3d(), this.getBoundingBox(), this.valid);
                canApply = hit == null || hit.getType() == Type.MISS;
            }
            if (canApply)
            {
                this.applied = true;
                // We only apply this to do block effects, not for damage. For
                // damage. we use the call above to doMoveUse(entity)
                this.getMove().doWorldAction(userMob, this.end);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(final CompoundNBT compound)
    {
    }
}
