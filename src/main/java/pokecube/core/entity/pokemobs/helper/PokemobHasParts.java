package pokecube.core.entity.pokemobs.helper;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.passive.ShoulderRidingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;
import thut.api.entity.ICompoundMob;

public abstract class PokemobHasParts extends PokemobCombat implements ICompoundMob
{
    private final PokemobPart[] parts;
    int                         numWide = 0;
    int                         numTall = 0;

    public PokemobHasParts(final EntityType<? extends ShoulderRidingEntity> type, final World worldIn)
    {
        super(type, worldIn);
        final double maxH = PokecubeCore.getConfig().largeMobForSplit;
        final double maxW = PokecubeCore.getConfig().largeMobForSplit;
        // These are the conditions for splitting us into parts.
        if (this.size.height > maxH || this.size.width > maxW)
        {
            this.numWide = MathHelper.ceil(this.pokemobCap.getPokedexEntry().width / maxW);
            this.numTall = MathHelper.ceil(this.pokemobCap.getPokedexEntry().height / maxH);
            this.parts = new PokemobPart[this.numWide * this.numWide * this.numTall];
            int i = 0;
            for (int y = 0; y < this.numTall; y++)
                for (int x = 0; x < this.numWide; x++)
                    for (int z = 0; z < this.numWide; z++)
                        this.parts[i++] = new PokemobPart(this, this.size.width / this.numWide, this.size.height
                                / this.numTall, x, y, z);
            this.size = EntitySize.fixed(1, 1);
        }
        else this.parts = new PokemobPart[0];
    }

    @Override
    protected void collideWithNearbyEntities()
    {
        final List<Entity> list = this.world.getEntitiesInAABBexcluding(this, this.getBoundingBox(), EntityPredicates
                .pushableBy(this));
        if (!list.isEmpty())
        {
            final int i = this.world.getGameRules().getInt(GameRules.MAX_ENTITY_CRAMMING);
            if (i > 0 && list.size() > i - 1 && this.rand.nextInt(4) == 0)
            {
                int j = 0;

                for (int k = 0; k < list.size(); ++k)
                    if (!list.get(k).isPassenger()) ++j;

                if (j > i - 1) this.attackEntityFrom(DamageSource.CRAMMING, 6.0F);
            }

            for (int l = 0; l < list.size(); ++l)
            {
                final Entity entity = list.get(l);
                this.collideWithEntity(entity);
            }
        }

    }

    @Override
    public void applyEntityCollision(final Entity entityIn)
    {
        if (entityIn.isEntityEqual(this)) return;
        super.applyEntityCollision(entityIn);
    }

    /**
     * Returns true if Entity argument is equal to this Entity
     */
    @Override
    public boolean isEntityEqual(final Entity entityIn)
    {
        return this == entityIn || entityIn instanceof PokemobPart && ((PokemobPart) entityIn).base == this;
    }

    @Override
    public void onAddedToWorld()
    {
        super.onAddedToWorld();
    }

    @Override
    public void remove(final boolean keepData)
    {
        super.remove(keepData);
    }

    @Override
    public void onRemovedFromWorld()
    {
        super.onRemovedFromWorld();
    }

    @Override
    public void tick()
    {
        this.updatePartsPos();
        super.tick();
    }

    private void updatePartsPos()
    {
        if (this.parts.length > 0)
        {
            final double dx = this.numWide / this.size.width;
            final double dy = this.numTall / this.size.height;
            final double dz = dx;
            final Vec3d r = this.getPositionVec();
            for (final PokemobPart p : this.parts)
                p.setPosition(r.x + p.shift.getX() * dx, r.y + p.shift.getY() * dy, r.z + p.shift.getZ() * dz);
        }
    }

    @Override
    public void move(final MoverType typeIn, final Vec3d pos)
    {
        if (this.parts.length == 0)
        {
            super.move(typeIn, pos);
            return;
        }
        Vec3d toMove = pos;
        for (final PokemobPart p : this.parts)
        {
            // Only consider top and bottom slabs for this.
            if (pos.y < 0 && p.shift.getY() > 0) continue;
            if (pos.y > 0 && p.shift.getY() < this.numTall - 1) continue;
            final Vec3d posA = p.getPositionVec();
            p.move(typeIn, toMove);
            final Vec3d posB = p.getPositionVec();
            toMove = posB.subtract(posA);
        }
        super.move(typeIn, toMove);
        this.updatePartsPos();
    }

    @Override
    public ICompoundPart[] getParts()
    {
        return this.parts;
    }

    @Override
    public Entity getMob()
    {
        return this;
    }
}
