package thut.core.common.terrain;

import java.util.Collection;

import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import thut.api.terrain.TerrainEffectEvent;
import thut.api.terrain.TerrainManager;
import thut.api.terrain.TerrainSegment;
import thut.api.terrain.TerrainSegment.ITerrainEffect;
import thut.core.common.ThutCore;
import thut.core.common.terrain.CapabilityTerrainAffected.ITerrainAffected;

public class CapabilityTerrainAffected
{
    public static class DefaultAffected implements ITerrainAffected, ICapabilityProvider
    {
        private final LazyOptional<ITerrainAffected> holder = LazyOptional.of(() -> this);
        private LivingEntity                         theMob;
        private TerrainSegment                       terrain;
        private Collection<ITerrainEffect>           effects;

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
            return CapabilityTerrainAffected.TERRAIN_CAP.orEmpty(capability, this.holder);
        }

        public void onTerrainEntry(final TerrainSegment entered)
        {
            if (entered == this.terrain || this.theMob == null) return;
            this.terrain = entered;
            this.effects = this.terrain.getEffects();

            for (final ITerrainEffect effect : this.effects)
            {
                final TerrainEffectEvent event = new TerrainEffectEvent(this.theMob, effect.getIdentifier(), true);
                if (!MinecraftForge.EVENT_BUS.post(event)) effect.doEffect(this.theMob, true);
            }
        }

        @Override
        public void onTerrainTick()
        {
            if (this.theMob == null) return;
            final TerrainSegment current = TerrainManager.getInstance().getTerrainForEntity(this.theMob);
            if (current != this.terrain)
            {
                this.onTerrainEntry(current);
                return;
            }
            if (this.effects == null) return;
            for (final ITerrainEffect effect : this.effects)
            {
                final TerrainEffectEvent event = new TerrainEffectEvent(this.theMob, effect.getIdentifier(), false);
                if (!MinecraftForge.EVENT_BUS.post(event)) effect.doEffect(this.theMob, false);
            }
        }

    }

    public static interface ITerrainAffected
    {
        void attach(LivingEntity mob);

        LivingEntity getAttached();

        void onTerrainTick();
    }

    private static final ResourceLocation TERRAINEFFECTCAP = new ResourceLocation(ThutCore.MODID, "terrain_effects");

    @CapabilityInject(ITerrainAffected.class)
    public static final Capability<ITerrainAffected> TERRAIN_CAP = null;

    public static void init()
    {
        MinecraftForge.EVENT_BUS.addListener(CapabilityTerrainAffected::EntityUpdate);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, CapabilityTerrainAffected::onEntityCapabilityAttach);

        CapabilityManager.INSTANCE.register(ITerrainAffected.class, new Capability.IStorage<ITerrainAffected>()
        {
            @Override
            public void readNBT(final Capability<ITerrainAffected> capability, final ITerrainAffected instance,
                    final Direction side, final Tag nbt)
            {
            }

            @Override
            public Tag writeNBT(final Capability<ITerrainAffected> capability, final ITerrainAffected instance,
                    final Direction side)
            {
                return null;
            }
        }, DefaultAffected::new);
    }

    private static void EntityUpdate(final LivingUpdateEvent evt)
    {
        final ITerrainAffected effects = evt.getEntity().getCapability(CapabilityTerrainAffected.TERRAIN_CAP, null)
                .orElse(null);
        if (effects != null) effects.onTerrainTick();
    }

    private static void onEntityCapabilityAttach(final AttachCapabilitiesEvent<Entity> event)
    {
        if (!(event.getObject() instanceof LivingEntity) || event.getCapabilities().containsKey(
                CapabilityTerrainAffected.TERRAINEFFECTCAP)) return;
        final DefaultAffected effects = new DefaultAffected();
        effects.attach((LivingEntity) event.getObject());
        event.addCapability(CapabilityTerrainAffected.TERRAINEFFECTCAP, effects);
    }
}
