package pokecube.api.data.spawns.matchers;

import org.nfunk.jep.JEP;

import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;

@MatcherFunction(name = "location")
public class Location implements MatchChecker
{
    public String f;

    private JEP _parser = new JEP();

    @Override
    public MatchResult matches(SpawnBiomeMatcher matcher, SpawnCheck checker)
    {
        _parser.setVarValue("x", checker.location.x);
        _parser.setVarValue("y", checker.location.y);
        _parser.setVarValue("z", checker.location.z);
        return _parser.getValue() > 0 ? MatchResult.SUCCEED : MatchResult.FAIL;
    }

    @Override
    public void init()
    {
        _parser.addStandardConstants();
        _parser.addStandardFunctions();
        _parser.addVariable("x", 0);
        _parser.addVariable("y", 0);
        _parser.addVariable("z", 0);
        _parser.parseExpression(f);
    }
}
