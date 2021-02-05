package pokecube.core.database.tags;

import pokecube.core.database.genes.MutationHelper;

public class Tags
{
    public static final StringTag ABILITY  = new StringTag("tags/pokemob_abilities/");
    public static final StringTag MOVE     = new StringTag("tags/pokemob_moves/");
    public static final StringTag BREEDING = new StringTag("tags/pokemob_egg_groups/");
    public static final StringTag POKEMOB  = new StringTag("tags/pokemob/");
    public static final StringTag MOVEMENT = new StringTag("tags/pokemob_movements/");

    public static final MutationHelper GENES = new MutationHelper("database/genes/");
}
