package pokecube.core.entity.pokemobs.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityEvent;
import pokecube.api.data.PokedexEntry;
import pokecube.core.PokecubeCore;
import thut.api.entity.multipart.BBPartEntity;
import thut.api.entity.multipart.BBPartEntity.Factory;
import thut.api.entity.multipart.IBBPartMultipart;
import thut.core.client.render.bbmodel.BBModel;
import thut.core.common.network.PartSync;

public abstract class PokemobHasParts extends PokemobCombat implements IBBPartMultipart<PokemobPart, PokemobHasParts>
{
    private PartHolder<PokemobPart> parts;

    private final List<PokemobPart> lowerList = Lists.newArrayList();
    private final List<PokemobPart> upperList = Lists.newArrayList();

    public PokemobHasParts(final EntityType<? extends TamableAnimal> type, final Level worldIn)
    {
        super(type, worldIn);
    }

    protected BBPartEntity.Factory<PokemobPart, PokemobHasParts> factory;
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
            Map<String, List<PokemobPart>> partMap = Maps.newHashMap();
            this.parts = new PartHolder<>(allParts, partMap, new Holder<>());
            this.factory = PokemobPart::new;
        }
        return parts;
    }

    @Override
    public boolean isMultipartEntity()
    {
        if (this.getUseParts() == null) this.initParts();
        return !this.getUseParts().isEmpty();
    }

    List<PokemobPart> _lastParts;
    PokemobPart[] cache;

    @Override
    public PokemobPart[] getParts()
    {
        // This only does something complex if the parts have changed, otherwise
        // it just ensures their locations are synced to us.
        this.checkUpdateParts();
        List<PokemobPart> parts;
        if (!this.isAddedToLevel())
        {
            parts = this.getAllParts();
        }
        else parts = this.getUseParts();
        if (parts == null || parts.isEmpty()) return null;
        cache = parts == _lastParts ? cache : parts.toArray(new PokemobPart[0]);
        _lastParts = parts;
        return cache;
    }

    @Override
    public BBModel getBBModel()
    {
        return this.getPokemob().getPokedexEntry().bodyModel;
    }

    protected void initSizes(final float size)
    {
        final PokedexEntry entry = this.getPokemob().getPokedexEntry();

        // final List<PokemobPart> allParts = this.allParts;
        // We need to here send a packet to sync the IDs of the new parts vs the
        // old parts.
        for (var part : getAllParts())
        {
            part.remove(RemovalReason.DISCARDED);
        }

        getHolder().clear();
        upperList.clear();
        lowerList.clear();

        final float maxH = this.maxH();
        final float maxW = this.maxW();
        float width = entry.getWidth() * size;
        float length = entry.getLength() * size;
        float height = entry.getHeight() * size;

        colWidth = width;
        colHeight = height;

        boolean subDivide = height > maxH || width > maxW || length > maxW || getPokemob().isPlayerOwned();

        // Special handling for client side gui only mobs:
        subDivide = subDivide && (!level.isClientSide() || this.isAddedToLevel());

        if (entry.bodyModel != null && subDivide)
        {
            this.initFromBBModel();
        }
        else
        {
            if (subDivide)
            {
                this.trySubDivideParts(width, length, height);
                colWidth = Math.min(1, maxW);
                colHeight = Math.min(1, maxH);
            }
            else
            {
                getHolder().setParts(new ArrayList<>());
            }
        }

        AABB containing = null;
        for (final PokemobPart part : getHolder().allParts())
        {
            if (containing == null) containing = part.getBoundingBox();
            else containing = containing.minmax(part.getBoundingBox());
        }
        if (containing != null)
        {
            var dh2 = containing.getYsize();
            var dw2 = Math.max(containing.getXsize(), containing.getZsize());
            colWidth = (float) dw2;
            colHeight = (float) dh2;
        }
        // This needs the larger bounding box regardless of parts, so that the
        // lookup finds the parts at all for things like projectile impact
        // calculations.
        this.dimensions = EntityDimensions.fixed(Math.max(width, length), height);

        final boolean first = this.firstTick;
        this.firstTick = true;
        this.refreshDimensions();
        this.firstTick = first;
        if (this.isAddedToLevel()) PartSync.sendUpdate(weSelf());
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
        float size = this.getScale();
        this.initSizes(size);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double dr2)
    {
        double d0 = this.getPokemob().getMobSizes().magSq();
        if (Double.isNaN(d0))
        {
            d0 = 1.0D;
        }
        d0 = Math.min(d0, 9);
        d0 *= 4096.0D * getViewScale() * getViewScale();
        return dr2 < d0;
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose)
    {
        if (!this.isMultipartEntity()) return super.getDefaultDimensions(pose);
        return EntityDimensions.scalable(colWidth, colHeight);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void refreshDimensions()
    {
        if (!this.isMultipartEntity())
        {
            super.refreshDimensions();
            return;
        }
        Pose pose = this.getPose();
        // Vanilla hardcodes sleeping pose check inside the final getDimensions
        if (pose == Pose.SLEEPING) pose = Pose.STANDING;

        AABB containing = null;
        for (final PokemobPart part : getHolder().allParts())
        {
            if (containing == null) containing = part.getBoundingBox();
            else containing = containing.minmax(part.getBoundingBox());
        }
        if (containing != null)
        {
            var dh2 = containing.getYsize();
            var dw2 = Math.max(containing.getXsize(), containing.getZsize());
            colWidth = (float) dw2;
            colHeight = (float) dh2;
        }

        final EntityEvent.Size sizeEvent = EventHooks.getEntitySizeForge(this, pose, this.getDimensions(pose));
        final EntityDimensions entitysize1 = sizeEvent.getNewSize();
        this.dimensions = entitysize1;
        this.fixupDimensions();
        final double d0 = entitysize1.width() / 2.0D;
        if (containing != null) this.setBoundingBox(containing);
        else this.setBoundingBox(new AABB(this.getX() - d0, this.getY(), this.getZ() - d0, this.getX() + d0,
                this.getY() + entitysize1.height(), this.getZ() + d0));
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

    protected float colWidth = 0;
    protected float colHeight = 0;

    @Override
    public void move(final MoverType typeIn, Vec3 velocity)
    {
        if (getUseParts().isEmpty())
        {
            super.move(typeIn, velocity);
            return;
        }
        final EntityDimensions backup = this.dimensions;
        this.dimensions = EntityDimensions.fixed(colWidth, colHeight);

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
            onGround |= part.onGround();
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
            onGround |= part.onGround();
            verticalCollision |= part.verticalCollision;
        }

        // Finally apply it to us to actually shift hitbox.
        super.move(typeIn, velocity);

        this.horizontalCollision = horizontalCollision;
        this.minorHorizontalCollision = minorHorizontalCollision;
        this.setOnGround(onGround);
        this.verticalCollision = verticalCollision;

        this.dimensions = backup;
        this.firstTick = true;
        this.refreshDimensions();
        this.firstTick = first;
    }

    @Override
    public void updatePartsPos()
    {
        var parts = getUseParts();
        IBBPartMultipart.super.updatePartsPos();
        if (parts != getUseParts() || (!parts.isEmpty() && lowerList.isEmpty()))
        {
            this.upperList.clear();
            this.lowerList.clear();
            float minY = Float.MAX_VALUE;
            float maxY = Float.MIN_VALUE;
            for (PokemobPart part : getUseParts())
            {
                minY = (float) Math.min(minY, part.getY());
                maxY = (float) Math.max(maxY, part.getY());
            }
            for (PokemobPart part : getUseParts())
            {
                if (Math.abs(part.getY() - minY) < 0.5) this.lowerList.add(part);
                    // Only allow it to be in one list, prioritsing lower, these are
                    // just used for ordered collision checks anyway.
                else if (Math.abs(part.getY() - maxY) < 0.5) this.upperList.add(part);
            }
        }
    }
    // ================= Pose Related =====================

    ImmutableList<Pose> poses = ImmutableList.copyOf(Pose.values());

    @Override
    public ImmutableList<Pose> getDismountPoses()
    {
        return this.poses;
    }
}
