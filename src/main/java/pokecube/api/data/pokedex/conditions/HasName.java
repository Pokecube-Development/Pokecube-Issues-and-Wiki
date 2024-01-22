package pokecube.api.data.pokedex.conditions;

import net.minecraft.network.chat.Component;
import pokecube.api.entity.pokemob.IPokemob;
import java.util.regex.Pattern;
import thut.lib.TComponent;

/**
 * This class matches a pokemob with the specified move<br>
 * <br>
 * Matcher key: "move" <br>
 * Json keys: <br>
 * "move" - string, name of the move to match
 */
@Condition(name = "nbt_name")
public class HasName implements PokemobCondition {
    public String nbt_name;
    private Pattern _name_pattern;
    public String desc;

    @Override
    public boolean matches(IPokemob mobIn) {
        if (mobIn != null && mobIn.getEntity() != null) {
            String entity_name = mobIn.getEntity().getName().getString();
            return _name_pattern.matcher(entity_name).matches();
        }
        return false;
    }

    @Override
    public void init() {
        _name_pattern = Pattern.compile(nbt_name);
    }

    @Override
    public Component makeDescription() {
        return TComponent.translatable(desc);
    }
}