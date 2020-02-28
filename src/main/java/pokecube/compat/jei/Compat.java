package pokecube.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeCore;

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
    public void registerCategories(final IRecipeCategoryRegistration registration)
    {

    }

}
