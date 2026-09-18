package pokecube.core.moves;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.joml.Vector3f;
import pokecube.api.effects.EffectPacketInfo;
import pokecube.api.effects.ParticleEffects;
import pokecube.api.effects.VectorPositionSource;
import pokecube.api.effects.context.MoveEntryContext;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.utils.PokeType;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.eventhandlers.EventsHandler;
import pokecube.core.moves.damage.effects.StatusEffects;
import pokecube.core.moves.damage.sources.TerrainDamageSource;
import pokecube.core.moves.damage.sources.TerrainDamageSource.TerrainType;
import pokecube.core.utils.AITools;
import thut.api.Tracker;
import thut.api.level.terrain.TerrainSegment;
import thut.api.level.terrain.TerrainSegment.ITerrainEffect;
import thut.core.common.ThutCore;
import thut.core.common.network.TerrainUpdate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class PokemobTerrainEffects implements ITerrainEffect
{
    public interface EffectType
    {
        int getIndex();
    }

    static Int2ObjectArrayMap<EffectType> EFFECTS = new Int2ObjectArrayMap<>(16);

    public enum WeatherEffectType implements EffectType
    {
        SAND(1), RAIN(2), HAIL(3), SUN(4), MIST(10);

        final int index;

        WeatherEffectType(final int index)
        {
            this.index = index;
            EFFECTS.put(index, this);
        }

        @Override
        public int getIndex()
        {
            return this.index;
        }
    }

    public enum TerrainEffectType implements EffectType
    {
        MUD(5), WATER(6), GRASS(7), ELECTRIC(8), MISTY(9), PSYCHIC(16);

        final int index;

        TerrainEffectType(final int index)
        {
            this.index = index;
            EFFECTS.put(index, this);
        }

        @Override
        public int getIndex()
        {
            return this.index;
        }
    }

    public enum EntryEffectType implements EffectType
    {
        SPIKES(11), ROCKS(12), POISON(13), POISON2(14), WEBS(15);

        final int index;

        EntryEffectType(final int index)
        {
            this.index = index;
            EFFECTS.put(index, this);
        }

        @Override
        public int getIndex()
        {
            return this.index;
        }
    }

    public enum NoEffects implements EffectType
    {
        NO_EFFECTS(-1), CLEAR_WEATHER(0);

        final int index;

        NoEffects(final int index)
        {
            this.index = index;
            EFFECTS.put(index, this);
        }

        @Override
        public int getIndex()
        {
            return this.index;
        }
    }

    public static Map<EffectType, Supplier<MoveEntry>> ANIMATION_SOURCES = new HashMap<>();

    public static class Effect
    {
        private long endTick;
        private final EffectType type;
        private IPokemob mob;
        protected final UUID mobID;
        private Level level;
        private final PokemobTerrainEffects holder;
        private EffectPacketInfo renderEffect;

        public Effect(PokemobTerrainEffects holder, final EffectType type, final long duration, final IPokemob mob)
        {
            this.holder = holder;
            this.type = type;
            this.endTick = duration;
            this.mob = mob;
            if (mob != null) this.mobID = mob.getEntity().getUUID();
            else this.mobID = null;
        }

        public Effect(PokemobTerrainEffects holder, final EffectType type, final long duration, final UUID mob)
        {
            this.holder = holder;
            this.type = type;
            this.endTick = duration;
            this.mobID = mob;
        }

        public long getEndTick()
        {
            return this.endTick;
        }

        public IPokemob getMob(ServerLevel level)
        {
            if (this.mob == null && this.mobID != null)
                this.mob = PokemobCaps.getPokemobFor(level.getEntity(this.mobID));
            return this.mob;
        }

        public void end()
        {
            if (renderEffect != null)
            {
                renderEffect.currentTick = renderEffect.removalTick;
                renderEffect = null;
                ThutCore.FORGE_BUS.unregister(this);
            }
            if (this.level != null && !this.level.isClientSide())
            {
                holder.segment.chunk.setUnsaved(true);
                TerrainUpdate.sendTerrainToWatching(holder.segment);
            }
        }

        public void start(Level level, int chunkX, int chunkY, int chunkZ)
        {
            var entry = ANIMATION_SOURCES.getOrDefault(type, () -> null).get();
            if (entry != null && entry.getAnimation() != null)
            {
                Vector3f chunkMid = new Vector3f(chunkX * 16 + 8, chunkY * 16 + 8, chunkZ * 16 + 8);
                var source = new VectorPositionSource(chunkMid);
                Vector3f target = new Vector3f(level.random.nextFloat(), 0, level.random.nextFloat()).normalize();
                var end = new VectorPositionSource(target.add(chunkMid));
                renderEffect = new EffectPacketInfo(entry.getAnimation(), level, source, end, 1, 1);
                renderEffect.setContext(new MoveEntryContext(entry));
                ParticleEffects.ADD_FOR_RENDER.accept(renderEffect);
                this.level = level;
                ThutCore.FORGE_BUS.register(this);
            }
        }

        @SubscribeEvent
        public void onLevelUnload(LevelEvent.Unload event)
        {
            if (event.getLevel() == this.level) ThutCore.FORGE_BUS.unregister(this);
        }

        @SubscribeEvent
        public void tick(LevelTickEvent.Pre event)
        {
            if (event.getLevel() != this.level) return;
            long remaining = endTick - Tracker.instance().getTick();
            if (renderEffect != null && renderEffect.isFinished() && remaining > 0)
            {
                renderEffect.currentTick = 0;
                ParticleEffects.ADD_FOR_RENDER.accept(renderEffect);
            }
            if (remaining <= 0) this.end();
        }

        public EffectType getType()
        {
            return this.type;
        }
    }

    public static TerrainDamageSource createHailSource(final IPokemob mobIn)
    {
        return new TerrainDamageSource(WeatherEffectType.HAIL, TerrainType.TERRAIN, mobIn);
    }

    public static TerrainDamageSource createSandstormSource(final IPokemob mobIn)
    {
        return new TerrainDamageSource(WeatherEffectType.SAND, TerrainType.TERRAIN, mobIn);
    }

    private final Int2ObjectArrayMap<Effect> effects = new Int2ObjectArrayMap<>();

    int chunkX;
    int chunkZ;
    int chunkY;

    public TerrainSegment segment;

    public PokemobTerrainEffects() {}

    @Override
    public void bindToTerrain(TerrainSegment segment)
    {
        this.chunkX = segment.chunkX;
        this.chunkY = segment.chunkY;
        this.chunkZ = segment.chunkZ;
        this.segment = segment;
    }

    public void doEffect(final LivingEntity entity)
    {
        if (EventsHandler.COOLDOWN_BASED
                && Tracker.instance().getTick() % (2L * PokecubeCore.getConfig().attackCooldown) != 0) return;
        if (!AITools.validCombatTargets.test(entity) || !(entity.level() instanceof ServerLevel level)) return;
        final IPokemob mob = PokemobCaps.getPokemobFor(entity);
        boolean immune = false;
        final float thisMaxHP = entity.getMaxHealth();
        float damage = 0;
        final boolean onGround = mob != null ? mob.onGround() : entity.onGround();
        DamageSource source = null;
        if (this.effects.containsKey(WeatherEffectType.HAIL.getIndex()))
        {
            damage = Math.max(1, (int) (0.0625 * thisMaxHP));
            immune = mob != null && mob.isType(PokeType.getType("ice"));
            source = PokemobTerrainEffects.createHailSource(
                    this.effects.get(WeatherEffectType.HAIL.getIndex()).getMob(level));
        }
        if (this.effects.containsKey(WeatherEffectType.SAND.getIndex()))
        {
            damage = Math.max(1, (int) (0.0625 * thisMaxHP));
            immune = mob != null && (mob.isType(PokeType.getType("rock")) || mob.isType(PokeType.getType("steel"))
                    || mob.isType(PokeType.getType("ground")));
            source = PokemobTerrainEffects.createSandstormSource(
                    this.effects.get(WeatherEffectType.SAND.getIndex()).getMob(level));
        }

        if (this.effects.containsKey(TerrainEffectType.GRASS.getIndex()) && onGround)
        {
            final float thisHP = entity.getHealth();
            damage = (float) Math.max(1, 0.0625 * thisMaxHP);
            entity.setHealth(Math.min(thisMaxHP, thisHP + damage));
        }

        if (!(entity instanceof ServerPlayer))
        {
            if (this.effects.containsKey(TerrainEffectType.ELECTRIC.getIndex()) && onGround && mob != null)
                if (mob.getEntity().hasEffect(StatusEffects.SLEEP)) mob.healStatus();
            if (this.effects.containsKey(TerrainEffectType.MISTY.getIndex()) && onGround && mob != null)
                if (StatusEffects.hasAnyStatusEffects(mob.getEntity())) mob.healStatus();
        }
        else if (!PokecubeCore.getConfig().pokemobsDamagePlayers) immune = true;

        if (source != null && !immune) entity.hurt(source, damage);
    }

    public boolean isEffectActive(final EffectType effect)
    {
        int i = effect.getIndex();
        boolean has = this.effects.containsKey(i);
        if (has)
        {
            var e = this.effects.get(i);
            boolean done = Tracker.instance().getTick() > e.endTick;
            if (done)
            {
                has = false;
                this.effects.remove(i);
            }
        }
        return has;
    }

    @Override
    public void doEffect(final LivingEntity entity, final boolean firstEntry)
    {
        if (!this.hasEffects()) return;
        if (firstEntry) this.doEntryEffect(entity);
        else this.doEffect(entity);
    }

    public void doEntryEffect(final LivingEntity entity)
    {
        final IPokemob mob = PokemobCaps.getPokemobFor(entity);
        if (mob != null && entity.level() instanceof ServerLevel level)
        {
            if (this.effects.containsKey(EntryEffectType.POISON.getIndex()))
            {
                var user = this.effects.get(EntryEffectType.POISON.getIndex()).getMob(level);
                StatusEffects.setStatus(mob, user, IMoveConstants.STATUS_PSN);
            }
            if (this.effects.containsKey(EntryEffectType.POISON2.getIndex()))
            {
                var user = this.effects.get(EntryEffectType.POISON2.getIndex()).getMob(level);
                StatusEffects.setStatus(mob, user, IMoveConstants.STATUS_PSN2);
            }
            if (this.effects.containsKey(EntryEffectType.SPIKES.getIndex()))
            {
                final float thisHP = mob.getHealth();
                final float thisMaxHP = mob.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                mob.setHealth(Math.min(thisMaxHP, thisHP + damage));
            }
            if (this.effects.containsKey(EntryEffectType.ROCKS.getIndex()))
            {
                final float thisMaxHP = mob.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                final double mult = Tools.getAttackEfficiency(PokeType.getType("rock"), mob.getType1(), mob.getType2());
                entity.hurt(entity.damageSources().generic(), (float) (damage * mult));
            }
            if (this.effects.containsKey(EntryEffectType.WEBS.getIndex()) && mob.onGround())
                MovesUtils.handleStats2(mob, null, IMoveConstants.VIT, IMoveConstants.FALL);
        }
    }

    @Override
    public String getIdentifier()
    {
        return "pokemob_effects";
    }

    public boolean hasEffects()
    {
        // Validate and remove any that are finished
        List<Effect> toTest = new ArrayList<>(this.effects.values());
        long tick = Tracker.instance().getTick();
        toTest.forEach(e -> {if (e.endTick <= tick) this.effects.remove(e.type.getIndex());});
        return !this.effects.isEmpty();
    }

    @Override
    public void readFromNBT(final CompoundTag nbt)
    {
        if (nbt.contains("e"))
        {
            long tick = Tracker.instance().getTick();
            var list = nbt.getList("e", CompoundTag.TAG_COMPOUND);
            for (var e : list)
            {
                CompoundTag tag = (CompoundTag) e;
                int i = tag.getInt("i");
                UUID id = tag.contains("u") ? UUIDUtil.uuidFromIntArray(tag.getIntArray("u")) : null;
                long duration = tag.getLong("t");
                if (effects.containsKey(i)) effects.get(i).end();
                if (duration > tick)
                {
                    var effect = new Effect(this, EFFECTS.get(i), duration, id);
                    effects.put(i, effect);
                    var level = segment.chunk.getLevel();
                    if (level != null)
                    {
                        effect.start(level, chunkX, chunkY, chunkZ);
                    }
                }
            }
        }
    }

    @Override
    public void writeToNBT(final CompoundTag nbt)
    {
        ListTag list = new ListTag();
        for (var effect : effects.values())
        {
            CompoundTag tag = new CompoundTag();
            tag.putInt("i", effect.getType().getIndex());
            if (effect.mobID != null) tag.putIntArray("u", UUIDUtil.uuidToIntArray(effect.mobID));
            tag.putLong("t", effect.getEndTick());
            list.add(tag);
        }
        if (!list.isEmpty()) nbt.put("e", list);
    }

    public Effect getEffect(final EffectType type)
    {
        return this.effects.get(type.getIndex());
    }

    /**
     * Adds the effect, and removes any non-compatible effects if any see the EFFECT_ variables owned by this class
     *
     * @param duration how long this effect lasts, this counter is decreased every time a pokemob uses a move.
     */

    public void setEffectDuration(final EffectType type, final long duration, final IPokemob mob)
    {
        final Effect effect = new Effect(this, type, duration, mob);
        effect.endTick = duration;
        if (type != NoEffects.NO_EFFECTS)
        {
            if (type != NoEffects.CLEAR_WEATHER)
            {
                if (!this.effects.containsKey(type.getIndex())) this.effects.put(type.getIndex(), effect);
                else this.effects.replace(type.getIndex(), effect).end();
            }
            else
            {
                this.effects.values().forEach(Effect::end);
                this.effects.clear();
                this.effects.put(type.getIndex(), effect);
            }
        }
        else
        {
            this.effects.values().forEach(Effect::end);
            this.effects.clear();
        }
        if(this.segment.chunk != null) this.segment.chunk.setUnsaved(true);
    }
}
