package pokecube.api.data.spawns.matchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.world.level.Level;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;
import thut.api.level.structures.StructureManager;
import thut.api.level.structures.NamedVolumes.INamedStructure;

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
        final Set<INamedStructure> set = StructureManager.getFor(((Level) checker.world).dimension(),
                checker.location.getPos(), false);
        for (var i : set) if (_validStructures.contains(i.getName())) return MatchResult.SUCCEED;
        return MatchResult.FAIL;
    }

    @Override
    public void init()
    {
        this._validStructures.clear();
        final String[] args = names.split(",");
        for (final String s : args) this._validStructures.add(s);
    }
}
