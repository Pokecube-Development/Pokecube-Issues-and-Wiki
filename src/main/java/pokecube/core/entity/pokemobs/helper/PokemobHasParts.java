package pokecube.core.entity.pokemobs.helper;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import pokecube.api.data.PokedexEntry;
import pokecube.core.PokecubeCore;
import thut.api.entity.multipart.GenericPartEntity;
import thut.api.entity.multipart.GenericPartEntity.BodyNode;
import thut.api.entity.multipart.GenericPartEntity.Factory;
import thut.api.entity.multipart.IMultpart;
import thut.core.common.network.PartSync;

public abstract class PokemobHasParts extends PokemobCombat implements IMultpart<PokemobPart, PokemobHasParts>
{

    protected GenericPartEntity.Factory<PokemobPart, PokemobHasParts> factory;
    private PartHolder<PokemobPart> parts;

    private List<PokemobPart> lowerList = Lists.newArrayList();
    private List<PokemobPart> upperList = Lists.newArrayList();

    public PokemobHasParts(final EntityType<? extends TamableAnimal> type, final Level worldIn)
    {
        super(type, worldIn);
    }

    @Override
    public Factory<PokemobPart, PokemobHasParts> getFactory()
    {
        return factory;
    }

    @Override
    public PartHolder<PokemobPart> getHolder()
    {
        if (parts == null)
        {
            List<PokemobPart> allParts = Lists.newArrayList();
            Map<String, PokemobPart[]> partMap = Maps.newHashMap();
            this.parts = new PartHolder<PokemobPart>(allParts, partMap, new Holder<PokemobPart>());
            this.factory = PokemobPart::new;
        }
        return parts;
    }

    @Override
    public boolean isMultipartEntity()
    {
        if (this.getHolder().getParts() == null) this.initParts();
        return this.getHolder().getParts().length > 0;
    }

    @Override
    public PokemobPart[] getParts()
    {
        // This only does something complex if the parts have changed, otherwise
        // it just ensures their locations are synced to us.
        this.checkUpdateParts();
        if (!this.isAddedToWorld())
        {
            return getHolder().makeAllParts(this.getPartClass());
        }
        return this.getHolder().getParts();
    }

    protected void initSizes(final float size)
    {
        final PokedexEntry entry = this.pokemobCap.getPokedexEntry();

        // final List<PokemobPart> allParts = this.allParts;
        // We need to here send a packet to sync the IDs of the new parts vs the
        // old parts.
        for (var part : getHolder().allParts())
        {
            part.remove(RemovalReason.DISCARDED);
        }

        getHolder().clear();
        upperList.clear();
        lowerList.clear();

        if (entry.poseShapes != null)
        {
            getHolder().partMap().clear();
            for (final Entry<String, BodyNode> s : entry.poseShapes.entrySet())
                this.addPart(s.getKey(), size, s.getValue());
        }

        final float maxH = this.maxH();
        final float maxW = this.maxW();
        float width = entry.width * size;
        float length = entry.length * size;
        float height = entry.height * size;

        getHolder().holder().colWidth = width;
        getHolder().holder().colHeight = height;

        boolean subDivide = height > maxH || width > maxW || length > maxW;

        // Special handling for client side gui only mobs:
        subDivide = subDivide && (!level.isClientSide() || this.isAddedToWorld());

        if (subDivide)
        {
            var split = this.splitToParts(width, height, length, 0, 0, 0);
            var parts = split.toArray(new PokemobPart[0]);

            getHolder().setParts(parts);
            for (var p : parts) getHolder().allParts().add(p);

            getHolder().holder().colWidth = Math.min(1, maxW);
            getHolder().holder().colHeight = Math.min(1, maxH);
        }
        else
        {
            getHolder().setParts(new PokemobPart[0]);
        }
        if (!getHolder().partMap().containsKey("idle")) getHolder().partMap().put("idle", getHolder().holder().parts);

        float minX = 0;
        float minY = 0;
        float minZ = 0;
        float maxX = 0;
        float maxY = 0;
        float maxZ = 0;
        int n = 0;
        for (final PokemobPart[] parts : getHolder().partMap().values()) for (final PokemobPart part : parts)
        {
            n++;
            minX = Math.min(minX, part.r0.x - part.width);
            minZ = Math.min(minZ, part.r0.z - part.width);
            minY = Math.min(minY, part.r0.y);
            maxX = Math.max(maxX, part.r0.x + part.width);
            maxZ = Math.max(maxZ, part.r0.z + part.width);
            maxY = Math.max(maxY, part.r0.y + part.height);
        }

        if (n != 0)
        {
            height = maxY - minY;
            width = maxX - minX;
            length = maxZ - minZ;
        }

        boolean subDivided = getHolder().getParts().length > 0;

        // This needs the larger bounding box regardless of parts, so that the
        // lookup finds the parts at all for things like projectile impact
        // calculations.
        if (subDivided)
        {
            width = Math.min(Math.max(width, length), maxW);
            height = Math.min(height, maxH);
            this.dimensions = EntityDimensions.fixed(width, height);
            this.noCulling = true;
        }
        else this.dimensions = EntityDimensions.fixed(Math.max(width, length), height);
        final boolean first = this.firstTick;
        this.firstTick = true;
        this.refreshDimensions();
        this.firstTick = first;
        if (this.isAddedToWorld()) PartSync.sendUpdate(weSelf());
    }

    @Override
    public float maxH()
    {
        return (float) PokecubeCore.getConfig().largeMobForSplit;
    }

    @Override
    public float maxW()
    {
        return (float) PokecubeCore.getConfig().largeMobForSplit;
    }

    @Override
    public void initParts()
    {
        float size = this.pokemobCap.getSize();
        this.initSizes(size);
    }

    @Override
    public Class<PokemobPart> getPartClass()
    {
        return PokemobPart.class;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double dr2)
    {
        double d0 = this.pokemobCap.getMobSizes().magSq();
        if (Double.isNaN(d0))
        {
            d0 = 1.0D;
        }
        d0 = Math.min(d0, 9);
        d0 *= 4096.0D * viewScale * viewScale;
        return dr2 < d0;
    }

    @Override
    public void refreshDimensions()
    {
        if (!this.isMultipartEntity())
        {
            super.refreshDimensions();
            return;
        }
        final EntityDimensions entitysize = this.dimensions;
        final Pose pose = this.getPose();
        final net.minecraftforge.event.entity.EntityEvent.Size sizeEvent = net.minecraftforge.event.ForgeEventFactory
                .getEntitySizeForge(this, pose, this.getDimensions(pose), this.getEyeHeight(pose, entitysize));
        final EntityDimensions entitysize1 = sizeEvent.getNewSize();
        this.dimensions = entitysize1;
        this.eyeHeight = sizeEvent.getNewEyeHeight();
        final double d0 = entitysize1.width / 2.0D;
        this.setBoundingBox(new AABB(this.getX() - d0, this.getY(), this.getZ() - d0, this.getX() + d0,
                this.getY() + entitysize1.height, this.getZ() + d0));
    }

    @Override
    public boolean isPickable()
    {
        if (this.isMultipartEntity()) return false;
        return super.isPickable();
    }

    @Override
    public boolean isPushable()
    {
        return !this.isMultipartEntity() && super.isPushable();
    }

    @Override
    protected void pushEntities()
    {
        if (!this.isMultipartEntity()) super.pushEntities();
    }

    @Override
    public void push(final Entity entityIn)
    {
        if (entityIn.is(this)) return;
        if (this.isMultipartEntity())
        {
            for (final PokemobPart part : this.getParts())
                if (part.getBoundingBox().intersects(entityIn.getBoundingBox())) part.push(entityIn);
        }
        else super.push(entityIn);
    }

    /**
     * Returns true if Entity argument is equal to this Entity
     */
    @Override
    public boolean is(final Entity entityIn)
    {
        return sameMob(entityIn);
    }

    @Override
    public void aiStep()
    {
        this.updatePartsPos();
        super.aiStep();
    }

    @Override
    public void move(final MoverType typeIn, Vec3 velocity)
    {
        if (getHolder().holder().parts.length == 0)
        {
            super.move(typeIn, velocity);
            return;
        }
        final EntityDimensions backup = this.dimensions;
        this.dimensions = new EntityDimensions(getHolder().holder().colWidth, getHolder().holder().colHeight, true);

        final boolean first = this.firstTick;
        this.firstTick = true;
        this.refreshDimensions();
        this.firstTick = first;

        boolean horizontalCollision = false;
        boolean minorHorizontalCollision = false;
        boolean onGround = false;
        boolean verticalCollision = false;

        // Check lower parts first (ground most likely to hit first and stop
        // motion)
        for (PokemobPart part : lowerList)
        {
            Vec3 before = part.position();
            part.move(typeIn, velocity);
            velocity = part.position().subtract(before);
            horizontalCollision |= part.horizontalCollision;
            minorHorizontalCollision |= part.minorHorizontalCollision;
            onGround |= part.onGround;
            verticalCollision |= part.verticalCollision;
        }
        // Then check upper parts
        for (PokemobPart part : upperList)
        {
            Vec3 before = part.position();
            part.move(typeIn, velocity);
            velocity = part.position().subtract(before);
            horizontalCollision |= part.horizontalCollision;
            minorHorizontalCollision |= part.minorHorizontalCollision;
            onGround |= part.onGround;
            verticalCollision |= part.verticalCollision;
        }

        // Finally apply it to us to actually shift hitbox.
        super.move(typeIn, velocity);

        this.horizontalCollision = horizontalCollision;
        this.minorHorizontalCollision = minorHorizontalCollision;
        this.onGround = onGround;
        this.verticalCollision = verticalCollision;

        this.dimensions = backup;
        this.firstTick = true;
        this.refreshDimensions();
        this.firstTick = first;
    }

    @Override
    public void updatePartsPos()
    {
        PokemobPart[] parts = getHolder().holder().parts;
        IMultpart.super.updatePartsPos();
        if (parts != getHolder().holder().parts || (parts.length > 0 && lowerList.isEmpty()))
        {
            this.upperList.clear();
            this.lowerList.clear();
            float minY = Float.MAX_VALUE;
            float maxY = Float.MIN_VALUE;
            for (PokemobPart part : getHolder().holder().parts)
            {
                minY = Math.min(minY, part.r0.y);
                maxY = Math.max(maxY, part.r0.y);
            }
            for (PokemobPart part : getHolder().holder().parts)
            {
                if (Math.abs(part.r0.y - minY) < 0.5) this.lowerList.add(part);
                // Only allow it to be in one list, prioritsing lower, these are
                // just used for ordered collision checks anyway.
                else if (Math.abs(part.r0.y - maxY) < 0.5) this.upperList.add(part);
            }
        }
    }

    @Override
    public float getBbHeight()
    {
        return getHolder().holder().colHeight;
    }

    @Override
    public float getBbWidth()
    {
        return getHolder().holder().colWidth;
    }

    // ================= Pose Related =====================

    ImmutableList<Pose> poses = ImmutableList.copyOf(Pose.values());

    @Override
    public ImmutableList<Pose> getDismountPoses()
    {
        return this.poses;
    }

    @Override
    public EntityDimensions getDimensions(final Pose poseIn)
    {
        return this.dimensions;
    }
}
