package pokecube.core.moves.animations;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

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
        EntityMoveUse.TYPE = EntityType.Builder.create(EntityMoveUse::new, EntityClassification.MISC).disableSummoning()
                .immuneToFire().setTrackingRange(64).setShouldReceiveVelocityUpdates(true).setUpdateInterval(1).size(
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
            this.toMake = new EntityMoveUse(EntityMoveUse.TYPE, user.getEntityWorld());
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

    static final DataParameter<String>  MOVENAME  = EntityDataManager.<String> createKey(EntityMoveUse.class,
            DataSerializers.STRING);
    static final DataParameter<Float>   ENDX      = EntityDataManager.<Float> createKey(EntityMoveUse.class,
            DataSerializers.FLOAT);
    static final DataParameter<Float>   ENDY      = EntityDataManager.<Float> createKey(EntityMoveUse.class,
            DataSerializers.FLOAT);
    static final DataParameter<Float>   ENDZ      = EntityDataManager.<Float> createKey(EntityMoveUse.class,
            DataSerializers.FLOAT);
    static final DataParameter<Float>   STARTX    = EntityDataManager.<Float> createKey(EntityMoveUse.class,
            DataSerializers.FLOAT);
    static final DataParameter<Float>   STARTY    = EntityDataManager.<Float> createKey(EntityMoveUse.class,
            DataSerializers.FLOAT);
    static final DataParameter<Float>   STARTZ    = EntityDataManager.<Float> createKey(EntityMoveUse.class,
            DataSerializers.FLOAT);
    static final DataParameter<Integer> USER      = EntityDataManager.<Integer> createKey(EntityMoveUse.class,
            DataSerializers.VARINT);
    static final DataParameter<Integer> TARGET    = EntityDataManager.<Integer> createKey(EntityMoveUse.class,
            DataSerializers.VARINT);
    static final DataParameter<Integer> TICK      = EntityDataManager.<Integer> createKey(EntityMoveUse.class,
            DataSerializers.VARINT);
    static final DataParameter<Integer> STARTTICK = EntityDataManager.<Integer> createKey(EntityMoveUse.class,
            DataSerializers.VARINT);
    static final DataParameter<Integer> APPLYTICK = EntityDataManager.<Integer> createKey(EntityMoveUse.class,
            DataSerializers.VARINT);

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
        final UUID targetID = EntityTools.getCoreEntity(e).getUniqueID();
        return !this.alreadyHit.contains(targetID);
    };

    public EntityMoveUse(final EntityType<EntityMoveUse> type, final World worldIn)
    {
        super(type, worldIn);
        this.ignoreFrustumCheck = true;
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
        this.recalculateSize();

        this.here.set(this);

        // Put us and our user in here by default.
        this.alreadyHit.add(this.getUniqueID());
        this.alreadyHit.add(this.user.getUniqueID());
    }

    @Override
    public EntitySize getSize(final Pose poseIn)
    {
        EntitySize size = super.getSize(poseIn);
        this.getMove();
        if (this.move == null) return size;
        this.contact = (this.move.move.attackCategory & IMoveConstants.CATEGORY_CONTACT) > 0;
        final boolean aoe = this.move.aoe;
        if (this.contact)
        {
            final float s = (float) Math.max(0.75, PokecubeCore.getConfig().contactAttackDistance);
            final float width = this.user.getWidth();
            final float height = this.user.getHeight();
            size = EntitySize.fixed(width + s, height + s);
        }
        else if (aoe)
        {
            final float s = 8;
            final float width = this.user.getWidth();
            final float height = this.user.getHeight();
            size = EntitySize.fixed(width + s, height + s);
        }
        return size;
    }

    @Override
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void doMoveUse(final Entity target)
    {
        final Move_Base attack = this.getMove();
        final Entity user = this.getUser();
        final LivingEntity living = EntityTools.getCoreLiving(target);
        if (!this.valid.test(target) || living == null) return;
        if (user == null || !this.isAlive() || !user.isAlive()) return;
        // We only want to hit living entities, but non-living ones can be
        // detected as parts of other mobs, like ender dragons.
        final UUID targetID = living.getUniqueID();
        // If it is null here, it means that the target was not a living entity,
        // or a part of one.
        if (targetID == null) return;

        final Entity targ = this.getTarget();
        final UUID targId = targ == null ? null : targ.getUniqueID();

        this.alreadyHit.add(targetID);

        // Only hit multipart entities once
        // Only can hit our valid target!
        if (targId != null && !attack.move.canHitNonTarget() && !targId.equals(targetID)) return;
        if (!this.getEntityWorld().isRemote)
        {
            final IPokemob userMob = CapabilityPokemob.getPokemobFor(user);
            MovesUtils.doAttack(attack.name, userMob, target);
            this.applied = true;
        }
    }

    public int getDuration()
    {
        return this.getDataManager().get(EntityMoveUse.TICK);
    }

    public int getApplicationTick()
    {
        return this.getDataManager().get(EntityMoveUse.APPLYTICK);
    }

    public Vector3 getEnd()
    {
        this.end.x = this.getDataManager().get(EntityMoveUse.ENDX);
        this.end.y = this.getDataManager().get(EntityMoveUse.ENDY);
        this.end.z = this.getDataManager().get(EntityMoveUse.ENDZ);
        return this.end;
    }

    public Move_Base getMove()
    {
        if (this.move != null) return this.move;
        final String name = this.getDataManager().get(EntityMoveUse.MOVENAME);
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
    public AxisAlignedBB getRenderBoundingBox()
    {
        return IForgeTileEntity.INFINITE_EXTENT_AABB;
    }

    public Vector3 getStart()
    {
        this.start.x = this.getDataManager().get(EntityMoveUse.STARTX);
        this.start.y = this.getDataManager().get(EntityMoveUse.STARTY);
        this.start.z = this.getDataManager().get(EntityMoveUse.STARTZ);
        return this.start;
    }

    public int getStartTick()
    {
        return this.getDataManager().get(EntityMoveUse.STARTTICK);
    }

    public Entity getTarget()
    {
        if (this.target != null) return this.target;
        return this.target = this.getEntityWorld().getEntityByID(this.getDataManager().get(EntityMoveUse.TARGET));
    }

    public Entity getUser()
    {
        if (this.user != null) return this.user;
        return this.user = PokecubeCore.getEntityProvider().getEntity(this.getEntityWorld(), this.getDataManager().get(
                EntityMoveUse.USER), true);
    }

    public boolean isDone()
    {
        return this.applied || !this.isAlive();
    }

    @Override
    protected void onImpact(final RayTraceResult result)
    {
        // We don't do anything, as we are a "fake" projectile, damage is
        // handled by whatever threw us instead.
    }

    @Override
    public void readAdditional(final CompoundNBT compound)
    {
        // Do nothing, if it needs to load/save, it should delete itself
        // instead.
    }

    @Override
    protected void registerData()
    {
        this.getDataManager().register(EntityMoveUse.MOVENAME, "");
        this.getDataManager().register(EntityMoveUse.ENDX, 0f);
        this.getDataManager().register(EntityMoveUse.ENDY, 0f);
        this.getDataManager().register(EntityMoveUse.ENDZ, 0f);
        this.getDataManager().register(EntityMoveUse.STARTX, 0f);
        this.getDataManager().register(EntityMoveUse.STARTY, 0f);
        this.getDataManager().register(EntityMoveUse.STARTZ, 0f);
        this.getDataManager().register(EntityMoveUse.USER, -1);
        this.getDataManager().register(EntityMoveUse.TARGET, -1);
        this.getDataManager().register(EntityMoveUse.TICK, 0);
        this.getDataManager().register(EntityMoveUse.APPLYTICK, 0);
        this.getDataManager().register(EntityMoveUse.STARTTICK, 0);
    }

    protected void setDuration(final int age)
    {
        this.getDataManager().set(EntityMoveUse.TICK, age);
    }

    protected void setApplicationTick(final int tick)
    {
        this.getDataManager().set(EntityMoveUse.APPLYTICK, tick);
    }

    protected void setStartTick(final int tick)
    {
        this.getDataManager().set(EntityMoveUse.STARTTICK, tick);
    }

    public EntityMoveUse setEnd(final Vector3 location)
    {
        this.end.set(location);
        this.getDataManager().set(EntityMoveUse.ENDX, (float) this.end.x);
        this.getDataManager().set(EntityMoveUse.ENDY, (float) this.end.y);
        this.getDataManager().set(EntityMoveUse.ENDZ, (float) this.end.z);
        return this;
    }

    public EntityMoveUse setMove(final Move_Base move)
    {
        this.move = move;
        String name = "";
        if (move != null) name = move.name;
        this.getDataManager().set(EntityMoveUse.MOVENAME, name);
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
        this.getDataManager().set(EntityMoveUse.STARTTICK, tickOffset);
        return this;
    }

    public EntityMoveUse setStart(final Vector3 location)
    {
        this.start.set(location);
        this.start.moveEntity(this);
        this.getDataManager().set(EntityMoveUse.STARTX, (float) this.start.x);
        this.getDataManager().set(EntityMoveUse.STARTY, (float) this.start.y);
        this.getDataManager().set(EntityMoveUse.STARTZ, (float) this.start.z);
        return this;
    }

    public EntityMoveUse setTarget(final Entity target)
    {
        this.target = target;
        if (target != null) this.getDataManager().set(EntityMoveUse.TARGET, target.getEntityId());
        else this.getDataManager().set(EntityMoveUse.TARGET, -1);
        return this;
    }

    public EntityMoveUse setUser(final Entity user)
    {
        this.user = user;
        this.getDataManager().set(EntityMoveUse.USER, user.getEntityId());
        this.alreadyHit.add(this.user.getUniqueID());
        if (this.init) this.recalculateSize();
        return this;
    }

    @Override
    public void tick()
    {
        this.init();
        if (!this.init) return;

        final int start = this.getStartTick() - 1;
        this.getDataManager().set(EntityMoveUse.STARTTICK, start);
        // Not ready to start yet
        if (start > 0) return;

        final int age = this.getDuration() - 1;
        this.setDuration(age);

        final IPokemob userMob = CapabilityPokemob.getPokemobFor(this.user);
        // Finished, or is invalid
        if (this.getMove() == null || this.getUser() == null || !this.isAlive() || !this.getUser().isAlive() || age < 0)
        {
            this.remove();
            return;
        }

        this.prev.set(this.here);
        AxisAlignedBB testBox = this.getBoundingBox();
        final Move_Base attack = this.getMove();

        if (attack.aoe)
        {
            // AOE moves are just a 8-radius box around us.
            final double frac = (this.startAge - this.getDuration()) / this.startAge;
            testBox = this.start.getAABB().grow(8 * frac);
        }
        else if (this.onSelf || this.contact)
        {
            // Self or contact moves will stick to the user.
            this.start.set(this.getUser());
            this.end.set(this.start);
            this.setMotion(this.getUser().getMotion());
            this.start.moveEntity(this);
        }
        else
        {
            // Otherwise they fly in a straight line from the user to the target
            final double frac = this.dist * (this.startAge - this.getDuration()) / this.startAge;
            this.setMotion(this.dir.x * frac, this.dir.y * frac, this.dir.z * frac);
            this.setPosition(this.start.x + this.dir.x * frac, this.start.y + this.dir.y * frac, this.start.z
                    + this.dir.z * frac);
            this.here.set(this);
        }

        if (this.getEntityWorld().isRemote && attack.getAnimation(userMob) != null) attack.getAnimation(userMob)
                .spawnClientEntities(this.getMoveInfo());

        // Not ready to apply yet
        if (this.getApplicationTick() < age) return;

        final Vector3d v = this.getMotion();
        testBox = testBox.expand(v.x, v.y, v.z);
        final List<Entity> hits = this.getEntityWorld().getEntitiesInAABBexcluding(this, testBox, this.valid);

        for (final Entity e : hits)
            this.doMoveUse(e);

        if (this.getMove() != null && userMob != null && !this.applied && !this.getEntityWorld().isRemote)
        {
            boolean canApply = false;
            this.getEnd();
            if (this.contact)
            {
                final double range = userMob.inCombat() ? 0.5 : 4;
                final AxisAlignedBB endPos = new AxisAlignedBB(this.end.getPos()).grow(range);
                canApply = endPos.intersects(this.getBoundingBox());
            }
            else
            {
                final EntityRayTraceResult hit = ProjectileHelper.rayTraceEntities(this.getEntityWorld(), this,
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
    public void writeAdditional(final CompoundNBT compound)
    {
    }
}
