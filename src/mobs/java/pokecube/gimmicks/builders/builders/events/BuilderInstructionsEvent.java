package pokecube.gimmicks.builders.builders.events;

import java.util.List;

import net.neoforged.bus.api.Event;
import pokecube.gimmicks.builders.builders.BuilderManager.BuilderClearer;

public class BuilderInstructionsEvent extends Event
{
    private final List<String> instructions;
    private BuilderClearer results;
    
    public BuilderInstructionsEvent(List<String> instructions, BuilderClearer results)
    {
        this.instructions = instructions;
        this.results = results;
    }

    public List<String> getInstructions()
    {
        return instructions;
    }

    public BuilderClearer getResults()
    {
        return results;
    }

    public void setResults(BuilderClearer results)
    {
        this.results = results;
    }
}
