package pokecube.core.moves;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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
    public static final int EFFECT_WEATHER_SAND     = 1;
    public static final int EFFECT_WEATHER_RAIN     = 2;
    public static final int EFFECT_WEATHER_HAIL     = 3;
    public static final int EFFECT_WEATHER_SUN      = 4;
    public static final int EFFECT_SPORT_MUD        = 5;
    public static final int EFFECT_SPORT_WATER      = 6;
    public static final int EFFECT_TERRAIN_GRASS    = 7;
    public static final int EFFECT_TERRAIN_ELECTRIC = 8;
    public static final int EFFECT_TERRAIN_MISTY    = 9;
    public static final int EFFECT_MIST             = 10;
    public static final int EFFECT_SPIKES           = 11;
    public static final int EFFECT_ROCKS            = 12;
    public static final int EFFECT_POISON           = 13;
    public static final int EFFECT_POISON2          = 14;
    public static final int EFFECT_WEBS             = 15;

    public static final int CLEAR_ENTRYEFFECTS = 16;

    public static final TerrainDamageSource createHailSource(final IPokemob mobIn)
    {
        return new TerrainDamageSource("terrain.hail", TerrainType.TERRAIN, mobIn);
    }

    public static final TerrainDamageSource createSandstormSource(final IPokemob mobIn)
    {
        return new TerrainDamageSource("terrain.sandstorm", TerrainType.TERRAIN, mobIn);
    }

    public final long[] effects = new long[16];

    int chunkX;
    int chunkZ;
    int chunkY;

    Set<IPokemob> pokemon = new HashSet<>();

    IPokemob[] users = new IPokemob[16];

    public PokemobTerrainEffects()
    {
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
            if (this.effects[PokemobTerrainEffects.EFFECT_WEATHER_HAIL] > 0 && !mob.isType(PokeType.getType("ice")))
            {
                final float thisMaxHP = entity.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.attackEntityFrom(PokemobTerrainEffects.createHailSource(
                        this.users[PokemobTerrainEffects.EFFECT_WEATHER_HAIL]), damage);
            }
            if (this.effects[PokemobTerrainEffects.EFFECT_WEATHER_SAND] > 0 && !(mob.isType(PokeType.getType("rock"))
                    || mob.isType(PokeType.getType("steel")) || mob.isType(PokeType.getType("ground"))))
            {
                final float thisMaxHP = entity.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.attackEntityFrom(PokemobTerrainEffects.createSandstormSource(
                        this.users[PokemobTerrainEffects.EFFECT_WEATHER_SAND]), damage);
            }
            if (this.effects[PokemobTerrainEffects.EFFECT_TERRAIN_ELECTRIC] > 0 && mob.isOnGround()) if (mob
                    .getStatus() == IMoveConstants.STATUS_SLP) mob.healStatus();
            if (this.effects[PokemobTerrainEffects.EFFECT_TERRAIN_GRASS] > 0 && mob.isOnGround())
            {
                final float thisHP = mob.getHealth();
                final float thisMaxHP = mob.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                mob.setHealth(Math.min(thisMaxHP, thisHP + damage));
            }
            if (this.effects[PokemobTerrainEffects.EFFECT_TERRAIN_MISTY] > 0 && mob.isOnGround()) if (mob
                    .getStatus() != IMoveConstants.STATUS_NON) mob.healStatus();
        }
        else if (PokecubeCore.getConfig().pokemobsDamagePlayers)
        {
            if (this.effects[PokemobTerrainEffects.EFFECT_WEATHER_HAIL] > 0)
            {
                final float thisMaxHP = entity.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.attackEntityFrom(PokemobTerrainEffects.createHailSource(
                        this.users[PokemobTerrainEffects.EFFECT_WEATHER_HAIL]), damage);
            }
            if (this.effects[PokemobTerrainEffects.EFFECT_WEATHER_SAND] > 0)
            {
                final float thisMaxHP = entity.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.attackEntityFrom(PokemobTerrainEffects.createSandstormSource(
                        this.users[PokemobTerrainEffects.EFFECT_WEATHER_SAND]), damage);
            }
            if (this.effects[PokemobTerrainEffects.EFFECT_TERRAIN_GRASS] > 0 && entity.onGround)
            {
                final float thisHP = entity.getHealth();
                final float thisMaxHP = entity.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                entity.setHealth(Math.min(thisMaxHP, thisHP + damage));
            }
        }
        this.dropDurations(entity);
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
            if (this.effects[PokemobTerrainEffects.EFFECT_POISON] > 0 && !mob.isType(PokeType.getType("poison")) && !mob
                    .isType(PokeType.getType("steel"))) mob.setStatus(IMoveConstants.STATUS_PSN);
            if (this.effects[PokemobTerrainEffects.EFFECT_POISON2] > 0 && !mob.isType(PokeType.getType("poison"))
                    && !mob.isType(PokeType.getType("steel"))) mob.setStatus(IMoveConstants.STATUS_PSN2);
            if (this.effects[PokemobTerrainEffects.EFFECT_SPIKES] > 0)
            {
                final float thisHP = mob.getHealth();
                final float thisMaxHP = mob.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                mob.setHealth(Math.min(thisMaxHP, thisHP + damage));
            }
            if (this.effects[PokemobTerrainEffects.EFFECT_ROCKS] > 0)
            {
                final float thisMaxHP = mob.getMaxHealth();
                final int damage = Math.max(1, (int) (0.0625 * thisMaxHP));
                final double mult = PokeType.getAttackEfficiency(PokeType.getType("rock"), mob.getType1(), mob
                        .getType2());
                entity.attackEntityFrom(DamageSource.GENERIC, (float) (damage * mult));
            }
            if (this.effects[PokemobTerrainEffects.EFFECT_WEBS] > 0 && mob.isOnGround()) MovesUtils.handleStats2(mob,
                    null, IMoveConstants.VIT, IMoveConstants.FALL);
        }
    }

    private void dropDurations(final Entity e)
    {
        final long time = e.getEntityWorld().getGameTime();
        boolean send = false;
        for (int i = 0; i < this.effects.length; i++)
            if (this.effects[i] > 0)
            {
                final long diff = this.effects[i] - time;
                if (diff < 0)
                {
                    this.effects[i] = 0;
                    this.users[i] = null;
                    send = true;
                }
            }
        if (send) if (!e.getEntityWorld().isRemote) PacketSyncTerrain.sendTerrainEffects(e, this.chunkX, this.chunkY,
                this.chunkZ, this);
    }

    public long getEffect(final int effect)
    {
        return this.effects[effect];
    }

    @Override
    public String getIdenitifer()
    {
        return "pokemobEffects";
    }

    public boolean hasEffects()
    {
        final boolean ret = false;
        for (int i = 1; i < 16; i++)
            if (this.effects[i] > 0) return true;
        return ret;
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
            if (this.effects[PokemobTerrainEffects.EFFECT_WEATHER_RAIN] > 0) this.renderEffect(builder, pos, origin,
                    direction, tick, 0, 0, 1, 1);
            if (this.effects[PokemobTerrainEffects.EFFECT_WEATHER_HAIL] > 0) this.renderEffect(builder, pos, origin,
                    direction, tick, 1, 1, 1, 1);
            direction.set(0, 0, 1);
            if (this.effects[PokemobTerrainEffects.EFFECT_WEATHER_SAND] > 0) this.renderEffect(builder, pos, origin,
                    direction, tick, 0.86f, 0.82f, 0.75f, 1);
            mat.pop();
        }
    }

    /**
     * Adds the effect, and removes any non-compatible effects if any
     *
     * @param effect
     *            see the EFFECT_ variables owned by this class
     * @param duration
     *            how long this effect lasts, this counter is decreased every
     *            time a pokemob uses a move.
     */
    public void setEffect(final int effect, final long duration)
    {
        this.users[effect] = user;
        if (effect == PokemobTerrainEffects.EFFECT_WEATHER_HAIL)
        {
            this.effects[PokemobTerrainEffects.EFFECT_WEATHER_RAIN] = 0;
            this.effects[PokemobTerrainEffects.EFFECT_WEATHER_SUN] = 0;
            this.effects[PokemobTerrainEffects.EFFECT_WEATHER_SAND] = 0;
        }
        if (effect == PokemobTerrainEffects.EFFECT_WEATHER_SUN)
        {
            this.effects[PokemobTerrainEffects.EFFECT_WEATHER_RAIN] = 0;
            this.effects[PokemobTerrainEffects.EFFECT_WEATHER_HAIL] = 0;
            this.effects[PokemobTerrainEffects.EFFECT_WEATHER_SAND] = 0;
        }
        if (effect == PokemobTerrainEffects.EFFECT_WEATHER_RAIN)
        {
            this.effects[PokemobTerrainEffects.EFFECT_WEATHER_HAIL] = 0;
            this.effects[PokemobTerrainEffects.EFFECT_WEATHER_SUN] = 0;
            this.effects[PokemobTerrainEffects.EFFECT_WEATHER_SAND] = 0;
        }
        if (effect == PokemobTerrainEffects.EFFECT_WEATHER_SAND)
        {
            this.effects[PokemobTerrainEffects.EFFECT_WEATHER_RAIN] = 0;
            this.effects[PokemobTerrainEffects.EFFECT_WEATHER_SUN] = 0;
            this.effects[PokemobTerrainEffects.EFFECT_WEATHER_HAIL] = 0;
        }
        if (effect == PokemobTerrainEffects.EFFECT_TERRAIN_ELECTRIC)
            this.effects[PokemobTerrainEffects.EFFECT_TERRAIN_GRASS] = this.effects[PokemobTerrainEffects.EFFECT_TERRAIN_MISTY] = 0;
        if (effect == PokemobTerrainEffects.EFFECT_TERRAIN_GRASS)
            this.effects[PokemobTerrainEffects.EFFECT_TERRAIN_ELECTRIC] = this.effects[PokemobTerrainEffects.EFFECT_TERRAIN_MISTY] = 0;
        if (effect == PokemobTerrainEffects.EFFECT_TERRAIN_MISTY)
            this.effects[PokemobTerrainEffects.EFFECT_TERRAIN_GRASS] = this.effects[PokemobTerrainEffects.EFFECT_TERRAIN_ELECTRIC] = 0;
        if (effect == PokemobTerrainEffects.CLEAR_ENTRYEFFECTS)
            this.effects[PokemobTerrainEffects.EFFECT_POISON] = this.effects[PokemobTerrainEffects.EFFECT_POISON2] = this.effects[PokemobTerrainEffects.EFFECT_SPIKES] = this.effects[PokemobTerrainEffects.EFFECT_ROCKS] = this.effects[PokemobTerrainEffects.EFFECT_WEBS] = 0;
        else this.effects[effect] = duration;
    }

    @Override
    public void writeToNBT(final CompoundNBT nbt)
    {

    }
}
