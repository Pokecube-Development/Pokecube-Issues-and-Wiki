package thut.api.entity.blockentity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import thut.api.entity.blockentity.block.TempBlock;
import thut.api.entity.blockentity.block.TempTile;
import thut.api.entity.blockentity.world.IBlockEntityWorld;
import thut.api.entity.blockentity.world.WorldEntity;
import thut.api.item.ItemList;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;
import thut.crafts.ThutCrafts;

public abstract class BlockEntityBase extends Entity implements IEntityAdditionalSpawnData, IBlockEntity
{
    public static class BlockEntityType<T extends BlockEntityBase> extends EntityType<T>
    {

        public BlockEntityType(final EntityType.EntityFactory<T> factory)
        {
            super(factory, MobCategory.MISC, true, false, true, true, ImmutableSet.of(),
                    new EntityDimensions(1, 1, true), 64, 1);
        }

        @Override
        public T customClientSpawn(final SpawnEntity packet, final Level world)
        {
            return this.create(world);
        }
    }

    private static class VecSer implements EntityDataSerializer<Optional<Vec3>>
    {
        @Override
        public Optional<Vec3> copy(final Optional<Vec3> value)
        {
            if (value.isPresent()) return Optional.of(new Vec3(value.get().x, value.get().y, value.get().z));
            return Optional.empty();
        }

        @Override
        public EntityDataAccessor<Optional<Vec3>> createAccessor(final int id)
        {
            return new EntityDataAccessor<>(id, this);
        }

        @Override
        public Optional<Vec3> read(final FriendlyByteBuf buf)
        {
            if (!buf.isReadable()) return Optional.empty();
            return Optional.of(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }

        @Override
        public void write(final FriendlyByteBuf buf, final Optional<Vec3> opt)
        {
            if (opt.isPresent())
            {
                var value = opt.get();
                buf.writeDouble(value.x);
                buf.writeDouble(value.y);
                buf.writeDouble(value.z);
            }
        }
    }

    public static final EntityDataSerializer<Optional<Vec3>> VEC3DSER = new VecSer();

    static final EntityDataAccessor<Optional<Vec3>> velocity = SynchedEntityData
            .<Optional<Vec3>>defineId(BlockEntityBase.class, BlockEntityBase.VEC3DSER);
    static final EntityDataAccessor<Optional<Vec3>> position = SynchedEntityData
            .<Optional<Vec3>>defineId(BlockEntityBase.class, BlockEntityBase.VEC3DSER);

    public BlockPos boundMin = BlockPos.ZERO;
    public BlockPos boundMax = BlockPos.ZERO;

    private IBlockEntityWorld fake_world;

    private final boolean shouldRevert = true;

    protected float speedUp = 0.5f;
    protected float speedDown = -0.5f;
    protected float speedHoriz = 0.5f;
    protected float acceleration = 0.05f;

    public boolean toMoveY = false;
    public boolean toMoveX = false;
    public boolean toMoveZ = false;

    public UUID owner;

    public List<AABB> blockBoxes = Lists.newArrayList();
    public BlockState[][][] blocks = null;
    public BlockEntity[][][] tiles = null;

    public BlockEntityUpdater collider;
    BlockEntityInteractHandler interacter;

    BlockPos originalPos = null;
    protected Vector3 F = new Vector3();

    public BlockEntityBase(final EntityType<? extends BlockEntityBase> type, final Level par1World)
    {
        super(type, par1World);
        this.noCulling = true;
        this.invulnerableTime = 0;
        this.noPhysics = true;
    }

    protected Vector3 getForceDirection()
    {
        return F;
    }

    @Override
    public BlockPos getOriginalPos()
    {
        if (this.originalPos == null) this.originalPos = this.blockPosition();
        return this.originalPos;
    }

    public Vec3 getV()
    {
        return this.getEntityData().get(velocity).orElse(Vec3.ZERO);
    }

    /**
     * Applies a velocity to each of the entities pushing them away from each
     * other. Args: entity
     */
    @Override
    public void push(final Entity entity)
    {
        if (this.collider == null) this.collider = new BlockEntityUpdater(this);
    }

    @Override
    /** Applies the given player interaction to this Entity. */
    public InteractionResult interactAt(final Player player, final Vec3 vec, final InteractionHand hand)
    {
        return super.interactAt(player, vec, hand);
    }

    /** Applies the given player interaction to this Entity. */
    public InteractionResult interactAtFromTile(final Player player, final Vec3 vec, final InteractionHand hand)
    {
        if (this.interacter == null) this.interacter = this.createInteractHandler();
        try
        {
            return this.interacter.applyPlayerInteraction(player, vec, player.getItemInHand(hand), hand);
        }
        catch (final Exception e)
        {
            ThutCore.LOGGER.error("Error handling interactions for " + this, e);
            return super.interactAt(player, vec, hand);
        }
    }

    @Override
    /** Called when the entity is attacked. */
    public boolean hurt(final DamageSource source, final float amount)
    {
        return false;
    }

    /**
     * Returns true if other Entities should be prevented from moving through
     * this Entity.
     */
    @Override
    public boolean isPickable()
    {
        return false;
    }

    public boolean isServerWorld()
    {
        return !this.level.isClientSide;
    }

    /**
     * Returns true if this entity should push and be pushed by other entities
     * when colliding.
     */
    @Override
    public boolean isPushable()
    {
        return false;
    }

    /**
     * This is here to prevent teleport packet processing in vanilla updates.
     */
    @Override
    public boolean isControlledByLocalInstance()
    {
        return true;
    }

    @Override
    public boolean displayFireAnimation()
    {
        return false;
    }

    abstract protected boolean checkAccelerationConditions();

    public void checkCollision()
    {
        BlockPos.betweenClosedStream(this.getBoundingBox()).forEach(p -> {
            final Level world = this.getLevel();
            final BlockState block = world.getBlockState(p);

            ResourceLocation replaceable = new ResourceLocation("thutcore:craft_replace");
            boolean air = block.isAir();

            boolean isReplaceable = air || ItemList.is(replaceable, block);

            if (isReplaceable && block.getBlock() != ThutCrafts.CRAFTBLOCK.get())
            {
                final boolean flag = world.getFluidState(p).getType() == Fluids.WATER;
                if (!air) world.destroyBlock(p, true);
                world.setBlockAndUpdate(p,
                        ThutCrafts.CRAFTBLOCK.get().defaultBlockState().setValue(TempBlock.WATERLOGGED, flag));
            }
            final BlockEntity te = world.getBlockEntity(p);
            if (te instanceof TempTile tile)
            {
                tile.blockEntity = this;
                tile.getShape();
            }
        });
    }

    public void onEntityCollision(final Entity entityIn)
    {}

    abstract protected BlockEntityInteractHandler createInteractHandler();

    @Override
    public Packet<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public final void doMotion()
    {
        if (this.isServerWorld())
        {
            Vec3 v = this.getV().scale(0.8);
            Vector3 a = this.getForceDirection();
            // Just cancel velocity in this case.
            if (a.magSq() < 0.05)
            {
                this.move(MoverType.SELF, a.toVec3d());
                this.setV(v = Vec3.ZERO);
                this.getEntityData().set(position, Optional.of(this.position()));
            }
            else
            {
                double vh = this.getSpeedHoriz();
                double v_x = getSpeed(this.getX(), this.getX() + a.x, v.x(), vh, vh);
                double v_y = getSpeed(this.getY(), this.getY() + a.y, v.y(), this.getSpeedUp(), this.getSpeedDown());
                double v_z = getSpeed(this.getZ(), this.getZ() + a.z, v.z(), vh, vh);
                this.setV(v = new Vec3(v_x, v_y, v_z));
            }
            if (v.lengthSqr() > 0)
            {
                this.move(MoverType.SELF, v);
            }
            else
            {
                this.getEntityData().set(position, Optional.of(this.position()));
            }
        }
        else
        {
            Optional<Vec3> rOpt = this.getEntityData().get(position);
            if (!rOpt.isPresent()) return;
            Vec3 r_1 = rOpt.get();
            Vec3 r_0 = this.position();
            Vec3 v = r_1.subtract(r_0);
            this.getEntityData().set(velocity, Optional.of(v));
            if (v.lengthSqr() > 0)
            {
                this.move(MoverType.SELF, v);
            }
        }
    }

    protected void setV(final Vec3 vec)
    {
        if (this.isServerWorld())
        {
            this.getEntityData().set(velocity, Optional.of(vec));
            Vec3 r = this.position();
            Vec3 v = vec;
            Vec3 a = F.normalize().scalarMult(this.getAccel()).toVec3d();
            this.getEntityData().set(position, Optional.of(r.add(v).add(a)));
        }
    }

    protected double getSpeed(final double pos, final double destPos, final double speed, double speedPos,
            double speedNeg)
    {
        speedPos = Math.abs(speedPos);
        speedNeg = Math.abs(speedNeg);
        double dr_dt = speed;
        final double dr = destPos - pos;
        final float dr_dt2 = this.getAccel();
        final double t_toStop = Math.abs(dr_dt / dr_dt2);
        final double stop_distance = dr_dt * t_toStop;

        if (dr > 0)
        {
            if (dr_dt <= 0)
            {
                dr_dt += dr_dt2;
                return Math.min(dr_dt, speedPos);
            }
            final boolean tooFast = stop_distance > dr;
            final boolean tooSlow = dr_dt < speedPos;
            if (tooFast) dr_dt -= dr_dt2;
            else if (tooSlow) dr_dt += dr_dt2;
            return Math.min(dr_dt, speedPos);
        }
        if (dr < 0)
        {
            if (dr_dt >= 0)
            {
                dr_dt -= dr_dt2;
                return Math.max(dr_dt, -speedNeg);
            }
            final boolean tooFast = stop_distance > -dr;
            final boolean tooSlow = dr_dt > -speedNeg;
            if (tooFast) dr_dt += dr_dt2;
            else if (tooSlow) dr_dt -= dr_dt2;
            return Math.max(dr_dt, -speedNeg);
        }
        return 0;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots()
    {
        return Lists.newArrayList();
    }

    @Override
    public BlockState[][][] getBlocks()
    {
        return this.blocks;
    }

    /**
     * Checks if the entity's current position is a valid location to spawn this
     * entity.
     */
    public boolean getCanSpawnHere()
    {
        return false;
    }

    @Override
    public IBlockEntityWorld getFakeWorld()
    {
        if (this.fake_world == null)
        {
            this.fake_world = new WorldEntity(this.level);
            this.fake_world.setBlockEntity(this);
        }
        return this.fake_world;
    }

    @Override
    public BlockEntityInteractHandler getInteractor()
    {
        if (this.interacter == null) this.interacter = this.createInteractHandler();
        return this.interacter;
    }

    @Override
    public BlockPos getMax()
    {
        return this.boundMax;
    }

    @Override
    public BlockPos getMin()
    {
        return this.boundMin;
    }

    @Override
    public BlockEntity[][][] getTiles()
    {
        return this.tiles;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldRenderAtSqrDistance(final double distance)
    {
        return true;
    }

    abstract protected void onGridAlign();

    abstract protected void preColliderTick();

    /** First layer of player interaction */
    @Override
    public InteractionResult interact(final Player player, final InteractionHand hand)
    {
        if (this.interacter == null) this.interacter = this.createInteractHandler();
        return this.interacter.processInitialInteract(player, player.getItemInHand(hand), hand);
    }

    @Override
    public void readAdditionalSaveData(final CompoundTag nbt)
    {
        if (nbt.contains("bounds"))
        {
            final CompoundTag bounds = nbt.getCompound("bounds");
            this.boundMin = new BlockPos(bounds.getDouble("minx"), bounds.getDouble("miny"), bounds.getDouble("minz"));
            this.boundMax = new BlockPos(bounds.getDouble("maxx"), bounds.getDouble("maxy"), bounds.getDouble("maxz"));
            if (bounds.contains("orix")) this.originalPos = new BlockPos(bounds.getDouble("orix"),
                    bounds.getDouble("oriy"), bounds.getDouble("oriz"));
        }
        this.readBlocks(nbt);
    }

    public void readBlocks(final CompoundTag nbt)
    {
        if (nbt.contains("Blocks"))
        {
            final CompoundTag blockTag = nbt.getCompound("Blocks");
            int sizeX = blockTag.getInt("BlocksLengthX");
            int sizeZ = blockTag.getInt("BlocksLengthZ");
            int sizeY = blockTag.getInt("BlocksLengthY");
            if (sizeX == 0 || sizeZ == 0) sizeX = sizeZ = nbt.getInt("BlocksLength");
            if (sizeY == 0) sizeY = 1;
            this.blocks = new BlockState[sizeX][sizeY][sizeZ];
            this.tiles = new BlockEntity[sizeX][sizeY][sizeZ];
            for (int i = 0; i < sizeX; i++) for (int k = 0; k < sizeY; k++) for (int j = 0; j < sizeZ; j++)
            {
                final String name = "B" + i + "," + k + "," + j;
                if (!blockTag.contains(name)) continue;
                final BlockState state = NbtUtils.readBlockState(blockTag.getCompound(name));
                this.blocks[i][k][j] = state;
                if (blockTag.contains("T" + i + "," + k + "," + j)) try
                {
                    final CompoundTag tag = blockTag.getCompound("T" + i + "," + k + "," + j);
                    this.tiles[i][k][j] = BlockEntity.loadStatic(BlockPos.ZERO, state, tag);
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }
            }
            // Call these in this order so any appropriate changes can be made.
            this.setBlocks(this.blocks);
            this.setTiles(this.tiles);
        }
    }

    @Override
    public void refreshDimensions()
    {
        // if (this.collider != null)
        // this.setBoundingBox(this.collider.getBoundingBox());
    }

    @Override
    public AABB getBoundingBox()
    {
        AABB box = super.getBoundingBox();
        final BlockPos size = this.getSize();
        if (this.collider != null && (box.getXsize() != size.getX() + 1 || box.getYsize() != size.getY() + 1
                || box.getZsize() != size.getZ() + 1))
            box = this.collider.getBoundingBox();
        return box;
    }

    @Override
    protected AABB getBoundingBoxForPose(final Pose pose)
    {
        return this.getBoundingBox();
    }

    @Override
    public void readSpawnData(final FriendlyByteBuf data)
    {
        this.readAdditionalSaveData(data.readNbt());
    }

    @Override
    protected void defineSynchedData()
    {
        this.entityData.define(position, Optional.empty());
        this.entityData.define(velocity, Optional.empty());
    }

    /** Will get destroyed next tick. */
    @Override
    public void remove(final RemovalReason reason)
    {
        if (!this.getLevel().isClientSide && this.isAlive() && this.shouldRevert)
            IBlockEntity.BlockEntityFormer.RevertEntity(this);
        super.remove(reason);
    }

    @Override
    public void setBlocks(final BlockState[][][] blocks)
    {
        this.blocks = blocks;
    }

    @Override
    public void setFakeWorld(final IBlockEntityWorld world)
    {
        this.fake_world = world;
    }

    @Override
    public void setMax(final BlockPos pos)
    {
        this.boundMax = pos;
    }

    @Override
    public void setMin(final BlockPos pos)
    {
        this.boundMin = pos;
    }

    @Override
    public void setPos(final double x, final double y, final double z)
    {
        this.setPosRaw(x, y, z);
    }

    @Override
    public void setTiles(final BlockEntity[][][] tiles)
    {
        this.tiles = tiles;
    }

    @Override
    public void tick()
    {
        super.tick();
        if (this.getBlocks() == null) return;
        if (!this.isAddedToWorld()) this.onAddedToWorld();
        if (this.collider == null) this.collider = new BlockEntityUpdater(this);
        this.setBoundingBox(this.collider.getBoundingBox());
        this.yRot = 0;
        this.xRot = 0;
        this.preColliderTick();
        this.collider.onUpdate();
        this.doMotion();
        this.checkCollision();
    }

    @Override
    public void addAdditionalSaveData(final CompoundTag nbt)
    {
        final CompoundTag vector = new CompoundTag();
        vector.putDouble("minx", this.boundMin.getX());
        vector.putDouble("miny", this.boundMin.getY());
        vector.putDouble("minz", this.boundMin.getZ());
        vector.putDouble("maxx", this.boundMax.getX());
        vector.putDouble("maxy", this.boundMax.getY());
        vector.putDouble("maxz", this.boundMax.getZ());
        vector.putDouble("orix", this.getOriginalPos().getX());
        vector.putDouble("oriy", this.getOriginalPos().getY());
        vector.putDouble("oriz", this.getOriginalPos().getZ());
        nbt.put("bounds", vector);
        try
        {
            this.writeBlocks(nbt);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }

    public void writeBlocks(final CompoundTag nbt)
    {
        if (this.blocks != null)
        {
            final CompoundTag blocksTag = new CompoundTag();
            blocksTag.putInt("BlocksLengthX", this.blocks.length);
            blocksTag.putInt("BlocksLengthY", this.blocks[0].length);
            blocksTag.putInt("BlocksLengthZ", this.blocks[0][0].length);
            final int sizeX = this.blocks.length;
            final int sizeY = this.blocks[0].length;
            final int sizeZ = this.blocks[0][0].length;
            for (int i = 0; i < sizeX; i++) for (int k = 0; k < sizeY; k++) for (int j = 0; j < sizeZ; j++)
            {
                final BlockState b = this.blocks[i][k][j];
                if (b == null) continue;
                blocksTag.put("B" + i + "," + k + "," + j, NbtUtils.writeBlockState(b));
                try
                {
                    if (this.tiles[i][k][j] != null)
                    {
                        CompoundTag tag = this.tiles[i][k][j].saveWithFullMetadata();
                        blocksTag.put("T" + i + "," + k + "," + j, tag);
                    }
                }
                catch (final Exception e)
                {
                    e.printStackTrace();
                }
            }
            nbt.put("Blocks", blocksTag);
        }
    }

    public float getSpeedUp()
    {
        return this.speedUp;
    }

    public float getSpeedDown()
    {
        return this.speedDown;
    }

    public float getSpeedHoriz()
    {
        return this.speedHoriz;
    }

    public float getAccel()
    {
        return this.acceleration;
    }

    @Override
    public void writeSpawnData(final FriendlyByteBuf data)
    {
        final CompoundTag tag = new CompoundTag();
        this.addAdditionalSaveData(tag);
        data.writeNbt(tag);
    }
}
