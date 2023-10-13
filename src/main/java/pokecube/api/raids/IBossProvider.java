package pokecube.api.raids;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;
import pokecube.api.raids.RaidManager.RaidContext;

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
     * @return a boss or null
     */
    LivingEntity makeBoss(@Nonnull RaidContext context);

    /**
     * Called after the boss is added to the world, can be used for anything
     * that needs to be done after it is in.
     * 
     * @param boss
     * @param context
     */
    default void postBossSpawn(LivingEntity boss, RaidContext context)
    {

    }

    /**
     * @return String key for this boss type.
     */
    String getKey();
}
