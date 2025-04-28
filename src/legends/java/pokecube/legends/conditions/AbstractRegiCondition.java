package pokecube.legends.conditions;

public abstract class AbstractRegiCondition extends AbstractEntriedCondition
{
    public AbstractRegiCondition(final Object... blocks)
    {
        super("relicanth", "wailord");
        for (final Object block : blocks)
            this.setRelevant(block);
    }
}
