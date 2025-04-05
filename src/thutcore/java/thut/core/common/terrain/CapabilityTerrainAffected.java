package thut.core.common.terrain;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import thut.api.ThutCaps;
import thut.api.level.terrain.ITerrainAffected;
import thut.api.level.terrain.TerrainEffectEvent;
import thut.api.level.terrain.TerrainManager;
import thut.api.level.terrain.TerrainSegment;
import thut.api.level.terrain.TerrainSegment.ITerrainEffect;
import thut.core.common.ThutCore;

public class CapabilityTerrainAffected
{
    public static class DefaultAffected implements ITerrainAffected
    {
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
    
    public static ITerrainAffected makeProvider(final IAttachmentHolder in)
    {
        if(!(in instanceof LivingEntity living)) return null;
        var affected = new DefaultAffected();
        affected.attach(living);
        return affected;
    }

    public static ITerrainAffected get(final IAttachmentHolder in)
    {
        if (in.hasData(TYPE_SAVE.get())) return in.getData(TYPE_SAVE.get());
        return null;
    }
    
    public static final ResourceLocation LOCSAVEABLE = ResourceLocation.parse("thutcore:terrain_effects");

    public static Supplier<AttachmentType<ITerrainAffected>> TYPE_SAVE;
    
    public static void registerAttachment(DeferredRegister<AttachmentType<?>> registry)
    {
        Function<IAttachmentHolder, ITerrainAffected> func_a = CapabilityTerrainAffected::makeProvider;
        var attach_a = AttachmentType.builder(func_a).build();
        TYPE_SAVE = registry.register(LOCSAVEABLE.getPath(), () -> attach_a);
    }

    public static void init()
    {
        ThutCore.FORGE_BUS.addListener(CapabilityTerrainAffected::EntityUpdate);
    }

    private static void EntityUpdate(final EntityTickEvent.Post evt)
    {
        if(!(evt.getEntity() instanceof LivingEntity)) return;
        final ITerrainAffected effects = ThutCaps.getTerrainAffected(evt.getEntity());
        if (effects != null) effects.onTerrainTick();
    }
}
