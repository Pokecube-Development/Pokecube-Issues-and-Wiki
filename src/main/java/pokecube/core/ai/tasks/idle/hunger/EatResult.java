package pokecube.core.ai.tasks.idle.hunger;

public enum EatResult
{
    EATEN, PATHING, NOEAT;

    public boolean test()
    {
        return this != NOEAT;
    }
}
