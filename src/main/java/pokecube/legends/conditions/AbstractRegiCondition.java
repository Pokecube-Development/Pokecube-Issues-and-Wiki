package pokecube.legends.conditions;

public abstract class AbstractRegiCondition extends AbstractCondition
{
    public AbstractRegiCondition(final Object... blocks)
    {
        super();
        for (final Object block : blocks)
            this.setRelevant(block);
    }
}
