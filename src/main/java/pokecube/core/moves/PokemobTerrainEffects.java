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

    public interface EffectType{
        int getIndex();
    }

    public enum WeatherEffectType implements EffectType
    {
        SAND (1), RAIN(2), HAIL(3), SUN(4), MIST(10);

        final int index;

        WeatherEffectType(int index)
        {
            this.index = index;
        }

        @Override
        public int getIndex() {
            return index;
        }
    }

    public enum TerrainEffectType implements EffectType
    {
        MUD(5), WATER(6), GRASS(7), ELECTRIC(8), MISTY(9), PHYSIC(16);

        final int index;

        TerrainEffectType(int index)
        {
            this.index = index;
        }

        @Override
        public int getIndex() {
            return index;
        }
    }

    public enum EntryEffectType implements EffectType
    {
        SPIKES(11), ROCKS(12), POISON(13), POISON2(14), WEBS(15);

        final int index;

        EntryEffectType(int index)
        {
            this.index = index;
        }

        @Override
        public int getIndex() {
            return index;
        }
    }

    public enum NoEffects implements EffectType
    {
        NO_EFFECTS(-1), CLEAR_WEATHER (0);

        final int index;

        NoEffects(int index)
        {
            this.index = index;
        }

        @Override
        public int getIndex() {
            return index;
        }
    }

    public static class Effect
    {
        long duration;
        private final EffectType type;
        private final IPokemob mob;

        public Effect(EffectType type, long duration, IPokemob mob)
        {
            this.type = type;
            this.duration = duration;
            this.mob = mob;
        }

        public long getDuration() {
            return duration;
        }

        public IPokemob getMob() {
            return mob;
        }

        public EffectType getType() {
            return type;
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

    public PokemobTerrainEffects()
    {
        effects = new HashMap<>();
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
            if (effects.containsKey(WeatherEffectType.HAIL.getIndex()) && !mob.isType(PokeType.getType("ice")))
            {
                final float thisMaxHP = entity.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.attackEntityFrom(PokemobTerrainEffects.createHailSource(effects.get(WeatherEffectType.HAIL.getIndex()).getMob()), damage);
            }

            if (effects.containsKey(WeatherEffectType.SAND.getIndex()) && !(mob.isType(PokeType.getType("rock"))
                    || mob.isType(PokeType.getType("steel")) || mob.isType(PokeType.getType("ground"))))
            {
                final float thisMaxHP = entity.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.attackEntityFrom(PokemobTerrainEffects.createSandstormSource(effects.get(WeatherEffectType.SAND.getIndex()).getMob()), damage);
            }

            if (effects.containsKey(TerrainEffectType.ELECTRIC.getIndex()) && mob.isOnGround())
                if (mob.getStatus() == IMoveConstants.STATUS_SLP) mob.healStatus();

            if (effects.containsKey(TerrainEffectType.GRASS.getIndex()) && mob.isOnGround())
            {
                final float thisHP = mob.getHealth();
                final float thisMaxHP = mob.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                mob.setHealth(Math.min(thisMaxHP, thisHP + damage));
            }

            if (effects.containsKey(TerrainEffectType.MISTY.getIndex()) && mob.isOnGround()) if (mob
                    .getStatus() != IMoveConstants.STATUS_NON) mob.healStatus();
        }
        else if (PokecubeCore.getConfig().pokemobsDamagePlayers)
        {
            if (effects.containsKey(WeatherEffectType.HAIL.getIndex()))
            {
                final float thisMaxHP = entity.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.attackEntityFrom(PokemobTerrainEffects.createHailSource(effects.get(WeatherEffectType.HAIL.getIndex()).getMob()), damage);
            }

            if (effects.containsKey(WeatherEffectType.SAND.getIndex()))
            {
                final float thisMaxHP = entity.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.attackEntityFrom(PokemobTerrainEffects.createSandstormSource(effects.get(WeatherEffectType.SAND.getIndex()).getMob()), damage);
            }

            if (effects.containsKey(TerrainEffectType.GRASS.getIndex()) && entity.onGround)
            {
                final float thisHP = entity.getHealth();
                final float thisMaxHP = entity.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.setHealth(Math.min(thisMaxHP, thisHP + damage));
            }
        }
        this.dropDurations(entity);
    }

    public boolean isEffectActive(EffectType effect)
    {
        return effects.containsKey(effect.getIndex());
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
            if (effects.containsKey(EntryEffectType.POISON.getIndex()) && !mob.isType(PokeType.getType("poison")) && !mob
                    .isType(PokeType.getType("steel")))
                mob.setStatus(IMoveConstants.STATUS_PSN);

            if (effects.containsKey(EntryEffectType.POISON2.getIndex()) && !mob.isType(PokeType.getType("poison"))
                    && !mob.isType(PokeType.getType("steel")))
                mob.setStatus(IMoveConstants.STATUS_PSN2);

            if (effects.containsKey(EntryEffectType.SPIKES.getIndex()))
            {
                final float thisHP = mob.getHealth();
                final float thisMaxHP = mob.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                mob.setHealth(Math.min(thisMaxHP, thisHP + damage));
            }

            if (effects.containsKey(EntryEffectType.ROCKS.getIndex()))
            {
                final float thisMaxHP = mob.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                final double mult = PokeType.getAttackEfficiency(PokeType.getType("rock"), mob.getType1(), mob
                        .getType2());
                entity.attackEntityFrom(DamageSource.GENERIC, (float) (damage * mult));
            }
            if (effects.containsKey(EntryEffectType.WEBS.getIndex()) && mob.isOnGround()) MovesUtils.handleStats2(mob,
                    null, IMoveConstants.VIT, IMoveConstants.FALL);
        }
    }

    private void dropDurations(final Entity e) {
        final long time = e.getEntityWorld().getGameTime();
        boolean send = false;

        for (int type : effects.keySet()) {

            Effect effect = effects.get(type);
            effect.duration -= time;
            if (effect.duration < 0) {
                effect.duration = 0;
                effects.remove(type);
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

    public boolean hasEffects()
    {
        return effects.isEmpty();
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
        if (this.hasEffects())
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

            if (this.effects.containsKey(WeatherEffectType.RAIN.getIndex()))
                this.renderEffect(builder, pos, origin, direction, tick, 0, 0, 1, 1);

            if (this.effects.containsKey(WeatherEffectType.HAIL.getIndex()))
                this.renderEffect(builder, pos, origin, direction, tick, 1, 1, 1, 1);
                direction.set(0, 0, 1);

            if (this.effects.containsKey(WeatherEffectType.SAND.getIndex()))
                this.renderEffect(builder, pos, origin, direction, tick, 0.86f, 0.82f, 0.75f, 1);

            mat.pop();
        }
    }

    public Effect getEffect(EffectType type)
    {
        return effects.get(type.getIndex());
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

    public void setTerrainEffectDuration(final EffectType type, final long duration, final IPokemob mob) {
        Effect effect = new Effect(type, duration, mob);
        effect.duration = duration;
        if (type != NoEffects.NO_EFFECTS) {
            if (type != NoEffects.CLEAR_WEATHER) {
                if (!effects.containsKey(type.getIndex())) {
                    effects.put(type.getIndex(), effect);
                } else {
                    effects.replace(type.getIndex(), effect);
                }
            } else {
                effects.clear();
                effects.put(type.getIndex(), effect);
            }
        } else {
            effects.clear();
        }
    }

    @Override
    public void writeToNBT(final CompoundNBT nbt)
    {

    }
}
