package thut.api.entity.blockentity;

import java.util.List;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages.SpawnEntity;
import thut.api.entity.blockentity.block.TempTile;
import thut.api.entity.blockentity.world.IBlockEntityWorld;
import thut.api.entity.blockentity.world.WorldEntity;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

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

    private static class VecSer implements EntityDataSerializer<Vec3>
    {
        @Override
        public Vec3 copy(final Vec3 value)
        {
            return new Vec3(value.x, value.y, value.z);
        }

        @Override
        public EntityDataAccessor<Vec3> createAccessor(final int id)
        {
            return new EntityDataAccessor<>(id, this);
        }

        @Override
        public Vec3 read(final FriendlyByteBuf buf)
        {
            return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }

        @Override
        public void write(final FriendlyByteBuf buf, final Vec3 value)
        {
            buf.writeDouble(value.x);
            buf.writeDouble(value.y);
            buf.writeDouble(value.z);
        }
    }

    public static final EntityDataSerializer<Vec3> VEC3DSER = new VecSer();

    public static Block FAKEBLOCK = null;

    static final EntityDataAccessor<Vec3> velocity = SynchedEntityData.<Vec3>defineId(BlockEntityBase.class,
            BlockEntityBase.VEC3DSER);
    static final EntityDataAccessor<Vec3> position = SynchedEntityData.<Vec3>defineId(BlockEntityBase.class,
            BlockEntityBase.VEC3DSER);

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
    Vector3 lastSyncPos = Vector3.getNewVector();

    public BlockEntityBase(final EntityType<? extends BlockEntityBase> type, final Level par1World)
    {
        super(type, par1World);
        this.noCulling = true;
        this.invulnerableTime = 0;
        this.noPhysics = true;
    }

    abstract protected void accelerate();

    @Override
    public BlockPos getOriginalPos()
    {
        if (this.originalPos == null) this.originalPos = this.blockPosition();
        return this.originalPos;
    }

    /**
     * Applies a velocity to each of the entities pushing them away from each
     * other. Args: entity
     */
    @Override
    public void push(final Entity entity)
    {
        if (this.collider == null) this.collider = new BlockEntityUpdater(this);
        if (!entity.isPushable()) return;
        try
        {
            // this.collider.applyEntityCollision(entity);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
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
            final Level world = this.getCommandSenderWorld();
            final BlockState block = world.getBlockState(p);
            if (world.isEmptyBlock(p) && block.getBlock() != BlockEntityBase.FAKEBLOCK)
                world.setBlockAndUpdate(p, BlockEntityBase.FAKEBLOCK.defaultBlockState());
            final BlockEntity te = world.getBlockEntity(p);
            if (te instanceof TempTile)
            {
                final TempTile tile = (TempTile) te;
                tile.blockEntity = this;
                tile.getShape();
            }
        });
        final double v = this.getDeltaMovement().lengthSqr();
        if (v > 1e-5)
        {
            final List<Entity> mobs = this.level.getEntities(this, this.getBoundingBox().inflate(Math.sqrt(v) + 0.5));
            mobs.forEach(m -> this.onEntityCollision(m));

            if (this.getPersistentData().contains("__lift_cache__"))
            {
                CompoundTag tag = this.getPersistentData().getCompound("__lift_cache__");
                int tick = tag.getInt("t");
                int id = tag.getInt("i");
                Entity mob = this.level.getEntity(id);
                if (tickCount - tick < 2 && mob != null)
                {
                    Vec3 dr = new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
                    Vec3 v0 = this.getDeltaMovement();
                    Vec3 r1 = this.position().add(dr).add(v0);

                    if (mob.position().distanceTo(r1) > 0)
                    {
                        System.out.println(tickCount + " Missed it? " + (tickCount - tick));

                        mob.setPos(r1);
                        mob.setDeltaMovement(v0);
                    }
                }
            }
        }

    }

    public void onEntityCollision(final Entity entityIn)
    {
        boolean isPassenger = this.passengers.contains(entityIn);
        if (isPassenger) return;

        final VoxelShape shapeHere = this.collider.buildShape();
        if (shapeHere.isEmpty()) return;

        Vec3 v0 = this.getDeltaMovement();
        Vec3 v1 = entityIn.getDeltaMovement();
        Vec3 dv = v1.add(v0).scale(-1);

        Vec3 r0 = this.position();
        Vec3 r1 = entityIn.position();

        boolean serverSide = entityIn.getCommandSenderWorld().isClientSide;
        final boolean isPlayer = entityIn instanceof Player && !(entityIn instanceof Npc);
        if (isPlayer) serverSide = entityIn instanceof ServerPlayer;

        if (v0.y < 0)
        {
            entityIn.setDeltaMovement(v0);
        }
        else if (!(serverSide && isPlayer))
        {
            boolean valid = false;
            if (entityIn.getPersistentData().contains("__lift_cache__"))
            {
                CompoundTag tag = entityIn.getPersistentData().getCompound("__lift_cache__");
                int tick = tag.getInt("t");
                if (tickCount - tick < 5)
                {
                    Vec3 dr = new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
                    r1 = r0.add(dr).add(v0);
                    entityIn.setPos(r1);
                    entityIn.setDeltaMovement(v0);
                    tag.putInt("t", tickCount);
                    tag.putInt("i", entityIn.getId());
                    valid = true;
                    this.getPersistentData().put("__lift_cache__", tag);
                }
            }
            if (!valid)
            {
                AABB box = entityIn.getBoundingBox();
                AABB boxBefore = entityIn.getBoundingBox().move(dv);

                List<AABB> boxes = shapeHere.toAabbs();
                for (AABB box2 : boxes)
                {
                    AABB intersectA = box2.intersect(box);
                    AABB intersectB = box2.intersect(boxBefore);

                    boolean intersectsY = intersectA.getYsize() > intersectB.getYsize();
                    boolean above = (box.minY > box2.minY) || (boxBefore.minY > box2.minY);
                    boolean intersects = box2.intersects(box) || box2.intersects(boxBefore);

                    if (intersectsY && above && intersects)
                    {
                        entityIn.setPos(r1.x, box2.maxY, r1.z);
                        entityIn.setDeltaMovement(v0);
                        r1 = entityIn.position();
                        v1 = entityIn.getDeltaMovement();

                        Vec3 dr = r1.subtract(r0);

                        CompoundTag tag = new CompoundTag();
                        tag.putInt("t", tickCount);
                        tag.putDouble("x", dr.x);
                        tag.putDouble("y", dr.y);
                        tag.putDouble("z", dr.z);

                        entityIn.getPersistentData().put("__lift_cache__", tag);
                    }
                }
            }
        }
        // Player floating tick adjustments
        if (isPlayer && serverSide)
        {
            final ServerPlayer serverplayer = (ServerPlayer) entityIn;
            // Meed to set floatingTickCount to prevent being kicked
            serverplayer.connection.aboveGroundVehicleTickCount = 0;
            serverplayer.connection.aboveGroundTickCount = 0;
            serverplayer.fallDistance = 0;
        }
    }

    abstract protected BlockEntityInteractHandler createInteractHandler();

    @Override
    public Packet<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public final void doMotion()
    {
        final Vec3 v = this.getDeltaMovement();
        if (v.lengthSqr() > 0)
        {
            this.move(MoverType.SELF, v);
        }
        else this.setPos(this.position());
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
    public Vec3 getDeltaMovement()
    {
        return super.getDeltaMovement();
    }

    protected double getSpeed(final double pos, final double destPos, final double speed, double speedPos,
            double speedNeg)
    {
        speedPos = Math.abs(speedPos);
        speedNeg = Math.abs(speedNeg);
        double dr_dt = speed;
        final double dr = destPos - pos;
        final float dr_dt2 = this.getAccel();
        final double t_toStop = speed / dr_dt2;
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

    }

    /** Will get destroyed next tick. */
    @Override
    public void remove(final RemovalReason reason)
    {
        if (!this.getCommandSenderWorld().isClientSide && this.isAlive() && this.shouldRevert)
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
    public void setDeltaMovement(final Vec3 vec)
    {
        super.setDeltaMovement(vec);
    }

    @Override
    public void setPosRaw(final double x, final double y, final double z)
    {
        super.setPosRaw(x, y, z);
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

        this.accelerate();
        this.doMotion();

        this.checkCollision();
    }

    @Override
    public void onAddedToWorld()
    {
        super.onAddedToWorld();
    }

    @Override
    public void onRemovedFromWorld()
    {
        super.onRemovedFromWorld();
    }

    @SubscribeEvent
    public void onTickServer(WorldTickEvent event)
    {
        if (!this.isAddedToWorld())
        {
            ThutCore.LOGGER.error("Block Entity ticking when not in world!", new IllegalStateException());
            return;
        }
        if (event.phase != Phase.END || event.world != level) return;
//        System.out.println(event.world + " Test");
//        this.checkCollision();
    }

    @SubscribeEvent
    public void onTickClient(ClientTickEvent event)
    {
        if (!this.isAddedToWorld())
        {
            ThutCore.LOGGER.error("Block Entity ticking when not in world!", new IllegalStateException());
            return;
        }
        if (event.phase != Phase.END || this.isServerWorld()) return;
//        System.out.println(this.level + " Test");
//        this.checkCollision();
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
