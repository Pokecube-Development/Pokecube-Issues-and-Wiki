package thut.api.boom;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainManager;
import thut.core.common.ThutCore;

public class ExplosionCustom extends Explosion
{
    static class BlastResult
    {
        final List<BlockPos>  results;
        final List<HitEntity> hit;
        final boolean         done;

        public BlastResult(final List<BlockPos> results, final List<HitEntity> hit, final boolean done)
        {
            this.results = results;
            this.hit = hit;
            this.done = done;
        }
    }

    static class HitEntity
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

    public static int       MAX_RADIUS     = 127;
    public static Integer[] MAXPERTICK     = { 10000, 50000 };
    public static float     MINBLASTDAMAGE = 0.1f;
    public static boolean   AFFECTINAIR    = true;
    public static Block     melt;
    public static Block     solidmelt;
    public static Block     dust;
    public IEntityHitter    hitter         = (e, power, boom) ->
                                           {
                                               final EntitySize size = e.getSize(e.getPose());
                                               final float area = size.width * size.height;
                                               final float damage = area * power;
                                               e.attackEntityFrom(DamageSource.causeExplosionDamage(boom), damage);
                                           };

    int              currentIndex = 0;
    int              nextIndex    = 0;
    float            minBlastDamage;
    int              radius       = ExplosionCustom.MAX_RADIUS;
    public Integer[] maxPerTick;
    World            world;
    Vector3          centre;

    float strength;

    public boolean meteor = false;

    public PlayerEntity owner = null;

    List<Entity> targets = new ArrayList<>();

    private final double explosionX;

    private final double explosionY;

    private final double explosionZ;
    Entity               exploder;

    public Set<BlockPos> affectedBlockPositions = new HashSet<>();

    Map<LivingEntity, Float> damages = new HashMap<>();

    List<IChunk> affected = new ArrayList<>();

    // DOLATER figure out a good way to clear these between each set of shells.
    HashMap<Integer, Float> resists = new HashMap<>(100000, 1);

    HashSet<Integer> blockedSet = new HashSet<>(100000, 1);

    Int2ObjectOpenHashMap<Float> thisShell = new Int2ObjectOpenHashMap<>();

    // used to speed up the checking of if a resist exists in the map
    BitSet checked = new BitSet();

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
        this.strength = power;
        this.explosionX = center.x;
        this.explosionY = center.y;
        this.explosionZ = center.z;
        this.centre = center.copy();
        this.minBlastDamage = ExplosionCustom.MINBLASTDAMAGE;
        this.maxPerTick = ExplosionCustom.MAXPERTICK.clone();
    }

    public void addChunkPosition(final Vector3 v)
    {
        this.affectedBlockPositions.add(new BlockPos(v.intX(), v.intY(), v.intZ()));
    }

    private void applyBlockEffects(final List<BlockPos> toRemove)
    {
        this.getAffectedBlockPositions().clear();
        for (final BlockPos pos : toRemove)
        {
            this.getAffectedBlockPositions().add(pos.toImmutable());
            final BlockState state = this.world.getBlockState(pos);
            this.doMeteorStuff(state, pos);
        }
    }

    private void applyEntityEffects(final List<HitEntity> affected)
    {
        this.targets.clear();
        for (final HitEntity e : affected)
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

    public boolean canBreak(final Vector3 location)
    {
        final boolean ret = true;

        if (this.owner != null) try
        {
            final BreakEvent evt = new BreakEvent(this.world, location.getPos(), location.getBlockState(this.world),
                    this.owner);
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
        this.nextIndex = this.currentIndex + ExplosionCustom.MAXPERTICK[0];
    }

    @Override
    public void doExplosionA()
    {
        this.affectedBlockPositions.clear();
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
        this.affectedBlockPositions.clear();
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

    /**
     * Handles the actual block removal, has a meteor argument to allow
     * converting to ash or dust on impact
     *
     * @param destroyed
     * @param pos
     */
    public void doMeteorStuff(final BlockState destroyed, final BlockPos pos)
    {
        if (!destroyed.getMaterial().isSolid() && !destroyed.getMaterial().isLiquid()) return;
        if (!this.meteor)
        {
            // TODO possibly world specific air?
            this.world.setBlockState(pos, Blocks.AIR.getDefaultState());
            return;
        }
        // TODO re-implement dust/melt at some point?
        this.world.setBlockState(pos, Blocks.AIR.getDefaultState());
    }

    @SubscribeEvent
    void doRemoveBlocks(final WorldTickEvent evt)
    {
        if (evt.phase == Phase.START || evt.world != this.world) return;
        final BlastResult result = new Checker(this).getBlocksToRemove();
        this.applyBlockEffects(result.results);
        this.applyEntityEffects(result.hit);
        final ExplosionEvent evt2 = new ExplosionEvent.Detonate(this.world, this, this.targets);
        MinecraftForge.EVENT_BUS.post(evt2);
        if (result.done) MinecraftForge.EVENT_BUS.unregister(this);
    }

    public ExplosionCustom setMaxRadius(final int radius)
    {
        this.radius = radius;
        return this;
    }

    public ExplosionCustom setMeteor(final boolean meteor)
    {
        this.meteor = meteor;
        return this;
    }

    @SubscribeEvent
    public void WorldUnloadEvent(final Unload evt)
    {
        if (evt.getWorld() == this.world) MinecraftForge.EVENT_BUS.unregister(this);
    }
}
