package pokecube.compat.jei;

import java.util.List;

import com.google.common.collect.Lists;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.util.ResourceLocation;
import pokecube.adventures.PokecubeAdv;
import pokecube.compat.jei.categories.evolution.Evolution;
import pokecube.compat.jei.ingredients.Pokemob;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;

@JeiPlugin
public class Compat implements IModPlugin
{
    private static final ResourceLocation UID = new ResourceLocation(PokecubeCore.MODID, "jei");

    @Override
    public ResourceLocation getPluginUid()
    {
        return Compat.UID;
    }

    @Override
    public void registerItemSubtypes(final ISubtypeRegistration registration)
    {

    }

    @Override
    public void registerIngredients(final IModIngredientRegistration registration)
    {
        registration.register(Pokemob.TYPE, Pokemob.getIngredients(), Pokemob.HELPER, Pokemob.RENDER);
    }

    @Override
    public void registerCategories(final IRecipeCategoryRegistration registration)
    {
        Pokemob.getIngredients();
        final IGuiHelper helper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
        //@formatter:off
                new pokecube.compat.jei.categories.cloner.Category(helper),
                new pokecube.compat.jei.categories.evolution.Category(helper)
        );//@formatter:on

    }

    @Override
    public void registerRecipes(final IRecipeRegistration registration)
    {
        registration.addRecipes(pokecube.compat.jei.categories.cloner.Wrapper.getWrapped(), PokecubeAdv.CLONER
                .getRegistryName());
        final List<Evolution> evos = Lists.newArrayList();
        for (final PokedexEntry entry : Database.getSortedFormes())
            if (entry.canEvolve()) for (final EvolutionData data : entry.evolutions)
                evos.add(new Evolution(entry, data));
        registration.addRecipes(evos, pokecube.compat.jei.categories.evolution.Category.GUID);

    }

}
