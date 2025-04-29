package pokecube.core.moves;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.utils.IMoveConstants;
import pokecube.api.utils.PokeType;
import pokecube.api.utils.Tools;
import pokecube.core.PokecubeCore;
import pokecube.core.client.render.mobs.overlays.Utils;
import pokecube.core.eventhandlers.EventsHandler;
import pokecube.core.moves.animations.MoveAnimationHelper;
import pokecube.core.moves.damage.effects.StatusEffects;
import pokecube.core.moves.damage.sources.TerrainDamageSource;
import pokecube.core.moves.damage.sources.TerrainDamageSource.TerrainType;
import pokecube.core.utils.AITools;
import thut.api.Tracker;
import thut.api.level.terrain.TerrainSegment;
import thut.api.level.terrain.TerrainSegment.ITerrainEffect;
import thut.api.maths.Vector3;
import thut.core.common.network.TerrainUpdate;

import java.util.List;
import java.util.Random;
import java.util.UUID;

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

    public static class Effect
    {
        long duration;
        private final EffectType type;
        private IPokemob mob;
        protected final UUID mobID;

        public Effect(final EffectType type, final long duration, final IPokemob mob)
        {
            this.type = type;
            this.duration = duration;
            this.mob = mob;
            if (mob != null) this.mobID = mob.getEntity().getUUID();
            else this.mobID = null;
        }

        public Effect(final EffectType type, final long duration, final UUID mob)
        {
            this.type = type;
            this.duration = duration;
            this.mobID = mob;
        }

        public long getDuration()
        {
            return this.duration;
        }

        public IPokemob getMob(ServerLevel level)
        {
            if (this.mob == null && this.mobID != null)
                this.mob = PokemobCaps.getPokemobFor(level.getEntity(this.mobID));
            return this.mob;
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

    long lastTick = 0;

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
        this.dropDurations(level);
    }

    public boolean isEffectActive(final EffectType effect)
    {
        return this.effects.containsKey(effect.getIndex());
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

    private void dropDurations(final ServerLevel world)
    {
        final long time = Tracker.instance().getTick();
        boolean send = false;
        final List<Integer> effectKeys = Lists.newArrayList(this.effects.keySet());
        for (final int type : effectKeys)
        {
            final Effect effect = this.effects.get(type);
            if (effect.duration < time)
            {
                effect.duration = 0;
                this.effects.remove(type);
                send = true;
            }
        }
        this.lastTick = time;
        if (send) if (!world.isClientSide)
        {
            this.segment.chunk.setUnsaved(true);
            TerrainUpdate.sendTerrainToWatching(this.segment);
        }
    }

    @Override
    public String getIdentifier()
    {
        return "pokemob_effects";
    }

    public boolean hasEffects()
    {
        return !this.effects.isEmpty();
    }

    @Override
    public void readFromNBT(final CompoundTag nbt)
    {
        if (nbt.contains("e"))
        {
            var list = nbt.getList("e", CompoundTag.TAG_COMPOUND);
            for (var e : list)
            {
                CompoundTag tag = (CompoundTag) e;
                int i = tag.getInt("i");
                UUID id = tag.contains("u") ? UUIDUtil.uuidFromIntArray(tag.getIntArray("u")) : null;
                long duration = tag.getLong("t");
                effects.put(i, new Effect(EFFECTS.get(i), duration, id));
                if (segment.chunk.getLevel() != null && segment.chunk.getLevel().isClientSide())
                {
                    MoveAnimationHelper.Instance().addForRender(this);
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
            tag.putLong("t", effect.getDuration());
            list.add(tag);
        }
        if (!list.isEmpty()) nbt.put("e", list);
    }

    @OnlyIn(Dist.CLIENT)
    private void renderEffect(final VertexConsumer builder, final Matrix4f pos, final Vector3 origin,
            final Vector3 direction, final float tick, final float r, final float g, final float b, final float a,
            int j)
    {
        if (Minecraft.getInstance().player == null) return;

        final Vector3 temp = new Vector3();
        final Vector3 temp2 = new Vector3();
        final Vector3 dir = direction.scalarMult(8);
        final int time = Minecraft.getInstance().player.tickCount;
        final Random rand = new Random(time / 200);

        final double dx = direction.x * 1;
        final double dy = direction.y * 1;
        final double dz = direction.z * 1;

        final int num = Minecraft.getInstance().options.particles().get() == ParticleStatus.ALL
                ? 10000
                : Minecraft.getInstance().options.particles().get() == ParticleStatus.DECREASED ? 1000 : 100;

        for (int i = 0; i < num; i++)
        {
            temp.set(rand.nextFloat() - 0.5, rand.nextFloat() - 0.5, rand.nextFloat() - 0.5);
            temp.scalarMultBy(16);
            temp.addTo(temp2.set(direction).scalarMultBy(tick));
            temp.y = temp.y % 16;
            temp.x = temp.x % 16;
            temp.z = temp.z % 16;
            temp.addTo(origin);
            temp.subtractFrom(dir);
            final float size = 0.03f;
            float x, y, z;

            // One face
            x = (float) (temp.x + dx);
            y = (float) (temp.y + dy);
            z = (float) (temp.z + dz);
            builder.addVertex(pos, x, y, z).setColor(r, g, b, a).setLight(j);

            x = (float) (temp.x + dx);
            y = (float) (temp.y - size + dy);
            z = (float) (temp.z + dz);
            builder.addVertex(pos, x, y, z).setColor(r, g, b, a).setLight(j);

            x = (float) (temp.x + dx);
            y = (float) (temp.y - size + dy);
            z = (float) (temp.z - size + dz);
            builder.addVertex(pos, x, y, z).setColor(r, g, b, a).setLight(j);

            x = (float) (temp.x + dx);
            y = (float) (temp.y + dy);
            z = (float) (temp.z - size + dz);
            builder.addVertex(pos, x, y, z).setColor(r, g, b, a).setLight(j);

            // Other face
            x = (float) (temp.x + dx);
            y = (float) (temp.y + dy);
            z = (float) (temp.z - size + dz);
            builder.addVertex(pos, x, y, z).setColor(r, g, b, a).setLight(j);

            x = (float) (temp.x + dx);
            y = (float) (temp.y - size + dy);
            z = (float) (temp.z - size + dz);
            builder.addVertex(pos, x, y, z).setColor(r, g, b, a).setLight(j);

            x = (float) (temp.x + dx);
            y = (float) (temp.y - size + dy);
            z = (float) (temp.z + dz);
            builder.addVertex(pos, x, y, z).setColor(r, g, b, a).setLight(j);

            x = (float) (temp.x + dx);
            y = (float) (temp.y + dy);
            z = (float) (temp.z + dz);
            builder.addVertex(pos, x, y, z).setColor(r, g, b, a).setLight(j);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void renderTerrainEffects(final RenderLevelStageEvent event, final Vector3 origin)
    {
        if (this.hasEffects())
        {
            PoseStack mat = event.getPoseStack();
            int time = Minecraft.getInstance().player.tickCount;

            Vector3 direction = new Vector3().set(0, -1, 0);
            float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(true);
            float tick = (time + partialTicks) / 10f;

            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

            var builder = Utils.makeBuilder(RenderType.textBackground(), buffer);
            var pos = mat.last().pose();

            mat.pushPose();
            RenderSystem.enableBlend();

            int j = 15 << 20 | 15 << 4;

            if (this.effects.containsKey(WeatherEffectType.RAIN.getIndex()))
                this.renderEffect(builder, pos, origin, direction, tick, 0, 0, 1, 0.25f, j);

            if (this.effects.containsKey(WeatherEffectType.HAIL.getIndex()))
                this.renderEffect(builder, pos, origin, direction, tick, 1, 1, 1, 0.25f, j);
            direction.set(0, 0, 1);

            if (this.effects.containsKey(WeatherEffectType.SAND.getIndex()))
                this.renderEffect(builder, pos, origin, direction, tick, 0.86f, 0.82f, 0.75f, 1, j);

            mat.popPose();
        }
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
        final Effect effect = new Effect(type, duration, mob);
        effect.duration = duration;
        if (type != NoEffects.NO_EFFECTS)
        {
            if (type != NoEffects.CLEAR_WEATHER)
            {
                if (!this.effects.containsKey(type.getIndex())) this.effects.put(type.getIndex(), effect);
                else this.effects.replace(type.getIndex(), effect);
            }
            else
            {
                this.effects.clear();
                this.effects.put(type.getIndex(), effect);
            }
        }
        else this.effects.clear();
        this.segment.chunk.setUnsaved(true);
    }
}
