package pokecube.core.moves.damage.attributes;

import net.minecraft.world.entity.ai.attributes.Attribute;

public class StatAttribute extends Attribute
{
    protected StatAttribute(String descriptionId, double defaultValue)
    {
        super(descriptionId, defaultValue);
        this.setSyncable(true);
    }
}
