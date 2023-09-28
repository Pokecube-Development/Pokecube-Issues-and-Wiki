package pokecube.api.data.spawns.matchers;

import java.util.Set;

import net.minecraft.world.level.Level;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;
import thut.api.level.structures.NamedVolumes.INamedStructure;
import thut.api.level.structures.StructureManager;

public interface StructureMatcher extends MatchChecker
{
    default MatchResult structuresMatch(final SpawnBiomeMatcher matcher, final SpawnCheck checker)
    {
        if (!matcher._validStructures.isEmpty())
        {
            final Set<INamedStructure> set = StructureManager.getFor(((Level) checker.world).dimension(),
                    checker.location.getPos(), false);
            for (var i : set) if (matcher._validStructures.contains(i.getName())) return MatchResult.SUCCEED;
            return MatchResult.FAIL;
        }
        return MatchResult.PASS;
    }

    @Override
    default MatchResult matches(final SpawnBiomeMatcher matcher, final SpawnCheck checker)
    {
        return structuresMatch(matcher, checker);
    }
}
