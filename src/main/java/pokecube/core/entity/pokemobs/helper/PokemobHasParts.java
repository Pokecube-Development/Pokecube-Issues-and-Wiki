package pokecube.core.entity.pokemobs.helper;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.pokedex.PokedexEntryLoader.BodyNode;
import pokecube.core.database.pokedex.PokedexEntryLoader.BodyPart;
import pokecube.core.interfaces.pokemob.ai.CombatStates;
import thut.api.AnimatedCaps;
import thut.api.entity.IAnimated;
import thut.api.maths.vecmath.Matrix3f;
import thut.api.maths.vecmath.Vector3f;

public abstract class PokemobHasParts extends PokemobCombat
{
    private PokemobPart[] parts;

    public List<PokemobPart> allParts = Lists.newArrayList();

    float colWidth = 0;
    float colHeight = 0;

    float last_size = 0;

    final Matrix3f rot = new Matrix3f();
    final Vector3f r = new Vector3f();

    int update_tick = 0;

    String effective_pose;

    Map<String, PokemobPart[]> partMap = Maps.newHashMap();

    public PokemobHasParts(final EntityType<? extends ShoulderRidingEntity> type, final Level worldIn)
    {
        super(type, worldIn);
        this.update_tick = -1;
        this.effective_pose = "";
    }

    private PokemobPart makePart(final BodyPart part, final float size, final Set<String> names)
    {
        final float dx = (float) (part.__pos__.x * size);
        final float dy = (float) (part.__pos__.y * size);
        final float dz = (float) (part.__pos__.z * size);

        final float sx = (float) (part.__size__.x * size);
        final float sy = (float) (part.__size__.y * size);
        final float sz = (float) (part.__size__.z * size);

        final float dw = Math.max(sx, sz);
        final float dh = sy;
        String name = part.name;
        int n = 0;
        while (names.contains(name)) name = part.name + n++;
        return new PokemobPart(this, dw, dh, dx, dy, dz, name);
    }

    private void addPart(final String key, final float size, final BodyNode node)
    {
        final PokemobPart[] parts = new PokemobPart[node.parts.size()];
        final Set<String> names = Sets.newHashSet();
        for (int i = 0; i < parts.length; i++)
        {
            parts[i] = this.makePart(node.parts.get(i), size, names);
            this.allParts.add(parts[i]);
        }
        this.partMap.put(key, parts);
    }

    protected void initSizes(final float size)
    {
        final PokedexEntry entry = this.pokemobCap.getPokedexEntry();

        // final List<PokemobPart> allParts = this.allParts;
        // We need to here send a packet to sync the IDs of the new parts vs the
        // old parts.
        // if (!this.getEntityWorld().isRemote) allParts =
        // Lists.newArrayList(allParts);

        this.allParts.clear();

        if (entry.poseShapes != null)
        {
            this.partMap.clear();
            for (final Entry<String, BodyNode> s : entry.poseShapes.entrySet())
                this.addPart(s.getKey(), size, s.getValue());
        }

        final float maxH = (float) PokecubeCore.getConfig().largeMobForSplit;
        final float maxW = (float) PokecubeCore.getConfig().largeMobForSplit;
        float width = entry.width * size;
        float length = entry.length * size;
        float height = entry.height * size;

        this.colWidth = width;
        this.colHeight = height;

        if (height > maxH || width > maxW || length > maxW)
        {
            final int nx = Mth.ceil(width / maxW);
            final int nz = Mth.ceil(length / maxH);
            final int ny = Mth.ceil(height / maxW);

            final float dx = width / nx;
            final float dy = height / ny;
            final float dz = length / nz;

            this.parts = new PokemobPart[nx * ny * nz];
            final float dw = Math.max(width / nx, length / nz);
            final float dh = dy;
            int i = 0;

            for (int y = 0; y < ny; y++) for (int x = 0; x < nx; x++) for (int z = 0; z < nz; z++)
            {
                this.parts[i] = new PokemobPart(this, dw, dh, x * dx - nx * dx / 2f, y * dy, z * dz - nz * dz / 2f,
                        "part_" + i);
                this.allParts.add(this.parts[i]);
                i++;
            }

            this.colWidth = Math.min(1, maxW);
            this.colHeight = Math.min(1, maxH);
            this.noCulling = true;
        }
        else
        {
            this.parts = new PokemobPart[0];
            this.noCulling = false;
        }
        if (!this.partMap.containsKey("idle")) this.partMap.put("idle", this.parts);

        float minX = 0;
        float minY = 0;
        float minZ = 0;
        float maxX = 0;
        float maxY = 0;
        float maxZ = 0;
        int n = 0;
        for (final PokemobPart[] parts : this.partMap.values()) for (final PokemobPart part : parts)
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
        // if (!this.getEntityWorld().isRemote)
        // PacketPartsUpdate.sendUpdate(this, allParts, this.allParts);
        // This needs the larger bounding box regardless of parts, so that the
        // lookup finds the parts at all for things like projectile impact
        // calculations.
        this.dimensions = EntityDimensions.fixed(Math.max(width, length), height);
        final boolean first = this.firstTick;
        this.firstTick = true;
        this.refreshDimensions();
        this.firstTick = first;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double dr2)
    {
        double d0 = this.pokemobCap.getMobSizes().magSq();
        if (Double.isNaN(d0))
        {
            d0 = 1.0D;
        }
        d0 = Math.min(d0,  9);
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
    public boolean isMultipartEntity()
    {
        if (this.parts == null) this.initSizes(this.pokemobCap.getSize());
        return this.parts.length > 0;
    }

    @Override
    public PokemobPart[] getParts()
    {
        // This only does something complex if the parts have changed, otherwise
        // it just ensures their locations are synced to us.
        if (this.update_tick != this.tickCount)
        {
            this.update_tick = this.tickCount;
            this.updatePartsPos();
        }

        if (!this.isAddedToWorld()) return this.allParts.toArray(new PokemobPart[0]);

        return this.parts;
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
    public boolean attackFromPart(final PokemobPart pokemobPart, final DamageSource source, final float amount)
    {
        return super.hurt(source, amount);
    }

    @Override
    public boolean hurt(final DamageSource source, final float amount)
    {
        if (this.isMultipartEntity()) return false;
        return super.hurt(source, amount);
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
        return this == entityIn || entityIn instanceof PokemobPart && ((PokemobPart) entityIn).base == this;
    }

    @Override
    public void aiStep()
    {
        this.updatePartsPos();
        super.aiStep();
    }

    protected void updatePartsPos()
    {
        float size = this.pokemobCap.getSize();
        if (this.pokemobCap.getCombatState(CombatStates.DYNAMAX)) size = 5 / this.pokemobCap.getPokedexEntry().height;
        this.initSizes(size);

        // check if effective_pose needs updating
        final IAnimated holder = AnimatedCaps.getAnimated(this);
        if (holder != null)
        {
            final List<String> anims = holder.getChoices();
            this.effective_pose = "idle";
            for (final String s : anims) if (this.partMap.containsKey(s))
            {
                this.effective_pose = s;
                break;
            }
            // Update the partmap if we know about this pose.
            if (this.partMap.containsKey(this.effective_pose)) this.parts = this.partMap.get(this.effective_pose);
        }

        if (this.parts.length == 0 && this.allParts.isEmpty()) return;

        final Vec3 v = this.position();
        this.r.set((float) v.x(), (float) v.y(), (float) v.z());
        final Vec3 dr = new Vec3(this.r.x - this.xOld, this.r.y - this.yOld, this.r.z - this.zOld);
        this.rot.rotY((float) Math.toRadians(180 - this.yBodyRot));

        if (this.isAddedToWorld()) for (final PokemobPart p : this.parts) p.update(this.rot, this.r, dr);
        else for (final PokemobPart p : this.allParts) p.update(this.rot, this.r, dr);
    }

    @Override
    public void move(final MoverType typeIn, final Vec3 pos)
    {
        if (this.parts.length == 0)
        {
            super.move(typeIn, pos);
            return;
        }
        final EntityDimensions backup = this.dimensions;
        this.dimensions = new EntityDimensions(this.colWidth, this.colHeight, true);

        final boolean first = this.firstTick;
        this.firstTick = true;
        this.refreshDimensions();
        this.firstTick = first;
        super.move(typeIn, pos);

        final BlockPos down = this.getBlockPosBelowThatAffectsMyMovement();
        final VoxelShape s = this.level.getBlockState(down).getCollisionShape(this.level, down).move(down.getX(),
                down.getY(), down.getZ());
        final double tol = -1e-3;
        final double d = s.collide(Axis.Y, this.getBoundingBox(), tol);
        if (d != tol) this.setOnGround(true);

        this.dimensions = backup;
        this.firstTick = true;
        this.refreshDimensions();
        this.firstTick = first;
    }

    @Override
    public float getBbHeight()
    {
        return this.colHeight;
    }

    @Override
    public float getBbWidth()
    {
        return this.colWidth;
    }

    // ================= Pose Related =====================

    ImmutableList<Pose> poses = ImmutableList.copyOf(Pose.values());

    @Override
    public ImmutableList<Pose> getDismountPoses()
    {
        return this.poses;
    }

    @Override
    public AABB getLocalBoundsForPose(final Pose pose)
    {
        return super.getLocalBoundsForPose(pose);
    }

    @Override
    public void setPose(final Pose poseIn)
    {
        super.setPose(poseIn);
    }

    @Override
    public EntityDimensions getDimensions(final Pose poseIn)
    {
        return this.dimensions;
    }
}
