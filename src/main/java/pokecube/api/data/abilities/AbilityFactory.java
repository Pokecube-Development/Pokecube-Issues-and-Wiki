package pokecube.api.data.abilities;

import java.util.function.Supplier;

public interface AbilityFactory
{
    public static AbilityFactory forSupplier(Supplier<Ability> supply)
    {
        return args -> {
            return supply.get().init(args);
        };
    }

    public static AbilityFactory forAbility(Ability supply)
    {
        return args -> {
            return supply.init(args);
        };
    }

    public static AbilityFactory DUMMY = args -> {
        return new DummyAbility().init(args);
    };

    Ability create(Object... args);
}
