package pokecube.api.raids;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event.Result;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.events.pokemobs.CaptureEvent;
import pokecube.api.events.pokemobs.FaintEvent;
import pokecube.api.raids.RaidManager.RaidContext;
import pokecube.core.items.pokecubes.helper.CaptureManager;
import thut.lib.TComponent;

/**
 * This interface is responsible for creating bosses for raids.
 */
public interface IBossProvider
{
    @Nullable
    /**
     * Makes a boss for this raid. Returns null if not a valid context for the
     * raid.
     * 
     * @param context
     * @param pokemob - optional pokemob to init instead of create
     * @return a boss or null
     */
    LivingEntity makeBoss(@Nonnull RaidContext context, @Nullable IPokemob pokemob);

    /**
     * Called after the boss is added to the world, can be used for anything
     * that needs to be done after it is in.
     * 
     * @param boss
     * @param context
     */
    default void postBossSpawn(@Nonnull LivingEntity boss, @Nonnull RaidContext context)
    {

    }

    default void onBossCaptureAttempt(CaptureEvent.Pre event)
    {
        if (event.mob.getHealth() >= 1)
        {
            final Entity catcher = event.pokecube.shootingEntity;
            if (catcher instanceof ServerPlayer player)
                thut.lib.ChatHelper.sendSystemMessage(player, TComponent.translatable("pokecube.denied"));
            event.setCanceled(true);
            event.setResult(Result.DENY);
            CaptureManager.onCaptureDenied(event.pokecube);
        }
        else
        {
            event.setResult(Result.ALLOW);
        }
    }

    default void postBossCapture(CaptureEvent.Post event, LivingEntity fromCube)
    {

    }

    default void onBossFaint(FaintEvent event)
    {
        if (event.pokemob.getEntity().getLastHurtByMob() instanceof ServerPlayer player)
            thut.lib.ChatHelper.sendSystemMessage(player,
                    TComponent.translatable("pokecube.raid.capture.generic", event.pokemob.getDisplayName()));
    }

    /**
     * @return String key for this boss type.
     */
    String getKey();

}
