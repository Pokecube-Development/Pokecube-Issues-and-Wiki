package pokecube.api.data.spawns.matchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.world.level.Level;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;
import thut.api.level.structures.NamedVolumes.INamedStructure;
import thut.api.level.structures.StructureManager;

/**
 * 
 * This class matches the structure at the location. <br>
 * <br>
 * Matcher key: "structures" <br>
 * Json keys: <br>
 * "names" - String, list of structures to match, seperated by ,
 *
 */
@MatcherFunction(name = "structures")
public class Structures extends BaseMatcher
{
    public String names = "";

    List<String> _validStructures = new ArrayList<>();

    @Override
    public MatchResult _matches(SpawnBiomeMatcher matcher, SpawnCheck checker)
    {
        Set<INamedStructure> set = checker.namedStructures;
        if (set == null) set = checker.namedStructures = StructureManager.getFor(((Level) checker.world).dimension(),
                checker.location.getPos(), false);
        for (var j : this._validStructures) for (var i : set) if (i.is(j)) return MatchResult.SUCCEED;
        return MatchResult.FAIL;
    }

    @Override
    public void init()
    {
        this._validStructures.clear();
        final String[] args = names.split(",");
        for (String s : args)
        {
            s = s.strip();
            this._validStructures.add(s.replace("#", ""));
        }
    }
}
