package pokecube.core.moves.animations;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeTileEntity;
import net.minecraftforge.fml.network.NetworkHooks;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IMoveAnimation.MovePacketInfo;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.MovesUtils;
import thut.api.maths.Vector3;

public class EntityMoveUse extends ThrowableEntity
{
    public static EntityType<EntityMoveUse> TYPE;
    static
    {
        EntityMoveUse.TYPE = EntityType.Builder.create(EntityMoveUse::new, EntityClassification.MISC).disableSummoning()
                .immuneToFire().setTrackingRange(64).setShouldReceiveVelocityUpdates(true).setUpdateInterval(1).size(1f,
                        1f).setCustomClientFactory((spawnEntity, world) ->
                        {
                            return EntityMoveUse.TYPE.create(world);
                        }).build("move_use");
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

    Vector3 end     = Vector3.getNewVector();
    Vector3 start   = Vector3.getNewVector();
    boolean applied = false;

    public EntityMoveUse(final EntityType<EntityMoveUse> type, final World worldIn)
    {
        super(type, worldIn);
        this.ignoreFrustumCheck = true;
    }

    @Override
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void doMoveUse()
    {
        final Move_Base attack = this.getMove();
        Entity user;
        if ((user = this.getUser()) == null || !this.isAlive() || !user.isAlive()) return;
        if (!this.getEntityWorld().isRemote)
        {
            final IPokemob userMob = CapabilityPokemob.getPokemobFor(user);
            final Entity target = this.getTarget();
            if (attack.move.isNotIntercepable() && target != null) MovesUtils.doAttack(attack.name, userMob, target);
            else
            {
                if (attack.getPRE(userMob, target) <= 0 && target != null) this.setEnd(Vector3.getNewVector().set(
                        target));
                MovesUtils.doAttack(attack.name, userMob, this.getEnd());
            }
        }
    }

    public int getAge()
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
        return MovesUtils.getMoveFromName(this.getDataManager().get(EntityMoveUse.MOVENAME));
    }

    public MovePacketInfo getMoveInfo()
    {
        final MovePacketInfo info = new MovePacketInfo(this.getMove(), this.getUser(), this.getTarget(), this
                .getStart(), this.getEnd());
        final IPokemob userMob = CapabilityPokemob.getPokemobFor(info.attacker);
        info.currentTick = info.move.getAnimation(userMob).getDuration() - this.getAge();
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
        return this.getEntityWorld().getEntityByID(this.getDataManager().get(EntityMoveUse.TARGET));
    }

    public Entity getUser()
    {
        return PokecubeCore.getEntityProvider().getEntity(this.getEntityWorld(), this.getDataManager().get(
                EntityMoveUse.USER), true);
    }

    public boolean isDone()
    {
        return this.applied || !this.isAlive();
    }

    @Override
    protected void onImpact(final RayTraceResult result)
    {
        // TODO Auto-generated method stub

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

    public void setAge(final int age)
    {
        this.getDataManager().set(EntityMoveUse.TICK, age);
    }

    public void setApplicationTick(final int tick)
    {
        this.getDataManager().set(EntityMoveUse.APPLYTICK, tick);
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
        String name = "";
        if (move != null) name = move.name;
        this.getDataManager().set(EntityMoveUse.MOVENAME, name);
        final IPokemob user = CapabilityPokemob.getPokemobFor(this.getUser());
        if (move.getAnimation(user) != null)
        {
            this.getDataManager().set(EntityMoveUse.TICK, move.getAnimation(user).getDuration() + 1);
            this.setApplicationTick(this.getAge() - move.getAnimation(user).getApplicationTick());
        }
        else this.getDataManager().set(EntityMoveUse.TICK, 1);
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
        if (target != null) this.getDataManager().set(EntityMoveUse.TARGET, target.getEntityId());
        else this.getDataManager().set(EntityMoveUse.TARGET, -1);
        return this;
    }

    public EntityMoveUse setUser(final Entity user)
    {
        this.getDataManager().set(EntityMoveUse.USER, user.getEntityId());
        return this;
    }

    @Override
    public void tick()
    {
        final int start = this.getStartTick() - 1;
        this.getDataManager().set(EntityMoveUse.STARTTICK, start);
        if (start > 0) return;
        final int age = this.getAge() - 1;
        this.setAge(age);

        if (this.getMove() == null || !this.isAlive() || age < 0)
        {
            this.remove();
            return;
        }
        final Move_Base attack = this.getMove();
        Entity user;
        valid:
        if ((user = this.getUser()) == null || !this.isAlive() || !user.isAlive() || !user.addedToChunk)
        {
            if (user != null && !user.addedToChunk) if (user.getPersistentData().getBoolean("isPlayer")) break valid;
            this.remove();
            return;
        }
        final IPokemob userMob = CapabilityPokemob.getPokemobFor(user);
        if (user instanceof LivingEntity && ((LivingEntity) user).getHealth() <= 1)
        {
            this.remove();
            return;
        }
        if (this.getEntityWorld().isRemote && attack.getAnimation(userMob) != null) attack.getAnimation(userMob)
                .spawnClientEntities(this.getMoveInfo());

        if (!this.applied && age <= this.getApplicationTick())
        {
            this.applied = true;
            this.doMoveUse();
        }

        if (age == 0) this.remove();
    }

    @Override
    public void writeAdditional(final CompoundNBT compound)
    {
    }
}
