package pokecube.core.moves;

import java.util.HashMap;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.interfaces.IMoveConstants;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;
import pokecube.core.moves.damage.TerrainDamageSource;
import pokecube.core.moves.damage.TerrainDamageSource.TerrainType;
import pokecube.core.network.packets.PacketSyncTerrain;
import pokecube.core.utils.AITools;
import pokecube.core.utils.PokeType;
import thut.api.Tracker;
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainSegment.ITerrainEffect;

public class PokemobTerrainEffects implements ITerrainEffect
{
    public interface EffectType
    {
        int getIndex();
    }

    static Int2ObjectArrayMap<EffectType> EFFECTS = new Int2ObjectArrayMap<>(16);

    public static EffectType getForIndex(final int extraInfo)
    {
        if (PokemobTerrainEffects.EFFECTS.isEmpty())
        {
            for (final EffectType t : WeatherEffectType.values())
                PokemobTerrainEffects.EFFECTS.put(t.getIndex(), t);
            for (final EffectType t : TerrainEffectType.values())
                PokemobTerrainEffects.EFFECTS.put(t.getIndex(), t);
            for (final EffectType t : EntryEffectType.values())
                PokemobTerrainEffects.EFFECTS.put(t.getIndex(), t);
            for (final EffectType t : NoEffects.values())
                PokemobTerrainEffects.EFFECTS.put(t.getIndex(), t);
        }
        return PokemobTerrainEffects.EFFECTS.getOrDefault(extraInfo, NoEffects.NO_EFFECTS);
    }

    public enum WeatherEffectType implements EffectType
    {
        SAND(1), RAIN(2), HAIL(3), SUN(4), MIST(10);

        final int index;

        WeatherEffectType(final int index)
        {
            this.index = index;
        }

        @Override
        public int getIndex()
        {
            return this.index;
        }
    }

    public enum TerrainEffectType implements EffectType
    {
        MUD(5), WATER(6), GRASS(7), ELECTRIC(8), MISTY(9), PHYSIC(16);

        final int index;

        TerrainEffectType(final int index)
        {
            this.index = index;
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
        }

        @Override
        public int getIndex()
        {
            return this.index;
        }
    }

    public static class Effect
    {
        long                     duration;
        private final EffectType type;
        private final IPokemob   mob;

        public Effect(final EffectType type, final long duration, final IPokemob mob)
        {
            this.type = type;
            this.duration = duration;
            this.mob = mob;
        }

        public long getDuration()
        {
            return this.duration;
        }

        public IPokemob getMob()
        {
            return this.mob;
        }

        public EffectType getType()
        {
            return this.type;
        }
    }

    public static TerrainDamageSource createHailSource(final IPokemob mobIn)
    {
        return new TerrainDamageSource("terrain.hail", TerrainType.TERRAIN, mobIn);
    }

    public static TerrainDamageSource createSandstormSource(final IPokemob mobIn)
    {
        return new TerrainDamageSource("terrain.sandstorm", TerrainType.TERRAIN, mobIn);
    }

    private final HashMap<Integer, Effect> effects;

    int chunkX;
    int chunkZ;
    int chunkY;

    long lastTick = 0;

    public PokemobTerrainEffects()
    {
        this.effects = new HashMap<>();
    }

    @Override
    public void bindToTerrain(final int x, final int y, final int z)
    {
        this.chunkX = x;
        this.chunkY = y;
        this.chunkZ = z;
    }

    public void doEffect(final LivingEntity entity)
    {
        if (Tracker.instance().getTick() % (2 * PokecubeCore.getConfig().attackCooldown) != 0) return;
        if (!AITools.validTargets.test(entity) || !(entity.getCommandSenderWorld() instanceof ServerWorld)) return;

        final IPokemob mob = CapabilityPokemob.getPokemobFor(entity);
        boolean immune = false;
        final float thisMaxHP = entity.getMaxHealth();
        float damage = 0;
        final boolean onGround = mob != null ? mob.isOnGround() : entity.isOnGround();
        DamageSource source = null;
        if (this.effects.containsKey(WeatherEffectType.HAIL.getIndex()))
        {
            damage = Math.max(1, (int) (0.0625 * thisMaxHP));
            immune = mob != null && mob.isType(PokeType.getType("ice"));
            source = PokemobTerrainEffects.createHailSource(this.effects.get(WeatherEffectType.HAIL.getIndex())
                    .getMob());
        }
        if (this.effects.containsKey(WeatherEffectType.SAND.getIndex()))
        {
            damage = Math.max(1, (int) (0.0625 * thisMaxHP));
            immune = mob != null && (mob.isType(PokeType.getType("rock")) || mob.isType(PokeType.getType("steel"))
                    || mob.isType(PokeType.getType("ground")));
            source = PokemobTerrainEffects.createSandstormSource(this.effects.get(WeatherEffectType.SAND.getIndex())
                    .getMob());
        }

        if (this.effects.containsKey(TerrainEffectType.GRASS.getIndex()) && onGround)
        {
            final float thisHP = mob.getHealth();
            damage = (float) Math.max(1, 0.0625 * thisMaxHP);
            mob.setHealth(Math.min(thisMaxHP, thisHP + damage));
        }

        if (!(entity instanceof ServerPlayerEntity))
        {
            if (this.effects.containsKey(TerrainEffectType.ELECTRIC.getIndex()) && onGround && mob != null) if (mob
                    .getStatus() == IMoveConstants.STATUS_SLP) mob.healStatus();
            if (this.effects.containsKey(TerrainEffectType.MISTY.getIndex()) && onGround && mob != null) if (mob
                    .getStatus() != IMoveConstants.STATUS_NON) mob.healStatus();
        }
        else if (!PokecubeCore.getConfig().pokemobsDamagePlayers) immune = true;

        if (source != null && !immune) entity.hurt(source, damage);
        this.dropDurations((ServerWorld) entity.getCommandSenderWorld());
    }

    public boolean isEffectActive(final EffectType effect)
    {
        return this.effects.containsKey(effect.getIndex());
    }

    @Override
    public void doEffect(final LivingEntity entity, final boolean firstEntry)
    {
        if (firstEntry) this.doEntryEffect(entity);
        else this.doEffect(entity);
    }

    public void doEntryEffect(final LivingEntity entity)
    {
        final IPokemob mob = CapabilityPokemob.getPokemobFor(entity);
        if (mob != null)
        {
            if (this.effects.containsKey(EntryEffectType.POISON.getIndex()) && !mob.isType(PokeType.getType("poison"))
                    && !mob.isType(PokeType.getType("steel"))) mob.setStatus(IMoveConstants.STATUS_PSN);

            if (this.effects.containsKey(EntryEffectType.POISON2.getIndex()) && !mob.isType(PokeType.getType("poison"))
                    && !mob.isType(PokeType.getType("steel"))) mob.setStatus(IMoveConstants.STATUS_PSN2);

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
                final double mult = PokeType.getAttackEfficiency(PokeType.getType("rock"), mob.getType1(), mob
                        .getType2());
                entity.hurt(DamageSource.GENERIC, (float) (damage * mult));
            }
            if (this.effects.containsKey(EntryEffectType.WEBS.getIndex()) && mob.isOnGround()) MovesUtils.handleStats2(
                    mob, null, IMoveConstants.VIT, IMoveConstants.FALL);
        }
    }

    private void dropDurations(final ServerWorld world)
    {
        final long time = Tracker.instance().getTick();
        boolean send = false;
        for (final int type : this.effects.keySet())
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
        if (send) if (!world.isClientSide) PacketSyncTerrain.sendTerrainEffects(world, this.chunkX, this.chunkY,
                this.chunkZ, this);
    }

    @Override
    public String getIdentifier()
    {
        return "pokemobEffects";
    }

    public boolean hasEffects()
    {
        return !this.effects.isEmpty();
    }

    @Override
    public void readFromNBT(final CompoundNBT nbt)
    {
    }

    @OnlyIn(Dist.CLIENT)
    private void renderEffect(final IVertexBuilder builder, final Matrix4f pos, final Vector3 origin,
            final Vector3 direction, final float tick, final float r, final float g, final float b, final float a)
    {
        if (Minecraft.getInstance().player == null) return;

        final Vector3 temp = Vector3.getNewVector();
        final Vector3 temp2 = Vector3.getNewVector();
        final Vector3 dir = direction.scalarMult(8);
        final int time = Minecraft.getInstance().player.tickCount;
        final Random rand = new Random(time / 200);

        final double dx = direction.x * 1;
        final double dy = direction.y * 1;
        final double dz = direction.z * 1;

        final int num = Minecraft.getInstance().options.particles == ParticleStatus.ALL ? 10000
                : Minecraft.getInstance().options.particles == ParticleStatus.DECREASED ? 1000 : 100;

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
            builder.vertex(pos, x, y, z).color(r, g, b, a).endVertex();

            x = (float) (temp.x + dx);
            y = (float) (temp.y - size + dy);
            z = (float) (temp.z + dz);
            builder.vertex(pos, x, y, z).color(r, g, b, a).endVertex();

            x = (float) (temp.x + dx);
            y = (float) (temp.y - size + dy);
            z = (float) (temp.z - size + dz);
            builder.vertex(pos, x, y, z).color(r, g, b, a).endVertex();

            x = (float) (temp.x + dx);
            y = (float) (temp.y + dy);
            z = (float) (temp.z - size + dz);
            builder.vertex(pos, x, y, z).color(r, g, b, a).endVertex();

            // Other face
            x = (float) (temp.x + dx);
            y = (float) (temp.y + dy);
            z = (float) (temp.z - size + dz);
            builder.vertex(pos, x, y, z).color(r, g, b, a).endVertex();

            x = (float) (temp.x + dx);
            y = (float) (temp.y - size + dy);
            z = (float) (temp.z - size + dz);
            builder.vertex(pos, x, y, z).color(r, g, b, a).endVertex();

            x = (float) (temp.x + dx);
            y = (float) (temp.y - size + dy);
            z = (float) (temp.z + dz);
            builder.vertex(pos, x, y, z).color(r, g, b, a).endVertex();

            x = (float) (temp.x + dx);
            y = (float) (temp.y + dy);
            z = (float) (temp.z + dz);
            builder.vertex(pos, x, y, z).color(r, g, b, a).endVertex();

        }
    }

    @OnlyIn(Dist.CLIENT)
    public void renderTerrainEffects(final RenderWorldLastEvent event, final Vector3 origin)
    {
        if (this.hasEffects())
        {
            final MatrixStack mat = event.getMatrixStack();
            assert Minecraft.getInstance().player != null;
            final int time = Minecraft.getInstance().player.tickCount;

            final Vector3 direction = Vector3.getNewVector().set(0, -1, 0);
            final float partialTicks = Minecraft.getInstance().getFrameTime();
            final float tick = (time + partialTicks) / 10f;

            final IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();

            final RenderType effectType = RenderType.create("pokecube:terrain_effects",
                    DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256, RenderType.State.builder()
                            .setDiffuseLightingState(new RenderState.DiffuseLightingState(true)).setAlphaState(
                                    new RenderState.AlphaState(0.003921569F)).createCompositeState(false));

            final IVertexBuilder builder = buffer.getBuffer(effectType);
            final Matrix4f pos = mat.last().pose();

            mat.pushPose();
            GlStateManager._enableDepthTest();

            if (this.effects.containsKey(WeatherEffectType.RAIN.getIndex())) this.renderEffect(builder, pos, origin,
                    direction, tick, 0, 0, 1, 1);

            if (this.effects.containsKey(WeatherEffectType.HAIL.getIndex())) this.renderEffect(builder, pos, origin,
                    direction, tick, 1, 1, 1, 1);
            direction.set(0, 0, 1);

            if (this.effects.containsKey(WeatherEffectType.SAND.getIndex())) this.renderEffect(builder, pos, origin,
                    direction, tick, 0.86f, 0.82f, 0.75f, 1);

            GlStateManager._disableDepthTest();
            mat.popPose();
        }
    }

    public Effect getEffect(final EffectType type)
    {
        return this.effects.get(type.getIndex());
    }

    /**
     * Adds the effect, and removes any non-compatible effects if any
     * see the EFFECT_ variables owned by this class
     *
     * @param duration
     *            how long this effect lasts, this counter is decreased every
     *            time a pokemob uses a move.
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
    }

    @Override
    public void writeToNBT(final CompoundNBT nbt)
    {

    }
}
