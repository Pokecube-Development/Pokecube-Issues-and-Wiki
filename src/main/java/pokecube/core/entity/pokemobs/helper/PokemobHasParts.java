package pokecube.core.entity.pokemobs.helper;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.passive.ShoulderRidingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import pokecube.core.PokecubeCore;

public abstract class PokemobHasParts extends PokemobCombat
{
    private PokemobPart[] parts;

    int numWide = 0;
    int numTall = 0;

    float last_size = 0;

    public PokemobHasParts(final EntityType<? extends ShoulderRidingEntity> type, final World worldIn)
    {
        super(type, worldIn);
        this.initSizes(1);
    }

    protected void initSizes(final float size)
    {
        if (size == this.last_size) return;
        this.last_size = size;

        final float maxH = (float) PokecubeCore.getConfig().largeMobForSplit;
        final float maxW = (float) PokecubeCore.getConfig().largeMobForSplit;
        final float width = this.pokemobCap.getPokedexEntry().width * size;
        final float height = this.pokemobCap.getPokedexEntry().height * size;

        if (height > maxH || width > maxW)
        {
            this.numWide = MathHelper.ceil(this.pokemobCap.getPokedexEntry().width / maxW);
            this.numTall = MathHelper.ceil(this.pokemobCap.getPokedexEntry().height / maxH);
            this.parts = new PokemobPart[this.numWide * this.numWide * this.numTall];
            int i = 0;
            for (int y = 0; y < this.numTall; y++)
                for (int x = 0; x < this.numWide; x++)
                    for (int z = 0; z < this.numWide; z++)
                        this.parts[i++] = new PokemobPart(this, width / this.numWide, height / this.numTall, x, y, z);
            this.size = EntitySize.fixed(Math.min(1, maxW), Math.min(1, maxH));
            this.ignoreFrustumCheck = true;
        }
        else
        {
            this.size = EntitySize.fixed(width, height);
            this.parts = new PokemobPart[0];
            this.ignoreFrustumCheck = false;
        }
        this.recalculateSize();
    }

    @Override
    public PokemobPart[] getParts()
    {
        return this.parts;
    }

    @Override
    protected void collideWithNearbyEntities()
    {
        final AxisAlignedBB box = this.getBoundingBox();
        final List<Entity> list = this.world.getEntitiesInAABBexcluding(this, box, EntityPredicates.pushableBy(this));
        if (!list.isEmpty())
        {
            list.removeIf(e -> e instanceof PokemobPart && ((PokemobPart) e).getParent() == this);

            final int i = this.world.getGameRules().getInt(GameRules.MAX_ENTITY_CRAMMING);
            if (i > 0 && list.size() > i - 1 && this.rand.nextInt(4) == 0)
            {
                int j = 0;

                for (final Entity element : list)
                    if (!element.isPassenger()) ++j;

                if (j > i - 1) this.attackEntityFrom(DamageSource.CRAMMING, 6.0F);
            }

            for (final Entity entity : list)
                this.collideWithEntity(entity);
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
    public void livingTick()
    {
        this.updatePartsPos();
        super.livingTick();
    }

    protected void updatePartsPos()
    {
        this.initSizes(this.pokemobCap.getSize());
        if (this.parts.length > 0)
        {
            final double dx = this.numWide / this.size.width;
            final double dy = this.numTall / this.size.height;
            final double dz = dx;
            final Vector3d r = this.getPositionVec();
            for (final PokemobPart p : this.parts)
                p.setPosition(r.x + p.shift.getX() * dx, r.y + p.shift.getY() * dy, r.z + p.shift.getZ() * dz);
        }
    }

    @Override
    public void move(final MoverType typeIn, final Vector3d pos)
    {
        if (this.parts.length == 0)
        {
            super.move(typeIn, pos);
            return;
        }
        // TODO get this working properly for large mobs.
        // final boolean applyPartLimits = false;
        // final float s = this.pokemobCap.getSize();
        // final double dist = Math.max(this.pokemobCap.getPokedexEntry().width
        // * s, this.pokemobCap
        // .getPokedexEntry().length * s) + 8;
        // Vector3d toMove = pos;
        // if (TerrainManager.isAreaLoaded(this.getEntityWorld(),
        // this.getPosition(), dist) && applyPartLimits)
        // {
        // boolean partsOnGround = false;
        // for (final PokemobPart p : this.parts)
        // {
        // // Only consider top and bottom slabs for this.
        // if (pos.y < 0 && p.shift.getY() > 0) continue;
        // if (pos.y > 0 && p.shift.getY() < this.numTall - 1) continue;
        //
        // final Vector3d posA = p.getPositionVec();
        // p.move(typeIn, pos);
        // partsOnGround = partsOnGround || p.onGround;
        // final Vector3d posB = p.getPositionVec();
        // toMove = posB.subtract(posA);
        // System.out.println(pos + " " + toMove);
        // }
        // this.setMotion(toMove);
        // super.move(typeIn, toMove);
        // this.updatePartsPos();
        // }
        // else
        super.move(typeIn, pos);
    }
}
