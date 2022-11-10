package pokecube.api.data.spawns;

import java.util.Set;

import net.minecraft.world.level.Level;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;
import thut.api.level.structures.NamedVolumes.INamedStructure;
import thut.api.level.structures.StructureManager;

public interface StructureMatcher
{
    static StructureMatcher or(final StructureMatcher A, final StructureMatcher B)
    {
        return new StructureMatcher()
        {
            @Override
            public MatchResult structuresMatch(final SpawnBiomeMatcher matcher, final SpawnCheck checker)
            {
                final MatchResult resA = A.structuresMatch(matcher, checker);
                if (resA == MatchResult.SUCCEED) return resA;
                final MatchResult resB = B.structuresMatch(matcher, checker);
                if (resB == MatchResult.SUCCEED) return resB;
                return resA == MatchResult.FAIL || resB == MatchResult.FAIL ? MatchResult.FAIL : MatchResult.PASS;
            }
        };
    }

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
}
