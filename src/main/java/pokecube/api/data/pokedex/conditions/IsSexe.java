package pokecube.api.data.pokedex.conditions;

import net.minecraft.network.chat.Component;
import pokecube.api.entity.pokemob.IPokemob;
import thut.lib.TComponent;

/**
 * This class matches a pokemob with the specified sex<br>
 * <br>
 * Matcher key: "sexe" <br>
 * Json keys: <br>
 * "sexe" - String, sex to match, "male" or "female"
 */
@Condition(name="sexe")
public class IsSexe implements PokemobCondition
{
    public String sexe;

    private byte _gender;

    @Override
    public boolean matches(IPokemob mobIn)
    {
        return mobIn.getSexe() == this._gender;
    }

    @Override
    public void init()
    {
        if (sexe != null)
        {
            if (sexe.equalsIgnoreCase("male")) this._gender = 1;
            if (sexe.equalsIgnoreCase("female")) this._gender = 2;
        }
    }

    @Override
    public Component makeDescription()
    {
        if (this._gender == 1) return TComponent.translatable("pokemob.description.evolve.male");
        if (this._gender == 2) return TComponent.translatable("pokemob.description.evolve.female");
        return PokemobCondition.super.makeDescription();
    }
}
