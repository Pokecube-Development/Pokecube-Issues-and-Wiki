package pokecube.legends.conditions;

public abstract class AbstractRegiCondition extends AbstractEntriedCondition
{
    public AbstractRegiCondition(final String name, final Object... blocks)
    {
        super(name, "relicanth", "wailord");
        for (final Object block : blocks)
            this.setRelevant(block);
    }
}
