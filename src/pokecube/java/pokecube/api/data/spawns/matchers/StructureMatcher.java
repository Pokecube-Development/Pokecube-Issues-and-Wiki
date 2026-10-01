package pokecube.api.data.spawns.matchers;

import net.minecraft.world.level.Level;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;
import thut.api.level.structures.NamedVolumes;
import thut.api.level.structures.StructureManager;

public interface StructureMatcher extends MatchChecker
{
    default MatchResult structuresMatch(final SpawnBiomeMatcher matcher, final SpawnCheck checker)
    {
        if (!matcher._validStructures.isEmpty())
        {
            var set = StructureManager.getFor(((Level) checker.world), checker.pos, false);
            for (var i : set)
                if (i instanceof NamedVolumes.NamedStructureWrapper && matcher._validStructures.contains(i.getName()))
                    return MatchResult.SUCCEED;
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
