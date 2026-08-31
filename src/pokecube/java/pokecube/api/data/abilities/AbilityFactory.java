package pokecube.api.data.abilities;

import java.util.function.Supplier;

public interface AbilityFactory
{
    public static AbilityFactory forSupplier(Supplier<Ability> supply)
    {
        return args -> supply.get().init(args);
    }

    public static AbilityFactory forAbility(Ability supply)
    {
        return supply::init;
    }

    public static AbilityFactory DUMMY = args -> new DummyAbility().init(args);

    Ability create(Object... args);
}
