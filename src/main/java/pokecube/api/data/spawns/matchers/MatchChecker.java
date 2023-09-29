package pokecube.api.data.spawns.matchers;

import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;

public interface MatchChecker
{
    static MatchChecker or(final MatchChecker A, final MatchChecker B)
    {
        return new StructureMatcher()
        {
            @Override
            public MatchResult matches(final SpawnBiomeMatcher matcher, final SpawnCheck checker)
            {
                final MatchResult resA = A.matches(matcher, checker);
                if (resA == MatchResult.SUCCEED) return resA;
                final MatchResult resB = B.matches(matcher, checker);
                if (resB == MatchResult.SUCCEED) return resB;
                return resA == MatchResult.FAIL || resB == MatchResult.FAIL ? MatchResult.FAIL : MatchResult.PASS;
            }
        };
    }

    static MatchChecker and(final MatchChecker A, final MatchChecker B)
    {
        return new StructureMatcher()
        {
            @Override
            public MatchResult matches(final SpawnBiomeMatcher matcher, final SpawnCheck checker)
            {
                final MatchResult resA = A.matches(matcher, checker);
                if (resA == MatchResult.FAIL) return MatchResult.FAIL;
                final MatchResult resB = B.matches(matcher, checker);
                if (resB == MatchResult.FAIL) return MatchResult.FAIL;
                boolean succeed = resA == MatchResult.SUCCEED || resB == MatchResult.SUCCEED;
                return succeed ? MatchResult.SUCCEED : MatchResult.PASS;
            }

            @Override
            public void init()
            {
                A.init();
                B.init();
            }
        };
    }

    default MatchChecker and(final MatchChecker A)
    {
        var B = this;
        return and(A, B);
    }

    default MatchResult matches(final SpawnBiomeMatcher matcher, final SpawnCheck checker)
    {
        return MatchResult.PASS;
    }

    default void init()
    {}

    default String makeDescription()
    {
        return "Missingno";
    }
}
