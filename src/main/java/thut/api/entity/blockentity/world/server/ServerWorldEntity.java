package thut.api.entity.blockentity.world.server;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.merchant.IReputationTracking;
import net.minecraft.entity.merchant.IReputationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.profiler.IProfiler;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.Explosion;
import net.minecraft.world.Explosion.Mode;
import net.minecraft.world.LightType;
import net.minecraft.world.ServerTickList;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.listener.IChunkStatusListener;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.raid.RaidManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.SessionLockException;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import thut.api.entity.blockentity.IBlockEntity;
import thut.api.entity.blockentity.world.client.IBlockEntityWorld;

public class ServerWorldEntity extends ServerWorld implements IBlockEntityWorld<ServerWorld>
{
    private static class NoopChunkStatusListener implements IChunkStatusListener
    {

        @Override
        public void start(final ChunkPos arg0)
        {
        }

        @Override
        public void statusChanged(final ChunkPos arg0, final ChunkStatus arg1)
        {
        }

        @Override
        public void stop()
        {
        }
    }

    final ServerWorld world;
    IBlockEntity      mob;
    public boolean    creating;

    public ServerWorldEntity(final ServerWorld world)
    {
        super(world.getServer(), world.getServer().getBackgroundExecutor(), world.getSaveHandler(), world
                .getWorldInfo(), world.dimension.getType(), world.getServer().getProfiler(),
                new NoopChunkStatusListener());
        this.world = world;
    }

    @Override
    public void addBlockEvent(final BlockPos pos, final Block blockIn, final int eventID, final int eventParam)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().addBlockEvent(pos, blockIn, eventID, eventParam);
    }

    @Override
    public boolean addEntity(final Entity p_217376_1_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().addEntity(p_217376_1_);
    }

    @Override
    public boolean addEntityIfNotDuplicate(final Entity p_217440_1_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().addEntityIfNotDuplicate(p_217440_1_);
    }

    @Override
    public void addLightningBolt(final LightningBoltEntity p_217468_1_)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().addLightningBolt(p_217468_1_);
    }

    @Override
    public void addNewPlayer(final ServerPlayerEntity p_217435_1_)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().addNewPlayer(p_217435_1_);
    }

//    @Override
//    public void addOptionalParticle(final IParticleData p_217404_1_, final boolean p_217404_2_,
//            final double p_217404_3_, final double p_217404_5_, final double p_217404_7_, final double p_217404_9_,
//            final double p_217404_11_, final double p_217404_13_)
//    {
//        // TODO Auto-generated method stub
//        this.getWrapped().addOptionalParticle(p_217404_1_, p_217404_2_, p_217404_3_, p_217404_5_, p_217404_7_,
//                p_217404_9_, p_217404_11_, p_217404_13_);
//    }

    @Override
    public void addOptionalParticle(final IParticleData particleData, final double x, final double y, final double z,
            final double xSpeed, final double ySpeed, final double zSpeed)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().addOptionalParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public void addParticle(final IParticleData particleData, final boolean forceAlwaysRender, final double x,
            final double y, final double z, final double xSpeed, final double ySpeed, final double zSpeed)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().addParticle(particleData, forceAlwaysRender, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public void addParticle(final IParticleData particleData, final double x, final double y, final double z,
            final double xSpeed, final double ySpeed, final double zSpeed)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().addParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public void addRespawnedPlayer(final ServerPlayerEntity p_217433_1_)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().addRespawnedPlayer(p_217433_1_);
    }

    @Override
    public void addTileEntities(final Collection<TileEntity> tileEntityCollection)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().addTileEntities(tileEntityCollection);
    }

    @Override
    public boolean addTileEntity(final TileEntity tile)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().addTileEntity(tile);
    }

    @Override
    public boolean areCollisionShapesEmpty(final AxisAlignedBB p_217351_1_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().areCollisionShapesEmpty(p_217351_1_);
    }

    @Override
    public boolean areCollisionShapesEmpty(final Entity p_217345_1_)
    {
        return this.getWrapped().areCollisionShapesEmpty(p_217345_1_);
    }

    @Override
    public boolean canBlockSeeSky(final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().canBlockSeeSky(pos);
    }

    @Override
    public boolean canMineBlockBody(final PlayerEntity player, final BlockPos pos)
    {
        return this.getWrapped().canMineBlockBody(player, pos);
    }

    @Override
    public boolean checkBlockCollision(final AxisAlignedBB bb)
    {
        return this.getWrapped().checkBlockCollision(bb);
    }

    @Override
    public boolean checkNoEntityCollision(final Entity p_217346_1_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().checkNoEntityCollision(p_217346_1_);
    }

    @Override
    public boolean checkNoEntityCollision(final Entity entityIn, final VoxelShape shape)
    {
        return this.getWrapped().checkNoEntityCollision(entityIn, shape);
    }

    @Override
    public void checkSessionLock() throws SessionLockException
    {
        this.getWrapped().checkSessionLock();
    }

    @Override
    public void chunkCheck(final Entity p_217464_1_)
    {
        this.getWrapped().chunkCheck(p_217464_1_);
    }

    @Override
    public boolean chunkExists(final int p_217354_1_, final int p_217354_2_)
    {
        return this.getWrapped().chunkExists(p_217354_1_, p_217354_2_);
    }

    @Override
    public void close() throws IOException
    {
        this.getWrapped().close();
    }

    @Override
    public boolean containsAnyLiquid(final AxisAlignedBB bb)
    {
        return this.getWrapped().containsAnyLiquid(bb);
    }

    @Override
    public Object2IntMap<EntityClassification> countEntities()
    {
        return this.getWrapped().countEntities();
    }

    @Override
    protected void createBonusChest()
    {
        // TO DO Auto-generated method stub
        super.createBonusChest();
    }

    @Override
    public Explosion createExplosion(final Entity p_217401_1_, final DamageSource p_217401_2_, final double p_217401_3_,
            final double p_217401_5_, final double p_217401_7_, final float p_217401_9_, final boolean p_217401_10_,
            final Mode p_217401_11_)
    {
        return this.getWrapped().createExplosion(p_217401_1_, p_217401_2_, p_217401_3_, p_217401_5_, p_217401_7_,
                p_217401_9_, p_217401_10_, p_217401_11_);
    }

    @Override
    public Explosion createExplosion(final Entity p_217398_1_, final double p_217398_2_, final double p_217398_4_,
            final double p_217398_6_, final float p_217398_8_, final boolean p_217398_9_, final Mode p_217398_10_)
    {
        return this.getWrapped().createExplosion(p_217398_1_, p_217398_2_, p_217398_4_, p_217398_6_, p_217398_8_,
                p_217398_9_, p_217398_10_);
    }

    @Override
    public Explosion createExplosion(final Entity p_217385_1_, final double p_217385_2_, final double p_217385_4_,
            final double p_217385_6_, final float p_217385_8_, final Mode p_217385_9_)
    {
        return this.getWrapped().createExplosion(p_217385_1_, p_217385_2_, p_217385_4_, p_217385_6_, p_217385_8_,
                p_217385_9_);
    }

    @Override
    public void createSpawnPosition(final WorldSettings settings)
    {
        this.getWrapped().createSpawnPosition(settings);
    }

    @Override
    public boolean destroyBlock(final BlockPos pos, final boolean dropBlock)
    {
        return this.getWrapped().destroyBlock(pos, dropBlock);
    }

    @Override
    public boolean equals(final Object obj)
    {
        return this.getWrapped().equals(obj);
    }

    @Override
    public boolean extinguishFire(final PlayerEntity player, final BlockPos pos, final Direction side)
    {
        return this.getWrapped().extinguishFire(player, pos, side);
    }

    @Override
    public CrashReportCategory fillCrashReport(final CrashReport report)
    {
        return this.getWrapped().fillCrashReport(report);
    }

    @Override
    public BlockState findBlockstateInArea(final AxisAlignedBB area, final Block blockIn)
    {
        return this.getWrapped().findBlockstateInArea(area, blockIn);
    }

    @Override
    public BlockPos findNearestStructure(final String name, final BlockPos pos, final int radius,
            final boolean p_211157_4_)
    {
        return this.getWrapped().findNearestStructure(name, pos, radius, p_211157_4_);
    }

    @Override
    public Raid findRaid(final BlockPos p_217475_1_)
    {
        return this.getWrapped().findRaid(p_217475_1_);
    }

    @Override
    public boolean forceChunk(final int p_217458_1_, final int p_217458_2_, final boolean p_217458_3_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().forceChunk(p_217458_1_, p_217458_2_, p_217458_3_);
    }

    @Override
    public BlockRayTraceResult func_217296_a(final Vec3d p_217296_1_, final Vec3d p_217296_2_,
            final BlockPos p_217296_3_, final VoxelShape p_217296_4_, final BlockState p_217296_5_)
    {
        return this.getWrapped().func_217296_a(p_217296_1_, p_217296_2_, p_217296_3_, p_217296_4_, p_217296_5_);
    }

    @Override
    public boolean func_217350_a(final BlockState p_217350_1_, final BlockPos p_217350_2_,
            final ISelectionContext p_217350_3_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().func_217350_a(p_217350_1_, p_217350_2_, p_217350_3_);
    }

    @Override
    public BlockPos func_217383_a(final int p_217383_1_, final int p_217383_2_, final int p_217383_3_,
            final int p_217383_4_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().func_217383_a(p_217383_1_, p_217383_2_, p_217383_3_, p_217383_4_);
    }

    @Override
    public void func_217390_a(final Consumer<Entity> p_217390_1_, final Entity p_217390_2_)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().func_217390_a(p_217390_1_, p_217390_2_);
    }

    @Override
    public void func_217391_K()
    {
        // TO DO Auto-generated method stub
        this.getWrapped().func_217391_K();
    }

    @Override
    public void func_217393_a(final BlockPos p_217393_1_, final BlockState p_217393_2_, final BlockState p_217393_3_)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().func_217393_a(p_217393_1_, p_217393_2_, p_217393_3_);
    }

    @Override
    public boolean func_217400_a(final BlockPos p_217400_1_, final Entity p_217400_2_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().func_217400_a(p_217400_1_, p_217400_2_);
    }

    @Override
    public void func_217441_a(final Chunk p_217441_1_, final int p_217441_2_)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().func_217441_a(p_217441_1_, p_217441_2_);
    }

    @Override
    public PointOfInterestManager func_217443_B()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().func_217443_B();
    }

    @Override
    public void func_217446_a(final ServerPlayerEntity p_217446_1_)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().func_217446_a(p_217446_1_);
    }

    @Override
    public void func_217447_b(final ServerPlayerEntity p_217447_1_)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().func_217447_b(p_217447_1_);
    }

    @Override
    public void func_217459_a(final Entity p_217459_1_, final Entity p_217459_2_)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().func_217459_a(p_217459_1_, p_217459_2_);
    }

    @Override
    public void func_217460_e(final Entity p_217460_1_)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().func_217460_e(p_217460_1_);
    }

    @Override
    public boolean func_217471_a(final BlockPos p_217471_1_, final int p_217471_2_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().func_217471_a(p_217471_1_, p_217471_2_);
    }

    @Override
    public boolean func_217483_b_(final BlockPos p_217483_1_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().func_217483_b_(p_217483_1_);
    }

    @Override
    public int func_217486_a(final SectionPos p_217486_1_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().func_217486_a(p_217486_1_);
    }

    @Override
    public void func_217489_a(final IReputationType p_217489_1_, final Entity p_217489_2_,
            final IReputationTracking p_217489_3_)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().func_217489_a(p_217489_1_, p_217489_2_, p_217489_3_);
    }

    @Override
    public boolean func_222887_a(final SectionPos p_222887_1_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().func_222887_a(p_222887_1_);
    }

    @Override
    public int getActualHeight()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getActualHeight();
    }

    @Override
    public Biome getBiome(final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getBiome(pos);
    }

    @Override
    public Biome getBiomeBody(final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getBiomeBody(pos);
    }

    @Override
    public IBlockEntity getBlockEntity()
    {
        return this.mob;
    }

    @Override
    public BlockState getBlockState(final BlockPos pos)
    {
        final BlockState state = this.getBlock(pos);
        if (state == null) return this.world.getBlockState(pos);
        return state;
    }

    @Override
    public float getBrightness(final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getBrightness(pos);
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> cap)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getCapability(cap);
    }

    @Override
    public <T> LazyOptional<T> getCapability(final Capability<T> cap, final Direction side)
    {
        return this.getWrapped().getCapability(cap, side);
    }

    @Override
    public float getCelestialAngle(final float partialTicks)
    {
        if (this.getWrapped() == null) return 0;
        return this.getWrapped().getCelestialAngle(partialTicks);
    }

    @Override
    public float getCelestialAngleRadians(final float partialTicks)
    {
        if (this.getWrapped() == null) return 0;
        return this.getWrapped().getCelestialAngleRadians(partialTicks);
    }

    @Override
    public IChunk getChunk(final BlockPos p_217349_1_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getChunk(p_217349_1_);
    }

    @Override
    public Chunk getChunk(final int chunkX, final int chunkZ)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getChunk(chunkX, chunkZ);
    }

    @Override
    public IChunk getChunk(final int p_217348_1_, final int p_217348_2_, final ChunkStatus p_217348_3_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getChunk(p_217348_1_, p_217348_2_, p_217348_3_);
    }

    @Override
    public IChunk getChunk(final int p_217353_1_, final int p_217353_2_, final ChunkStatus p_217353_3_,
            final boolean p_217353_4_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getChunk(p_217353_1_, p_217353_2_, p_217353_3_, p_217353_4_);
    }

    @Override
    public ChunkStatus getChunkStatus()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getChunkStatus();
    }

    @Override
    public <T extends LivingEntity> T getClosestEntity(final List<? extends T> p_217361_1_,
            final EntityPredicate p_217361_2_, final LivingEntity p_217361_3_, final double p_217361_4_,
            final double p_217361_6_, final double p_217361_8_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getClosestEntity(p_217361_1_, p_217361_2_, p_217361_3_, p_217361_4_, p_217361_6_,
                p_217361_8_);
    }

    @Override
    public <T extends LivingEntity> T getClosestEntityWithinAABB(final Class<? extends T> p_217360_1_,
            final EntityPredicate p_217360_2_, final LivingEntity p_217360_3_, final double p_217360_4_,
            final double p_217360_6_, final double p_217360_8_, final AxisAlignedBB p_217360_10_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getClosestEntityWithinAABB(p_217360_1_, p_217360_2_, p_217360_3_, p_217360_4_,
                p_217360_6_, p_217360_8_, p_217360_10_);
    }

    @Override
    public PlayerEntity getClosestPlayer(final double p_217365_1_, final double p_217365_3_, final double p_217365_5_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getClosestPlayer(p_217365_1_, p_217365_3_, p_217365_5_);
    }

    @Override
    public PlayerEntity getClosestPlayer(final double p_217366_1_, final double p_217366_3_, final double p_217366_5_,
            final double p_217366_7_, final boolean p_217366_9_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getClosestPlayer(p_217366_1_, p_217366_3_, p_217366_5_, p_217366_7_, p_217366_9_);
    }

    @Override
    public PlayerEntity getClosestPlayer(final double x, final double y, final double z, final double distance,
            final Predicate<Entity> predicate)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getClosestPlayer(x, y, z, distance, predicate);
    }

    @Override
    public PlayerEntity getClosestPlayer(final Entity p_217362_1_, final double p_217362_2_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getClosestPlayer(p_217362_1_, p_217362_2_);
    }

    @Override
    public PlayerEntity getClosestPlayer(final EntityPredicate p_217359_1_, final double p_217359_2_,
            final double p_217359_4_, final double p_217359_6_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getClosestPlayer(p_217359_1_, p_217359_2_, p_217359_4_, p_217359_6_);
    }

    @Override
    public PlayerEntity getClosestPlayer(final EntityPredicate p_217370_1_, final LivingEntity p_217370_2_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getClosestPlayer(p_217370_1_, p_217370_2_);
    }

    @Override
    public PlayerEntity getClosestPlayer(final EntityPredicate p_217372_1_, final LivingEntity p_217372_2_,
            final double p_217372_3_, final double p_217372_5_, final double p_217372_7_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getClosestPlayer(p_217372_1_, p_217372_2_, p_217372_3_, p_217372_5_, p_217372_7_);
    }

    @Override
    public Vec3d getCloudColorBody(final float partialTicks)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getCloudColorBody(partialTicks);
    }

    @Override
    public Vec3d getCloudColour(final float partialTicks)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getCloudColour(partialTicks);
    }

    @Override
    public Stream<VoxelShape> getCollisionShapes(final Entity p_217352_1_, final AxisAlignedBB p_217352_2_,
            final Set<Entity> p_217352_3_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getCollisionShapes(p_217352_1_, p_217352_2_, p_217352_3_);
    }

    @Override
    public int getCombinedLight(final BlockPos pos, final int minLight)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getCombinedLight(pos, minLight);
    }

    @Override
    public Teleporter getDefaultTeleporter()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getDefaultTeleporter();
    }

    @Override
    public DifficultyInstance getDifficultyForLocation(final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getDifficultyForLocation(pos);
    }

    // @Override
    // public Stream<VoxelShape> getCollisionBoxes(final Entity entityIn, final
    // VoxelShape shape,
    // final Set<Entity> breakOnEntityCollide)
    // {
    // Stream<VoxelShape> var = this.getWrapped().
    // // TO DO Auto-generated method stub
    // return this.getWrapped().getCollisionBoxes(entityIn, shape,
    // breakOnEntityCollide);
    // }

    @Override
    public List<EnderDragonEntity> getDragons()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getDragons();
    }

    @Override
    public Stream<Entity> getEntities()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getEntities();
    }

    @Override
    public List<Entity> getEntities(final EntityType<?> p_217482_1_, final Predicate<? super Entity> p_217482_2_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getEntities(p_217482_1_, p_217482_2_);
    }

    @Override
    public List<Entity> getEntitiesInAABBexcluding(final Entity entityIn, final AxisAlignedBB boundingBox,
            final Predicate<? super Entity> predicate)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(final Class<? extends T> p_217357_1_,
            final AxisAlignedBB p_217357_2_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getEntitiesWithinAABB(p_217357_1_, p_217357_2_);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(final Class<? extends T> clazz, final AxisAlignedBB aabb,
            final Predicate<? super T> filter)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getEntitiesWithinAABB(clazz, aabb, filter);
    }

    @Override
    public List<Entity> getEntitiesWithinAABB(final EntityType<?> p_217394_1_, final AxisAlignedBB p_217394_2_,
            final Predicate<? super Entity> p_217394_3_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getEntitiesWithinAABB(p_217394_1_, p_217394_2_, p_217394_3_);
    }

    @Override
    public List<Entity> getEntitiesWithinAABBExcludingEntity(final Entity entityIn, final AxisAlignedBB bb)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getEntitiesWithinAABBExcludingEntity(entityIn, bb);
    }

    @Override
    public Entity getEntityByID(final int id)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getEntityByID(id);
    }

    @Override
    public Entity getEntityByUuid(final UUID p_217461_1_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getEntityByUuid(p_217461_1_);
    }

    @Override
    public IFluidState getFluidState(final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getFluidState(pos);
    }

    @Override
    public Vec3d getFogColor(final float partialTicks)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getFogColor(partialTicks);
    }

    @Override
    public LongSet getForcedChunks()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getForcedChunks();
    }

    @Override
    public BlockState getGroundAboveSeaLevel(final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getGroundAboveSeaLevel(pos);
    }

    @Override
    public int getHeight()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getHeight();
    }

    @Override
    public BlockPos getHeight(final Type heightmapType, final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getHeight(heightmapType, pos);
    }

    @Override
    public int getHeight(final Type heightmapType, final int x, final int z)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getHeight(heightmapType, x, z);
    }

    @Override
    public int getLight(final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getLight(pos);
    }

    @Override
    public int getLightFor(final LightType type, final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getLightFor(type, pos);
    }

    @Override
    public int getLightSubtracted(final BlockPos pos, final int amount)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getLightSubtracted(pos, amount);
    }

    @Override
    public int getLightValue(final BlockPos p_217298_1_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getLightValue(p_217298_1_);
    }

//    @Override
//    public MapData getMapData(final String p_217406_1_)
//    {
//        // TODO Auto-generated method stub
//        return this.getWrapped().getMapData(p_217406_1_);
//    }

    @Override
    public double getMaxEntityRadius()
    {
        return this.getWrapped().getMaxEntityRadius();
    }

    @Override
    public int getMaxLightLevel()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getMaxLightLevel();
    }

    @Override
    public int getNeighborAwareLightSubtracted(final BlockPos pos, final int amount)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getNeighborAwareLightSubtracted(pos, amount);
    }

    @Override
    public int getNextMapId()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getNextMapId();
    }

    @Override
    public ServerTickList<Block> getPendingBlockTicks()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getPendingBlockTicks();
    }

    @Override
    public ServerTickList<Fluid> getPendingFluidTicks()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getPendingFluidTicks();
    }

    @Override
    public PlayerEntity getPlayerByUuid(final UUID p_217371_1_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getPlayerByUuid(p_217371_1_);
    }

    @Override
    public List<ServerPlayerEntity> getPlayers()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getPlayers();
    }

    @Override
    public List<ServerPlayerEntity> getPlayers(final Predicate<? super ServerPlayerEntity> p_217490_1_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getPlayers(p_217490_1_);
    }

    @Override
    public IProfiler getProfiler()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getProfiler();
    }

    @Override
    public String getProviderName()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getProviderName();
    }

    @Override
    public RaidManager getRaids()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getRaids();
    }

    @Override
    public float getRainStrength(final float delta)
    {
        if (this.getWrapped() == null) return 0;
        return this.getWrapped().getRainStrength(delta);
    }

    @Override
    public Random getRandom()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getRandom();
    }

    @Override
    public ServerPlayerEntity getRandomPlayer()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getRandomPlayer();
    }

    @Override
    public RecipeManager getRecipeManager()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getRecipeManager();
    }

    @Override
    public int getRedstonePower(final BlockPos pos, final Direction facing)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getRedstonePower(pos, facing);
    }

    @Override
    public int getRedstonePowerFromNeighbors(final BlockPos pos)
    {
        return this.getWrapped().getRedstonePowerFromNeighbors(pos);
    }

    @Override
    public ServerScoreboard getScoreboard()
    {
        return this.getWrapped().getScoreboard();
    }

    @Override
    public int getSeaLevel()
    {
        return this.getWrapped().getSeaLevel();
    }

    @Override
    public MinecraftServer getServer()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getServer();
    }

    @Override
    public Vec3d getSkyColor(final BlockPos p_217382_1_, final float p_217382_2_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getSkyColor(p_217382_1_, p_217382_2_);
    }

    @Override
    public Vec3d getSkyColorBody(final BlockPos p_217382_1_, final float p_217382_2_)
    {
        return this.getWrapped().getSkyColorBody(p_217382_1_, p_217382_2_);
    }

    @Override
    public int getSkylightSubtracted()
    {
        return this.getWrapped().getSkylightSubtracted();
    }

    @Override
    public BlockPos getSpawnCoordinate()
    {
        return this.getWrapped().getSpawnCoordinate();
    }

    @Override
    public BlockPos getSpawnPoint()
    {
        return this.getWrapped().getSpawnPoint();
    }

    @Override
    public float getStarBrightness(final float partialTicks)
    {
        return this.getWrapped().getStarBrightness(partialTicks);
    }

    @Override
    public float getStarBrightnessBody(final float partialTicks)
    {
        if (this.getWrapped() == null) return 0;
        return this.getWrapped().getStarBrightnessBody(partialTicks);
    }

    @Override
    public int getStrongPower(final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getStrongPower(pos);
    }

    @Override
    public int getStrongPower(final BlockPos pos, final Direction direction)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getStrongPower(pos, direction);
    }

    @Override
    public TemplateManager getStructureTemplateManager()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getStructureTemplateManager();
    }

    @Override
    public float getSunBrightness(final float partialTicks)
    {
        if (this.getWrapped() == null) return 0;
        return this.getWrapped().getSunBrightness(partialTicks);
    }

    @Override
    public float getSunBrightnessBody(final float partialTicks)
    {
        if (this.getWrapped() == null) return 0;
        return this.getWrapped().getSunBrightnessBody(partialTicks);
    }

    @Override
    public NetworkTagManager getTags()
    {
        return this.getWrapped().getTags();
    }

    @Override
    public <T extends LivingEntity> List<T> getTargettableEntitiesWithinAABB(final Class<? extends T> p_217374_1_,
            final EntityPredicate p_217374_2_, final LivingEntity p_217374_3_, final AxisAlignedBB p_217374_4_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getTargettableEntitiesWithinAABB(p_217374_1_, p_217374_2_, p_217374_3_, p_217374_4_);
    }

    @Override
    public List<PlayerEntity> getTargettablePlayersWithinAABB(final EntityPredicate p_217373_1_,
            final LivingEntity p_217373_2_, final AxisAlignedBB p_217373_3_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().getTargettablePlayersWithinAABB(p_217373_1_, p_217373_2_, p_217373_3_);
    }

    @Override
    public float getThunderStrength(final float delta)
    {
        if (this.getWrapped() == null) return 0;
        return this.getWrapped().getThunderStrength(delta);
    }

    @Override
    public TileEntity getTileEntity(final BlockPos pos)
    {
        final TileEntity tile = this.getTile(pos);
        if (tile == null) return this.world.getTileEntity(pos);
        return tile;
    }

    @Override
    public World getWorld()
    {
        return this.getWrapped().getWorld();
    }

    @Override
    public WorldBorder getWorldBorder()
    {
        if (this.getWrapped() == null) return super.getWorldBorder();
        return this.getWrapped().getWorldBorder();
    }

    @Override
    public WorldInfo getWorldInfo()
    {
        // TO DO Auto-generated method stub
        return super.getWorldInfo();
    }

    @Override
    public ServerWorld getWorldServer()
    {
        return this.getWrapped();
    }

    @Override
    public WorldType getWorldType()
    {
        // TO DO Auto-generated method stub
        return super.getWorldType();
    }

    @Override
    public ServerWorld getWrapped()
    {
        return this.world;
    }

    @Override
    public boolean hasBlockState(final BlockPos p_217375_1_, final Predicate<BlockState> p_217375_2_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().hasBlockState(p_217375_1_, p_217375_2_);
    }

    @Override
    public int hashCode()
    {
        return this.getWrapped().hashCode();
    }

    @Override
    public boolean hasRaid(final BlockPos p_217455_1_)
    {
        return this.getWrapped().hasRaid(p_217455_1_);
    }

    @Override
    public boolean hasWater(final BlockPos pos)
    {
        return this.getWrapped().hasWater(pos);
    }

    @Override
    public double increaseMaxEntityRadius(final double value)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().increaseMaxEntityRadius(value);
    }

    @Override
    public boolean isAirBlock(final BlockPos pos)
    {
        return this.getWrapped().isAirBlock(pos);
    }

    @Override
    public boolean isAreaLoaded(final BlockPos center, final int range)
    {
        return this.getWrapped().isAreaLoaded(center, range);
    }

    @Override
    public boolean isBlockinHighHumidity(final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isBlockinHighHumidity(pos);
    }

    @Override
    public boolean isBlockModifiable(final PlayerEntity player, final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isBlockModifiable(player, pos);
    }

    @Override
    public boolean isBlockPowered(final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isBlockPowered(pos);
    }

    @Override
    public boolean isBlockPresent(final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isBlockPresent(pos);
    }

    @Override
    public boolean isCollisionBoxesEmpty(final Entity entityIn, final AxisAlignedBB aabb)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isCollisionBoxesEmpty(entityIn, aabb);
    }

    @Override
    public boolean isCollisionBoxesEmpty(final Entity entityIn, final AxisAlignedBB aabb,
            final Set<Entity> entitiesToIgnore)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isCollisionBoxesEmpty(entityIn, aabb, entitiesToIgnore);
    }

    @Override
    public boolean isDaytime()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isDaytime();
    }

    @Override
    public boolean isFlammableWithin(final AxisAlignedBB bb)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isFlammableWithin(bb);
    }

    @Override
    public boolean isInsideTick()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isInsideTick();
    }

    @Override
    public boolean isMaterialInBB(final AxisAlignedBB bb, final Material materialIn)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isMaterialInBB(bb, materialIn);
    }

    @Override
    public boolean isPlayerWithin(final double p_217358_1_, final double p_217358_3_, final double p_217358_5_,
            final double p_217358_7_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isPlayerWithin(p_217358_1_, p_217358_3_, p_217358_5_, p_217358_7_);
    }

    @Override
    public boolean isRaining()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isRaining();
    }

    @Override
    public boolean isRainingAt(final BlockPos position)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isRainingAt(position);
    }

    @Override
    public boolean isRemote()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isRemote();
    }

    @Override
    public boolean isSaveDisabled()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isSaveDisabled();
    }

    @Override
    public boolean isSidePowered(final BlockPos pos, final Direction side)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isSidePowered(pos, side);
    }

    @Override
    public boolean isSkyLightMax(final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isSkyLightMax(pos);
    }

    @Override
    public boolean isThundering()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().isThundering();
    }

    @Override
    public void makeFireworks(final double x, final double y, final double z, final double motionX,
            final double motionY, final double motionZ, final CompoundNBT compound)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().makeFireworks(x, y, z, motionX, motionY, motionZ, compound);
    }

    @Override
    public void markAndNotifyBlock(final BlockPos pos, final Chunk chunk, final BlockState blockstate,
            final BlockState newState, final int flags)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().markAndNotifyBlock(pos, chunk, blockstate, newState, flags);
    }

    @Override
    public void markChunkDirty(final BlockPos pos, final TileEntity unusedTileEntity)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().markChunkDirty(pos, unusedTileEntity);
    }

    @Override
    public void neighborChanged(final BlockPos pos, final Block blockIn, final BlockPos fromPos)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().neighborChanged(pos, blockIn, fromPos);
    }

    // @Override
    // public void markForRerender(final BlockPos p_217396_1_)
    // {
    // // TO DO Auto-generated method stub
    // this.getWrapped().markForRerender(p_217396_1_);
    // }

    @Override
    public void notifyBlockUpdate(final BlockPos pos, final BlockState oldState, final BlockState newState,
            final int flags)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().notifyBlockUpdate(pos, oldState, newState, flags);
    }

    @Override
    public void notifyNeighbors(final BlockPos pos, final Block blockIn)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().notifyNeighbors(pos, blockIn);
    }

    @Override
    public void notifyNeighborsOfStateChange(final BlockPos pos, final Block blockIn)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().notifyNeighborsOfStateChange(pos, blockIn);
    }

    @Override
    public void notifyNeighborsOfStateExcept(final BlockPos pos, final Block blockType, final Direction skipSide)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().notifyNeighborsOfStateExcept(pos, blockType, skipSide);
    }

    @Override
    public void onChunkUnloading(final Chunk p_217466_1_)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().onChunkUnloading(p_217466_1_);
    }

    @Override
    public void onEntityRemoved(final Entity p_217484_1_)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().onEntityRemoved(p_217484_1_);
    }

    @Override
    public void playBroadcastSound(final int id, final BlockPos pos, final int data)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().playBroadcastSound(id, pos, data);
    }

    @Override
    public void playEvent(final int p_217379_1_, final BlockPos p_217379_2_, final int p_217379_3_)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().playEvent(p_217379_1_, p_217379_2_, p_217379_3_);
    }

    @Override
    public void playEvent(final PlayerEntity p_217378_1_, final int p_217378_2_, final BlockPos p_217378_3_,
            final int p_217378_4_)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().playEvent(p_217378_1_, p_217378_2_, p_217378_3_, p_217378_4_);
    }

    @Override
    public void playMovingSound(final PlayerEntity p_217384_1_, final Entity p_217384_2_, final SoundEvent p_217384_3_,
            final SoundCategory p_217384_4_, final float p_217384_5_, final float p_217384_6_)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().playMovingSound(p_217384_1_, p_217384_2_, p_217384_3_, p_217384_4_, p_217384_5_, p_217384_6_);
    }

    @Override
    public void playSound(final double x, final double y, final double z, final SoundEvent soundIn,
            final SoundCategory category, final float volume, final float pitch, final boolean distanceDelay)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().playSound(x, y, z, soundIn, category, volume, pitch, distanceDelay);
    }

    @Override
    public void playSound(final PlayerEntity player, final BlockPos pos, final SoundEvent soundIn,
            final SoundCategory category, final float volume, final float pitch)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().playSound(player, pos, soundIn, category, volume, pitch);
    }

    @Override
    public void playSound(final PlayerEntity player, final double x, final double y, final double z,
            final SoundEvent soundIn, final SoundCategory category, final float volume, final float pitch)
    {
        this.getWrapped().playSound(player, x, y, z, soundIn, category, volume, pitch);
    }

    @Override
    public BlockRayTraceResult rayTraceBlocks(final RayTraceContext p_217299_1_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().rayTraceBlocks(p_217299_1_);
    }

//    @Override
//    public void registerMapData(final MapData p_217399_1_)
//    {
//        // TODO Auto-generated method stub
//        this.getWrapped().registerMapData(p_217399_1_);
//    }

    @Override
    public boolean removeBlock(final BlockPos p_217377_1_, final boolean p_217377_2_)
    {
        return this.getWrapped().removeBlock(p_217377_1_, p_217377_2_);
    }

    @Override
    public void removeEntity(final Entity p_217467_1_)
    {
        this.getWrapped().removeEntity(p_217467_1_);
    }

    @Override
    public void removeEntity(final Entity p_217467_1_, final boolean keepData)
    {
        this.getWrapped().removeEntity(p_217467_1_, keepData);
    }

    @Override
    public void removeEntityComplete(final Entity p_217484_1_, final boolean keepData)
    {
        this.getWrapped().removeEntityComplete(p_217484_1_, keepData);
    }

    @Override
    public void removePlayer(final ServerPlayerEntity p_217434_1_)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().removePlayer(p_217434_1_);
    }

    @Override
    public void removePlayer(final ServerPlayerEntity p_217434_1_, final boolean keepData)
    {
        this.getWrapped().removePlayer(p_217434_1_, keepData);
    }

    @Override
    public void removeTileEntity(final BlockPos pos)
    {
        this.getWrapped().removeTileEntity(pos);
    }

    @Override
    public void resetUpdateEntityTick()
    {
        this.getWrapped().resetUpdateEntityTick();
    }

    @Override
    public void save(final IProgressUpdate p_217445_1_, final boolean p_217445_2_, final boolean p_217445_3_)
            throws SessionLockException
    {
        // TO DO Auto-generated method stub
        this.getWrapped().save(p_217445_1_, p_217445_2_, p_217445_3_);
    }

    @Override
    protected void saveLevel() throws SessionLockException
    {
        // TO DO Auto-generated method stub
        super.saveLevel();
    }

    @Override
    public void sendBlockBreakProgress(final int breakerId, final BlockPos pos, final int progress)
    {
        this.getWrapped().sendBlockBreakProgress(breakerId, pos, progress);
    }

    @Override
    public void sendPacketToServer(final IPacket<?> packetIn)
    {
        this.getWrapped().sendPacketToServer(packetIn);
    }

    @Override
    public void sendQuittingDisconnectingPacket()
    {
        this.getWrapped().sendQuittingDisconnectingPacket();
    }

    @Override
    public void setAllowedSpawnTypes(final boolean hostile, final boolean peaceful)
    {
        this.getWrapped().setAllowedSpawnTypes(hostile, peaceful);
    }

    @Override
    public void setBlockEntity(final IBlockEntity mob)
    {
        IBlockEntityWorld.super.setBlockEntity(mob);
        this.mob = mob;
    }

    @Override
    public boolean setBlockState(final BlockPos pos, final BlockState state)
    {
        return this.getWrapped().setBlockState(pos, state);
    }

    /**
     * Sets the block state at a given location. Flag 1 will cause a block
     * update. Flag 2 will send the change to clients (you almost always want
     * this). Flag 4 prevents the block from being re-rendered, if this is a
     * client world. Flags can be added together.
     */
    @Override
    public boolean setBlockState(final BlockPos pos, final BlockState newState, final int flags)
    {
        if (this.setBlock(pos, newState)) return true;
        else return this.world.setBlockState(pos, newState, flags);
    }

    @Override
    public void setDayTime(final long time)
    {
        this.getWrapped().setDayTime(time);
    }

    @Override
    public void setEntityState(final Entity entityIn, final byte state)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().setEntityState(entityIn, state);
    }

    @Override
    public void setGameTime(final long worldTime)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().setGameTime(worldTime);
    }

    @Override
    public void setInitialSpawnLocation()
    {
        // TO DO Auto-generated method stub
        this.getWrapped().setInitialSpawnLocation();
    }

    @Override
    public void setLastLightningBolt(final int lastLightningBoltIn)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().setLastLightningBolt(lastLightningBoltIn);
    }

    @Override
    public void setRainStrength(final float strength)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().setRainStrength(strength);
    }

    @Override
    public void setSpawnPoint(final BlockPos pos)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().setSpawnPoint(pos);
    }

    @Override
    public void setThunderStrength(final float strength)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().setThunderStrength(strength);
    }

    @Override
    public void setTileEntity(final BlockPos pos, @Nullable final TileEntity tileEntityIn)
    {
        if (this.setTile(pos, tileEntityIn)) return;
        this.getWrapped().setTileEntity(pos, tileEntityIn);
    }

    @Override
    public <T extends IParticleData> boolean spawnParticle(final ServerPlayerEntity p_195600_1_, final T p_195600_2_,
            final boolean p_195600_3_, final double p_195600_4_, final double p_195600_6_, final double p_195600_8_,
            final int p_195600_10_, final double p_195600_11_, final double p_195600_13_, final double p_195600_15_,
            final double p_195600_17_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().spawnParticle(p_195600_1_, p_195600_2_, p_195600_3_, p_195600_4_, p_195600_6_,
                p_195600_8_, p_195600_10_, p_195600_11_, p_195600_13_, p_195600_15_, p_195600_17_);
    }

    @Override
    public <T extends IParticleData> int spawnParticle(final T type, final double posX, final double posY,
            final double posZ, final int particleCount, final double xOffset, final double yOffset,
            final double zOffset, final double speed)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().spawnParticle(type, posX, posY, posZ, particleCount, xOffset, yOffset, zOffset, speed);
    }

    @Override
    public boolean summonEntity(final Entity p_217470_1_)
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().summonEntity(p_217470_1_);
    }

    @Override
    public void tick(final BooleanSupplier hasTimeLeft)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().tick(hasTimeLeft);
    }

    @Override
    public String toString()
    {
        // TO DO Auto-generated method stub
        return this.getWrapped().toString();
    }

    @Override
    public void updateAllPlayersSleepingFlag()
    {
        // TO DO Auto-generated method stub
        this.getWrapped().updateAllPlayersSleepingFlag();
    }

    @Override
    public void updateComparatorOutputLevel(final BlockPos pos, final Block blockIn)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().updateComparatorOutputLevel(pos, blockIn);
    }

    @Override
    public void updateEntity(final Entity p_217479_1_)
    {
        // TO DO Auto-generated method stub
        this.getWrapped().updateEntity(p_217479_1_);
    }
}