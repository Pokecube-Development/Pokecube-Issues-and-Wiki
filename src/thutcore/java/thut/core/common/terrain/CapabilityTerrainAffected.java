package thut.core.common.terrain;

import java.util.Collection;

import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import thut.api.ThutCaps;
import thut.api.level.terrain.ITerrainAffected;
import thut.api.level.terrain.TerrainEffectEvent;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;
import thut.api.level.terrain.TerrainSegment.ITerrainEffect;
import thut.core.common.ThutCore;

public class CapabilityTerrainAffected
{
    public static class DefaultAffected implements ITerrainAffected, ICapabilityProvider
    {
        private final LazyOptional<ITerrainAffected> holder = LazyOptional.of(() -> this);
        private LivingEntity theMob;
        private TerrainSegment terrain;
        private Collection<ITerrainEffect> effects;

        @Override
        public void attach(final LivingEntity mob)
        {
            this.theMob = mob;
        }

        @Override
        public LivingEntity getAttached()
        {
            return this.theMob;
        }

        @Override
        public <T> LazyOptional<T> getCapability(final Capability<T> capability, final Direction facing)
        {
            return ThutCaps.TERRAIN_AFFECTED.orEmpty(capability, this.holder);
        }

        public void onTerrainEntry(final TerrainSegment entered)
        {
            if (entered == this.terrain || this.theMob == null) return;
            this.terrain = entered;
            this.effects = this.terrain.getEffects();

            for (final ITerrainEffect effect : this.effects)
            {
                final TerrainEffectEvent event = new TerrainEffectEvent(this.theMob, effect.getIdentifier(), true);
                ThutCore.FORGE_BUS.post(event);
                if (!event.isCanceled()) effect.doEffect(this.theMob, true);
            }
        }

        @Override
        public void onTerrainTick()
        {
            if (this.theMob == null) return;
            if (this.terrain == null)
            {
                var terrain = TerrainManager.getInstance().getTerrainForEntity(this.theMob);
                this.onTerrainEntry(terrain);
                return;
            }
            var mobPos = SectionPos.of(this.theMob.blockPosition());
            boolean samePos = mobPos.x() == this.terrain.chunkX && mobPos.y() == this.terrain.chunkY
                    && mobPos.y() == this.terrain.chunkY;
            if (!samePos)
            {
                var terrain = TerrainManager.getInstance().getTerrainForEntity(this.theMob);
                this.onTerrainEntry(terrain);
                return;
            }
            if (this.effects == null) return;
            for (final ITerrainEffect effect : this.effects)
            {
                final TerrainEffectEvent event = new TerrainEffectEvent(this.theMob, effect.getIdentifier(), false);
                ThutCore.FORGE_BUS.post(event);
                if (!event.isCanceled()) effect.doEffect(this.theMob, false);
            }
        }

    }

    private static final ResourceLocation TERRAINEFFECTCAP = new ResourceLocation(ThutCore.MODID, "terrain_effects");

    public static void init()
    {
        ThutCore.FORGE_BUS.addListener(CapabilityTerrainAffected::EntityUpdate);
        ThutCore.FORGE_BUS.addGenericListener(Entity.class, CapabilityTerrainAffected::onEntityCapabilityAttach);
    }

    private static void EntityUpdate(final LivingUpdateEvent evt)
    {
        final ITerrainAffected effects = ThutCaps.getTerrainAffected(evt.getEntity());
        if (effects != null) effects.onTerrainTick();
    }

    private static void onEntityCapabilityAttach(final AttachCapabilitiesEvent<Entity> event)
    {
        if (!(event.getObject() instanceof LivingEntity living)
                || event.getCapabilities().containsKey(CapabilityTerrainAffected.TERRAINEFFECTCAP))
            return;
        final DefaultAffected effects = new DefaultAffected();
        effects.attach(living);
        event.addCapability(CapabilityTerrainAffected.TERRAINEFFECTCAP, effects);
    }
}
