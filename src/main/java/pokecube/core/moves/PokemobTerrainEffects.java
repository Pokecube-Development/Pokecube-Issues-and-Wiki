package pokecube.core.moves;

import java.util.HashMap;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
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
import thut.api.maths.Vector3;
import thut.api.terrain.TerrainSegment.ITerrainEffect;

public class PokemobTerrainEffects implements ITerrainEffect
{
    public enum WeatherEffectType
    {
        NO_EFFECTS, SAND, RAIN, HAIL, SUN, MIST, CLEAR_WEATHER, MUD, WATER, GRASS, ELECTRIC, MISTY, SPIKES, ROCKS, POISON, POISON2, WEBS;
    }

    public class WeatherEffect
    {
        long duration;
        private WeatherEffectType type;
        private IPokemob mob;

        public WeatherEffect(WeatherEffectType type, long duration, IPokemob mob)
        {
            this.type = type;
            this.duration = duration;
            this.mob = mob;
        }

        public long getDuration() {
            System.out.println(duration);
            return duration;
        }

        public IPokemob getMob() {
            return mob;
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

    private final HashMap<WeatherEffectType, WeatherEffect> weatherEffects;

    int chunkX;
    int chunkZ;
    int chunkY;

    public PokemobTerrainEffects()
    {
        weatherEffects = new HashMap<>();
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
        if (entity.getEntityWorld().getGameTime() % (2 * PokecubeCore.getConfig().attackCooldown) != 0) return;
        if (!AITools.validTargets.test(entity)) return;

        final IPokemob mob = CapabilityPokemob.getPokemobFor(entity);
        if (mob != null)
        {
            if (weatherEffects.containsKey(WeatherEffectType.HAIL) && !mob.isType(PokeType.getType("ice")))
            {
                final float thisMaxHP = entity.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.attackEntityFrom(PokemobTerrainEffects.createHailSource(weatherEffects.get(WeatherEffectType.HAIL).getMob()), damage);
            }

            if (weatherEffects.containsKey(WeatherEffectType.SAND) && !(mob.isType(PokeType.getType("rock"))
                    || mob.isType(PokeType.getType("steel")) || mob.isType(PokeType.getType("ground"))))
            {
                final float thisMaxHP = entity.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.attackEntityFrom(PokemobTerrainEffects.createSandstormSource(weatherEffects.get(WeatherEffectType.SAND).getMob()), damage);
            }

            if (weatherEffects.containsKey(WeatherEffectType.ELECTRIC) && mob.isOnGround())
                if (mob.getStatus() == IMoveConstants.STATUS_SLP) mob.healStatus();

            if (weatherEffects.containsKey(WeatherEffectType.GRASS) && mob.isOnGround())
            {
                final float thisHP = mob.getHealth();
                final float thisMaxHP = mob.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                mob.setHealth(Math.min(thisMaxHP, thisHP + damage));
            }

            if (weatherEffects.containsKey(WeatherEffectType.MISTY) && mob.isOnGround()) if (mob
                    .getStatus() != IMoveConstants.STATUS_NON) mob.healStatus();
        }
        else if (PokecubeCore.getConfig().pokemobsDamagePlayers)
        {
            if (weatherEffects.containsKey(WeatherEffectType.HAIL))
            {
                final float thisMaxHP = entity.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.attackEntityFrom(PokemobTerrainEffects.createHailSource(weatherEffects.get(WeatherEffectType.HAIL).getMob()), damage);
            }

            if (weatherEffects.containsKey(WeatherEffectType.SAND))
            {
                final float thisMaxHP = entity.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.attackEntityFrom(PokemobTerrainEffects.createSandstormSource(weatherEffects.get(WeatherEffectType.SAND).getMob()), damage);
            }

            if (weatherEffects.containsKey(WeatherEffectType.GRASS) && entity.onGround)
            {
                final float thisHP = entity.getHealth();
                final float thisMaxHP = entity.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.setHealth(Math.min(thisMaxHP, thisHP + damage));
            }
        }
        this.dropDurations(entity);
    }

    public boolean isEffectActive(WeatherEffectType effect)
    {
        return weatherEffects.containsKey(effect);
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
            if (weatherEffects.containsKey(WeatherEffectType.POISON) && !mob.isType(PokeType.getType("poison")) && !mob
                    .isType(PokeType.getType("steel")))
                mob.setStatus(IMoveConstants.STATUS_PSN);

            if (weatherEffects.containsKey(WeatherEffectType.POISON2) && !mob.isType(PokeType.getType("poison"))
                    && !mob.isType(PokeType.getType("steel")))
                mob.setStatus(IMoveConstants.STATUS_PSN2);

            if (weatherEffects.containsKey(WeatherEffectType.SPIKES))
            {
                final float thisHP = mob.getHealth();
                final float thisMaxHP = mob.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                mob.setHealth(Math.min(thisMaxHP, thisHP + damage));
            }

            if (weatherEffects.containsKey(WeatherEffectType.ROCKS))
            {
                final float thisMaxHP = mob.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                final double mult = PokeType.getAttackEfficiency(PokeType.getType("rock"), mob.getType1(), mob
                        .getType2());
                entity.attackEntityFrom(DamageSource.GENERIC, (float) (damage * mult));
            }
            if (weatherEffects.containsKey(WeatherEffectType.WEBS) && mob.isOnGround()) MovesUtils.handleStats2(mob,
                    null, IMoveConstants.VIT, IMoveConstants.FALL);
        }
    }

    private void dropDurations(final Entity e) {
        final long time = e.getEntityWorld().getGameTime();
        boolean send = false;

        for (WeatherEffectType type : weatherEffects.keySet()) {

            WeatherEffect effect = weatherEffects.get(type);
            effect.duration -= time;
            if (effect.duration < 0) {
                effect.duration = 0;
                weatherEffects.remove(effect);
                send = true;
            }
        }

        if (send) if (!e.getEntityWorld().isRemote) PacketSyncTerrain.sendTerrainEffects(e, this.chunkX, this.chunkY,
                this.chunkZ, this);
    }

    @Override
    public String getIdentifier()
    {
        return "pokemobEffects";
    }

    public boolean hasWeatherEffects()
    {
        return weatherEffects.isEmpty();
    }

    @Override
    public void readFromNBT(final CompoundNBT nbt)
    {
    }

    @OnlyIn(Dist.CLIENT)
    private void renderEffect(final IVertexBuilder builder, final Matrix4f pos, final Vector3 origin,
            final Vector3 direction, final float tick, final float r, final float g, final float b, final float a)
    {
        final Vector3 temp = Vector3.getNewVector();
        final Vector3 temp2 = Vector3.getNewVector();

        assert Minecraft.getInstance().player != null;
        final Random rand = new Random(Minecraft.getInstance().player.ticksExisted / 200);

        final double dx = direction.x * 1;
        final double dy = direction.y * 1;
        final double dz = direction.z * 1;

        for (int i = 0; i < 1000; i++)
        {
            temp.set(rand.nextFloat() - 0.5, rand.nextFloat() - 0.5, rand.nextFloat() - 0.5);
            temp.scalarMultBy(16);
            temp.addTo(temp2.set(direction).scalarMultBy(tick));
            temp.y = temp.y % 16;
            temp.x = temp.x % 16;
            temp.z = temp.z % 16;
            temp.addTo(origin);
            final float size = 0.03f;
            float x, y, z;

            x = (float) (temp.x + dx);
            y = (float) (temp.y + dy);
            z = (float) (temp.z + dz);
            builder.pos(pos, x, y, z).color(r, g, b, a).endVertex();

            x = (float) (temp.x + dx);
            y = (float) (temp.y - size + dy);
            z = (float) (temp.z + dz);
            builder.pos(pos, x, y, z).color(r, g, b, a).endVertex();

            x = (float) (temp.x + dx);
            y = (float) (temp.y - size + dy);
            z = (float) (temp.z - size + dz);
            builder.pos(pos, x, y, z).color(r, g, b, a).endVertex();

            x = (float) (temp.x + dx);
            y = (float) (temp.y + dy);
            z = (float) (temp.z - size + dz);
            builder.pos(pos, x, y, z).color(r, g, b, a).endVertex();

            // x = (float) (temp.x + dx);
            // y = (float) (temp.y + dy);
            // z = (float) (temp.z + dz);
            // builder.pos(pos, x, y, z).color(r, g, b, a).endVertex();
            //
            // x = (float) (temp.x - size + dx);
            // y = (float) (temp.y + dy);
            // z = (float) (temp.z + dz);
            // builder.pos(pos, x, y, z).color(r, g, b, a).endVertex();
            //
            // x = (float) (temp.x - size + dx);
            // y = (float) (temp.y + dy);
            // z = (float) (temp.z - size + dz);
            // builder.pos(pos, x, y, z).color(r, g, b, a).endVertex();
            //
            // x = (float) (temp.x + dx);
            // y = (float) (temp.y + dy);
            // z = (float) (temp.z - size + dz);
            // builder.pos(pos, x, y, z).color(r, g, b, a).endVertex();

        }
    }

    @OnlyIn(Dist.CLIENT)
    public void renderTerrainEffects(final RenderWorldLastEvent event, final Vector3 origin)
    {
        if (this.hasWeatherEffects())
        {
            final MatrixStack mat = event.getMatrixStack();
            assert Minecraft.getInstance().player != null;
            final int time = Minecraft.getInstance().player.ticksExisted;

            final Vector3 direction = Vector3.getNewVector().set(0, -1, 0);
            final float partialTicks = Minecraft.getInstance().getRenderPartialTicks();
            final float tick = (time + partialTicks) / 10f;

            final IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();

            final RenderType effectType = RenderType.makeType("pokecube:terrain_effects",
                    DefaultVertexFormats.POSITION_COLOR, GL11.GL_QUADS, 256, RenderType.State.getBuilder()
                            .diffuseLighting(new RenderState.DiffuseLightingState(true)).alpha(
                                    new RenderState.AlphaState(0.003921569F)).build(false));

            final IVertexBuilder builder = buffer.getBuffer(effectType);
            final Matrix4f pos = mat.getLast().getMatrix();

            // FIXME figure out the offsets for this
            mat.push();

            if (this.weatherEffects.containsKey(WeatherEffectType.RAIN))
                this.renderEffect(builder, pos, origin, direction, tick, 0, 0, 1, 1);

            if (this.weatherEffects.containsKey(WeatherEffectType.HAIL))
                this.renderEffect(builder, pos, origin, direction, tick, 1, 1, 1, 1);
                direction.set(0, 0, 1);

            if (this.weatherEffects.containsKey(WeatherEffectType.SAND))
                this.renderEffect(builder, pos, origin, direction, tick, 0.86f, 0.82f, 0.75f, 1);

            mat.pop();
        }
    }

    public WeatherEffect getWeatherEffect(WeatherEffectType type)
    {
        return weatherEffects.get(type);
    }

    /**
     * Adds the effect, and removes any non-compatible effects if any
     *
     *
     *            see the EFFECT_ variables owned by this class
     * @param duration
     *            how long this effect lasts, this counter is decreased every
     *            time a pokemob uses a move.
     */

    public void setTerrainEffectDuration(final WeatherEffectType type, final long duration, final IPokemob mob)
    {
        WeatherEffect effect = new WeatherEffect(type, duration, mob);
        effect.duration = duration;
        if(type != WeatherEffectType.NO_EFFECTS) {
            if (!weatherEffects.containsKey(type)) {
                weatherEffects.put(type, effect);
            }else {
                weatherEffects.replace(type, effect);
            }
        }else {
            weatherEffects.clear();
        }
    }

    @Override
    public void writeToNBT(final CompoundNBT nbt)
    {

    }
}
