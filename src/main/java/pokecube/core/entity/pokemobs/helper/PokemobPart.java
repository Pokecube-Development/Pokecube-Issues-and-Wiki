package pokecube.core.entity.pokemobs.helper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.Pose;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.entity.PartEntity;
import pokecube.core.interfaces.IPokemob;

public class PokemobPart extends PartEntity<PokemobHasParts>
{
    public final PokemobHasParts base;

    public final IPokemob pokemob;

    private final EntitySize size;

    public final BlockPos shift;

    public PokemobPart(final PokemobHasParts base, final float width, final float height, final int x, final int y,
            final int z)
    {
        super(base);
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
    public EntitySize getSize(final Pose poseIn)
    {
        return this.size;
    }

}
