package pokecube.core.handlers;

import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.scoreboard.Team;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.capabilities.CapabilityPokemob;

public class TeamManager
{
    public static class DefaultProvider implements ITeamProvider
    {
        @Override
        @Nonnull
        public String getTeam(Entity entityIn)
        {
            final Team team = entityIn.getTeam();
            String name = team == null ? "" : team.getName();
            IPokemob pokemob;
            if (entityIn instanceof TameableEntity && team == null)
            {
                final UUID id = ((TameableEntity) entityIn).getOwnerId();
                if (id != null) name = id.toString();
            }
            else if ((pokemob = CapabilityPokemob.getPokemobFor(entityIn)) != null)
            {
                final UUID id = pokemob.getOwnerId();
                if (id != null) name = id.toString();
            }
            return name;
        }
    }

    public static interface ITeamProvider
    {
        default boolean areAllied(String team, Entity target)
        {
            return team.equals(this.getTeam(target));
        }

        @Nonnull
        String getTeam(Entity entityIn);
    }

    public static ITeamProvider provider = new DefaultProvider();

    @Nonnull
    public static String getTeam(Entity entityIn)
    {
        return TeamManager.provider.getTeam(entityIn);
    }

    public static boolean sameTeam(Entity entityA, Entity entityB)
    {
        final String teamA = TeamManager.getTeam(entityA);
        return !teamA.isEmpty() && TeamManager.provider.areAllied(teamA, entityB);
    }
}
