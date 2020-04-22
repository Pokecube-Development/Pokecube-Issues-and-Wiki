package pokecube.core.entity.pokemobs.helper;

import java.io.IOException;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.Pose;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import pokecube.core.interfaces.IPokemob;
import thut.api.entity.ICompoundMob;
import thut.api.entity.ICompoundMob.ICompoundPart;

public class PokemobPart extends Entity implements ICompoundPart
{
    public final PokemobHasParts base;

    public final IPokemob pokemob;

    private final EntitySize size;

    public final BlockPos shift;

    public PokemobPart(final PokemobHasParts base, final float width, final float height, final int x, final int y,
            final int z)
    {
        super(base.getType(), base.getEntityWorld());
        this.size = EntitySize.flexible(width, height);
        this.pokemob = base.pokemobCap;
        this.base = base;
        this.shift = new BlockPos(x, y, z);
    }

    @Override
    protected void registerData()
    {
    }

    @Override
    public void read(final CompoundNBT compound)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        return new CompoundNBT();
    }

    @Override
    public void applyEntityCollision(final Entity entityIn)
    {
        if (entityIn.isEntityEqual(this)) return;
        super.applyEntityCollision(entityIn);
    }

    @Override
    protected void readAdditional(final CompoundNBT compound)
    {
    }

    @Override
    protected void writeAdditional(final CompoundNBT compound)
    {
    }

    @Override
    public void tick()
    {
        if (this.base == null)
        {
            this.remove();
            return;
        }
    }

    /**
     * Called when the entity is attacked.
     */
    @Override
    public boolean attackEntityFrom(final DamageSource source, final float amount)
    {
        return this.isInvulnerableTo(source) ? false : this.base.attackFromPart(this, source, amount);
    }

    /**
     * Returns true if Entity argument is equal to this Entity
     */
    @Override
    public boolean isEntityEqual(final Entity entityIn)
    {
        return this == entityIn || this.base == entityIn;
    }

    @Override
    public IPacket<?> createSpawnPacket()
    {
        return new IPacket<INetHandler>()
        {
            @Override
            public void readPacketData(final PacketBuffer buf) throws IOException
            {
            }

            @Override
            public void writePacketData(final PacketBuffer buf) throws IOException
            {
            }

            @Override
            public void processPacket(final INetHandler handler)
            {
            }
        };
    }

    @Override
    public EntitySize getSize(final Pose poseIn)
    {
        return this.size;
    }

    @Override
    public ICompoundMob getBase()
    {
        return this.base;
    }

    @Override
    public Entity getMob()
    {
        return this;
    }

}
