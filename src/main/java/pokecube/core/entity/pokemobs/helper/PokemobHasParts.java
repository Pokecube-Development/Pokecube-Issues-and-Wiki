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

    int numWide = 0;
    int numTall = 0;

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
            list.removeIf(e -> e instanceof PokemobPart && ((PokemobPart) e).getBase() == this);

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
        // TODO get this working properly for large mobs.
        // final boolean applyPartLimits = false;
        // final float s = this.pokemobCap.getSize();
        // final double dist = Math.max(this.pokemobCap.getPokedexEntry().width
        // * s, this.pokemobCap
        // .getPokedexEntry().length * s) + 8;
        // Vec3d toMove = pos;
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
        // final Vec3d posA = p.getPositionVec();
        // p.move(typeIn, pos);
        // partsOnGround = partsOnGround || p.onGround;
        // final Vec3d posB = p.getPositionVec();
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
