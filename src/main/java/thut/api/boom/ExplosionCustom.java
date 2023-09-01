package thut.api.boom;

import it.unimi.dsi.fastutil.objects.Object2FloatMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.LevelEvent.Unload;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import thut.api.boom.ShadowMaskChecker.ResistProvider;
import thut.api.entity.event.BreakTestEvent;
import thut.api.item.ItemList;
import thut.api.level.terrain.TerrainManager;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;
import thut.lib.RegHelper;

public class ExplosionCustom extends Explosion
{
    public static class BlastResult
    {
        public final Object2FloatOpenHashMap<BlockPos> destroyedBlocks;
        public final Object2FloatOpenHashMap<BlockPos> damagedBlocks;
        public final Set<ChunkPos> affectedChunks;

        public final List<HitEntity> hit;
        public final boolean done;

        public BlastResult(final Object2FloatOpenHashMap<BlockPos> results,
                final Object2FloatOpenHashMap<BlockPos> remaining, final List<HitEntity> hit,
                final Set<ChunkPos> affectedChunks, final boolean done)
        {
            this.destroyedBlocks = results;
            this.damagedBlocks = remaining;
            this.hit = hit;
            this.done = done;
            this.affectedChunks = affectedChunks;
        }
    }

    public static class HitEntity
    {
        final Entity entity;
        final float blastStrength;

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
        default boolean shouldBreak(BlockPos pos, BlockState state, float power, ServerLevel level)
        {
            if (ItemList.is(EXPLOSION_TRANSPARENT, state)) return false;
            return true;
        }

        default BlockState applyBreak(ExplosionCustom boom, BlockPos pos, BlockState state, float power,
                boolean destroy, ServerLevel level)
        {
            if (!destroy) return state;
            BlockState to = Blocks.AIR.defaultBlockState();
            if (power < 36)
            {
                if (state.is(BlockTags.LEAVES)) to = Blocks.FIRE.defaultBlockState();
                if (state.canBeReplaced()) to = Blocks.FIRE.defaultBlockState();
            }
            level.setBlock(pos, to, 3);
            return to;
        }

        default BlockState onAbsorbed(ExplosionCustom boom, BlockPos pos, BlockState state, float power,
                boolean canEffect, ServerLevel level)
        {
            return state;
        }

        default void destroyBlocks(final BlastResult result, final ExplosionCustom boom)
        {
            for (final Entry<BlockPos> entry : result.destroyedBlocks.object2FloatEntrySet())
            {
                BlockPos pos = entry.getKey();
                float power = entry.getFloatValue();
                final BlockState state = boom.level.getBlockState(pos);
                BlockState broken = applyBreak(boom, pos, state, power, shouldBreak(pos, state, power, boom.level),
                        boom.level);
                if (broken != state) boom.getToBlow().add(pos);
            }
        }

        default void damageBlocks(final BlastResult result, final ExplosionCustom boom)
        {
            for (final Entry<BlockPos> entry : result.damagedBlocks.object2FloatEntrySet())
            {
                BlockPos pos = entry.getKey();
                float power = entry.getFloatValue();
                final BlockState state = boom.level.getBlockState(pos);
                BlockState broken = onAbsorbed(boom, pos, state, power, shouldBreak(pos, state, power, boom.level),
                        boom.level);
                if (broken != state) boom.getToBlow().add(pos);
            }
        }
    }

    public static class DefaultBreaker implements BlockBreaker
    {
        public static final ResourceLocation DAMAGE_LIST = new ResourceLocation("thutcore:absorption_damage");

        final ServerLevel level;
        final StructureProcessorList list;
        final StructurePlaceSettings settings;

        public DefaultBreaker(ServerLevel level)
        {
            this.level = level;
            list = level.registryAccess().registryOrThrow(RegHelper.PROCESSOR_LIST_REGISTRY).get(DAMAGE_LIST);
            settings = new StructurePlaceSettings();
        }

        @Override
        public BlockState onAbsorbed(ExplosionCustom boom, BlockPos pos, BlockState state, float power,
                boolean canEffect, ServerLevel level)
        {
            if (!canEffect) return state;

            StructureBlockInfo info = new StructureBlockInfo(pos, state, null);
            StructureBlockInfo processed = info;
            for (var list : this.list.list())
            {
                info = list.process(level, pos, pos, info, processed, settings, null);
            }
            if (info != null && state != info.state())
            {
                state = info.state();
                level.setBlock(pos, state, 3);
            }
            return state;
        }
    }

    public static int MAX_RADIUS = 127;
    public static int MAXPERTICK = 25;
    public static float MINBLASTDAMAGE = 0.1f;
    public static boolean AFFECTINAIR = true;

    public static final ResourceLocation EXPLOSION_BLOCKING = new ResourceLocation("thutcore:explosion_blocking");
    public static final ResourceLocation EXPLOSION_TRANSPARENT = new ResourceLocation("thutcore:explosion_transparent");
    public static final ResourceLocation EXPLOSION_2X_WEAK = new ResourceLocation("thutcore:explosion_2x_weaker");
    public static final ResourceLocation EXPLOSION_10X_WEAK = new ResourceLocation("thutcore:explosion_10x_weaker");

    public IEntityHitter hitter = (e, power, boom) -> {
        final EntityDimensions size = e.getDimensions(e.getPose());
        final float area = size.width * size.height;
        final float damage = area * power;
        if (!e.isInvulnerable()) e.hurt(e.damageSources().explosion(boom), damage);
    };

    float minBlastDamage;

    int radius = ExplosionCustom.MAX_RADIUS;

    public int maxPerTick;

    public ServerLevel level;

    Vector3 centre;

    final float strength;

    public BlockBreaker breaker;

    public ResistProvider resistProvider = new ResistProvider()
    {
    };

    public Player owner = null;

    List<Entity> targets = new ArrayList<>();

    public float factor = 150;

    Entity exploder;

    List<ExplosionCustom> subBooms = new ArrayList<>();
    boolean hasSubBooms = false;
    boolean boomDone = false;

    private AbstractChecker boomApplier;

    public ExplosionCustom(final ServerLevel world, final Entity par2Entity, final double x, final double y,
            final double z, final float power)
    {
        this(world, par2Entity, new Vector3().set(x, y, z), power);
    }

    public ExplosionCustom(final ServerLevel world, final Entity par2Entity, final Vector3 center, final float power)
    {
        // TODO replace the 2 nulls here with damage source and context!
        super(world, par2Entity, null, null, center.x, center.y, center.z, power, false, BlockInteraction.DESTROY);
        this.level = world;
        this.exploder = par2Entity;
        this.centre = center.copy();
        this.minBlastDamage = ExplosionCustom.MINBLASTDAMAGE;
        this.maxPerTick = ExplosionCustom.MAXPERTICK;

        this.strength = factor * power;

        boomApplier = new SphereMaskChecker(this);
        this.breaker = new DefaultBreaker(world);
    }

    private void applyBlockEffects(final BlastResult result)
    {
        this.getToBlow().clear();
        this.breaker.destroyBlocks(result, this);
        this.breaker.damageBlocks(result, this);
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
        final boolean ret = !ItemList.is(EXPLOSION_BLOCKING, state);
        if (!ret) return false;
        if (this.owner != null && !BreakTestEvent.testBreak(this.level, location.getPos(), state, this.owner))
            return false;
        return true;
    }

    public void doExplosion()
    {
        this.level.playSound((Player) null, this.centre.x, this.centre.y, this.centre.z, SoundEvents.GENERIC_EXPLODE,
                SoundSource.BLOCKS, 4.0F,
                (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F);
        this.level.addParticle(ParticleTypes.EXPLOSION, this.centre.x, this.centre.y, this.centre.z, 1.0D, 0.0D, 0.0D);
        MinecraftForge.EVENT_BUS.register(this);
        boomApplier.start();
        if (this.hasSubBooms)
        {
            this.subBooms.get(0).doExplosion();
        }
    }

    @Override
    public void explode()
    {
        ThutCore.LOGGER.error("This should not be run anymore", new Exception());
    }

    /** Does the second part of the explosion (sound, particles, drop spawn) */
    @Override
    public void finalizeExplosion(final boolean par1)
    {
        ThutCore.LOGGER.error("This should not be run anymore", new Exception());
    }

    public void doKineticImpactor(final ServerLevel world, final Vector3 velocity, Vector3 hitLocation,
            Vector3 acceleration, float density, float energy)
    {
        if (density < 0 || energy <= 0) return;
        factor = 1;
        final int max = 63;
        if (acceleration == null) acceleration = Vector3.empty;

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
            boo.breaker = this.breaker;
            boo.owner = this.owner;
            boo.doExplosion();
            return;
        }
        Vector3 absorbedLoc = new Vector3();
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
                boo.breaker = this.breaker;
                boo.owner = this.owner;
                this.subBooms.add(boo);
                hasSubBooms = true;
            }
        }
        if (remainingEnergy > 10)
        {
            absorbedLoc = absorbedLoc.subtract(velocity.normalize());
            final ExplosionCustom boo = new ExplosionCustom(world, this.exploder, absorbedLoc,
                    remainingEnergy * factor);
            boo.setMaxRadius(this.radius);
            boo.breaker = this.breaker;
            boo.owner = this.owner;
            this.subBooms.add(boo);
            hasSubBooms = true;
        }
        this.doExplosion();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void doRemoveBlocks(final LevelTickEvent evt)
    {
        if (evt.phase == Phase.START || evt.level != this.level) return;

        if (this.hasSubBooms)
        {
            if (this.subBooms.isEmpty())
            {
                MinecraftForge.EVENT_BUS.unregister(this);
                boomDone = true;
            }
            else
            {
                ExplosionCustom boom = subBooms.get(0);
                if (boom.boomDone)
                {
                    subBooms.remove(0);
                    if (subBooms.size() > 0) subBooms.get(0).doExplosion();
                }
            }
            return;
        }

        long dt = level.getServer().getNextTickTime() - Util.getMillis();
        this.maxPerTick = Math.min((int) dt / 2, MAXPERTICK);

        this.clearToBlow();
        final BlastResult result = boomApplier.getBlocksToRemove();
        this.applyBlockEffects(result);
        this.applyEntityEffects(result);
        final ExplosionEvent evt2 = new ExplosionEvent.Detonate(this.level, this, this.targets);
        MinecraftForge.EVENT_BUS.post(evt2);

        // Process the chunks and set them not unsaved, this will prevent them
        // re-saving to disk each tick the explosion runs.
        for (ChunkPos pos : result.affectedChunks)
        {
            LevelChunk chunk = this.level.getChunkSource().getChunkNow(pos.x, pos.z);
            if (chunk != null) chunk.setUnsaved(false);
        }

        if (result.done)
        {
            MinecraftForge.EVENT_BUS.unregister(this);
            boomApplier.printDebugInfo();
            boomDone = true;
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
        if (evt.getLevel() == this.level) MinecraftForge.EVENT_BUS.unregister(this);
    }
}
