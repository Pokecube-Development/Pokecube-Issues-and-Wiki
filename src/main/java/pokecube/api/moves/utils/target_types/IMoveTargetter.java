package pokecube.api.moves.utils.target_types;

import java.util.function.Predicate;

import pokecube.api.moves.utils.MoveApplication;

public interface IMoveTargetter extends Predicate<MoveApplication>
{
}
