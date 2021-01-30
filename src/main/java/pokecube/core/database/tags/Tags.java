package pokecube.core.database.tags;

import pokecube.core.database.genes.MutationHelper;
import pokecube.core.database.util.StringTagsHelper;

public class Tags
{
    public static final StringTagsHelper ABILITY  = new StringTagsHelper("tags/pokemob_abilities/");
    public static final StringTagsHelper MOVE     = new StringTagsHelper("tags/pokemob_moves/");
    public static final StringTagsHelper BREEDING = new StringTagsHelper("tags/pokemob_egg_groups/");
    public static final StringTagsHelper POKEMOB  = new StringTagsHelper("tags/pokemob/");

    public static final MutationHelper GENES = new MutationHelper("database/genes/");
}
