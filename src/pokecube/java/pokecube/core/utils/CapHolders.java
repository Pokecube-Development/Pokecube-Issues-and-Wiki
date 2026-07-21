package pokecube.core.utils;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import pokecube.api.ai.IInhabitor;
import pokecube.api.blocks.IInhabitable;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.ai.routes.GuardAICapability;
import pokecube.core.ai.routes.IGuardAICapability;


public class CapHolders
{
    public static IInhabitor getInhabitor(final IAttachmentHolder in)
    {
        return PokemobCaps.getInhabitorFor(in);
    }

    public static IInhabitable getInhabitable(final IAttachmentHolder in, ResourceLocation defaultHabitat)
    {
        return PokemobCaps.getHabitatFor(in, defaultHabitat);
    }

    public static IInhabitable getInhabitable(IAttachmentHolder in)
    {
        return PokemobCaps.getHabitatFor(in);
    }

    public static IGuardAICapability getGuardAI(IAttachmentHolder in)
    {
        return GuardAICapability.getGuardInfo(in);
    }
}
