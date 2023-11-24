package pokecube.core.moves.damage;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeBlockEntity;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.network.NetworkHooks;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.moves.Battle;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveAnimation.MovePacketInfo;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.init.EntityTypes;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.EntityTools;
import thut.api.maths.Vector3;

public class EntityMoveUse extends ThrowableProjectile
{
    static final EntityDataAccessor<String> MOVENAME;
    static final EntityDataAccessor<Float> ENDX;
    static final EntityDataAccessor<Float> ENDY;
    static final EntityDataAccessor<Float> ENDZ;
    static final EntityDataAccessor<Float> STARTX;
    static final EntityDataAccessor<Float> STARTY;
    static final EntityDataAccessor<Float> STARTZ;
    static final EntityDataAccessor<Integer> USER;
    static final EntityDataAccessor<Integer> TARGET;
    static final EntityDataAccessor<Integer> TICK;
    static final EntityDataAccessor<Integer> STARTTICK;
    static final EntityDataAccessor<Integer> APPLYTICK;

    static
    {
        MOVENAME = SynchedEntityData.<String>defineId(EntityMoveUse.class, EntityDataSerializers.STRING);
        ENDX = SynchedEntityData.<Float>defineId(EntityMoveUse.class, EntityDataSerializers.FLOAT);
        ENDY = SynchedEntityData.<Float>defineId(EntityMoveUse.class, EntityDataSerializers.FLOAT);
        ENDZ = SynchedEntityData.<Float>defineId(EntityMoveUse.class, EntityDataSerializers.FLOAT);
        STARTX = SynchedEntityData.<Float>defineId(EntityMoveUse.class, EntityDataSerializers.FLOAT);
        STARTY = SynchedEntityData.<Float>defineId(EntityMoveUse.class, EntityDataSerializers.FLOAT);
        STARTZ = SynchedEntityData.<Float>defineId(EntityMoveUse.class, EntityDataSerializers.FLOAT);
        USER = SynchedEntityData.<Integer>defineId(EntityMoveUse.class, EntityDataSerializers.INT);
        TARGET = SynchedEntityData.<Integer>defineId(EntityMoveUse.class, EntityDataSerializers.INT);
        TICK = SynchedEntityData.<Integer>defineId(EntityMoveUse.class, EntityDataSerializers.INT);
        STARTTICK = SynchedEntityData.<Integer>defineId(EntityMoveUse.class, EntityDataSerializers.INT);
        APPLYTICK = SynchedEntityData.<Integer>defineId(EntityMoveUse.class, EntityDataSerializers.INT);
    }

    public static EntityMoveUse create(Level level, MoveApplication apply, Vector3 endpoint)
    {
        var entity = new EntityMoveUse(EntityTypes.getMove(), level);
        entity.apply = apply.copyForMoveUse();
        Vector3 start = new Vector3(apply.getUser().getEntity());
        entity.setStart(start);
        start.moveEntity(entity);
        entity.setMove(apply.getMove());
        entity.setUser(apply.getUser().getEntity());
        entity.setTarget(apply.getTarget());
        entity.setEnd(endpoint);
        return entity;
    }

    Vector3 end = new Vector3();
    Vector3 start = new Vector3();

    Vector3 here = new Vector3();
    Vector3 prev = new Vector3();

    Vector3 dir = new Vector3();

    Mob user = null;
    LivingEntity target = null;

    MoveEntry move = null;

    boolean finished = false;
    boolean applied = false;
    boolean onSelf = false;
    boolean contact = false;
    boolean init = false;

    int startAge = 1;
    int initTimer = 10;

    double dist = 0;

    private final Vector3 size = new Vector3();

    private MoveApplication apply;

    Predicate<Entity> valid = e -> {
        LivingEntity living = EntityTools.getCoreLiving(e);
        if (living == null) return false;
        final UUID targetID = living.getUUID();
        return !this.apply.alreadyHit.contains(targetID);
    };

    public EntityMoveUse(final EntityType<EntityMoveUse> type, final Level worldIn)
    {
        super(type, worldIn);
        this.noCulling = true;
    }

    protected void init(MoveApplication apply)
    {
        if (this.init) return;
        if (this.initTimer-- < 0)
        {
            // If we timed out, it means our user died before the move could
            // finish (ie it died on the same tick it used the attack), in this
            // case, we just remove the attack.
            this.discard();
            return;
        }

        if (apply != null)
        {
            this.setMove(apply.getMove());
            this.setUser(apply.getUser().getEntity());
            this.setTarget(apply.getTarget());
        }

        // This should initialise these values on the client side correctly.
        this.getStart();
        this.getEnd();
        this.getMove();
        this.getUser();
        this.getTarget();

        if (this.getUser() == null) return;

        IPokemob userMob = PokemobCaps.getPokemobFor(this.getUser());
        if (userMob == null)
        {
            this.discard();
            return;
        }

        if (this.apply == null)
        {
            this.apply = new MoveApplication(getMove(), userMob, this.getTarget());
        }

        this.size.clear();
        this.contact = move.isContact(userMob);
        float s = 0;
        if (this.move.isAoE())
        {
            s = 8;
            this.size.set(s, s, s);
        }
        else if (this.contact)
        {
            s = (float) Math.max(0.75, PokecubeCore.getConfig().contactAttackDistance);
            final float width = this.getUser().getBbWidth() + s;
            final float height = this.getUser().getBbHeight() + s;
            this.size.set(width, height, width);
        }
        else
        {
            this.size.set(0.75);
        }
        if (this.move.customSize != null) this.size.set(this.move.customSize);

        this.init = true;
        this.startAge = this.getDuration();
        if (this.start.distToSq(end) > 0.125) this.dir.set(this.end).subtractFrom(this.start).norm();
        else this.onSelf = true;

        if (this.getUser() == this.getTarget()) this.onSelf = true;

        this.dist = this.start.distanceTo(this.end);
        this.refreshDimensions();

        this.here.set(this);

        if (!this.getUser().getLevel().isClientSide())
        {
            // Put us here by default.
            this.addIgnoredEntity(this);
            this.apply.finished = this::isDone;
            userMob.getMoveStats().addMoveInProgress(userMob, this.apply);
        }
    }

    public void addIgnoredEntity(Entity entity)
    {
        if (entity != null) this.apply.alreadyHit.add(entity.getUUID());
    }

    @Override
    public EntityDimensions getDimensions(final Pose poseIn)
    {
        final EntityDimensions size = super.getDimensions(poseIn);
        this.getMove();
        if (this.move == null) return size;
        return size;
    }

    @Override
    public Packet<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void doMoveUse(LivingEntity target)
    {
        final MoveEntry attack = this.getMove();
        final Mob user = this.getUser();
        if (user == null || !this.isAlive() || !user.isAlive()) return;

        final LivingEntity living = EntityTools.getCoreLiving(target);
        // If the core living is not valid, we just quit there.
        if (!this.valid.test(living)) return;

        boolean selfMove = user == this.getTarget();
        // Self move should only hit user.
        if (selfMove && target != user) return;

        final UUID targetID = living.getUUID();
        final Entity targ = this.getTarget();
        final UUID targId = targ == null ? null : targ.getUUID();

        this.addIgnoredEntity(targ);

        // Only hit multipart entities once
        // Only can hit our valid target!
        if (targId != null && !attack.canHitNonTarget() && !targId.equals(targetID)) return;
        if (!this.level.isClientSide)
        {
            final IPokemob userMob = PokemobCaps.getPokemobFor(user);
            Battle b = Battle.getBattle(user);
            // Initiate battle in here if the target was not the intended
            // target.
            if (target != apply.getTarget())
            {
                boolean newCombat = target instanceof Mob mob && BrainUtils.getAttackTarget(mob) != user;
                if (b != null && b.getEnemies(user).contains(target)) newCombat = false;
                if (b == null && userMob.getMoveStats().targetAlly == target) newCombat = false;
                if (target instanceof Mob mob && newCombat) Battle.createOrAddToBattle(mob, user);
            }

            if (target.getLastHurtByMob() != user)
            {
                target.setLastHurtByMob(user);
                user.setLastHurtByMob(target);
            }

            MovesUtils.doAttack(attack.name, userMob, target);
            this.applied = true;

            // Don't penetrate through blocking mobs, so end the move here.
            if (selfMove || (living.isBlocking() && !this.getMove().isAoE()))
            {
                this.finished = true;
                // We only apply this to do block effects, not for damage. For
                // damage. we use the call above to doMoveUse(entity)
                this.getMove().doWorldAction(userMob, this.end);
                this.discard();
            }
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

    public MoveEntry getMove()
    {
        if (this.move != null) return this.move;
        final String name = this.getEntityData().get(EntityMoveUse.MOVENAME);
        if (name.isEmpty()) return null;
        return this.move = MovesUtils.getMove(name);
    }

    public MovePacketInfo getMoveInfo()
    {
        final MovePacketInfo info = new MovePacketInfo(this.getMove(), this.getUser(), this.getTarget(),
                this.getStart(), this.getEnd());
        final IPokemob userMob = PokemobCaps.getPokemobFor(info.attacker);
        info.currentTick = info.move.getAnimation(userMob).getDuration() - this.getDuration();
        return info;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public AABB getBoundingBoxForCulling()
    {
        return IForgeBlockEntity.INFINITE_EXTENT_AABB;
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

    public LivingEntity getTarget()
    {
        if (this.target != null) return this.target;
        if (this.level.getEntity(this.getEntityData().get(EntityMoveUse.TARGET)) instanceof LivingEntity target)
            this.target = target;
        return this.target;
    }

    public Mob getUser()
    {
        if (this.user != null) return this.user;
        if (this.level.getEntity(this.getEntityData().get(EntityMoveUse.USER)) instanceof Mob user) this.user = user;
        return this.user;
    }

    public boolean isDone()
    {
        return this.finished || this.isRemoved() || this.getUser() == null || !this.getUser().isAlive();
    }

    @Override
    protected void onHit(final HitResult result)
    {
        // We don't do anything, as we are a "fake" projectile, damage is
        // handled by whatever threw us instead.
    }

    @Override
    public void readAdditionalSaveData(final CompoundTag compound)
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

    public EntityMoveUse setMove(final MoveEntry move)
    {
        this.move = move;
        String name = "";
        if (move != null) name = move.name;
        this.getEntityData().set(EntityMoveUse.MOVENAME, name);
        final IPokemob user = PokemobCaps.getPokemobFor(this.getUser());
        if (move.getAnimation(user) != null)
        {
            this.setDuration(move.getAnimation(user).getDuration() + 1);
            this.setApplicationTick(move.getAnimation(user).getApplicationTick());
        }
        else this.setDuration(1);
        return this;
    }

    public EntityMoveUse setMove(final MoveEntry move, final int tickOffset)
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

    public EntityMoveUse setTarget(final LivingEntity target)
    {
        this.target = target;
        if (target != null) this.getEntityData().set(EntityMoveUse.TARGET, target.getId());
        else this.getEntityData().set(EntityMoveUse.TARGET, -1);
        return this;
    }

    public EntityMoveUse setUser(final Mob user)
    {
        this.user = user;
        this.getEntityData().set(EntityMoveUse.USER, user.getId());
        if (this.init) this.refreshDimensions();
        return this;
    }

    @Override
    public void tick()
    {
        this.init(this.apply);
        if (!this.init) return;

        final int start = this.getStartTick() - 1;
        this.getEntityData().set(EntityMoveUse.STARTTICK, start);
        // Not ready to start yet
        if (start > 0) return;

        final int age = this.getDuration() - 1;
        this.setDuration(age);

        final Entity user = this.getUser();
        final IPokemob userMob = PokemobCaps.getPokemobFor(user);
        if (userMob != null) userMob.setCombatState(CombatStates.EXECUTINGMOVE, true);

        // Finished, or is invalid
        if (this.getMove() == null || user == null || age < 0 || !this.isAlive() || !user.isAlive())
        {
            if (userMob != null)
            {
                userMob.setCombatState(CombatStates.EXECUTINGMOVE, false);
                BrainUtils.clearMoveUseTarget(userMob.getEntity());
            }
            if (!applied)
            {
                // Send message about having missed the target
                if (target != null && userMob != null) MovesUtils.displayEfficiencyMessages(userMob, target, -1, 0);
                if (PokecubeCore.getConfig().debug_moves && user != null && this.getMove() != null)
                {
                    PokecubeAPI.logInfo("B: Attack {} by {} terminated without applying!", this.getMove().getName(),
                            user.getDisplayName().getString());
                }
            }
            this.discard();
            return;
        }

        this.prev.set(this.here);

        AABB testBox = this.getBoundingBox();
        final MoveEntry attack = this.getMove();

        final List<AABB> hitboxes = Lists.newArrayList();

        // These are divided by 2, as inflate applies to both directions!
        final float sh = 0.25f + (float) Math.max(this.size.x, this.size.z) / 2;
        final float sv = 0.25f + (float) this.size.y / 2;

        if (attack.isAoE())
        {
            // AOE moves are just a 8-radius box around us.
            final double frac = 2 * (this.startAge - this.getDuration()) / this.startAge;
            // The 2x above is as sh and sv were divided by 2 earlier.
            testBox = this.start.getAABB().inflate(sh * frac, sv * frac, sh * frac);
            hitboxes.add(testBox);
        }
        else if (this.onSelf || this.contact)
        {
            // Self or contact moves will stick to the user.
            this.start.set(user);
            this.end.set(this.start);
            EntityTools.copyPositions(this, user);
            testBox = user.getBoundingBox().inflate(sh, sv, sh);
            if (user.isMultipartEntity())
            {
                testBox = null;
                for (final PartEntity<?> part : user.getParts())
                {
                    final AABB box = part.getBoundingBox().inflate(sh, sv, sh);
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
            this.setPos(this.start.x + this.dir.x * frac, this.start.y + this.dir.y * frac,
                    this.start.z + this.dir.z * frac);
            this.here.set(this);
            testBox = this.getBoundingBox();
            // Increase size near end to increase accuracy a bit
            testBox = testBox.inflate(sh, sv, sh);
            if (this.end.distToSq(this.here) < 1) testBox = testBox.inflate(0.5, 0.5, 0.5);
            hitboxes.add(testBox);
        }

        if (this.level.isClientSide && attack.getAnimation(userMob) != null)
            attack.getAnimation(userMob).spawnClientEntities(this.getMoveInfo());

        // Not ready to apply yet
        if (this.getApplicationTick() < age) return;

        final Vec3 v = this.getDeltaMovement();
        testBox = testBox.expandTowards(v.x, v.y, v.z);
        final List<Entity> hits = this.level.getEntities(this, testBox, this.valid);
        final AABB hitBox = testBox;

        hits.removeIf(e -> {
            boolean hit = hitboxes.size() > 1;
            if (!hit) for (final AABB box : hitboxes) if (box.intersects(e.getBoundingBox()))
            {
                hit = true;
                break;
            }
            if (!hit) return true;
            if (!e.isMultipartEntity()) return false;
            if (!(EntityTools.getCoreEntity(e) instanceof LivingEntity)) return true;
            final PartEntity<?>[] parts = e.getParts();
            for (final PartEntity<?> part : parts) if (part.getBoundingBox().intersects(hitBox)) return false;
            return true;
        });

        for (final Entity e : hits) if (e instanceof LivingEntity living) this.doMoveUse(living);

        if (this.getMove() != null && userMob != null && !this.finished && !this.level.isClientSide)
        {
            boolean canApply = age == 0;
            this.getEnd();
            if (this.contact)
            {
                final double range = userMob.inCombat() ? 0.5 : 4;
                final AABB endPos = new AABB(this.end.getPos()).inflate(range);
                canApply |= endPos.intersects(this.getBoundingBox());
            }
            else
            {
                final EntityHitResult hit = ProjectileUtil.getEntityHitResult(this.level, this, this.here.toVec3d(),
                        this.end.toVec3d(), this.getBoundingBox(), this.valid);
                canApply |= hit == null || hit.getType() == Type.MISS;
            }
            if (canApply)
            {
                this.finished = true;
                // We only apply this to do block effects, not for damage. For
                // damage. we use the call above to doMoveUse(entity)
                this.getMove().doWorldAction(userMob, this.end);
            }
        }

        if (this.isDone())
        {
            userMob.setCombatState(CombatStates.EXECUTINGMOVE, false);
            BrainUtils.clearMoveUseTarget(userMob.getEntity());
            this.remove(RemovalReason.DISCARDED);
            if (!this.applied)
            {
                // Send message about having missed the target
                if (target != null) MovesUtils.displayEfficiencyMessages(userMob, target, -1, 0);
                if (PokecubeCore.getConfig().debug_moves && user != null && this.getMove() != null)
                {
                    PokecubeAPI.logInfo("B: Attack {} by {} terminated without applying!", this.getMove().getName(),
                            user.getDisplayName().getString());
                }
            }
        }
    }

    @Override
    public void addAdditionalSaveData(final CompoundTag compound)
    {}
}
