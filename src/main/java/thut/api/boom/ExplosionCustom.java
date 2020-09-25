package thut.api.boom;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2FloatMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.boom.Checker.Cubes;
import thut.api.boom.Checker.ResistCache;
import thut.api.boom.Checker.ResistMap;
import thut.api.boom.Checker.ResistProvider;
import thut.api.boom.Checker.ShadowMap;
import thut.api.boom.Checker.ShadowSet;
import thut.api.maths.Vector3;
import thut.api.maths.vecmath.Vector3f;
import thut.api.terrain.TerrainManager;
import thut.core.common.ThutCore;

public class ExplosionCustom extends Explosion
{
    public static class BlastResult
    {
        public final Object2FloatOpenHashMap<BlockPos> results;

        public final List<HitEntity> hit;
        public final boolean         done;

        public BlastResult(final Object2FloatOpenHashMap<BlockPos> results, final List<HitEntity> hit,
                final boolean done)
        {
            this.results = results;
            this.hit = hit;
            this.done = done;
        }
    }

    public static class HitEntity
    {
        final Entity entity;
        final float  blastStrength;

        public HitEntity(final Entity entity, final float blastStrength)
        {
            this.entity = entity;
            this.blastStrength = blastStrength;
        }
    }

    public static interface IEntityHitter
    {
        void hitEntity(Entity e, float power, Explosion boom);
    }

    public static interface BlockBreaker
    {
        default void breakBlocks(final BlastResult result, final ExplosionCustom boom)
        {
            for (final Entry<BlockPos> pos : result.results.object2FloatEntrySet())
            {
                boom.getAffectedBlockPositions().add(pos.getKey());
                final BlockState destroyed = boom.world.getBlockState(pos.getKey());
                final float power = pos.getFloatValue();
                BlockState to = Blocks.AIR.getDefaultState();
                if (power < 36)
                {
                    if (destroyed.getMaterial() == Material.LEAVES) to = Blocks.FIRE.getDefaultState();
                    if (destroyed.getMaterial() == Material.TALL_PLANTS) to = Blocks.FIRE.getDefaultState();
                }
                // TODO re-implement dust/melt at some point?
                boom.world.setBlockState(pos.getKey(), to, 3);
            }
        }
    }

    public static int     MAX_RADIUS     = 127;
    public static int     MAXPERTICK     = 25;
    public static float   MINBLASTDAMAGE = 0.1f;
    public static boolean AFFECTINAIR    = true;

    public static Block melt;
    public static Block solidmelt;
    public static Block dust;

    public IEntityHitter hitter = (e, power, boom) ->
    {
        final EntitySize size = e.getSize(e.getPose());
        final float area = size.width * size.height;
        final float damage = area * power;
        e.attackEntityFrom(DamageSource.causeExplosionDamage(boom), damage);
    };

    int currentIndex = 0;
    int nextIndex    = 0;

    double last_phi = 0;
    double last_rad = 0.25;

    int ind1;
    int ind2;
    int ind3;
    int ind4;

    float minBlastDamage;

    int radius = ExplosionCustom.MAX_RADIUS;

    int currentRadius = 0;

    public int maxPerTick;

    public World world;

    Vector3 centre;

    final float strength;

    public BlockBreaker breaker = new BlockBreaker()
    {
    };

    public ResistProvider resistProvider = new ResistProvider()
    {
    };

    public PlayerEntity owner = null;

    List<Entity> targets = new ArrayList<>();

    private final double explosionX;
    private final double explosionY;
    private final double explosionZ;

    public long totalTime = 0;

    public long realTotalTime = 0;

    Entity exploder;

    Vector3f min = new Vector3f(-1, -1, -1);
    Vector3f max = new Vector3f(1, 1, 1);

    Vector3f min_next = new Vector3f(1, 1, 1);
    Vector3f max_next = new Vector3f(-1, -1, -1);

    float lastBoundCheck = 10;

    // DOLATER figure out a good way to clear these between each set of shells.
    Long2FloatOpenHashMap resistMap = new Long2FloatOpenHashMap();

    LongSet blockedSet = new LongOpenHashSet();

    ShadowMap shadow;

    ResistCache resists;

    // used to speed up the checking of if a resist exists in the map
    LongSet checked = new LongOpenHashSet();
    LongSet seen    = new LongOpenHashSet();

    Cubes cubes;

    Vector3 r = Vector3.getNewVector(), rAbs = Vector3.getNewVector(), rHat = Vector3.getNewVector(), rTest = Vector3
            .getNewVector(), rTestPrev = Vector3.getNewVector(), rTestAbs = Vector3.getNewVector();

    public ExplosionCustom(final World world, final Entity par2Entity, final double x, final double y, final double z,
            final float power)
    {
        this(world, par2Entity, Vector3.getNewVector().set(x, y, z), power);
    }

    public ExplosionCustom(final World world, final Entity par2Entity, final Vector3 center, final float power)
    {
        super(world, par2Entity, center.x, center.y, center.z, power, false, Mode.DESTROY);
        this.world = world;
        this.exploder = par2Entity;
        this.explosionX = center.x;
        this.explosionY = center.y;
        this.explosionZ = center.z;
        this.centre = center.copy();
        this.minBlastDamage = ExplosionCustom.MINBLASTDAMAGE;
        this.maxPerTick = ExplosionCustom.MAXPERTICK;

        final double scaleFactor = 150;
        this.strength = (float) (scaleFactor * power);

        this.cubes = new Cubes(this);
        this.shadow = new ShadowSet(this);
        this.resists = new ResistMap();
        this.resists = this.cubes;

        this.lastBoundCheck = center.intY() - world.getHeight(Type.MOTION_BLOCKING, center.intX(), center.intZ()) + 10;
        this.lastBoundCheck = Math.max(this.lastBoundCheck, 10);
    }

    private void applyBlockEffects(final BlastResult result)
    {
        this.getAffectedBlockPositions().clear();
        this.breaker.breakBlocks(result, this);
    }

    private void applyEntityEffects(final BlastResult result)
    {
        this.targets.clear();
        for (final HitEntity e : result.hit)
        {
            final Entity hit = e.entity;
            final float power = e.blastStrength;
            if (power > 0)
            {
                this.hitter.hitEntity(hit, power, this);
                this.targets.add(hit);
            }
        }
    }

    public boolean canBreak(final Vector3 location, final BlockState state)
    {
        final boolean ret = state.getBlock() != Blocks.BEDROCK;

        if (this.owner != null) try
        {
            final BreakEvent evt = new BreakEvent(this.world, location.getPos(), state, this.owner);
            MinecraftForge.EVENT_BUS.post(evt);
            if (evt.isCanceled()) return false;
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            return false;
        }

        return ret;
    }

    public void doExplosion()
    {
        this.world.playSound((PlayerEntity) null, this.explosionX, this.explosionY, this.explosionZ,
                SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.rand.nextFloat()
                        - this.world.rand.nextFloat()) * 0.2F) * 0.7F);
        this.world.addParticle(ParticleTypes.EXPLOSION, this.explosionX, this.explosionY, this.explosionZ, 1.0D, 0.0D,
                0.0D);
        MinecraftForge.EVENT_BUS.register(this);
        this.realTotalTime = System.nanoTime();
    }

    @Override
    public void doExplosionA()
    {
        ThutCore.LOGGER.error("This should not be run anymore", new Exception());
    }

    /** Does the second part of the explosion (sound, particles, drop spawn) */
    @Override
    public void doExplosionB(final boolean par1)
    {
        ThutCore.LOGGER.error("This should not be run anymore", new Exception());
    }

    // TODO Revisit this to make blast energy more conserved
    public void doKineticImpactor(final World world, final Vector3 velocity, Vector3 hitLocation, Vector3 acceleration,
            float density, float energy)
    {
        if (density < 0 || energy <= 0) return;
        final int max = 63;
        if (acceleration == null) acceleration = Vector3.empty;
        final float factor = 1;
        int n = 0;
        final List<Vector3> locations = new ArrayList<>();
        final List<Float> blasts = new ArrayList<>();

        float resist = hitLocation.getExplosionResistance(this, world);
        float blast = Math.min(energy * (resist / density), energy);

        if (resist > density)
        {
            hitLocation = hitLocation.subtract(velocity.normalize());
            final ExplosionCustom boo = new ExplosionCustom(world, this.exploder, hitLocation, blast * factor);
            boo.setMaxRadius(this.radius);
            boo.owner = this.owner;
            boo.doExplosion();
            return;
        }
        Vector3 absorbedLoc = Vector3.getNewVector();
        float remainingEnergy = 0;
        density -= resist;

        while (energy > 0 && density > 0)
        {
            locations.add(hitLocation.subtract(velocity.normalize()));
            blasts.add(blast);
            hitLocation = hitLocation.add(velocity.normalize());
            velocity.add(acceleration);
            resist = Math.max(hitLocation.getExplosionResistance(this, world), 0);
            blast = Math.min(energy * (resist / density), energy);
            if (resist > density)
            {
                absorbedLoc.set(hitLocation);
                remainingEnergy = energy;
                break;
            }
            energy -= energy * (resist / density);
            density -= resist + 0.1;
        }

        n = locations.size();
        if (n != 0)

            for (int i = 0; i < n; i++)
        {
            final Vector3 source = locations.get(i);
            final float strength = Math.min(blasts.get(i), 256);
            if (TerrainManager.isAreaLoaded(world, source.getPos(), max)) if (strength != 0)
            {
                final ExplosionCustom boo = new ExplosionCustom(world, this.exploder, source, strength * factor);
                boo.setMaxRadius(this.radius);
                boo.owner = this.owner;
                boo.doExplosion();
            }
        }
        if (remainingEnergy > 10)
        {
            absorbedLoc = absorbedLoc.subtract(velocity.normalize());
            final ExplosionCustom boo = new ExplosionCustom(world, this.exploder, absorbedLoc, remainingEnergy
                    * factor);
            boo.setMaxRadius(this.radius);
            boo.owner = this.owner;
            boo.doExplosion();
        }
    }

    @SubscribeEvent
    public void doRemoveBlocks(final WorldTickEvent evt)
    {
        if (evt.phase == Phase.START || evt.world != this.world) return;

        this.clearAffectedBlockPositions();
        final BlastResult result = new Checker(this).getBlocksToRemove();
        this.applyBlockEffects(result);
        this.applyEntityEffects(result);
        final ExplosionEvent evt2 = new ExplosionEvent.Detonate(this.world, this, this.targets);
        // ThutCore.LOGGER.info("Strength: {}, Max radius: {}, Last Radius: {}",
        // this.strength, this.radius, this.r.mag());
        MinecraftForge.EVENT_BUS.post(evt2);
        if (result.done)
        {
            MinecraftForge.EVENT_BUS.unregister(this);
            this.realTotalTime = System.nanoTime() - this.realTotalTime;
            ThutCore.LOGGER.info("Strength: {}, Max radius: {}, Last Radius: {}", this.strength, this.radius, this.r
                    .mag());
            ThutCore.LOGGER.info("time (tick/real): {}/{}ms, {} shadowed, {} denied, {} blocked, {} checked",
                    this.totalTime / 1e6, this.realTotalTime / 1e6, this.ind1, this.ind2, this.ind3, this.ind4);
            ThutCore.LOGGER.info("bounds: {} {}", this.min, this.max);
        }
    }

    public ExplosionCustom setMaxRadius(final int radius)
    {
        this.radius = radius;
        return this;
    }

    @SubscribeEvent
    public void WorldUnloadEvent(final Unload evt)
    {
        if (evt.getWorld() == this.world) MinecraftForge.EVENT_BUS.unregister(this);
    }
}
