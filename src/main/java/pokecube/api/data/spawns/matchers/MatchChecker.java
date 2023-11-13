package pokecube.api.data.spawns.matchers;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import pokecube.api.data.spawns.SpawnBiomeMatcher;
import pokecube.api.data.spawns.SpawnCheck;
import pokecube.api.data.spawns.SpawnCheck.MatchResult;
import pokecube.api.events.data.SpawnMatchInit;

public interface MatchChecker
{
    public static class OrMatch implements MatchChecker
    {
        public MatchChecker A;
        public MatchChecker B;

        public OrMatch(MatchChecker A, MatchChecker B)
        {
            this.A = A;
            this.B = B;
        }

        @Override
        public MatchResult matches(SpawnBiomeMatcher matcher, SpawnCheck checker)
        {
            final MatchResult resA = A.matches(matcher, checker);
            if (resA == MatchResult.SUCCEED) return resA;
            final MatchResult resB = B.matches(matcher, checker);
            if (resB == MatchResult.SUCCEED) return resB;
            return resA == MatchResult.FAIL || resB == MatchResult.FAIL ? MatchResult.FAIL : MatchResult.PASS;
        }

        @Override
        public void init()
        {
            A = SpawnMatchInit.initMatchChecker(A);
            B = SpawnMatchInit.initMatchChecker(B);
        }
    }

    public static class AndMatch implements MatchChecker
    {
        public MatchChecker A;
        public MatchChecker B;

        public AndMatch(MatchChecker A, MatchChecker B)
        {
            this.A = A;
            this.B = B;
        }

        @Override
        public MatchResult matches(SpawnBiomeMatcher matcher, SpawnCheck checker)
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
            A = SpawnMatchInit.initMatchChecker(A);
            B = SpawnMatchInit.initMatchChecker(B);
        }
    }

    static MatchChecker or(final MatchChecker A, final MatchChecker B)
    {
        return new OrMatch(A, B);
    }

    static MatchChecker and(final MatchChecker A, final MatchChecker B)
    {
        return new AndMatch(A, B);
    }

    default MatchChecker and(final MatchChecker B)
    {
        var A = this;
        return and(A, B);
    }

    default MatchResult matches(final SpawnBiomeMatcher matcher, final SpawnCheck checker)
    {
        return MatchResult.PASS;
    }

    default void init()
    {}

    @Nullable
    default Component makeDescription()
    {
        return null;
    }
}
