package pokecube.mobs.moves.attacks;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import org.apache.commons.lang3.NotImplementedException;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.moves.MoveProvider;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.pokemobs.RecallEvent;
import pokecube.api.events.pokemobs.combat.KillEvent;
import pokecube.api.moves.MoveEntry;
import pokecube.api.moves.utils.MoveApplication;
import pokecube.api.moves.utils.MoveApplication.PreApplyTests;
import pokecube.core.moves.damage.sources.PokemobDamageSource;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@MoveProvider(name = "destiny-bond")
public class DestinyBond implements PreApplyTests
{
    static Map<UUID, Set<UUID>> usedOn = Maps.newHashMap();
    static MoveEntry dbond = null;

    static
    {
        PokecubeAPI.POKEMOB_BUS.register(DestinyBond.class);
    }

    @SubscribeEvent
    public static void onKill(final KillEvent event)
    {

        if (dbond == null)
        {
            dbond = MoveEntry.get("destiny-bond");
            if (dbond == null)
            {
                PokecubeAPI.POKEMOB_BUS.unregister(DestinyBond.class);
                return;
            }
        }

        final UUID killed = event.killedEntity.getUUID();
        final Set<UUID> targets = usedOn.remove(killed);

        if (targets != null && event.killedEntity.level() instanceof ServerLevel world)
        {
            PokecubeAPI.LOGGER.error(new NotImplementedException("destiny-bond"));
            final DamageSource source = new PokemobDamageSource(event.killedEntity, dbond);
            source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS);
            source.is(DamageTypeTags.BYPASSES_ARMOR);
            for (final UUID id : targets)
            {
                final Entity mob = world.getEntity(id);
                if (mob != null && !mob.isInvulnerable()) mob.hurt(source, Float.MAX_VALUE);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRecall(final RecallEvent event)
    {
        usedOn.remove(event.recalled.getEntity().getUUID());
    }

    @Override
    public boolean checkPreApply(MoveApplication t)
    {
        LivingEntity target = t.getTarget();
        if (target == null) return false;
        final LivingEntity attackerMob = t.getUserEntity();
        final UUID userId = attackerMob.getUUID();
        final Set<UUID> hits = usedOn.getOrDefault(userId, Sets.newHashSet());
        final boolean added = hits.add(target.getUUID());
        usedOn.put(userId, hits);
        return added;
    }
}
