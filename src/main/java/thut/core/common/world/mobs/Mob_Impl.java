package thut.core.common.world.mobs;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import thut.api.world.World;
import thut.api.world.mobs.Mob;
import thut.api.world.mobs.ai.AI;
import thut.api.world.utils.Info;
import thut.api.world.utils.Vector;
import thut.core.common.world.WorldManager;
import thut.core.common.world.mobs.ai.AI_Impl;
import thut.core.common.world.utils.Info_Impl;
import thut.core.common.world.utils.Vector_D;
import thut.core.common.world.utils.Vector_I;

public class Mob_Impl implements Mob, ICapabilityProvider
{
    World           world;
    Entity          entity;
    String          key;
    final AI_Impl   ai       = new AI_Impl();
    final Info_Impl info     = new Info_Impl();
    final Vector_I  worldPos = new Vector_I();
    final Vector_D  position = new Vector_D();
    final Vector_D  velocity = new Vector_D();

    @Override
    public AI getAI()
    {
        return this.ai;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing)
    {
        return this.entity.getCapability(capability, facing);
    }

    @Override
    public float getMaxHealth()
    {
        float num = 0;
        if (this.entity instanceof MobEntity) num = ((MobEntity) this.entity).getMaxHealth();
        return num;
    }

    @Override
    public UUID id()
    {
        return this.entity.getUniqueID();
    }

    @Override
    public Info info()
    {
        return this.info;
    }

    @Override
    public boolean inWorld()
    {
        return this.entity.addedToChunk;
    }

    @Override
    public boolean isDead()
    {
        return !this.entity.isAlive();
    }

    @Override
    public String key()
    {
        return this.key;
    }

    @Override
    public boolean onClient()
    {
        return this.entity.getEntityWorld().isRemote;
    }

    @Override
    public Vector<Double> position()
    {
        this.position.setValue(0, this.entity.getPosX());
        this.position.setValue(1, this.entity.getPosY());
        this.position.setValue(2, this.entity.getPosZ());
        return this.position;
    }

    @Override
    public void setDead()
    {
        this.entity.remove();
    }

    public void setEntity(Entity entity)
    {
        if (this.world != null) this.world.removeMob(this);
        this.entity = entity;
        this.world = WorldManager.instance().getWorld(this.entity.dimension);
        this.world.addMob(this);
        this.key = entity.getType().toString();
    }

    @Override
    public void setHealth(float health)
    {
        if (this.entity instanceof MobEntity) ((MobEntity) this.entity).setHealth(health);
    }

    @Override
    public void setID(UUID id)
    {
        this.entity.setUniqueId(id);
    }

    @Override
    public void setWorld(World world)
    {
        this.world = world;
    }

    @Override
    public Vector<Double> velocity()
    {
        final Vec3d motion = this.entity.getMotion();
        this.velocity.setValue(0, motion.x);
        this.velocity.setValue(1, motion.y);
        this.velocity.setValue(2, motion.z);
        return this.velocity;
    }

    @Override
    public World world()
    {
        return this.world;
    }

    @Override
    public Vector<Integer> worldPos()
    {
        ((Vector_D) this.position()).toInts(this.worldPos);
        return this.worldPos;
    }

    @Override
    public Object wrapped()
    {
        return this.entity;
    }

}
