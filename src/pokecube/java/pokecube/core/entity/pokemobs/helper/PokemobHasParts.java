package pokecube.core.entity.pokemobs.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.server.level.ServerLevel;
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
import org.joml.Vector3f;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.core.PokecubeCore;
import thut.api.entity.multipart.BBPartEntity;
import thut.api.entity.multipart.BBPartEntity.Factory;
import thut.api.entity.multipart.IBBPartMultipart;
import thut.api.world.WorldTickManager;
import thut.core.client.render.bbmodel.BBModel;
import thut.core.common.network.PartSync;

public abstract class PokemobHasParts extends PokemobCombat implements IBBPartMultipart<PokemobPart, PokemobHasParts>
{
    private PartHolder<PokemobPart> parts;

    private final List<PokemobPart> lowerList = Lists.newArrayList();

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

    protected void initSizes(final float size, boolean forceAdd)
    {
        final PokedexEntry entry = this.getPokemob().getPokedexEntry();
        entry.onResourcesReloaded();

        // final List<PokemobPart> allParts = this.allParts;
        // We need to here send a packet to sync the IDs of the new parts vs the
        // old parts.
        for (var part : getAllParts())
        {
            part.remove(RemovalReason.DISCARDED);
        }

        getHolder().clear();
        lowerList.clear();

        final float maxH = this.maxH();
        final float maxW = this.maxW();
        float width = entry.getWidth() * size;
        float length = entry.getLength() * size;
        float height = entry.getHeight() * size;

        colWidth = width;
        colHeight = height;

        boolean subDivide = height > maxH || width > maxW || length > maxW || getPokemob().isPlayerOwned() || forceAdd;

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
        this.dimensions = EntityDimensions.fixed(Math.max(width, length), height).withEyeHeight(0.75f * height);

        final boolean first = this.firstTick;
        this.firstTick = true;
        this.refreshDimensions();
        this.firstTick = first;
        if (this.level instanceof ServerLevel)
        {
            WorldTickManager.scheduleTask(this.level, () -> {
                if (this.isAddedToLevel()) PartSync.sendUpdate(weSelf());
            });
        }
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
    public void initParts(boolean fromPacket)
    {
        float size = this.getScale();
        this.initSizes(size, fromPacket);
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
    public void setPose(Pose pose)
    {
        // NO-OP, we handle pose differently
        //        super.setPose(pose);
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose)
    {
        if (!this.isMultipartEntity()) return super.getDefaultDimensions(pose);
        return this.dimensions.scale(1 / this.getScale());
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
            if (colWidth * colHeight > 190)
            {
                // Throttle warning to once per 5s
                if (this.tickCount % 100 == 0) PokecubeAPI.LOGGER.warn("Warning, {} is too big!", this);
                colHeight = Math.min(9, colHeight);
                colWidth = Math.min(21, colWidth);
            }
        }

        final EntityEvent.Size sizeEvent = EventHooks.getEntitySizeForge(this, pose, this.getDimensions(pose));
        final EntityDimensions entitysize1 = sizeEvent.getNewSize();
        this.dimensions = entitysize1;
        this.fixupDimensions();
        double dx = entitysize1.width() / 2.0D;
        double dz = dx;
        double dh = entitysize1.height();
        if (containing != null)
        {
            dh = containing.getYsize();
            dx = containing.getXsize() / 2;
            dz = containing.getZsize() / 2;
        }
        this.setBoundingBox(
                new AABB(this.getX() - dx, this.getY(), this.getZ() - dz, this.getX() + dx, this.getY() + dh,
                        this.getZ() + dz));
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
        colHeight = this.dimensions.height();
        colWidth = this.dimensions.width();
        super.aiStep();
    }

    protected float colWidth = 0;
    protected float colHeight = 0;

    @Override
    public void move(final MoverType typeIn, Vec3 velocity)
    {
        var useParts = getUseParts();
        if (useParts.isEmpty())
        {
            super.move(typeIn, velocity);
            return;
        }

        boolean horizontalCollision = false;
        boolean minorHorizontalCollision = false;
        boolean verticalCollision = false;
        boolean verticalCollisionBelow = false;

        Vector3f subV = new Vector3f();
        double stepUpAmount = 0;
        Vector3f allowed = null;
        // Check parts for collision
        for (PokemobPart part : useParts)
        {
            var dr = part.limitMove(typeIn, velocity);
            if (allowed == null) allowed = dr;
            else
            {
                subV.set(dr.x, dr.y, dr.z);
                // Can't use "min" as we need a "minAbs"
                if (Math.abs(subV.x) < Math.abs(allowed.x)) allowed.x = subV.x;
                if (Math.abs(subV.y) < Math.abs(allowed.y)) allowed.y = subV.y;
                if (Math.abs(subV.z) < Math.abs(allowed.z)) allowed.z = subV.z;

                // If any step up, allow that as a step
                if (velocity.y < 0 && dr.y > 0)
                {
                    stepUpAmount = Math.max(stepUpAmount, dr.y);
                }
            }
            horizontalCollision |= part.horizontalCollision;
            minorHorizontalCollision |= part.minorHorizontalCollision;
            verticalCollision |= part.verticalCollision;
            verticalCollisionBelow |= part.verticalCollisionBelow;
        }

        velocity = new Vec3(allowed.x, allowed.y, allowed.z);

        // Next apply it to us to actually shift hitbox.
        this.noPhysics = true;
        super.move(typeIn, velocity.add(0, stepUpAmount, 0));
        this.noPhysics = false;

        // Then set the boolean flags calculated
        this.horizontalCollision = horizontalCollision;
        this.minorHorizontalCollision = minorHorizontalCollision;
        this.verticalCollision = verticalCollision;
        this.verticalCollisionBelow = verticalCollisionBelow;
        this.setOnGroundWithMovement(verticalCollisionBelow, velocity);
    }

    @Override
    public void updatePartsPos()
    {
        var parts = getUseParts();
        IBBPartMultipart.super.updatePartsPos();
        if (parts != getUseParts() || (!parts.isEmpty() && lowerList.isEmpty()))
        {
            this.lowerList.clear();
            if (this.getUseParts().size() < 25)
            {
                this.lowerList.addAll(this.getUseParts());
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
