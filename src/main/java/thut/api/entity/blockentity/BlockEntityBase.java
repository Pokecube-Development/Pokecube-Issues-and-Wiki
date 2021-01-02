package thut.api.entity.blockentity;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages.SpawnEntity;
import net.minecraftforge.fml.network.NetworkHooks;
import thut.api.entity.blockentity.block.TempTile;
import thut.api.entity.blockentity.world.IBlockEntityWorld;
import thut.api.entity.blockentity.world.WorldEntity;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;

public abstract class BlockEntityBase extends Entity implements IEntityAdditionalSpawnData, IBlockEntity
{
    public static class BlockEntityType<T extends BlockEntityBase> extends EntityType<T>
    {

        public BlockEntityType(final EntityType.IFactory<T> factory)
        {
            super(factory, EntityClassification.MISC, true, false, true, true, ImmutableSet.of(), new EntitySize(1, 1,
                    true), 64, 1);
        }

        @Override
        public T customClientSpawn(final SpawnEntity packet, final World world)
        {
            return this.create(world);
        }

    }

    private static class VecSer implements IDataSerializer<Vector3d>
    {
        @Override
        public Vector3d copyValue(final Vector3d value)
        {
            return new Vector3d(value.x, value.y, value.z);
        }

        @Override
        public DataParameter<Vector3d> createKey(final int id)
        {
            return new DataParameter<>(id, this);
        }

        @Override
        public Vector3d read(final PacketBuffer buf)
        {
            return new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }

        @Override
        public void write(final PacketBuffer buf, final Vector3d value)
        {
            buf.writeDouble(value.x);
            buf.writeDouble(value.y);
            buf.writeDouble(value.z);
        }
    }

    public static final IDataSerializer<Vector3d> VEC3DSER = new VecSer();

    public static Block FAKEBLOCK = null;

    static final DataParameter<Vector3d> velocity = EntityDataManager.<Vector3d> createKey(BlockEntityBase.class,
            BlockEntityBase.VEC3DSER);
    static final DataParameter<Vector3d> position = EntityDataManager.<Vector3d> createKey(BlockEntityBase.class,
            BlockEntityBase.VEC3DSER);

    public BlockPos boundMin = BlockPos.ZERO;
    public BlockPos boundMax = BlockPos.ZERO;

    private IBlockEntityWorld fake_world;

    private boolean shouldRevert = true;

    protected float speedUp      = 0.5f;
    protected float speedDown    = -0.5f;
    protected float speedHoriz   = 0.5f;
    protected float acceleration = 0.05f;

    public boolean toMoveY = false;
    public boolean toMoveX = false;
    public boolean toMoveZ = false;

    public UUID owner;

    public List<AxisAlignedBB> blockBoxes = Lists.newArrayList();
    public BlockState[][][]    blocks     = null;
    public TileEntity[][][]    tiles      = null;

    public BlockEntityUpdater  collider;
    BlockEntityInteractHandler interacter;

    BlockPos originalPos = null;
    Vector3  lastSyncPos = Vector3.getNewVector();

    public BlockEntityBase(final EntityType<? extends BlockEntityBase> type, final World par1World)
    {
        super(type, par1World);
        this.ignoreFrustumCheck = true;
        this.hurtResistantTime = 0;
        this.noClip = true;
    }

    abstract protected void accelerate();

    @Override
    public BlockPos getOriginalPos()
    {
        if (this.originalPos == null) this.originalPos = this.getPosition();
        return this.originalPos;
    }

    /**
     * Applies a velocity to each of the entities pushing them away from each
     * other. Args: entity
     */
    @Override
    public void applyEntityCollision(final Entity entity)
    {
        if (this.collider == null) this.collider = new BlockEntityUpdater(this);
        if (!entity.canBePushed()) return;
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
    public ActionResultType applyPlayerInteraction(final PlayerEntity player, final Vector3d vec, final Hand hand)
    {
        if (this.interacter == null) this.interacter = this.createInteractHandler();
        try
        {
            return this.interacter.applyPlayerInteraction(player, vec, player.getHeldItem(hand), hand);
        }
        catch (final Exception e)
        {
            ThutCore.LOGGER.error("Error handling interactions for " + this, e);
            return super.applyPlayerInteraction(player, vec, hand);
        }
    }

    @Override
    /** Called when the entity is attacked. */
    public boolean attackEntityFrom(final DamageSource source, final float amount)
    {
        return false;
    }

    /**
     * Returns true if other Entities should be prevented from moving through
     * this Entity.
     */
    @Override
    public boolean canBeCollidedWith()
    {
        return this.isAlive();
    }

    public boolean isServerWorld()
    {
        return !this.world.isRemote;
    }

    /**
     * Returns true if this entity should push and be pushed by other entities
     * when colliding.
     */
    @Override
    public boolean canBePushed()
    {
        return false;
    }

    /**
     * This is here to prevent teleport packet processing in vanilla
     * updates.
     */
    @Override
    public boolean canPassengerSteer()
    {
        return true;
    }

    @Override
    public boolean canRenderOnFire()
    {
        return false;
    }

    abstract protected boolean checkAccelerationConditions();

    public void checkCollision()
    {
        BlockPos.getAllInBox(this.getBoundingBox().grow(1)).forEach(p ->
        {
            final World world = this.getEntityWorld();
            final BlockState block = world.getBlockState(p);
            if (world.isAirBlock(p) && block.getBlock() != BlockEntityBase.FAKEBLOCK) world.setBlockState(p,
                    BlockEntityBase.FAKEBLOCK.getDefaultState());
            final TileEntity te = world.getTileEntity(p);
            if (te instanceof TempTile)
            {
                final TempTile tile = (TempTile) te;
                tile.blockEntity = this;
                tile.getShape();
            }
        });
        // final List<?> list =
        // this.world.getEntitiesWithinAABBExcludingEntity(this,
        // this.getBoundingBox().grow(2));
        // if (list != null && !list.isEmpty())
        // {
        // if (list.size() == 1 && this.getRecursivePassengers() != null &&
        // !this.getRecursivePassengers().isEmpty())
        // return;
        // for (int i = 0; i < list.size(); ++i)
        // {
        // final Entity entity = (Entity) list.get(i);
        // this.applyEntityCollision(entity);
        // }
        // }
    }

    abstract protected BlockEntityInteractHandler createInteractHandler();

    @Override
    public IPacket<?> createSpawnPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public final void doMotion()
    {
        final Vector3d v = this.getMotion();
        this.move(MoverType.SELF, v);
    }

    @Override
    public Iterable<ItemStack> getArmorInventoryList()
    {
        return Lists.newArrayList();
    }

    @Override
    public BlockState[][][] getBlocks()
    {
        return this.blocks;
    }

    /**
     * Checks if the entity's current position is a valid location to spawn
     * this entity.
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
            this.fake_world = new WorldEntity(this.world);
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
    public Vector3d getMotion()
    {
        return this.getDataManager().get(BlockEntityBase.velocity);
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
    public TileEntity[][][] getTiles()
    {
        return this.tiles;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isInRangeToRenderDist(final double distance)
    {
        return true;
    }

    abstract protected void onGridAlign();

    abstract protected void preColliderTick();

    /** First layer of player interaction */
    @Override
    public ActionResultType processInitialInteract(final PlayerEntity player, final Hand hand)
    {
        if (this.interacter == null) this.interacter = this.createInteractHandler();
        return this.interacter.processInitialInteract(player, player.getHeldItem(hand), hand);
    }

    @Override
    public void readAdditional(final CompoundNBT nbt)
    {
        if (nbt.contains("bounds"))
        {
            final CompoundNBT bounds = nbt.getCompound("bounds");
            this.boundMin = new BlockPos(bounds.getDouble("minx"), bounds.getDouble("miny"), bounds.getDouble("minz"));
            this.boundMax = new BlockPos(bounds.getDouble("maxx"), bounds.getDouble("maxy"), bounds.getDouble("maxz"));
            if (bounds.contains("orix")) this.originalPos = new BlockPos(bounds.getDouble("orix"), bounds.getDouble(
                    "oriy"), bounds.getDouble("oriz"));
        }
        this.readBlocks(nbt);
    }

    public void readBlocks(final CompoundNBT nbt)
    {
        if (nbt.contains("Blocks"))
        {
            final CompoundNBT blockTag = nbt.getCompound("Blocks");
            int sizeX = blockTag.getInt("BlocksLengthX");
            int sizeZ = blockTag.getInt("BlocksLengthZ");
            int sizeY = blockTag.getInt("BlocksLengthY");
            if (sizeX == 0 || sizeZ == 0) sizeX = sizeZ = nbt.getInt("BlocksLength");
            if (sizeY == 0) sizeY = 1;
            this.blocks = new BlockState[sizeX][sizeY][sizeZ];
            this.tiles = new TileEntity[sizeX][sizeY][sizeZ];
            for (int i = 0; i < sizeX; i++)
                for (int k = 0; k < sizeY; k++)
                    for (int j = 0; j < sizeZ; j++)
                    {
                        final String name = "B" + i + "," + k + "," + j;
                        if (!blockTag.contains(name)) continue;
                        final BlockState state = NBTUtil.readBlockState(blockTag.getCompound(name));
                        this.blocks[i][k][j] = state;
                        if (blockTag.contains("T" + i + "," + k + "," + j)) try
                        {
                            final CompoundNBT tag = blockTag.getCompound("T" + i + "," + k + "," + j);
                            this.tiles[i][k][j] = TileEntity.readTileEntity(state, tag);
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
    public void recalculateSize()
    {
        // if (this.collider != null)
        // this.setBoundingBox(this.collider.getBoundingBox());
    }

    @Override
    public AxisAlignedBB getBoundingBox()
    {
        AxisAlignedBB box = super.getBoundingBox();
        final BlockPos size = this.getSize();
        if (this.collider != null && (box.getXSize() != size.getX() + 1 || box.getYSize() != size.getY() + 1 || box
                .getZSize() != size.getZ() + 1)) box = this.collider.getBoundingBox();
        return box;
    }

    @Override
    protected AxisAlignedBB getBoundingBox(final Pose pose)
    {
        return this.getBoundingBox();
    }

    @Override
    public void readSpawnData(final PacketBuffer data)
    {
        this.readAdditional(data.readCompoundTag());
    }

    @Override
    protected void registerData()
    {
        this.getDataManager().register(BlockEntityBase.velocity, Vector3d.ZERO);
        this.getDataManager().register(BlockEntityBase.position, Vector3d.ZERO);
    }

    /** Will get destroyed next tick. */
    @Override
    public void remove()
    {
        if (!this.getEntityWorld().isRemote && this.isAlive() && this.shouldRevert) IBlockEntity.BlockEntityFormer
                .RevertEntity(this);
        super.remove();
    }

    @Override
    public void remove(final boolean keepData)
    {
        this.shouldRevert = !keepData;
        super.remove(keepData);
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
    public void setMotion(final Vector3d vec)
    {
        this.getDataManager().set(BlockEntityBase.velocity, vec);
    }

    @Override
    public void setRawPosition(final double x, final double y, final double z)
    {
        final Vector3d pos = new Vector3d(x, y, z);
        Vector3d vec = pos;
        if (this.getDataManager() != null) vec = this.getDataManager().get(BlockEntityBase.position);
        final double ds2 = vec.squareDistanceTo(pos);
        super.setRawPosition(x, y, z);

        if (this.isServerWorld() && this.getDataManager() != null && ds2 > 0) this.getDataManager().set(
                BlockEntityBase.position, pos);

        // This is null during init, when setPosition is first called.
        if (this.getDataManager() == null) return;
        if (!this.isServerWorld())
        {
            vec = this.getDataManager().get(BlockEntityBase.position);
            if (!vec.equals(Vector3d.ZERO)) super.setRawPosition(vec.x, vec.y, vec.z);
        }
    }

    @Override
    public void resetPositionToBB()
    {
        final AxisAlignedBB box = this.getBoundingBox();
        this.setRawPosition(box.minX, box.minY, box.minZ);
        // Forge - Process chunk registration after moving.
        if (this.isAddedToWorld() && !this.world.isRemote && this.world instanceof ServerWorld)
            ((ServerWorld) this.world).chunkCheck(this);
    }

    @Override
    public void setPosition(final double x, final double y, final double z)
    {
        super.setRawPosition(x, y, z);
    }

    @Override
    public void setTiles(final TileEntity[][][] tiles)
    {
        this.tiles = tiles;
    }

    @Override
    public void tick()
    {
        if (this.getBlocks() == null && this.getEntityWorld().isRemote) return;

        this.prevPosX = this.getPosX();
        this.prevPosY = this.getPosY();
        this.prevPosZ = this.getPosZ();

        if (this.collider == null) this.collider = new BlockEntityUpdater(this);
        this.setBoundingBox(this.collider.getBoundingBox());

        if (this.isServerWorld())
        {
            final Vector3d orig = this.getMotion();
            final Vector3d motion = new Vector3d(orig.x, orig.y, orig.z);
            this.setMotion(motion);
        }

        this.rotationYaw = 0;
        this.rotationPitch = 0;
        this.preColliderTick();
        this.collider.onUpdate();
        this.accelerate();
        final int dy = (int) (this.getMotion().x * 16);
        final int dx = (int) (this.getMotion().y * 16);
        final int dz = (int) (this.getMotion().z * 16);
        if (this.toMoveY || this.toMoveX || this.toMoveZ) this.doMotion();
        else if (dx == dy && dy == dz && dz == 0 && !this.world.isRemote)
        {
            final BlockPos pos = this.getPosition();
            final boolean update = this.getPosX() != pos.getX() || this.getPosY() != Math.round(this.getPosY()) || this
                    .getPosZ() != pos.getZ();
            if (update) this.onGridAlign();
        }
        this.checkCollision();
    }

    @Override
    public void writeAdditional(final CompoundNBT nbt)
    {
        final CompoundNBT vector = new CompoundNBT();
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

    public void writeBlocks(final CompoundNBT nbt)
    {
        if (this.blocks != null)
        {
            final CompoundNBT blocksTag = new CompoundNBT();
            blocksTag.putInt("BlocksLengthX", this.blocks.length);
            blocksTag.putInt("BlocksLengthY", this.blocks[0].length);
            blocksTag.putInt("BlocksLengthZ", this.blocks[0][0].length);
            final int sizeX = this.blocks.length;
            final int sizeY = this.blocks[0].length;
            final int sizeZ = this.blocks[0][0].length;
            for (int i = 0; i < sizeX; i++)
                for (int k = 0; k < sizeY; k++)
                    for (int j = 0; j < sizeZ; j++)
                    {
                        final BlockState b = this.blocks[i][k][j];
                        if (b == null) continue;
                        blocksTag.put("B" + i + "," + k + "," + j, NBTUtil.writeBlockState(b));
                        try
                        {
                            if (this.tiles[i][k][j] != null)
                            {
                                CompoundNBT tag = new CompoundNBT();
                                tag = this.tiles[i][k][j].write(tag);
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
    public void writeSpawnData(final PacketBuffer data)
    {
        final CompoundNBT tag = new CompoundNBT();
        this.writeAdditional(tag);
        data.writeCompoundTag(tag);
    }
}
