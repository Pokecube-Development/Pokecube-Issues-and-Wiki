package pokecube.core.contributors;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokecube.PokecubeBehavior;

public class Contributor
{
    public String                name;
    public UUID                  uuid;
    public List<ContributorType> types        = Lists.newArrayList();
    public String                legacy       = "";
    public String                cubeOverride = "";

    public ItemStack getStarterCube()
    {
        ResourceLocation id = PokecubeBehavior.DEFAULTCUBE;
        override:
        if (!this.cubeOverride.isEmpty())
        {
            id = new ResourceLocation(this.cubeOverride);
            for (final ResourceLocation h : IPokecube.BEHAVIORS.getKeys())
                if (h.equals(id)) break override;
            id = PokecubeBehavior.DEFAULTCUBE;
        }
        else
        {
            id = new ResourceLocation("pokecube:park");
            for (final ResourceLocation h : IPokecube.BEHAVIORS.getKeys())
                if (h.equals(id)) break override;
            id = PokecubeBehavior.DEFAULTCUBE;
        }
        final ItemStack stack = new ItemStack(PokecubeItems.getFilledCube(id));
        return stack;
    }

}
