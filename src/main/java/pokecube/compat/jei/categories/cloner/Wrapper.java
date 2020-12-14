package pokecube.compat.jei.categories.cloner;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import pokecube.adventures.blocks.genetics.helper.recipe.RecipeClone;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeClone.ReviveMatcher;
import pokecube.core.database.Database;

public class Wrapper
{
    public final ReviveMatcher wrapped;

    public Wrapper(final ReviveMatcher toWrap)
    {
        this.wrapped = toWrap;
    }

    public static Collection<Wrapper> getWrapped()
    {
        final List<Wrapper> wrapped = Lists.newArrayList();
        RecipeClone.MATCHERS.forEach(c ->
        {
            if (c.getDefault() != Database.missingno) wrapped.add(new Wrapper(c));
        });
        return wrapped;
    }

}
