package pokecube.api.raids;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.Battle;

public class RaidManager
{
    public static record RaidContext(@Nonnull ServerLevel level, @Nonnull BlockPos pos, @Nullable ServerPlayer player)
    {
    }

    public static Map<String, IBossProvider> RAID_TYPES = new HashMap<>();

    public static void registerBossType(IBossProvider provider)
    {
        RAID_TYPES.put(provider.getKey(), provider);
    }

    public static boolean makeRaid(@Nonnull ServerLevel level, @Nonnull BlockPos pos, @Nullable ServerPlayer player)
    {
        return makeRaid(level, pos, player, null);
    }

    public static boolean makeRaid(@Nonnull ServerLevel level, @Nonnull BlockPos pos, @Nullable ServerPlayer player,
            @Nullable String key)
    {
        if (RAID_TYPES.isEmpty()) return false;
        IBossProvider bossMaker = null;
        if (key != null)
        {
            bossMaker = RAID_TYPES.get(key);
        }
        else
        {
            List<IBossProvider> choices = new ArrayList<>(RAID_TYPES.values());
            if (choices.size() > 1) bossMaker = choices.get(level.getRandom().nextInt(choices.size()));
            else bossMaker = choices.get(0);
        }
        if (bossMaker == null) return false;
        RaidContext context = new RaidContext(level, pos, player);
        LivingEntity boss = bossMaker.makeBoss(context);
        if (boss == null) return false;

        level.addFreshEntity(boss);
        boss.setHealth(boss.getMaxHealth());

        IPokemob pokemob = PokemobCaps.getPokemobFor(boss);
        if (pokemob != null) pokemob.setBossInfo(new ServerBossEvent(boss.getDisplayName(), BossEvent.BossBarColor.RED,
                BossEvent.BossBarOverlay.PROGRESS));

        bossMaker.postBossSpawn(boss, context);

        if (player != null)
        {
            Battle.createOrAddToBattle(player, boss);
        }

        return true;
    }
}
