package pokecube.core.handlers;

import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Team;
import thut.api.IOwnable;
import thut.api.OwnableCaps;

public class TeamManager
{
    public static class DefaultProvider implements ITeamProvider
    {
        @Override
        @Nonnull
        public String getTeam(final Entity entityIn)
        {
            final Team team = entityIn.getTeam();
            String name = team == null ? "" : team.getName();
            final IOwnable ownable = OwnableCaps.getOwnable(entityIn);
            if (ownable != null)
            {
                final UUID id = ownable.getOwnerId();
                if (id != null) name = id.toString();
            }
            return name;
        }
    }

    public static interface ITeamProvider
    {
        default boolean areAllied(final String team, final Entity target)
        {
            return team.equals(this.getTeam(target));
        }

        @Nonnull
        String getTeam(Entity entityIn);
    }

    public static ITeamProvider provider = new DefaultProvider();

    @Nonnull
    public static String getTeam(final Entity entityIn)
    {
        return TeamManager.provider.getTeam(entityIn);
    }

    public static boolean sameTeam(final Entity entityA, final Entity entityB)
    {
        final String teamA = TeamManager.getTeam(entityA);
        return !teamA.isEmpty() && TeamManager.provider.areAllied(teamA, entityB);
    }
}
