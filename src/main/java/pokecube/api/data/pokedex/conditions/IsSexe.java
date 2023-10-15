package pokecube.api.data.pokedex.conditions;

import pokecube.api.entity.pokemob.IPokemob;

public class IsSexe implements PokemobCondition
{
    public String sexe;

    private byte _gender;

    @Override
    public boolean matches(IPokemob mobIn)
    {
        return  mobIn.getSexe() == this._gender;
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
}
